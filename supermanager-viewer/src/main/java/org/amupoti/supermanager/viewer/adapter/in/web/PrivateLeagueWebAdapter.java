package org.amupoti.supermanager.viewer.adapter.in.web;

import org.amupoti.supermanager.viewer.application.model.PrivateLeagueTeamData;
import org.amupoti.supermanager.viewer.application.port.in.ViewPrivateLeagueUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class PrivateLeagueWebAdapter {

    @Autowired
    private ViewPrivateLeagueUseCase privateLeagueUseCase;

    @RequestMapping(value = "/private/league.html", method = RequestMethod.GET)
    public String getPrivateLeagueData(Model model) {
        Map<String, PrivateLeagueTeamData> teamMap = privateLeagueUseCase.getPrivateLeagueTeams();
        model.addAttribute("teamMap", teamMap);
        return "privateLeague";
    }
}
