package org.amupoti.sm.main.controller;

import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.ComputePlayerValuesService;
import org.amupoti.sm.main.services.ComputeTeamValuesService;
import org.amupoti.sm.main.services.DataPopulationService;
import org.amupoti.sm.main.services.MatchControlService;
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


    @RequestMapping(value = "/players/table.html")
    public String getSupermagerInfo(Model model) throws URISyntaxException, ExecutionException, XPatherException, InterruptedException, IOException {

        populatePlayerData(model);
        return "wizard";
    }

    @RequestMapping(value = "/players/simple.html")
    public String getSimpleSupermagerInfo(Model model) throws URISyntaxException, ExecutionException, XPatherException, InterruptedException, IOException {

        populatePlayerData(model);
        return "simple";
    }

    private void populatePlayerData(Model model) {
        Iterable<PlayerEntity> playerList =  playerService.getPlayers();
        List<SMPlayerDataBean> smDataList = new LinkedList<>();
        for (PlayerEntity playerEntity:playerList){
            SMPlayerDataBean smPlayerDataBean = computePlayerValuesService.addPlayerData(playerEntity);
            smDataList.add(smPlayerDataBean);

        }
        //TODO: compute max and min values
        //TODO: create list with percentage values
        model.addAttribute("smDataList", smDataList);
        model.addAttribute("matchNumber", matchControlService.getMatchNumber());
    }

    @RequestMapping(value = "/")
    public String root(Model model) {

        return "index";
    }

    @RequestMapping(value = "/players/news.html")
    public String getPLayerNews(Model model) throws URISyntaxException, ExecutionException, XPatherException, InterruptedException, IOException {

        populatePlayerData(model);
        return "news";
    }



}

