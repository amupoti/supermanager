package org.amupoti.sm.main.controller;

import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.*;
import org.amupoti.sm.main.services.repository.PlayerService;
import org.amupoti.sm.main.services.repository.TeamService;
import org.amupoti.supermanager.parser.acb.ACBTeamService;
import org.amupoti.supermanager.parser.acb.bean.SMPlayerDataBean;
import org.amupoti.supermanager.parser.acb.bean.SMTeamDataBean;
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

    @Autowired
    private PlayerService playerService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private DataPopulationService dataPopulationService;

    @Autowired
    private ComputePlayerValuesService computePlayerValuesService;

    @Autowired
    private ComputeTeamValuesService computeTeamValuesService;

    @Autowired
    private MatchControlService matchControlService;

    @Autowired
    private ACBTeamService acbTeamService;

    @RequestMapping(value = "/teams/")
    public String getTeams(Model model) throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {

        Iterable<TeamEntity> teamList =  teamService.getTeams();
        model.addAttribute("teams", teamList);
        return "teams";
    }

    @RequestMapping(value = "/teams/scores.html")
    public String getTeamScores(Model model) throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {

        List<SMTeamDataBean> teamPoints = computeTeamValuesService.getTeamPoints();
        model.addAttribute("teams", teamPoints);
        return "teamScores";
    }

    @RequestMapping(value = "/populate/{match}")
    public String populateData(@PathVariable(value = "match") Integer match) throws IOException, XPatherException, InterruptedException, ExecutionException, URISyntaxException {
        LOG.info("Populating data for match "+match);
        matchControlService.setCurrentMatch(match);
        dataPopulationService.populate();
        return "populate";
    }


    @RequestMapping(value = "/wizard/")
    public String getSupermagerInfo(Model model) throws URISyntaxException, ExecutionException, XPatherException, InterruptedException, IOException {

        Iterable<PlayerEntity> playerList =  playerService.getPlayers();
        List<SMPlayerDataBean> smDataList = new LinkedList<>();
        for (PlayerEntity playerEntity:playerList){
            SMPlayerDataBean smPlayerDataBean = new SMPlayerDataBean();
            computePlayerValuesService.addPlayerData(playerEntity, smPlayerDataBean);
            computePlayerValuesService.addTeamData(playerEntity, smPlayerDataBean);
            computePlayerValuesService.addMvpData(smPlayerDataBean);

            smDataList.add(smPlayerDataBean);

        }
        model.addAttribute("smDataList", smDataList);
        model.addAttribute("matchNumber", matchControlService.getMatchNumber());
        return "wizard";
    }

    @RequestMapping(value = "/")
    public String root(Model model) {

        return "index";
    }



}

