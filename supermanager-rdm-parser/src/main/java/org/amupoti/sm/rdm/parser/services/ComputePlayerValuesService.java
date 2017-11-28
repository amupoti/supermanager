package org.amupoti.sm.rdm.parser.services;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.sm.rdm.parser.bean.PlayerPositionRdm;
import org.amupoti.sm.rdm.parser.bean.SMPlayerDataBean;
import org.amupoti.sm.rdm.parser.config.SMConstants;
import org.amupoti.sm.rdm.parser.repository.entity.MatchEntity;
import org.amupoti.sm.rdm.parser.repository.entity.PlayerEntity;
import org.amupoti.sm.rdm.parser.repository.entity.TeamEntity;
import org.amupoti.sm.rdm.parser.repository.entity.ValueEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static org.amupoti.sm.rdm.parser.bean.DataUtils.toFloat;
import static org.amupoti.sm.rdm.parser.config.SMConstants.NOT_PLAYING_MATCH_TEXT;

/**
 * Created by Marcel on 18/11/2015.
 */
@Service
@Slf4j
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
     *
     * @param playerEntity
     * @param smPlayerDataBean
     */
    private void addTeamData(PlayerEntity playerEntity, SMPlayerDataBean smPlayerDataBean) {

        int matchNumber = matchControlService.getMatchNumber();
        LOG.debug("Showing data for match number " + matchNumber);

        TeamEntity teamEntity = playerEntity.getTeam();

        addFollowingMatchesData(playerEntity, smPlayerDataBean, matchNumber, teamEntity);

        /*
         * Get mean values depending if local or visitor
         */
        ValueEntity teamValues = teamEntity.getValMap().get(PlayerPositionRdm.TOTAL.getId());
        MatchEntity matchEntity = teamEntity.getMatchMap().get(matchNumber);

        boolean isLocal = matchEntity.isLocal(teamEntity.getName());


        if (matchEntity.isNotPlayingMatch()) {

            setEmptyDataForNoMatch(smPlayerDataBean);
        } else {

            TeamEntity otherTeam;
            ValueEntity otherTeamValues;
            String teamVal;
            String otherTeamValAsLV;
            String localOrVisitor;

            if (isLocal) {
                teamVal = teamValues.getValL();
                otherTeam = teamService.getTeam(matchEntity.getVisitor());
                otherTeamValues = otherTeam.getValMap().get(PlayerPositionRdm.TOTAL.getId());
                otherTeamValAsLV = otherTeamValues.getValRecV();
                localOrVisitor = SMConstants.LOCAL;
            } else {
                teamVal = teamValues.getValV();
                otherTeam = teamService.getTeam(matchEntity.getLocal());
                otherTeamValues = otherTeam.getValMap().get(PlayerPositionRdm.TOTAL.getId());
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
        smPlayerDataBean.setPlayerOtherTeamReceivedValMedium(dataBoostService.getCalendar(teamEntity, matchNumber, MEDIUM_TERM, playerEntity.getPlayerPosition()));
        smPlayerDataBean.setPlayerOtherTeamReceivedValLong(dataBoostService.getCalendar(teamEntity, matchNumber, LONG_TERM, playerEntity.getPlayerPosition()));
        smPlayerDataBean.setPlayerOtherNextMatchesVal(dataBoostService.getCalendarSum(teamEntity, matchNumber, LONG_TERM, playerEntity.getPlayerPosition()));
    }

    /**
     * Adds all data related to the player that we want to show in the wizard
     *
     * @param playerEntity
     * @param smPlayerDataBean
     */
    private void addRawPlayerData(PlayerEntity playerEntity, SMPlayerDataBean smPlayerDataBean) {
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
     *
     * @param smPlayerDataBean
     */
    private void addPlayerComputedData(SMPlayerDataBean smPlayerDataBean) {

        float meanNoNegative = (smPlayerDataBean.getMeanLastMatches() + 20) * 5;
        float mvpVal = meanNoNegative + (smPlayerDataBean.getPlayerOtherTeamReceivedValShort()) +
                (smPlayerDataBean.getOtherTeamReceivedVal()) / 2 + (smPlayerDataBean.getTeamValAsLV()) / 3;

        mvpVal = (mvpVal - 130) * 100 / 260;
        smPlayerDataBean.setMvp(toFloat(bounded(mvpVal)));

        float playerValue =
                meanNoNegative * 2 +
                        (smPlayerDataBean.getPlayerOtherTeamReceivedValShort()) * 4 +
                        (smPlayerDataBean.getPlayerOtherTeamReceivedValMedium()) * 3 +
                        (smPlayerDataBean.getPlayerOtherTeamReceivedValLong()) * 2 +
                        (smPlayerDataBean.getOtherTeamReceivedVal()) / 2 +
                        (smPlayerDataBean.getTeamValAsLV()) / 3;
        playerValue = (playerValue - 180) * 100 / 900;
        smPlayerDataBean.setRanking(toFloat(bounded(playerValue)));
        smPlayerDataBean.setColor(computeColor(bounded(playerValue)));
    }


    private String computeColor(Float mvp) {
        float red = 255 * (100 - mvp) / 100;
        float blue = 110;
        float green = (255 * mvp) / 100;
        float offset = 80;
        red = boundedColor(red + offset);
        green = boundedColor(green + offset);
        return Integer.toHexString((int) red)
                + Integer.toHexString((int) green)
                + Integer.toHexString((int) blue);


    }

    private float bounded(float value, float max) {
        return Math.min(max, Math.max(value, 0));
    }
    private float bounded(float playerValue) {
        return bounded(playerValue, 100);
    }

    private float boundedColor(float playerValue) {
        return bounded(playerValue, 255);
    }

    @Cacheable("playerData")
    public SMPlayerDataBean addPlayerData(PlayerEntity playerEntity) {
        SMPlayerDataBean smPlayerDataBean = new SMPlayerDataBean();
        addRawPlayerData(playerEntity, smPlayerDataBean);
        addTeamData(playerEntity, smPlayerDataBean);
        addPlayerComputedData(smPlayerDataBean);
        return smPlayerDataBean;
    }
}
