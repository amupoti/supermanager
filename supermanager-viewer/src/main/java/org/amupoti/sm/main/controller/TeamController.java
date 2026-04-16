package org.amupoti.sm.main.controller;

import org.amupoti.supermanager.rdm.application.port.in.GetAllSchedulesUseCase;
import org.amupoti.supermanager.rdm.application.port.in.GetTeamScheduleUseCase;
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
    private GetTeamScheduleUseCase scheduleUseCase;

    @Autowired
    private GetAllSchedulesUseCase allSchedulesUseCase;

    @RequestMapping(value = "/teams/calendar.html")
    public String getCalendar(@RequestParam(required = false, name = "jornada") Optional<String> matchNumber, Model model) {

        Integer firstMatch = scheduleUseCase.getNextMatch();
        if (matchNumber.isPresent()) {
            firstMatch = Integer.parseInt(matchNumber.get());
        }
        model.addAttribute("firstMatch", firstMatch.intValue());
        model.addAttribute("teamsData", allSchedulesUseCase.getAllSchedules().toArray());
        return "calendar";
    }
}

