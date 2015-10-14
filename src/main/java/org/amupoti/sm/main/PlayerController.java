package org.amupoti.sm.main;

import org.amupoti.sm.main.repository.entity.*;
import org.amupoti.sm.main.services.*;
import org.amupoti.sm.main.services.provider.team.TeamConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Marcel on 03/08/2015.
 */
@Controller
public class PlayerController {

    private final static Log LOG = LogFactory.getLog(PlayerController.class);
    private static final int LONG_TERM = 6;
    private static final int MEDIUM_TERM = 4;
    private static final int SHORT_TERM = 1;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private DataPopulationService dataPopulationService;

    @Autowired
    private DataBoostService dataBoostService;

    @RequestMapping(value = "/players/{id}")
    public String getPlayer(@PathVariable String id,Model model) throws IOException, XPatherException, URISyntaxException {
        PlayerId playerId = new PlayerId(id);
        LOG.info("Retrieving info for player:" + playerId);
        PlayerEntity player= playerService.getPlayer(playerId);
        model.addAttribute("playerId",playerId);
        model.addAttribute("player",player);
        LOG.info("Retrieved info for player:" + playerId + "." + player);
        return "player";
    }

    @RequestMapping(value = "/players/")
    public String getPlayers(Model model) throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {
        Iterable<PlayerEntity> playerList =  playerService.getPlayers();
        model.addAttribute("players", playerList);
        return "players";
    }

    @RequestMapping(value = "/overview/")
    public String getOverview(Model model) throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {
        Iterable<PlayerEntity> playerList =  playerService.getPlayers();
        model.addAttribute("players", playerList);

        return "overview";
    }

    @RequestMapping(value = "/teams/")
    public String getTeams(Model model) throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {

        Iterable<TeamEntity> teamList =  teamService.getTeams();
        model.addAttribute("teams", teamList);
        return "teams";
    }


    @RequestMapping(value = "/populate")
    public String populateData() throws IOException, XPatherException, InterruptedException, ExecutionException, URISyntaxException {
        dataPopulationService.populate();
        return "populate";
    }


    @RequestMapping(value = "/wizard/")
    public String getSupermagerInfo(Model model)  {
        Iterable<PlayerEntity> playerList =  playerService.getPlayers();


        List<SMDataBean> smDataList = new LinkedList<>();
        for (PlayerEntity playerEntity:playerList){
            SMDataBean smDataBean = new SMDataBean();
            addPlayerData(playerEntity, smDataBean);
            addTeamData(playerEntity,smDataBean);

            smDataList.add(smDataBean);

        }
        model.addAttribute("smDataList", smDataList);

        return "wizard";
    }

    /**
     * Adds all information related to the team, like calendar-related boosts, mean values per received per team, etc.
     * @param playerEntity
     * @param smDataBean
     */
    private void addTeamData(PlayerEntity playerEntity, SMDataBean smDataBean) {

        int matchNumber = TeamConstants.CURRENT_MATCH_NUMBER;
        //TeamEntity teamEntity = teamService.getTeam(playerEntity.getTeam().getName());
        TeamEntity teamEntity = playerEntity.getTeam();
        /*
         * Get boost depending on calendar
         */
        smDataBean.setCalendarBoostShort(dataBoostService.getCalendarData(teamEntity, matchNumber, SHORT_TERM));
        smDataBean.setCalendarBoostMedium(dataBoostService.getCalendarData(teamEntity, matchNumber , MEDIUM_TERM));
        smDataBean.setCalendarBoostLong(dataBoostService.getCalendarData(teamEntity, matchNumber , LONG_TERM));
        /*
         * Get mean values depending if local or visitor
         */
        ValueEntity teamValues = teamEntity.getValMap().get(PlayerPosition.TOTAL.getId());
        MatchEntity matchEntity = teamEntity.getMatchMap().get(matchNumber);
        boolean isLocal = matchEntity.isLocal(teamEntity.getName());

        smDataBean.setTeamVal(teamValues.getVal());
        TeamEntity otherTeam;
        ValueEntity otherTeamValues;
        String teamVal;
        if (isLocal) {
            teamVal  = teamValues.getValL();
            otherTeam = teamService.getTeam(matchEntity.getVisitor());
            otherTeamValues = otherTeam.getValMap().get(PlayerPosition.TOTAL.getId());
            smDataBean.setOtherTeamReceivedValAsLV(otherTeamValues.getValRecV());
            smDataBean.setLocalOrVisitor(TeamConstants.LOCAL);
        } else {

            teamVal = teamValues.getValV();
            otherTeam = teamService.getTeam(matchEntity.getLocal());
            otherTeamValues = otherTeam.getValMap().get(PlayerPosition.TOTAL.getId());
            smDataBean.setOtherTeamReceivedValAsLV(otherTeamValues.getValRecL());
            smDataBean.setLocalOrVisitor(TeamConstants.VISITOR);
        }
        smDataBean.setTeamValAsLV(teamVal);
        smDataBean.setOtherTeamReceivedVal(otherTeamValues.getValRec());
        smDataBean.setOtherTeamName(otherTeam.getName());
        /*
         *  Get mean values depending on player position
         */
        //TODO: load players with position so we can add value for that position
    }

    /**
     * Adds all data related to the player that we want to show in the wizard
     * @param playerEntity
     * @param smDataBean
     */
    private void addPlayerData(PlayerEntity playerEntity, SMDataBean smDataBean) {
        smDataBean.setPlayerId(playerEntity.getId().toString());
        smDataBean.setPlayerPosition(playerEntity.getPlayerPosition().name());
        smDataBean.setPlayerLocalVal(playerEntity.getLocalMean());
        smDataBean.setPlayerVisitorVal(playerEntity.getVisitorMean());
        smDataBean.setKeepBroker(playerEntity.getKeepBroker());
        smDataBean.setTeamName(playerEntity.getTeam().getName());
    }


}

