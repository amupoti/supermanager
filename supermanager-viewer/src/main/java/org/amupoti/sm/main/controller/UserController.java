package org.amupoti.sm.main.controller;

import org.amupoti.sm.main.bean.SMUser;
import org.amupoti.sm.main.model.PrivateLeagueTeamData;
import org.amupoti.sm.main.service.PrivateLeagueService;
import org.amupoti.sm.main.service.RdmSmTeamService;
import org.amupoti.sm.main.users.UserCredentialsHolder;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.amupoti.supermanager.parser.acb.utils.DataUtils;
import org.amupoti.supermanager.parser.rdm.RdmMatchService;
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

import java.util.*;

import static org.amupoti.sm.main.service.RdmSmTeamService.NEXT_MATCHES;

/**
 * Created by Marcel on 13/01/2016.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final static Log log = LogFactory.getLog(UserController.class);

    @Autowired
    private org.amupoti.supermanager.parser.acb.teams.SMUserTeamService SMUserTeamService;

    @Autowired
    private UserCredentialsHolder userCredentialsHolder;

    @Autowired
    private RdmSmTeamService rdmSmTeamService;

    @Autowired
    private RdmMatchService matchService;

    @Autowired
    private PrivateLeagueService privateLeagueService;

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
    public String getUserTeams(@RequestParam String id, Model model) throws Exception {

        //TODO: validate if null
        Optional<SMUser> credentialsByKey = userCredentialsHolder.getCredentialsByKey(id);
        if (!credentialsByKey.isPresent()) {
            throw new SmException(ErrorCode.INCORRECT_SESSION_ID);
        }

        SMUser user = credentialsByKey.get();
        Map<String, PrivateLeagueTeamData> teamMap = new HashMap<>();

        if (user != null) {
            log.info("Getting teams for user " + user.getLogin());
        } else {
            log.info("Login was not found. Cannot provide team data");
        }

        List<SmTeam> userTeams = SMUserTeamService.getTeamsByCredentials(user.getLogin(), user.getPassword());
        for (SmTeam team : userTeams) {
            teamMap.put(team.getName(), PrivateLeagueTeamData.builder()
                    .user(user.getLogin())
                    //TODO: .updatedAt(Instant.now())
                    .playerList(rdmSmTeamService.buildPlayerList(team.getPlayerList()))
                    .score(team.getScore())
                    .computedScore(team.getComputedScore())
                    .usedPlayers(team.getUsedPlayers())
                    .meanScorePerPlayer(team.getMeanScorePerPlayer())
                    .scorePrediction(team.getScorePrediction())
                    .cash(DataUtils.format(team.getCash()))
                    .totalBroker(DataUtils.format(team.getTotalBroker()))
                    .teamBroker(DataUtils.format(team.getTeamBroker()))
                    .teamUrl(team.getWebUrl())
                    .build());
        }

        privateLeagueService.storePrivateLeagueTeams(teamMap);
        buildModel(id, model, user, teamMap);
        return "userTeams";
    }

    private void buildModel(String id, Model model, SMUser user, Map<String, PrivateLeagueTeamData> teamMap) {
        Integer firstMatch = matchService.getNextMatch();
        int lastMatch = Math.min(34, firstMatch + NEXT_MATCHES - 1);
        model.addAttribute("firstMatch", firstMatch.intValue());
        model.addAttribute("lastMatch", lastMatch);
        model.addAttribute("teamMap", teamMap);
        model.addAttribute("username", user.getLogin());
        model.addAttribute("id", id);
    }


}
