package org.amupoti.sm.main.services;

import org.amupoti.sm.main.repository.MatchRepository;
import org.amupoti.sm.main.repository.PlayerRepository;
import org.amupoti.sm.main.repository.TeamRepository;
import org.amupoti.sm.main.repository.ValueRepository;
import org.amupoti.sm.main.repository.entity.*;
import org.amupoti.sm.main.services.provider.MatchDataProvider;
import org.amupoti.sm.main.services.provider.PlayerDataProvider;
import org.amupoti.sm.main.services.provider.TeamDataProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Recovers data from the player page
 * Created by Marcel on 04/08/2015.
 */
@Service
public class DataPopulationService {

    //TODO: this XPATH expressions are not valid since the page changes depending on the number of matches played by player


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
    private PlayerDataProvider dataProviderStrategy;

    @Autowired
    private TeamDataProvider teamDataProvider;

    @Autowired
    private MatchDataProvider matchDataProvider;


    /**
     * Populates the data from the web if necessary
     * @return
     * @throws IOException
     * @throws XPatherException
     */

    public void populate() throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {

        populatePlayers();
        populateTeams();
        populateMatches();

    }

    private void populateMatches() throws IOException {

        for (int i=0;i<NUM_MATCHES;i++){

            Iterable<TeamEntity> teams = teamRepository.findAll();
            for (TeamEntity teamEntity:teams){
                populateMatchesByTeam(teamEntity.getName());
            }
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
        List<PlayerId> playerIdList = dataProviderStrategy.getPlayerIds();
        List<PlayerEntity> playerEntityList = dataProviderStrategy.getPlayersData(playerIdList);

        for (int i=0;i<playerIdList.size();i++){
            PlayerEntity playerData = playerEntityList.get(i);
            playerRepository.save(playerData);
        }
    }

    /*
     * Teams
     */

    private void populateTeams() throws IOException, XPatherException {
        //Load from static data
        String[] teamIds = teamDataProvider.populateTeamIds();
        //Load from web, load players + calendar
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
                populateTeamValues(teamIds[i], p, valueEntity);
                valueEntity = valueRepository.save(valueEntity);
                teamEntity.getValMap().put(id, valueEntity);
            }

            teamRepository.save(teamEntity);
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
    private ValueEntity populateTeamValues(String teamName, PlayerPosition position, ValueEntity valueEntity) throws IOException, XPatherException {
        LOG.info("Populating " + position + " for :" + teamName);

        //Get page for the given position
        String html = teamDataProvider.getTeamPage(teamName, position);
        //Obtain values via XPath
        String value = teamDataProvider.getTeamMean(html, teamName);// PlayerDataProvider.VAL);
        String valueReceived = teamDataProvider.getTeamMeanReceived(html, teamName);
        String valueLocal = teamDataProvider.getTeamMeanLocal(html,  teamName);
        String valueVisitor = teamDataProvider.getTeamMeanVisitor(html, teamName);

        String valueLocalReceived = teamDataProvider.getTeamMeanLocalReceived(html, teamName);
        String valueVisitorReceived = teamDataProvider.getTeamMeanVisitorReceived(html, teamName);
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

    private void populateMatchesByTeam(String teamName) throws IOException {
        TeamEntity teamEntity = teamRepository.findByName(teamName);
        Iterable<MatchEntity> matchEntities = matchRepository.findByLocalAndVisitor(teamName);
        if (matchEntities==null){
            LOG.info("Getting matches for team " + teamName);
            matchEntities = matchDataProvider.getTeamMatches(teamName);
        }
        else{
            return;
        }

        for (MatchEntity matchEntity:matchEntities){

            teamEntity.getMatchMap().put(matchEntity.getNumber(), matchEntity);
        }
    }


}

