package org.amupoti.supermanager.viewer.application.service;

import org.amupoti.supermanager.rdm.application.port.in.GetAllSchedulesUseCase;
import org.amupoti.supermanager.rdm.application.port.in.GetTeamScheduleUseCase;
import org.amupoti.supermanager.rdm.domain.model.TeamSchedule;
import org.amupoti.supermanager.viewer.application.port.in.ViewCalendarUseCase;

import java.util.List;
import java.util.Optional;

/**
 * Use case: resolve the match number to display and retrieve all team schedules for the calendar view.
 */
public class ViewCalendarService implements ViewCalendarUseCase {

    private final GetTeamScheduleUseCase scheduleUseCase;
    private final GetAllSchedulesUseCase allSchedulesUseCase;

    public ViewCalendarService(GetTeamScheduleUseCase scheduleUseCase,
                               GetAllSchedulesUseCase allSchedulesUseCase) {
        this.scheduleUseCase = scheduleUseCase;
        this.allSchedulesUseCase = allSchedulesUseCase;
    }

    @Override
    public int resolveMatchNumber(Optional<String> matchNumberOverride) {
        return matchNumberOverride
                .map(Integer::parseInt)
                .orElseGet(scheduleUseCase::getNextMatch);
    }

    @Override
    public List<TeamSchedule> getAllSchedules() {
        return allSchedulesUseCase.getAllSchedules();
    }
}
