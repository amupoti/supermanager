package org.amupoti.sm.main.repository;

import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Marcel on 08/08/2015.
 */
public interface PlayerRepository extends CrudRepository <PlayerEntity,Integer>{
    PlayerEntity findByPlayerId(PlayerId playerId);
}
