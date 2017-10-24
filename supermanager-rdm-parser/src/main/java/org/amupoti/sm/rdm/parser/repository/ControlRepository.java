package org.amupoti.sm.rdm.parser.repository;

import org.amupoti.sm.rdm.parser.repository.entity.ControlEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Marcel on 03/11/2015.
 */
public interface ControlRepository extends CrudRepository<ControlEntity, Integer> {

    ControlEntity findByName(String name);
}
