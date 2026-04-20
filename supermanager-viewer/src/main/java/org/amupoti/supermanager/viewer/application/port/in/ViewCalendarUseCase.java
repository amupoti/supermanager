package org.amupoti.supermanager.viewer.application.port.in;

import org.amupoti.supermanager.rdm.domain.model.TeamSchedule;

import java.util.List;
import java.util.Optional;

public interface ViewCalendarUseCase {
    int resolveMatchNumber(Optional<String> matchNumberOverride);
    List<TeamSchedule> getAllSchedules();
}
