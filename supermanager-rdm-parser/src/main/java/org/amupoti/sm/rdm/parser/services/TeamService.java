package org.amupoti.sm.rdm.parser.services;

import org.amupoti.sm.rdm.parser.repository.TeamRepository;
import org.amupoti.sm.rdm.parser.repository.ValueRepository;
import org.amupoti.sm.rdm.parser.repository.entity.TeamEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Marcel on 11/08/2015.
 */
@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ValueRepository valueRepository;

    public Iterable<TeamEntity> getTeams() {
        return teamRepository.findAll();
    }

    public TeamEntity getTeam(String name) {
        return teamRepository.findByName(name);
    }
}
