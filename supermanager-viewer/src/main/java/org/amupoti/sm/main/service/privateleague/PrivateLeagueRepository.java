package org.amupoti.sm.main.service.privateleague;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by amupoti on 05/10/2019.
 */
@Repository
public interface PrivateLeagueRepository extends CrudRepository<PlayerLeagueStateEntity, PlayerLeagueStateId> {
    List<PlayerLeagueStateEntity> findByMatchNumber(int matchNumber);

    List<PlayerLeagueStateEntity> findByMatchNumberAndStat(int matchNumber, String stat);
}
