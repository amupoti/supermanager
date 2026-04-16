package org.amupoti.sm.main.controller;

import org.amupoti.sm.main.bean.SMUser;
import org.amupoti.sm.main.model.PlayerRow;
import org.amupoti.sm.main.model.PrivateLeagueTeamData;
import org.amupoti.sm.main.model.ViewerPlayer;
import org.amupoti.sm.main.service.PrivateLeagueService;
import org.amupoti.sm.main.service.RdmSmTeamService;
import org.amupoti.sm.main.users.UserCredentialsHolder;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.amupoti.supermanager.parser.acb.beans.SmPlayer;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.amupoti.supermanager.parser.acb.utils.DataUtils;
import org.amupoti.supermanager.parser.rdm.RdmMatchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Marcel on 13/01/2016.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final Map<String, Integer> POSITION_QUOTA = Map.of("B", 2, "A", 4, "P", 4);
    private static final List<String> POSITION_ORDER = List.of("B", "A", "P");

    private final static Log log = LogFactory.getLog(UserController.class);

    @Autowired
    private org.amupoti.supermanager.parser.acb.teams.SMUserTeamService SMUserTeamService;

    @Autowired
    private UserCredentialsHolder userCredentialsHolder;

    @Autowired
    private SmContentProvider smContentProvider;

    @Autowired
    private RdmSmTeamService rdmSmTeamService;

    @Autowired
    private RdmMatchService matchService;

    @Autowired
    private PrivateLeagueService privateLeagueService;

    @Value("${scraping.next-matches:5}")
    private int nextMatches;

    @RequestMapping(value = "/login.html", method = RequestMethod.GET)
    public String getUserTeamsForm(Model model) {
        return "form";
    }

    @RequestMapping(value = "/dologin.html", method = RequestMethod.POST)
    public String doLogin(@ModelAttribute SMUser user, Model model) {
        String id = UUID.randomUUID().toString();
        userCredentialsHolder.addCredentials(id, user);
        return "redirect:/users/teams.html?id=" + id;
    }

    @RequestMapping(value = "/teams.html", method = RequestMethod.GET)
    public String getUserTeams(@RequestParam String id,
                               @RequestParam(required = false) String error,
                               Model model) throws Exception {

        Optional<SMUser> credentialsByKey = userCredentialsHolder.getCredentialsByKey(id);
        if (!credentialsByKey.isPresent()) {
            throw new SmException(ErrorCode.INCORRECT_SESSION_ID);
        }

        SMUser user = credentialsByKey.get();
        if (user != null) {
            log.info("Getting teams for user " + user.getLogin());
        } else {
            log.info("Login was not found. Cannot provide team data");
        }

        List<SmTeam> userTeams = SMUserTeamService.getTeamsByCredentials(user.getLogin(), user.getPassword());
        Map<String, PrivateLeagueTeamData> teamMap = new HashMap<>();
        for (SmTeam team : userTeams) {
            List<ViewerPlayer> viewerPlayers = rdmSmTeamService.buildPlayerList(team.getPlayerList());
            List<PlayerRow> rows = buildPlayerRows(team, viewerPlayers);
            teamMap.put(team.getName(), PrivateLeagueTeamData.builder()
                    .user(user.getLogin())
                    .playerList(viewerPlayers)
                    .score(team.getScore())
                    .computedScore(team.getComputedScore())
                    .usedPlayers(team.getUsedPlayers())
                    .meanScorePerPlayer(team.getMeanScorePerPlayer())
                    .scorePrediction(team.getScorePrediction())
                    .cash(DataUtils.format(team.getCash()))
                    .totalBroker(DataUtils.format(team.getTotalBroker()))
                    .teamBroker(DataUtils.format(team.getTeamBroker()))
                    .teamUrl(team.getWebUrl())
                    .teamId(team.getTeamId())
                    .rows(rows)
                    .changesUsed(team.getChangesUsed())
                    .maxChanges(team.getMaxChanges())
                    .build());
        }
     privateLeagueService.storePrivateLeagueTeams(teamMap);
        buildModel(id, model, user, teamMap);

        if ("cancel-failed".equals(error)) {
            model.addAttribute("errorMessage", "No se pudo liberar al jugador. El cambio solicitado no existe o ya fue procesado.");
        } else if ("buy-failed".equals(error)) {
            model.addAttribute("errorMessage", "No se pudo comprar al jugador. Verifica tu caja o intenta más tarde.");
        } else if ("undo-failed".equals(error)) {
            model.addAttribute("errorMessage", "No se pudo deshacer el cambio. Es posible que el partido ya haya comenzado.");
        }
        return "userTeams";
    }

    /**
     * Builds the ordered row list for the players table.
     * For each position (B → A → P): real players first, then one row per missing slot
     * (showing the best affordable candidate for that position, or an empty slot if none found).
     */
    private List<PlayerRow> buildPlayerRows(SmTeam team, List<ViewerPlayer> viewerPlayers) {
        Map<String, SmPlayer> candidates = team.getCandidatesByPosition() != null
                ? team.getCandidatesByPosition() : Collections.emptyMap();

        // Count actual team players per position (using the full unfiltered list for accuracy)
        Map<String, Long> teamPosCounts = team.getPlayerList().stream()
                .filter(p -> p.getPosition() != null)
                .collect(Collectors.groupingBy(SmPlayer::getPosition, Collectors.counting()));

        // Group viewer players by position (preserving existing sort order within each group)
        Map<String, List<ViewerPlayer>> viewerByPos = viewerPlayers.stream()
                .filter(vp -> vp.getPlayer().getPosition() != null)
                .collect(Collectors.groupingBy(vp -> vp.getPlayer().getPosition(),
                        LinkedHashMap::new, Collectors.toList()));

        List<PlayerRow> rows = new ArrayList<>();
        for (String pos : POSITION_ORDER) {
            // Real players for this position
            viewerByPos.getOrDefault(pos, Collections.emptyList())
                    .stream()
                    .map(PlayerRow::ofReal)
                    .forEach(rows::add);

            // Missing slots (based on the full team list, not just the viewer-filtered one)
            int missing = POSITION_QUOTA.get(pos) - teamPosCounts.getOrDefault(pos, 0L).intValue();
            SmPlayer candidate = candidates.get(pos);
            for (int i = 0; i < missing; i++) {
                rows.add(PlayerRow.ofSlot(pos, candidate));
            }
        }
        return rows;
    }

    @RequestMapping(value = "/undo-change.html", method = RequestMethod.POST)
    public String undoChange(@RequestParam String id,
                             @RequestParam long idUserTeamPlayerChange,
                             Model model) {
        Optional<SMUser> credentialsByKey = userCredentialsHolder.getCredentialsByKey(id);
        if (!credentialsByKey.isPresent()) {
            throw new SmException(ErrorCode.INCORRECT_SESSION_ID);
        }
        SMUser user = credentialsByKey.get();
        try {
            String token = smContentProvider.authenticateUser(user.getLogin(), user.getPassword()).getJwt();
            smContentProvider.cancelPlayerChange(idUserTeamPlayerChange, token);
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
        Optional<SMUser> credentialsByKey = userCredentialsHolder.getCredentialsByKey(id);
        if (!credentialsByKey.isPresent()) {
            throw new SmException(ErrorCode.INCORRECT_SESSION_ID);
        }
        SMUser user = credentialsByKey.get();
        try {
            String token = smContentProvider.authenticateUser(user.getLogin(), user.getPassword()).getJwt();
            smContentProvider.buyPlayer(teamId, idPlayer, token);
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
        Optional<SMUser> credentialsByKey = userCredentialsHolder.getCredentialsByKey(id);
        if (!credentialsByKey.isPresent()) {
            throw new SmException(ErrorCode.INCORRECT_SESSION_ID);
        }
        SMUser user = credentialsByKey.get();
        try {
            String token = smContentProvider.authenticateUser(user.getLogin(), user.getPassword()).getJwt();
            smContentProvider.removePlayer(teamId, idPlayer, token);
        } catch (Exception e) {
            log.warn("Failed to remove player " + idPlayer + " from team " + teamId + ": " + e.getMessage());
            return "redirect:/users/teams.html?id=" + id + "&error=cancel-failed";
        }
        return "redirect:/users/teams.html?id=" + id;
    }

    private void buildModel(String id, Model model, SMUser user, Map<String, PrivateLeagueTeamData> teamMap) {
        Integer firstMatch = matchService.getNextMatch();
        int lastMatch = Math.min(34, firstMatch + nextMatches - 1);
        model.addAttribute("firstMatch", firstMatch.intValue());
        model.addAttribute("lastMatch", lastMatch);
        model.addAttribute("teamMap", teamMap);
        model.addAttribute("username", user.getLogin());
        model.addAttribute("id", id);
    }
}
