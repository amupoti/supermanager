package org.amupoti.sm.main.services.compute;

import org.amupoti.sm.main.bean.PlayerPosition;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.services.provider.team.TeamConstants;
import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.repository.entity.ValueEntity;
import org.amupoti.sm.main.services.MatchControlService;
import org.amupoti.sm.main.services.TeamService;
import org.amupoti.sm.main.services.compute.bean.SMPlayerDataBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Marcel on 18/11/2015.
 */
@Service
public class ComputePlayerValuesService {


    private static final int LONG_TERM = 2;
    private static final int MEDIUM_TERM = 1;
    private static final int SHORT_TERM = 0;

    private static Log LOG = LogFactory.getLog(ComputePlayerValuesService.class);

    @Autowired
    private TeamService teamService;

    @Autowired
    private DataBoostService dataBoostService;

    @Autowired
    private MatchControlService matchControlService;
    /**
     * Adds all information related to the team, like calendar-related boosts, mean values per received per team, etc.
     * @param playerEntity
     * @param smPlayerDataBean
     */
    public void addTeamData(PlayerEntity playerEntity, SMPlayerDataBean smPlayerDataBean) {

        int matchNumber = matchControlService.getMatchNumber();
        LOG.debug("Showing data for match number "+matchNumber);

        TeamEntity teamEntity = playerEntity.getTeam();

        smPlayerDataBean.setPlayerOtherTeamReceivedValShort(dataBoostService.getCalendar(teamEntity, matchNumber, SHORT_TERM, playerEntity.getPlayerPosition()));
        smPlayerDataBean.setPlayerOtherTeamReceivedValMedium(dataBoostService.getCalendar(teamEntity,matchNumber, MEDIUM_TERM, playerEntity.getPlayerPosition()));
        smPlayerDataBean.setPlayerOtherTeamReceivedValLong(dataBoostService.getCalendar(teamEntity,matchNumber, LONG_TERM, playerEntity.getPlayerPosition()));
        smPlayerDataBean.setPlayerOtherNextMatchesVal(dataBoostService.getCalendarSum(teamEntity,matchNumber, LONG_TERM, playerEntity.getPlayerPosition()));

        /*
         * Get mean values depending if local or visitor
         */
        ValueEntity teamValues = teamEntity.getValMap().get(PlayerPosition.TOTAL.getId());
        MatchEntity matchEntity = teamEntity.getMatchMap().get(matchNumber);
        boolean isLocal = matchEntity.isLocal(teamEntity.getName());

        smPlayerDataBean.setTeamVal(teamValues.getVal());
        TeamEntity otherTeam;
        ValueEntity otherTeamValues;
        String teamVal;
        if (isLocal) {
            teamVal  = teamValues.getValL();
            otherTeam = teamService.getTeam(matchEntity.getVisitor());
            otherTeamValues = otherTeam.getValMap().get(PlayerPosition.TOTAL.getId());
            smPlayerDataBean.setOtherTeamReceivedValAsLV(otherTeamValues.getValRecV());
            smPlayerDataBean.setLocalOrVisitor(TeamConstants.LOCAL);
        } else {

            teamVal = teamValues.getValV();
            otherTeam = teamService.getTeam(matchEntity.getLocal());
            otherTeamValues = otherTeam.getValMap().get(PlayerPosition.TOTAL.getId());
            smPlayerDataBean.setOtherTeamReceivedValAsLV(otherTeamValues.getValRecL());
            smPlayerDataBean.setLocalOrVisitor(TeamConstants.VISITOR);
        }
        smPlayerDataBean.setTeamValAsLV(teamVal);
        smPlayerDataBean.setOtherTeamReceivedVal(otherTeamValues.getValRec());
        smPlayerDataBean.setOtherTeamName(otherTeam.getName());
        /*
         *  Get mean values depending on player position
         */
        //TODO: load players with position so we can add value for that position
    }

    /**
     * Adds all data related to the player that we want to show in the wizard
     * @param playerEntity
     * @param smPlayerDataBean
     */
    public void addPlayerData(PlayerEntity playerEntity, SMPlayerDataBean smPlayerDataBean) {
        smPlayerDataBean.setPlayerId(playerEntity.getPlayerId().toString());
        smPlayerDataBean.setPlayerPosition(playerEntity.getPlayerPosition().name());
        smPlayerDataBean.setPlayerLocalVal(playerEntity.getLocalMean());
        smPlayerDataBean.setPlayerVisitorVal(playerEntity.getVisitorMean());
        smPlayerDataBean.setKeepBroker(playerEntity.getKeepBroker());
        smPlayerDataBean.setBroker(playerEntity.getBroker());
        smPlayerDataBean.setTeamName(playerEntity.getTeam().getName());


    }

}
