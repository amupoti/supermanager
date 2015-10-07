package org.amupoti.sm.main.services;

import org.amupoti.sm.main.repository.TeamRepository;
import org.amupoti.sm.main.repository.ValueRepository;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Marcel on 11/08/2015.
 */
@Service
public class TeamService {


    @Autowired
    TeamRepository teamRepository;

    @Autowired
    ValueRepository valueRepository;


    public Iterable<TeamEntity> getTeams(){
        return teamRepository.findAll();
    }


}
