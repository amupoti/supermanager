package org.amupoti.sm.rdm.parser.repository;

import org.amupoti.sm.rdm.parser.repository.entity.TeamEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Marcel on 10/08/2015.
 */
public interface TeamRepository extends CrudRepository<TeamEntity, Integer> {

    TeamEntity findByName(String name);
}
