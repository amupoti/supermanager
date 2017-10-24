package org.amupoti.sm.rdm.parser.repository;

import org.amupoti.sm.rdm.parser.repository.entity.PlayerEntity;
import org.amupoti.sm.rdm.parser.repository.entity.PlayerId;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Marcel on 08/08/2015.
 */
public interface PlayerRepository extends CrudRepository<PlayerEntity, Integer> {
    PlayerEntity findByPlayerId(PlayerId playerId);
}
