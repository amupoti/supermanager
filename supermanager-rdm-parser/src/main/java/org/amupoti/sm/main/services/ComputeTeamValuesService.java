package org.amupoti.sm.main.services;

import org.amupoti.sm.main.bean.PlayerPosition;
import org.amupoti.sm.main.repository.entity.ValueEntity;
import org.amupoti.supermanager.parser.acb.bean.DataUtils;
import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.supermanager.parser.acb.bean.SMTeamDataBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

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


    public List<SMTeamDataBean> getTeamPoints(){

        //TODO: refactor to simplify
        List<SMTeamDataBean> list = new LinkedList<>();
        Iterable<TeamEntity> teams = teamService.getTeams();
        for(TeamEntity team:teams){
            SMTeamDataBean smTeamDataBean = new SMTeamDataBean();
            smTeamDataBean.setTeam(team.getName());
            ValueEntity valueEntity = team.getValMap().get(getPointsId());
            smTeamDataBean.setPointsLocal(valueEntity.getValL());
            smTeamDataBean.setPointsReceivedLocal(valueEntity.getValRecL());
            smTeamDataBean.setPointsVisitor(valueEntity.getValV());
            smTeamDataBean.setPointsReceivedVisitor(valueEntity.getValRecV());

            int matchNumber = matchControlService.getMatchNumber();
            LOG.debug("Computing team data for team " + team);
            MatchEntity matchEntity = team.getMatchMap().get(matchNumber);
            boolean isLocal = matchEntity.isLocal(team.getName());

            TeamEntity otherTeam;
            Float pointsRecOtherTeam;
            Float pointsOtherTeam;
            Float pointsRec;
            Float points;
            String pointsTeamExpected;
            String pointsTeamReceivedExpected;
            if (isLocal) {
                otherTeam = teamService.getTeam(matchEntity.getVisitor());

                points = Float.parseFloat(team.getValMap().get(getPointsId()).getValL());
                pointsRec = Float.parseFloat(team.getValMap().get(getPointsId()).getValRecL());
                pointsRecOtherTeam = Float.parseFloat(otherTeam.getValMap().get(getPointsId()).getValRecV());
                pointsOtherTeam = Float.parseFloat(otherTeam.getValMap().get(getPointsId()).getValV());
                pointsTeamExpected= DataUtils.format(points * FACTOR + pointsRecOtherTeam * (1 - FACTOR));
                pointsTeamReceivedExpected= DataUtils.format(pointsRec * FACTOR + pointsOtherTeam * (1 - FACTOR));
            } else {
                otherTeam = teamService.getTeam(matchEntity.getLocal());
                points = Float.parseFloat(team.getValMap().get(getPointsId()).getValV());
                pointsRec = Float.parseFloat(team.getValMap().get(getPointsId()).getValRecV());
                pointsRecOtherTeam = Float.parseFloat(otherTeam.getValMap().get(getPointsId()).getValRecL());
                pointsOtherTeam = Float.parseFloat(otherTeam.getValMap().get(getPointsId()).getValL());
                pointsTeamExpected= DataUtils.format(points * (1 - FACTOR) + pointsRecOtherTeam * (FACTOR));
                pointsTeamReceivedExpected= DataUtils.format(pointsRec *  (1 - FACTOR) + pointsOtherTeam * (FACTOR));

            }
            smTeamDataBean.setTeamVs(otherTeam.getName());
            smTeamDataBean.setPointsExpected(pointsTeamExpected.toString());
            smTeamDataBean.setPointsReceivedExpected(pointsTeamReceivedExpected);
            list.add(smTeamDataBean);
        }
        return list;
    }

    private String getPointsId() {
        return PlayerPosition.TOTAL.getId() + "-points";
    }


}
