package org.amupoti.sm.main;

import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.DataPopulationService;
import org.amupoti.sm.main.services.PlayerService;
import org.amupoti.sm.main.services.TeamService;
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
    public void populateData() throws IOException, XPatherException, InterruptedException, ExecutionException, URISyntaxException {
        dataPopulationService.populate();
    }




}

