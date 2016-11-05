package org.amupoti.sm.main.services;

import org.amupoti.sm.main.bean.PlayerPosition;
import org.amupoti.sm.main.config.SMConstants;
import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.repository.entity.ValueEntity;
import org.amupoti.sm.main.services.repository.TeamService;
import org.amupoti.supermanager.parser.acb.bean.SMPlayerDataBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.amupoti.sm.main.config.SMConstants.NOT_PLAYING_MATCH_TEXT;
import static org.amupoti.supermanager.parser.acb.bean.DataUtils.toFloat;

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

        addFollowingMatchesData(playerEntity, smPlayerDataBean, matchNumber, teamEntity);

        /*
         * Get mean values depending if local or visitor
         */
        ValueEntity teamValues = teamEntity.getValMap().get(PlayerPosition.TOTAL.getId());
        MatchEntity matchEntity = teamEntity.getMatchMap().get(matchNumber);

        boolean isLocal = matchEntity.isLocal(teamEntity.getName());


        if (matchEntity.isNotPlayingMatch()){

            setEmptyDataForNoMatch(smPlayerDataBean);
        }
        else{

            TeamEntity otherTeam;
            ValueEntity otherTeamValues;
            String teamVal;
            String otherTeamValAsLV;
            String localOrVisitor;

            if (isLocal) {
                teamVal  = teamValues.getValL();
                otherTeam = teamService.getTeam(matchEntity.getVisitor());
                otherTeamValues = otherTeam.getValMap().get(PlayerPosition.TOTAL.getId());
                otherTeamValAsLV = otherTeamValues.getValRecV();
                localOrVisitor = SMConstants.LOCAL;
            } else {
                teamVal = teamValues.getValV();
                otherTeam = teamService.getTeam(matchEntity.getLocal());
                otherTeamValues = otherTeam.getValMap().get(PlayerPosition.TOTAL.getId());
                otherTeamValAsLV = otherTeamValues.getValRecL();
                localOrVisitor = SMConstants.VISITOR;
            }
            smPlayerDataBean.setOtherTeamReceivedVal(toFloat(otherTeamValues.getValRec()));
            smPlayerDataBean.setOtherTeamName(otherTeam.getName());
            smPlayerDataBean.setTeamValAsLV(toFloat(teamVal));
            smPlayerDataBean.setOtherTeamReceivedValAsLV(toFloat(otherTeamValAsLV));
            smPlayerDataBean.setLocalOrVisitor(localOrVisitor);
            smPlayerDataBean.setTeamVal(toFloat(teamValues.getVal()));
        }

    }

    private void setEmptyDataForNoMatch(SMPlayerDataBean smPlayerDataBean) {
        smPlayerDataBean.setOtherTeamReceivedVal(0.0f);
        smPlayerDataBean.setOtherTeamName(SMConstants.NOT_PLAYING_MATCH);
        smPlayerDataBean.setTeamVal(0.0f);
        smPlayerDataBean.setTeamValAsLV(0.0f);
        smPlayerDataBean.setOtherTeamReceivedValAsLV(0.0f);
        smPlayerDataBean.setLocalOrVisitor(NOT_PLAYING_MATCH_TEXT);

    }

    private void addFollowingMatchesData(PlayerEntity playerEntity, SMPlayerDataBean smPlayerDataBean, int matchNumber, TeamEntity teamEntity) {
        smPlayerDataBean.setPlayerOtherTeamReceivedValShort(dataBoostService.getCalendar(teamEntity, matchNumber, SHORT_TERM, playerEntity.getPlayerPosition()));
        smPlayerDataBean.setPlayerOtherTeamReceivedValMedium(dataBoostService.getCalendar(teamEntity,matchNumber, MEDIUM_TERM, playerEntity.getPlayerPosition()));
        smPlayerDataBean.setPlayerOtherTeamReceivedValLong(dataBoostService.getCalendar(teamEntity,matchNumber, LONG_TERM, playerEntity.getPlayerPosition()));
        smPlayerDataBean.setPlayerOtherNextMatchesVal(dataBoostService.getCalendarSum(teamEntity,matchNumber, LONG_TERM, playerEntity.getPlayerPosition()));
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
        smPlayerDataBean.setMeanLastMatches(playerEntity.getMeanLastMatches());
        smPlayerDataBean.setKeepBroker(playerEntity.getKeepBroker());
        smPlayerDataBean.setBroker(playerEntity.getBroker());
        smPlayerDataBean.setTeamName(playerEntity.getTeam().getName());


    }

    /**
     * Adds data to the player bean which has been computed from raw stats
     * @param smPlayerDataBean
     */
    public void addPlayerComputedData(SMPlayerDataBean smPlayerDataBean) {

        float meanNoNegative = (smPlayerDataBean.getMeanLastMatches()+20)*5;
        float mvpVal = meanNoNegative + (smPlayerDataBean.getPlayerOtherTeamReceivedValShort()) +
                (smPlayerDataBean.getOtherTeamReceivedVal()) / 2 + (smPlayerDataBean.getTeamValAsLV()) / 3;

        smPlayerDataBean.setMvp(toFloat(mvpVal));

        float playerValue =
                meanNoNegative*5 +
                        (smPlayerDataBean.getPlayerOtherTeamReceivedValShort()) * 4 +
                        (smPlayerDataBean.getPlayerOtherTeamReceivedValMedium()) * 3 +
                        (smPlayerDataBean.getPlayerOtherTeamReceivedValLong()) * 2 +
                        (smPlayerDataBean.getOtherTeamReceivedVal()) / 2 +
                        (smPlayerDataBean.getTeamValAsLV()) / 3;

        smPlayerDataBean.setRanking(toFloat(playerValue));

    }

}
