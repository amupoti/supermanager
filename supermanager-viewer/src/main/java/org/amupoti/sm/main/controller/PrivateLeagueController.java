package org.amupoti.sm.main.controller;

import org.amupoti.sm.main.bean.SMUser;
import org.amupoti.sm.main.service.privateleague.PlayerLeagueStateEntity;
import org.amupoti.sm.main.service.privateleague.PrivateLeagueStatsService;
import org.amupoti.sm.main.users.UserCredentialsHolder;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.amupoti.supermanager.parser.acb.privateleague.PrivateLeagueCategory;
import org.amupoti.supermanager.parser.rdm.RdmMatchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

/**
 * Created by amupoti on 13/10/2019.
 */
@Controller
@RequestMapping("/league")
public class PrivateLeagueController {

    private final static Log log = LogFactory.getLog(PrivateLeagueController.class);

    @Autowired
    private PrivateLeagueStatsService privateLeagueStatsService;
    @Autowired
    private RdmMatchService matchService;

    @Autowired
    private UserCredentialsHolder userCredentialsHolder;


    @RequestMapping(value = "/store.html", method = RequestMethod.GET)
    public String storePrivateLeagueStats(@RequestParam String id, Model model) throws XPatherException {

        log.info("Populating private league data");
        Optional<SMUser> credentialsByKey = userCredentialsHolder.getCredentialsByKey(id);
        if (!credentialsByKey.isPresent()) {
            throw new SmException(ErrorCode.INCORRECT_SESSION_ID);
        }

        SMUser user = credentialsByKey.get();

        int currentMatch = matchService.getNextMatch() - 1;
        privateLeagueStatsService.storeLeagueStatsForMatch(user.getLogin(), user.getPassword(), currentMatch);


        log.info("Private league data successfully stored");
        String message = "Stats updated successfully for match number " + currentMatch;
        model.addAttribute("message", message);
        return "storeTeams";

    }

    @RequestMapping(value = "/stats.html", method = RequestMethod.GET)
    public String getPrivateLeagueStats(@RequestParam(required = false) String stat, @RequestParam(required = false) String match, Model model) throws XPatherException {

        int matchNumber;

        if (stat == null) {
            stat = PrivateLeagueCategory.REBOUNDS.toString();
        }
        if (match == null) {
            matchNumber = matchService.getNextMatch() - 1;
        } else {

            matchNumber = Integer.parseInt(match);
        }

        List<PlayerLeagueStateEntity> leagueStats = privateLeagueStatsService.getLeagueStats(stat, matchNumber);


        log.info("Private league data successfully retrieved: " + leagueStats.size() + " elements");

        model.addAttribute("leagueStats", leagueStats);
        model.addAttribute("stat", stat);
        model.addAttribute("match", matchNumber);
        return "leagueStats";

    }
}
