package org.amupoti.sm.main.services.compute;

import org.amupoti.sm.main.bean.PlayerPosition;
import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.repository.entity.ValueEntity;
import org.amupoti.sm.main.services.MatchControlService;
import org.amupoti.sm.main.services.TeamService;
import org.amupoti.sm.main.services.compute.bean.SMDataBean;
import org.amupoti.sm.main.services.provider.team.TeamConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Marcel on 18/11/2015.
 */
@Service
public class ComputePlayerValuesService {


    private static final int LONG_TERM = 6;
    private static final int MEDIUM_TERM = 4;
    private static final int SHORT_TERM = 1;

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
     * @param smDataBean
     */
    public void addTeamData(PlayerEntity playerEntity, SMDataBean smDataBean) {

        int matchNumber = matchControlService.getMatchNumber();
        LOG.debug("Showing data for match number "+matchNumber);

        TeamEntity teamEntity = playerEntity.getTeam();

        smDataBean.setPlayerOtherTeamReceivedValShort(dataBoostService.getCalendar(teamEntity, matchNumber, SHORT_TERM, playerEntity.getPlayerPosition()));
        smDataBean.setPlayerOtherTeamReceivedValMedium(dataBoostService.getCalendar(teamEntity,matchNumber, MEDIUM_TERM, playerEntity.getPlayerPosition()));
        smDataBean.setPlayerOtherTeamReceivedValLong(dataBoostService.getCalendar(teamEntity,matchNumber, LONG_TERM, playerEntity.getPlayerPosition()));

        /*
         * Get mean values depending if local or visitor
         */
        ValueEntity teamValues = teamEntity.getValMap().get(PlayerPosition.TOTAL.getId());
        MatchEntity matchEntity = teamEntity.getMatchMap().get(matchNumber);
        boolean isLocal = matchEntity.isLocal(teamEntity.getName());

        smDataBean.setTeamVal(teamValues.getVal());
        TeamEntity otherTeam;
        ValueEntity otherTeamValues;
        String teamVal;
        if (isLocal) {
            teamVal  = teamValues.getValL();
            otherTeam = teamService.getTeam(matchEntity.getVisitor());
            otherTeamValues = otherTeam.getValMap().get(PlayerPosition.TOTAL.getId());
            smDataBean.setOtherTeamReceivedValAsLV(otherTeamValues.getValRecV());
            smDataBean.setLocalOrVisitor(TeamConstants.LOCAL);
        } else {

            teamVal = teamValues.getValV();
            otherTeam = teamService.getTeam(matchEntity.getLocal());
            otherTeamValues = otherTeam.getValMap().get(PlayerPosition.TOTAL.getId());
            smDataBean.setOtherTeamReceivedValAsLV(otherTeamValues.getValRecL());
            smDataBean.setLocalOrVisitor(TeamConstants.VISITOR);
        }
        smDataBean.setTeamValAsLV(teamVal);
        smDataBean.setOtherTeamReceivedVal(otherTeamValues.getValRec());
        smDataBean.setOtherTeamName(otherTeam.getName());
        /*
         *  Get mean values depending on player position
         */
        //TODO: load players with position so we can add value for that position
    }

    /**
     * Adds all data related to the player that we want to show in the wizard
     * @param playerEntity
     * @param smDataBean
     */
    public void addPlayerData(PlayerEntity playerEntity, SMDataBean smDataBean) {
        smDataBean.setPlayerId(playerEntity.getPlayerId().toString());
        smDataBean.setPlayerPosition(playerEntity.getPlayerPosition().name());
        smDataBean.setPlayerLocalVal(playerEntity.getLocalMean());
        smDataBean.setPlayerVisitorVal(playerEntity.getVisitorMean());
        smDataBean.setKeepBroker(playerEntity.getKeepBroker());
        smDataBean.setBroker(playerEntity.getBroker());
        smDataBean.setTeamName(playerEntity.getTeam().getName());


    }

}
