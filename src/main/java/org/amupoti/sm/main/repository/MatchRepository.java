package org.amupoti.sm.main.repository;

import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Marcel on 16/08/2015.
 */
public interface MatchRepository extends CrudRepository<MatchEntity,String>{

    /**
     * Returns all the matches for the given teamName
     * @return
     */
    @Query("select m from MatchEntity m where m.local= ?1 OR m.visitor=?1")
    Iterable<MatchEntity> findByLocalOrVisitor(String teamName);
}
