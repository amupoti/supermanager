package org.amupoti.sm.main;

import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.DataPopulationService;
import org.amupoti.sm.main.services.MatchControlService;
import org.amupoti.sm.main.services.PlayerService;
import org.amupoti.sm.main.services.TeamService;
import org.amupoti.sm.main.services.compute.ComputePlayerValuesService;
import org.amupoti.sm.main.services.compute.bean.SMDataBean;
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
    private MatchControlService matchControlService;


    @RequestMapping(value = "/teams/")
    public String getTeams(Model model) throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {

        Iterable<TeamEntity> teamList =  teamService.getTeams();
        model.addAttribute("teams", teamList);
        return "teams";
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
        List<SMDataBean> smDataList = new LinkedList<>();
        for (PlayerEntity playerEntity:playerList){
            SMDataBean smDataBean = new SMDataBean();
            computePlayerValuesService.addPlayerData(playerEntity, smDataBean);
            computePlayerValuesService.addTeamData(playerEntity,smDataBean);

            smDataList.add(smDataBean);

        }
        model.addAttribute("smDataList", smDataList);

        return "wizard";
    }

    @RequestMapping(value = "/")
    public String rootRedirect(Model model) throws URISyntaxException, ExecutionException, XPatherException, InterruptedException, IOException {

        return "redirect:/wizard/";
    }



}

