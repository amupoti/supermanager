package org.amupoti.supermanager.viewer.adapter.in.web;

import org.amupoti.supermanager.viewer.application.port.in.ViewCalendarUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class CalendarWebAdapter {

    @Autowired
    private ViewCalendarUseCase viewCalendarUseCase;

    @RequestMapping(value = "/teams/calendar.html")
    public String getCalendar(@RequestParam(required = false, name = "jornada") Optional<String> matchNumber,
                              Model model) {
        int firstMatch = viewCalendarUseCase.resolveMatchNumber(matchNumber);
        model.addAttribute("firstMatch", firstMatch);
        model.addAttribute("teamsData", viewCalendarUseCase.getAllSchedules().toArray());
        return "calendar";
    }
}
