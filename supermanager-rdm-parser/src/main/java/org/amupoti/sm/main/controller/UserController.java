package org.amupoti.sm.main.controller;

import org.amupoti.sm.main.bean.SMUser;
import org.amupoti.supermanager.parser.acb.ACBTeamService;
import org.amupoti.supermanager.parser.acb.bean.SMPlayerDataBean;
import org.amupoti.supermanager.parser.acb.bean.SMUserTeamDataBean;
import org.amupoti.supermanager.parser.acb.beans.ACBPlayer;
import org.amupoti.supermanager.parser.acb.beans.ACBSupermanagerTeam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marcel on 13/01/2016.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final static Log log = LogFactory.getLog(UserController.class);

    private ACBTeamService acbTeamService;

    @RequestMapping(value = "/teams.html", method = RequestMethod.GET)
    public String getUserTeamsForm(Model model) {

        return "form";
    }

    @RequestMapping(value = "/teams.html", method = RequestMethod.POST)
    public String getUserTeams(@ModelAttribute SMUser user, Model model) throws XPatherException {

        HashMap<String, SMUserTeamDataBean> teamMap = new HashMap<>();
        if (user != null) {
            log.info("Getting teams for user " + user.getLogin());
        } else {
            log.info("Login was not found. Cannot provide team data");
        }

        List<ACBSupermanagerTeam> teamsByCredentials = acbTeamService.getTeamsByCredentials(user.getLogin(), user.getPassword());
        for (ACBSupermanagerTeam team : teamsByCredentials) {
            List<SMPlayerDataBean> playerList = new LinkedList<>();

            for (ACBPlayer player : team.getPlayers()) {
                //TODO: add correct player to the list
                //SMPlayerDataBean smPlayerDataBean = computePlayerValuesService.addPlayerData(playerEntity);
                //  playerList.add(smPlayerDataBean);

            }

            teamMap.put(team.getName(), new SMUserTeamDataBean(playerList, team.getScore()));

        }

        model.addAttribute("teamMap", teamMap);

        return "userTeams";

    }


}
