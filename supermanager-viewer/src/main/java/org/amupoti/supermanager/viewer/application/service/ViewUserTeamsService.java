package org.amupoti.supermanager.viewer.application.service;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.application.port.in.LoadUserTeamsUseCase;
import org.amupoti.supermanager.acb.domain.model.MarketCategory;
import org.amupoti.supermanager.acb.domain.model.Player;
import org.amupoti.supermanager.acb.domain.model.Team;
import org.amupoti.supermanager.acb.utils.DataUtils;
import org.amupoti.supermanager.rdm.application.port.in.GetTeamScheduleUseCase;
import org.amupoti.supermanager.rdm.domain.model.LeagueTeam;
import org.amupoti.supermanager.rdm.domain.model.Match;
import org.amupoti.supermanager.rdm.domain.model.TeamSchedule;
import org.amupoti.supermanager.viewer.application.model.PlayerRow;
import org.amupoti.supermanager.viewer.application.model.PrivateLeagueTeamData;
import org.amupoti.supermanager.viewer.application.model.ViewerMatch;
import org.amupoti.supermanager.viewer.application.model.ViewerPlayer;
import org.amupoti.supermanager.viewer.application.port.in.ViewUserTeamsUseCase;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use case: load all user teams and build the full view model (player list enriched with RDM schedule
 * data and roster slot rows) ready for the web adapter to render.
 */
@Slf4j
public class ViewUserTeamsService implements ViewUserTeamsUseCase {

    private static final Map<String, Integer> POSITION_QUOTA = Map.of("B", 2, "A", 4, "P", 4);
    private static final List<String> POSITION_ORDER = List.of("B", "A", "P");
    private static final String TEAM_RDM = "TEAM_RDM";

    private final LoadUserTeamsUseCase loadUserTeamsUseCase;
    private final GetTeamScheduleUseCase scheduleUseCase;
    private final int nextMatches;

    public ViewUserTeamsService(LoadUserTeamsUseCase loadUserTeamsUseCase,
                                 GetTeamScheduleUseCase scheduleUseCase,
                                 int nextMatches) {
        this.loadUserTeamsUseCase = loadUserTeamsUseCase;
        this.scheduleUseCase = scheduleUseCase;
        this.nextMatches = nextMatches;
    }

    @Override
    public Map<String, PrivateLeagueTeamData> loadUserTeamView(String login, String password) throws IOException {
        List<Team> userTeams = loadUserTeamsUseCase.loadTeams(login, password);
        int nextMatch = scheduleUseCase.getNextMatch();

        Map<String, PrivateLeagueTeamData> teamMap = new HashMap<>();
        for (Team team : userTeams) {
            List<ViewerPlayer> viewerPlayers = buildPlayerList(team.getPlayerList(), nextMatch);
            List<PlayerRow> rows = buildPlayerRows(team, viewerPlayers);
            teamMap.put(team.getName(), PrivateLeagueTeamData.builder()
                    .user(login)
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
        return teamMap;
    }

    private List<ViewerPlayer> buildPlayerList(List<Player> playerList, int nextMatch) {
        return playerList.stream()
                .filter(p -> p.getMarketData() != null)
                .map(p -> enrichWithRdmTeam(p, nextMatch))
                .collect(Collectors.toList());
    }

    private ViewerPlayer enrichWithRdmTeam(Player player, int nextMatch) {
        String smTeamName = player.getMarketData().get(MarketCategory.TEAM.name());
        LeagueTeam rdmTeam = LeagueTeam.fromTeamName(smTeamName);
        player.getMarketData().put(TEAM_RDM, rdmTeam.name());
        TeamSchedule schedule = scheduleUseCase.getTeamSchedule(rdmTeam, nextMatch, nextMatches);
        List<ViewerMatch> matches = schedule.getMatches().stream()
                .map(m -> ViewerMatch.builder()
                        .againstTeam(m.getAgainstTeam())
                        .local(m.isLocal())
                        .build())
                .collect(Collectors.toList());
        return ViewerPlayer.builder().player(player).matches(matches).build();
    }

    private List<PlayerRow> buildPlayerRows(Team team, List<ViewerPlayer> viewerPlayers) {
        Map<String, Player> candidates = team.getCandidatesByPosition() != null
                ? team.getCandidatesByPosition() : Collections.emptyMap();

        Map<String, Long> teamPosCounts = team.getPlayerList().stream()
                .filter(p -> p.getPosition() != null)
                .collect(Collectors.groupingBy(Player::getPosition, Collectors.counting()));

        Map<String, List<ViewerPlayer>> viewerByPos = viewerPlayers.stream()
                .filter(vp -> vp.getPlayer().getPosition() != null)
                .collect(Collectors.groupingBy(vp -> vp.getPlayer().getPosition(),
                        LinkedHashMap::new, Collectors.toList()));

        List<PlayerRow> rows = new ArrayList<>();
        for (String pos : POSITION_ORDER) {
            viewerByPos.getOrDefault(pos, Collections.emptyList())
                    .stream()
                    .map(PlayerRow::ofReal)
                    .forEach(rows::add);

            int missing = POSITION_QUOTA.get(pos) - teamPosCounts.getOrDefault(pos, 0L).intValue();
            Player candidate = candidates.get(pos);
            for (int i = 0; i < missing; i++) {
                rows.add(PlayerRow.ofSlot(pos, candidate));
            }
        }
        return rows;
    }
}
