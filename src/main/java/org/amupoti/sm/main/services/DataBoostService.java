package org.amupoti.sm.main.services;

import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.provider.team.TeamConstants;
import org.springframework.stereotype.Service;

/**
 * Created by Marcel on 05/10/2015.
 */
@Service
public class DataBoostService {


    private static final int MAX_GAMES = 34;

    public Float getCalendarData(TeamEntity team, int matchNumber,int matchesAhead) {

        Float ranking = 0.0f;
        int matchesAheadLimit = matchesAhead+matchNumber;
        for (int i=matchNumber;i<=MAX_GAMES && i<matchesAheadLimit;i++) {
            MatchEntity matchEntity = team.getMatchMap().get(i);
            if (matchEntity.isLocal(team.getName())) {

                ranking += TeamConstants.getTeamBoosts().get(matchEntity.getVisitor());
                ranking += TeamConstants.LOCAL_BOOST;
            }else{
                ranking += TeamConstants.getTeamBoosts().get(matchEntity.getLocal());
            }
        }

        return ranking;

    }

}
