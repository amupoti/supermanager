package org.amupoti.sm.main.services.compute;

import org.amupoti.sm.main.bean.PlayerPosition;
import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.TeamService;
import org.amupoti.sm.main.services.compute.bean.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Created by Marcel on 05/10/2015.
 */
@Service
public class DataBoostService {


    @Autowired
    private TeamService teamService;

    private static final int MAX_GAMES = 34;

    /**
     *
     * @param team
     * @param matchNumber The current match number that will be played
     * @param matchesAhead How many matches after the current match we want to check
     * @param playerPosition
     * @return
     */
    public String getCalendar(TeamEntity team, int matchNumber, int matchesAhead, PlayerPosition playerPosition) {

        int match = matchesAhead+matchNumber;
        MatchEntity matchEntity = team.getMatchMap().get(match);
        String valRec;
        if (matchEntity.isLocal(team.getName())) {
            valRec = teamService.getTeam(matchEntity.getVisitor()).getValMap().get(playerPosition.getId()).getValRecV();
        }else{
            valRec = teamService.getTeam(matchEntity.getLocal()).getValMap().get(playerPosition.getId()).getValRecL();

        }
        return DataUtils.format(parseVal(valRec));
    }

    /**
     *
     * @param team
     * @param matchNumber The current match number that will be played
     * @param matchesAhead How many matches after the current match we want to check. 0 means we are just checking the current match
     * @param playerPosition
     * @return
     */
    public String getCalendarSum(TeamEntity team, int matchNumber,int matchesAhead,PlayerPosition playerPosition) {

        BigDecimal ranking = new BigDecimal(0);
        int matchesAheadLimit = matchesAhead+matchNumber;
        for (int i=matchNumber;i<=MAX_GAMES && i<=matchesAheadLimit;i++) {
            MatchEntity matchEntity = team.getMatchMap().get(i);
            String valRec;
            if (matchEntity.isLocal(team.getName())) {
                valRec = teamService.getTeam(matchEntity.getVisitor()).getValMap().get(playerPosition.getId()).getValRecV();
            }else{
                valRec = teamService.getTeam(matchEntity.getLocal()).getValMap().get(playerPosition.getId()).getValRecL();

            }
            ranking=ranking.add(parseVal(valRec));
        }

        //Get the ranking mean
        ranking = ranking.divide(new BigDecimal(matchesAhead+1),BigDecimal.ROUND_HALF_EVEN);

        return DataUtils.format(ranking);


    }


    private BigDecimal parseVal(String valRec) {

        return new BigDecimal(Float.parseFloat(valRec));

    }

}
