package org.amupoti.sm.main.repository;

import org.amupoti.sm.main.repository.entity.ValueEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Marcel on 10/08/2015.
 */
public interface ValueRepository extends CrudRepository<ValueEntity,String> {
}
