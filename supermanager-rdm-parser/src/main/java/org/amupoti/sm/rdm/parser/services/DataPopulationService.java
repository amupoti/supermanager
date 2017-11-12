package org.amupoti.sm.rdm.parser.services;


import org.amupoti.sm.rdm.parser.bean.PlayerPositionRdm;
import org.amupoti.sm.rdm.parser.config.SMConstants;
import org.amupoti.sm.rdm.parser.repository.MatchRepository;
import org.amupoti.sm.rdm.parser.repository.PlayerRepository;
import org.amupoti.sm.rdm.parser.repository.TeamRepository;
import org.amupoti.sm.rdm.parser.repository.ValueRepository;
import org.amupoti.sm.rdm.parser.repository.entity.*;
import org.amupoti.sm.rdm.parser.services.scraper.match.MatchDataScraper;
import org.amupoti.sm.rdm.parser.services.scraper.player.PlayerDataService;
import org.amupoti.sm.rdm.parser.services.scraper.team.TeamDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;


/**
 * Recovers data from the player page
 * Created by Marcel on 04/08/2015.
 */
@Service
public class DataPopulationService {

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
    private MatchDataScraper matchDataScraper;


    /**
     * Populates the data from the website: teams, players and calendar
     *
     * @return
     */

    public void populate() throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {

        LOG.info("Starting data load");
        populateTeams();
        populatePlayers();
        populateMatches();
        LOG.info("Finished data load");

    }

    private void populateMatches() throws IOException, XPatherException {
        LOG.info("Populating matches");

        Iterable<TeamEntity> teams = teamRepository.findAll();
        for (TeamEntity teamEntity : teams) {
            populateMatchesByTeam(teamEntity.getName());
        }
    }

    /**
     * Populates data from player pages
     *
     * @throws IOException
     * @throws XPatherException
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private void populatePlayers() throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {
        LOG.info("Populating players");
        Set<PlayerId> playerIdList = dataProviderStrategy.getPlayerIds();
        Iterable<PlayerEntity> playerEntityList = dataProviderStrategy.getPlayersData(playerIdList);

        Iterator<PlayerEntity> iterator = playerEntityList.iterator();
        while (iterator.hasNext()) {
            PlayerEntity playerData = iterator.next();
            playerRepository.save(playerData);
        }
    }

    /*
     * Teams
     */

    private void populateTeams() throws IOException, XPatherException {
        LOG.info("Populating teams");
        //Load from web, load players + calendarBoost
        populateTeamData(SMConstants.teamIds);
    }


    private void populateTeamData(String... teamIds) throws IOException, XPatherException {
        //Load all team data from every team page
        for (int i = 0; i < teamIds.length; i++) {
            LOG.info("Populating team " + teamIds[i]);

            TeamEntity teamEntity = teamRepository.findByName(teamIds[i]);
            if (teamEntity == null) {
                teamEntity = new TeamEntity();
            }
            teamEntity.setName(teamIds[i]);

            doPopulateVal(teamIds[i], teamEntity);

            teamRepository.save(teamEntity);
        }
    }

    private void populatePointsTeamValues(String teamId, TeamEntity teamEntity) throws IOException, XPatherException {
        ValueEntity valueEntity;
        PlayerPositionRdm playerPosition = PlayerPositionRdm.TOTAL;
        String id = getPointId(playerPosition);
        if (teamEntity.getValMap().get(id) != null) {
            valueEntity = teamEntity.getValMap().get(id);
        } else {
            valueEntity = new ValueEntity();
        }
        doPopulatePoints(teamId, playerPosition, valueEntity);
        valueEntity = valueRepository.save(valueEntity);
        teamEntity.getValMap().put(id, valueEntity);
    }

    private String getPointId(PlayerPositionRdm playerPosition) {
        return playerPosition.getId() + "-points";
    }

    private void doPopulateVal(String teamId, TeamEntity teamEntity) throws IOException, XPatherException {

        //Populate val.
        for (PlayerPositionRdm p : PlayerPositionRdm.values()) {
            String id = p.getId();
            LOG.info("Populating position " + id);
            doPopulateValTeamValues(teamId, teamEntity, p, id);
        }

        populatePointsTeamValues(teamId, teamEntity);

    }


    private void doPopulateValTeamValues(String teamId, TeamEntity teamEntity, PlayerPositionRdm p, String id) throws IOException, XPatherException {
        ValueEntity valueEntity;
        if (teamEntity.getValMap().get(id) != null) {
            valueEntity = teamEntity.getValMap().get(id);
        } else valueEntity = new ValueEntity();

        doPopulateVal(teamId, p, valueEntity);

        valueEntity = valueRepository.save(valueEntity);
        teamEntity.getValMap().put(id, valueEntity);
    }


    /**
     * Populates the mean values for each team for a given position, and returns the given entity with the results
     *
     * @param teamName
     * @param position
     * @param valueEntity
     * @return
     * @throws IOException
     * @throws XPatherException
     */
    private ValueEntity doPopulateVal(String teamName, PlayerPositionRdm position, ValueEntity valueEntity) throws IOException, XPatherException {
        LOG.info("Populating val. " + position + " for :" + teamName);
        //Get page for the given position

        //Obtain values via XPath
        String value = teamDataService.getTeamMean(teamName, position);
        String valueReceived = teamDataService.getTeamMeanReceived(teamName, position);
        String valueLocal = teamDataService.getTeamMeanLocal(teamName, position);
        String valueVisitor = teamDataService.getTeamMeanVisitor(teamName, position);

        String valueLocalReceived = teamDataService.getTeamMeanLocalReceived(teamName, position);
        String valueVisitorReceived = teamDataService.getTeamMeanVisitorReceived(teamName, position);
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

    private ValueEntity doPopulatePoints(String teamName, PlayerPositionRdm position, ValueEntity valueEntity) throws IOException, XPatherException {
        LOG.info("Populating points " + position + " for :" + teamName);
        //Get page for the given position

        //Obtain values via XPath
        String value = teamDataService.getTeamMeanPoints(teamName, position);// RDMPlayerDataScraper.VAL);
        String valueReceived = teamDataService.getTeamMeanPointsReceived(teamName, position);
        String valueLocal = teamDataService.getTeamMeanPointsLocal(teamName, position);
        String valueVisitor = teamDataService.getTeamMeanPointsVisitor(teamName, position);

        String valueLocalReceived = teamDataService.getTeamMeanPointsLocalReceived(teamName, position);
        String valueVisitorReceived = teamDataService.getTeamMeanPointsVisitorReceived(teamName, position);
        //Set values into entity for persistence
        valueEntity.setType(getPointId(position));
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
     *
     * @param teamName
     * @throws IOException
     * @throws XPatherException
     */
    private void populateMatchesByTeam(String teamName) throws IOException, XPatherException {
        TeamEntity teamEntity = teamRepository.findByName(teamName);

        if (teamEntity.getMatchMap() == null || teamEntity.getMatchMap().keySet().size() == 0) {

            LOG.info("Getting matches for team " + teamName);
            Iterable<MatchEntity> matchEntities = matchDataScraper.getTeamMatches(teamName);
            matchRepository.save(matchEntities);

            for (MatchEntity matchEntity : matchEntities) {

                teamEntity.getMatchMap().put(matchEntity.getNumber(), matchEntity);
            }
        } else {
            LOG.info("Matches were already populated in DB for team " + teamName);
        }

        teamRepository.save(teamEntity);
    }
}

