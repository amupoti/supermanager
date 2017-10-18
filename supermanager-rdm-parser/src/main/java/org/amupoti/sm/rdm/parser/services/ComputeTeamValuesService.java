package org.amupoti.sm.rdm.parser.services;


import org.amupoti.sm.rdm.parser.bean.PlayerPositionRdm;
import org.amupoti.sm.rdm.parser.bean.SMTeamDataBean;
import org.amupoti.sm.rdm.parser.repository.entity.MatchEntity;
import org.amupoti.sm.rdm.parser.repository.entity.TeamEntity;
import org.amupoti.sm.rdm.parser.repository.entity.ValueEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

import static org.amupoti.sm.rdm.parser.bean.DataUtils.toFloat;
import static org.amupoti.sm.rdm.parser.config.SMConstants.NOT_PLAYING_MATCH_TEXT;

/**
 */
@Service
public class ComputeTeamValuesService {

    private static final Float FACTOR = 0.6f;
    private static Log LOG = LogFactory.getLog(ComputeTeamValuesService.class);

    @Autowired
    private TeamService teamService;

    @Autowired
    private MatchControlService matchControlService;


    public List<SMTeamDataBean> getTeamPoints() {

        //TODO: refactor to simplify
        List<SMTeamDataBean> list = new LinkedList<>();
        Iterable<TeamEntity> teams = teamService.getTeams();
        for (TeamEntity team : teams) {
            SMTeamDataBean smTeamDataBean = new SMTeamDataBean();
            smTeamDataBean.setTeam(team.getName());
            ValueEntity valueEntity = team.getValMap().get(getPointsId());
            smTeamDataBean.setPointsLocal(toFloat(valueEntity.getValL()));
            smTeamDataBean.setPointsReceivedLocal(toFloat(valueEntity.getValRecL()));
            smTeamDataBean.setPointsVisitor(toFloat(valueEntity.getValV()));
            smTeamDataBean.setPointsReceivedVisitor(toFloat(valueEntity.getValRecV()));

            int matchNumber = matchControlService.getMatchNumber();
            LOG.debug("Computing team data for team " + team);
            MatchEntity matchEntity = team.getMatchMap().get(matchNumber);
            boolean isLocal = matchEntity.isLocal(team.getName());


            //TODO: consider if team does not play
            TeamEntity otherTeam;
            Float pointsRecOtherTeam;
            Float pointsOtherTeam;
            Float pointsRec;
            Float points;
            Float pointsTeamExpected = 0.0f;
            Float pointsTeamReceivedExpected = 0.0f;
            String teamName;
            if (matchEntity.isNotPlayingMatch()) {
                teamName = NOT_PLAYING_MATCH_TEXT;
            } else {
                if (isLocal) {
                    otherTeam = teamService.getTeam(matchEntity.getVisitor());
                    points = Float.parseFloat(team.getValMap().get(getPointsId()).getValL());
                    pointsRec = Float.parseFloat(team.getValMap().get(getPointsId()).getValRecL());
                    pointsRecOtherTeam = Float.parseFloat(otherTeam.getValMap().get(getPointsId()).getValRecV());
                    pointsOtherTeam = Float.parseFloat(otherTeam.getValMap().get(getPointsId()).getValV());
                    pointsTeamExpected = toFloat(points * FACTOR + pointsRecOtherTeam * (1 - FACTOR));
                    pointsTeamReceivedExpected = toFloat(pointsRec * FACTOR + pointsOtherTeam * (1 - FACTOR));
                } else {
                    otherTeam = teamService.getTeam(matchEntity.getLocal());
                    points = Float.parseFloat(team.getValMap().get(getPointsId()).getValV());
                    pointsRec = Float.parseFloat(team.getValMap().get(getPointsId()).getValRecV());
                    pointsRecOtherTeam = Float.parseFloat(otherTeam.getValMap().get(getPointsId()).getValRecL());
                    pointsOtherTeam = Float.parseFloat(otherTeam.getValMap().get(getPointsId()).getValL());
                    pointsTeamExpected = toFloat(points * (1 - FACTOR) + pointsRecOtherTeam * (FACTOR));
                    pointsTeamReceivedExpected = toFloat(pointsRec * (1 - FACTOR) + pointsOtherTeam * (FACTOR));

                }
                teamName = otherTeam.getName();
            }


            smTeamDataBean.setTeamVs(teamName);
            smTeamDataBean.setPointsExpected(pointsTeamExpected);
            smTeamDataBean.setPointsReceivedExpected(pointsTeamReceivedExpected);
            list.add(smTeamDataBean);
        }
        return list;
    }

    private String getPointsId() {
        return PlayerPositionRdm.TOTAL.getId() + "-points";
    }


}
