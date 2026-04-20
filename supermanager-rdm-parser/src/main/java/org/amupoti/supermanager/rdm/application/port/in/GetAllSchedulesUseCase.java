package org.amupoti.supermanager.rdm.application.port.in;

import org.amupoti.supermanager.rdm.domain.model.TeamSchedule;

import java.util.List;

/**
 * Driving port: retrieve all team schedules for the current season.
 */
public interface GetAllSchedulesUseCase {
    List<TeamSchedule> getAllSchedules();
    void evictCaches();
}
