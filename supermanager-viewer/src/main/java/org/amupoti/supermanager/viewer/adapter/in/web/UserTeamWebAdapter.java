package org.amupoti.supermanager.viewer.adapter.in.web;

import org.amupoti.supermanager.acb.application.port.in.BuyPlayerUseCase;
import org.amupoti.supermanager.acb.application.port.in.CancelAllChangesUseCase;
import org.amupoti.supermanager.acb.application.port.in.SellPlayerUseCase;
import org.amupoti.supermanager.acb.application.port.in.UndoChangeUseCase;
import org.amupoti.supermanager.acb.application.port.out.AuthenticationPort;
import org.amupoti.supermanager.acb.exception.ErrorCode;
import org.amupoti.supermanager.acb.exception.SmException;
import org.amupoti.supermanager.rdm.application.port.in.GetTeamScheduleUseCase;
import org.amupoti.supermanager.viewer.application.model.PrivateLeagueTeamData;
import org.amupoti.supermanager.viewer.application.port.in.ViewPrivateLeagueUseCase;
import org.amupoti.supermanager.viewer.application.port.in.ViewUserTeamsUseCase;
import org.amupoti.supermanager.viewer.application.port.out.CredentialsStorePort;
import org.amupoti.supermanager.viewer.domain.model.SMUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserTeamWebAdapter {

    private static final Log log = LogFactory.getLog(UserTeamWebAdapter.class);

    @Autowired
    private ViewUserTeamsUseCase viewUserTeamsUseCase;

    @Autowired
    private ViewPrivateLeagueUseCase privateLeagueUseCase;

    @Autowired
    private CredentialsStorePort credentialsStore;

    @Autowired
    private AuthenticationPort authenticationPort;

    @Autowired
    private BuyPlayerUseCase buyPlayerUseCase;

    @Autowired
    private SellPlayerUseCase sellPlayerUseCase;

    @Autowired
    private UndoChangeUseCase undoChangeUseCase;

    @Autowired
    private CancelAllChangesUseCase cancelAllChangesUseCase;

    @Autowired
    private GetTeamScheduleUseCase matchService;

    @Value("${scraping.next-matches:5}")
    private int nextMatches;

    @RequestMapping(value = "/login.html", method = RequestMethod.GET)
    public String getUserTeamsForm(Model model) {
        return "form";
    }

    @RequestMapping(value = "/dologin.html", method = RequestMethod.POST)
    public String doLogin(@ModelAttribute SMUser user, Model model) {
        String id = UUID.randomUUID().toString();
        credentialsStore.store(id, user);
        return "redirect:/users/teams.html?id=" + id;
    }

    @RequestMapping(value = "/teams.html", method = RequestMethod.GET)
    public String getUserTeams(@RequestParam String id,
                               @RequestParam(required = false) String error,
                               Model model) throws Exception {

        SMUser user = credentialsStore.find(id)
                .orElseThrow(() -> new SmException(ErrorCode.INCORRECT_SESSION_ID));

        log.info("Getting teams for user " + user.getLogin());

        Map<String, PrivateLeagueTeamData> teamMap = viewUserTeamsUseCase.loadUserTeamView(
                user.getLogin(), user.getPassword());
        privateLeagueUseCase.storePrivateLeagueTeams(teamMap);

        Integer firstMatch = matchService.getNextMatch();
        int lastMatch = Math.min(34, firstMatch + nextMatches - 1);
        model.addAttribute("firstMatch", firstMatch.intValue());
        model.addAttribute("lastMatch", lastMatch);
        model.addAttribute("teamMap", teamMap);
        model.addAttribute("username", user.getLogin());
        model.addAttribute("id", id);

        if ("cancel-failed".equals(error)) {
            model.addAttribute("errorMessage", "No se pudo liberar al jugador. El cambio solicitado no existe o ya fue procesado.");
        } else if ("buy-failed".equals(error)) {
            model.addAttribute("errorMessage", "No se pudo comprar al jugador. Verifica tu caja o intenta más tarde.");
        } else if ("undo-failed".equals(error)) {
            model.addAttribute("errorMessage", "No se pudo deshacer el cambio. Es posible que el partido ya haya comenzado.");
        }
        return "userTeams";
    }

    @RequestMapping(value = "/cancel-all-changes.html", method = RequestMethod.POST)
    public String cancelAllChanges(@RequestParam String id,
                                   @RequestParam String teamId,
                                   Model model) {
        SMUser user = credentialsStore.find(id)
                .orElseThrow(() -> new SmException(ErrorCode.INCORRECT_SESSION_ID));
        try {
            String token = authenticationPort.authenticate(user.getLogin(), user.getPassword());
            cancelAllChangesUseCase.cancelAllChanges(teamId, token);
        } catch (Exception e) {
            log.warn("Failed to cancel all changes for team " + teamId + ": " + e.getMessage());
            return "redirect:/users/teams.html?id=" + id + "&error=undo-failed";
        }
        return "redirect:/users/teams.html?id=" + id;
    }

    @RequestMapping(value = "/undo-change.html", method = RequestMethod.POST)
    public String undoChange(@RequestParam String id,
                             @RequestParam long idUserTeamPlayerChange,
                             Model model) {
        SMUser user = credentialsStore.find(id)
                .orElseThrow(() -> new SmException(ErrorCode.INCORRECT_SESSION_ID));
        try {
            String token = authenticationPort.authenticate(user.getLogin(), user.getPassword());
            undoChangeUseCase.undoChange(idUserTeamPlayerChange, token);
        } catch (Exception e) {
            log.warn("Failed to undo change " + idUserTeamPlayerChange + ": " + e.getMessage());
            return "redirect:/users/teams.html?id=" + id + "&error=undo-failed";
        }
        return "redirect:/users/teams.html?id=" + id;
    }

    @RequestMapping(value = "/buy-player.html", method = RequestMethod.POST)
    public String buyPlayer(@RequestParam String id,
                            @RequestParam String teamId,
                            @RequestParam long idPlayer,
                            Model model) {
        SMUser user = credentialsStore.find(id)
                .orElseThrow(() -> new SmException(ErrorCode.INCORRECT_SESSION_ID));
        try {
            String token = authenticationPort.authenticate(user.getLogin(), user.getPassword());
            buyPlayerUseCase.buyPlayer(teamId, idPlayer, token);
        } catch (Exception e) {
            log.warn("Failed to buy player " + idPlayer + " for team " + teamId + ": " + e.getMessage());
            return "redirect:/users/teams.html?id=" + id + "&error=buy-failed";
        }
        return "redirect:/users/teams.html?id=" + id;
    }

    @RequestMapping(value = "/cancel-player.html", method = RequestMethod.POST)
    public String cancelPlayer(@RequestParam String id,
                               @RequestParam String teamId,
                               @RequestParam long idPlayer,
                               Model model) {
        SMUser user = credentialsStore.find(id)
                .orElseThrow(() -> new SmException(ErrorCode.INCORRECT_SESSION_ID));
        try {
            String token = authenticationPort.authenticate(user.getLogin(), user.getPassword());
            sellPlayerUseCase.sellPlayer(teamId, idPlayer, token);
        } catch (Exception e) {
            log.warn("Failed to remove player " + idPlayer + " from team " + teamId + ": " + e.getMessage());
            return "redirect:/users/teams.html?id=" + id + "&error=cancel-failed";
        }
        return "redirect:/users/teams.html?id=" + id;
    }
}
