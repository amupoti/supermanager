package org.amupoti.sm.main.services.compute;

import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.bean.PlayerPosition;
import org.amupoti.sm.main.services.TeamService;
import org.amupoti.sm.main.services.provider.team.TeamConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Marcel on 05/10/2015.
 */
@Service
public class DataBoostService {


    @Autowired
    private TeamService teamService;

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

    public Float getCalendar(TeamEntity team, int matchNumber,int matchesAhead,PlayerPosition playerPosition) {

        Float ranking = 0.0f;
        int matchesAheadLimit = matchesAhead+matchNumber;
        for (int i=matchNumber;i<=MAX_GAMES && i<matchesAheadLimit;i++) {
            MatchEntity matchEntity = team.getMatchMap().get(i);
            String valRec;
            if (matchEntity.isLocal(team.getName())) {
                valRec = teamService.getTeam(matchEntity.getVisitor()).getValMap().get(playerPosition.getId()).getValRecV();
            }else{
                valRec = teamService.getTeam(matchEntity.getLocal()).getValMap().get(playerPosition.getId()).getValRecL();

            }
            ranking += parseVal(valRec);
        }

        return ranking;

    }

    private Float parseVal(String valRec) {

        return Float.parseFloat(valRec);

    }

}
