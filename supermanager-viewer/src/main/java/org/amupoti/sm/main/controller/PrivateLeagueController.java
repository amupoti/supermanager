package org.amupoti.sm.main.controller;

import org.amupoti.sm.main.model.PrivateLeagueTeamData;
import org.amupoti.sm.main.service.PrivateLeagueService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * Created by Marcel on 13/01/2016.
 */
@Controller
public class PrivateLeagueController {

    private final static Log log = LogFactory.getLog(PrivateLeagueController.class);

    @Autowired
    private PrivateLeagueService privateLeagueService;


    @RequestMapping(value = "/private/league.html", method = RequestMethod.GET)
    public String getPrivateLeagueData(Model model) {

        Map<String, PrivateLeagueTeamData> teamMap = privateLeagueService.getPrivateLeagueTeams();
        model.addAttribute("teamMap", teamMap);
        return "privateLeague";
    }

}
