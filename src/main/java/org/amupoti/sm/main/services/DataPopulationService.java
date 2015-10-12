package org.amupoti.sm.main.services;

import org.amupoti.sm.main.repository.MatchRepository;
import org.amupoti.sm.main.repository.PlayerRepository;
import org.amupoti.sm.main.repository.TeamRepository;
import org.amupoti.sm.main.repository.ValueRepository;
import org.amupoti.sm.main.repository.entity.*;
import org.amupoti.sm.main.services.provider.match.MatchDataProvider;
import org.amupoti.sm.main.services.provider.player.PlayerDataService;
import org.amupoti.sm.main.services.provider.team.TeamDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Recovers data from the player page
 * Created by Marcel on 04/08/2015.
 */
@Service
public class DataPopulationService {

    private static final int NUM_MATCHES = 34;

    private final static Log LOG = LogFactory.getLog(DataPopulationService.class);


    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ValueRepository valueRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerDataService dataProviderStrategy;

    @Autowired
    private TeamDataService teamDataService;

    @Autowired
    private MatchDataProvider matchDataProvider;


    /**
     * Populates the data from the web if necessary
     * @return
     * @throws IOException
     * @throws XPatherException
     */

    public void populate() throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {

        populateTeams();
        populatePlayers();
        populateMatches();

    }

    private void populateMatches() throws IOException, XPatherException {
        LOG.info("Populating matches");

        Iterable<TeamEntity> teams = teamRepository.findAll();
        for (TeamEntity teamEntity:teams){
            populateMatchesByTeam(teamEntity.getName());
        }

    }



    /**
     * Populates data from player pages
     * @throws IOException
     * @throws XPatherException
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private void populatePlayers() throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {
        LOG.info("Populating players");
        List<PlayerId> playerIdList = dataProviderStrategy.getPlayerIds();
        Iterable<PlayerEntity> playerEntityList = dataProviderStrategy.getPlayersData(playerIdList);

        Iterator<PlayerEntity> iterator = playerEntityList.iterator();
        while (iterator.hasNext()){
            PlayerEntity playerData = iterator.next();
            playerRepository.save(playerData);
        }
    }

    /*
     * Teams
     */

    private void populateTeams() throws IOException, XPatherException {
        LOG.info("Populating teams");
        //Load from static data
        String[] teamIds = teamDataService.getTeamIds();
        //Load from web, load players + calendarBoost
        populateTeamData(teamIds);
    }


    private void populateTeamData(String ...teamIds) throws IOException, XPatherException {
        //Load all team data from every team page
        for (int i = 0; i < teamIds.length; i++) {
            LOG.info("Populating team " + teamIds[i]);

            TeamEntity teamEntity = teamRepository.findByName(teamIds[i]);
            if (teamEntity==null){
                teamEntity = new TeamEntity();
            }
            teamEntity.setName(teamIds[i]);

            doPopulateTeamValues(teamIds[i], teamEntity);

            teamRepository.save(teamEntity);
        }
    }

    private void doPopulateTeamValues(String teamId, TeamEntity teamEntity) throws IOException, XPatherException {
        for (PlayerPosition p:PlayerPosition.values()){
            String id = p.getId();
            LOG.info("Populating position " +id );
            ValueEntity valueEntity;
            if (teamEntity.getValMap().get(id)!=null){
                valueEntity = teamEntity.getValMap().get(id);
            }
            else{
                valueEntity = new ValueEntity();
            }
            doPopulateTeamValues(teamId, p, valueEntity);
            valueEntity = valueRepository.save(valueEntity);
            teamEntity.getValMap().put(id, valueEntity);
        }
    }


    /**
     * Populates the mean values for each team for a given position, and returns the given entity with the results
     * @param teamName
     * @param position
     * @param valueEntity
     * @return
     * @throws IOException
     * @throws XPatherException
     */
    private ValueEntity doPopulateTeamValues(String teamName, PlayerPosition position, ValueEntity valueEntity) throws IOException, XPatherException {
        LOG.info("Populating " + position + " for :" + teamName);
        //Get page for the given position

        //Obtain values via XPath
        String value = teamDataService.getTeamMean(teamName,position);// RDMPlayerDataService.VAL);
        String valueReceived = teamDataService.getTeamMeanReceived(teamName,position);
        String valueLocal = teamDataService.getTeamMeanLocal(teamName,position);
        String valueVisitor = teamDataService.getTeamMeanVisitor(teamName,position);

        String valueLocalReceived = teamDataService.getTeamMeanLocalReceived(teamName,position);
        String valueVisitorReceived = teamDataService.getTeamMeanVisitorReceived(teamName,position);
        //Set values into entity for persistence
        valueEntity.setType(position.getId());
        valueEntity.setVal(value);
        valueEntity.setValV(valueVisitor);
        valueEntity.setValL(valueLocal);
        valueEntity.setValRecL(valueLocalReceived);
        valueEntity.setValRec(valueReceived);
        valueEntity.setValRecV(valueVisitorReceived);
        return valueEntity;

    }

    /**
     * Match data will be always loaded
     * @param teamName
     * @throws IOException
     * @throws XPatherException
     */
    private void populateMatchesByTeam(String teamName) throws IOException, XPatherException {
        TeamEntity teamEntity = teamRepository.findByName(teamName);

        LOG.info("Getting matches for team " + teamName);
        Iterable<MatchEntity> matchEntities = matchDataProvider.getTeamMatches(teamName);
        matchRepository.save(matchEntities);

        for (MatchEntity matchEntity:matchEntities){

            teamEntity.getMatchMap().put(matchEntity.getNumber(), matchEntity);
        }
        teamRepository.save(teamEntity);
    }


}

