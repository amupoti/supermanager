package org.amupoti.sm.main.repository;

import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Marcel on 10/08/2015.
 */
public interface TeamRepository extends CrudRepository<TeamEntity,String> {

    public TeamEntity findByName(String name);
}
