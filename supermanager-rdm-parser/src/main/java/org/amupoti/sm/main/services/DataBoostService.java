package org.amupoti.sm.main.services;

import org.amupoti.sm.main.bean.PlayerPosition;
import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.repository.TeamService;
import org.amupoti.supermanager.parser.acb.bean.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static org.amupoti.sm.main.config.SMConstants.MAX_GAMES;

/**
 * Created by Marcel on 05/10/2015.
 */
@Service
public class DataBoostService {


    public static final Float NO_VALUE = 50.99f;
    @Autowired
    private TeamService teamService;

    /**
     *
     * @param team
     * @param matchNumber The current match number that will be played
     * @param matchesAhead How many matches after the current match we want to check
     * @param playerPosition
     * @return
     */
    public Float getCalendar(TeamEntity team, int matchNumber, int matchesAhead, PlayerPosition playerPosition) {


        int match = matchesAhead+matchNumber;
        if (match > MAX_GAMES) return 0.0f;

        MatchEntity matchEntity = team.getMatchMap().get(match);
        if (matchEntity.isNotPlayingMatch()) return NO_VALUE;
        String valRec;
        if (matchEntity.isLocal(team.getName())) {
            valRec = teamService.getTeam(matchEntity.getVisitor()).getValMap().get(playerPosition.getId()).getValRecV();
        }else{
            valRec = teamService.getTeam(matchEntity.getLocal()).getValMap().get(playerPosition.getId()).getValRecL();

        }
        return DataUtils.toFloat(parseVal(valRec));
    }

    /**
     *
     * @param team
     * @param matchNumber The current match number that will be played
     * @param matchesAhead How many matches after the current match we want to check. 0 means we are just checking the current match
     * @param playerPosition
     * @return
     */
    public Float getCalendarSum(TeamEntity team, int matchNumber, int matchesAhead, PlayerPosition playerPosition) {

        BigDecimal ranking = new BigDecimal(0);
        int matchesAheadLimit = matchesAhead+matchNumber;
        int matchesComputed=0;
        for (int i=matchNumber;i<=MAX_GAMES && i<=matchesAheadLimit;i++) {
            MatchEntity matchEntity = team.getMatchMap().get(i);
            if (matchEntity.isNotPlayingMatch()) continue;
            else matchesComputed++;

            String valRec;
            if (matchEntity.isLocal(team.getName())) {
                valRec = teamService.getTeam(matchEntity.getVisitor()).getValMap().get(playerPosition.getId()).getValRecV();
            }else{
                valRec = teamService.getTeam(matchEntity.getLocal()).getValMap().get(playerPosition.getId()).getValRecL();

            }
            ranking=ranking.add(parseVal(valRec));
        }

        //Get the ranking mean
        ranking = ranking.divide(new BigDecimal(matchesComputed+1),BigDecimal.ROUND_HALF_EVEN);

        return DataUtils.toFloat(ranking);


    }


    private BigDecimal parseVal(String valRec) {

        return new BigDecimal(Float.parseFloat(valRec));

    }

}
