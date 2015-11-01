package org.amupoti.sm.main.services;

import org.amupoti.sm.main.repository.PlayerRepository;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Recovers data from the repository.
 * Created by Marcel on 04/08/2015.
 */
@Service
public class PlayerService {


    @Autowired
    private PlayerRepository playerRepository;


    public Iterable<PlayerEntity> getPlayers() {
        return playerRepository.findAll();
    }

    public PlayerEntity getPlayer(PlayerId playerId){
        return  playerRepository.findByPlayerId(playerId);

    }
}

