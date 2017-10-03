package org.amupoti.sm.main.controller;

import org.amupoti.sm.main.bean.SMUser;
import org.amupoti.sm.main.model.UserTeamViewData;
import org.amupoti.sm.main.users.UserCredentialsHolder;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.amupoti.supermanager.parser.acb.teams.SMUserTeamService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Marcel on 13/01/2016.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final static Log log = LogFactory.getLog(UserController.class);

    @Autowired
    private SMUserTeamService SMUserTeamService;

    private UserCredentialsHolder userCredentialsHolder = new UserCredentialsHolder();

    @RequestMapping(value = "/login.html", method = RequestMethod.GET)
    public String getUserTeamsForm(Model model) {

        return "form";
    }

    @RequestMapping(value = "/dologin.html", method = RequestMethod.POST)
    public String doLogin(@ModelAttribute SMUser user, Model model) throws XPatherException {

        String id = UUID.randomUUID().toString();

        userCredentialsHolder.addCredentials(id, user);
        String redirectURL = "/users/teams.html?id=" + id;
        return "redirect:" + redirectURL;

    }

    @RequestMapping(value = "/teams.html", method = RequestMethod.GET)
    public String getUserTeams(@RequestParam String id, Model model) throws XPatherException {

        //TODO: validate if null
        Optional<SMUser> credentialsByKey = userCredentialsHolder.getCredentialsByKey(id);
        if (!credentialsByKey.isPresent()) {
            throw new SmException(ErrorCode.INCORRECT_SESSION_ID);
        }

        SMUser user = credentialsByKey.get();
        HashMap<String, UserTeamViewData> teamMap = new HashMap<>();

        if (user != null) {
            log.info("Getting teams for user " + user.getLogin());
        } else {
            log.info("Login was not found. Cannot provide team data");
        }

        List<SmTeam> userTeams = SMUserTeamService.getTeamsByCredentials(user.getLogin(), user.getPassword());
        for (SmTeam team : userTeams) {
            teamMap.put(team.getName(), new UserTeamViewData(team.getPlayerList(), team.getScore(), team.getComputedScore(),
                    team.getUsedPlayers(), team.getMeanScorePerPlayer(), team.getScorePrediction()));
        }

        model.addAttribute("teamMap", teamMap);
        return "userTeams";
    }
}
