package org.amupoti.sm.main.controller;

import org.amupoti.supermanager.parser.rdm.RdmMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * Created by Marcel on 03/08/2015.
 */
@Controller
public class TeamController {

    @Autowired
    private RdmMatchService matchService;

    @RequestMapping(value = "/teams/calendar.html")
    public String getCalendar(@RequestParam(required = false, name = "jornada") Optional<String> matchNumber, Model model) {

        Integer firstMatch = matchService.getNextMatch();
        if (matchNumber.isPresent()) {
            firstMatch = Integer.parseInt(matchNumber.get());
        }
        model.addAttribute("firstMatch", firstMatch.intValue());
        model.addAttribute("teamsData", matchService.getTeamsData().toArray());
        return "calendar";
    }


}

