package org.amupoti.sm.main.service.privateleague;

import org.amupoti.supermanager.parser.acb.privateleague.PrivateLeagueCategory;
import org.amupoti.supermanager.parser.acb.teams.SMUserTeamService;
import org.amupoti.supermanager.parser.rdm.RdmMatchService;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by amupoti on 05/10/2019.
 */
@Service
public class PrivateLeagueStatsService {

    @Autowired
    private RdmMatchService matchService;

    @Autowired
    private SMUserTeamService smUserTeamService;

    @Autowired
    private PrivateLeagueRepository privateLeagueRepository;

    public void storeLeagueStatsForMatch(String user, String password, int matchNumber) throws XPatherException {


        //getStatsForTeams
        Map<PrivateLeagueCategory, Map<String, Integer>> privateLeagueData = smUserTeamService.getPrivateLeagueData(user, password);

        List<PlayerLeagueStateEntity> collect = privateLeagueData.entrySet().stream()
                .map(e -> generatePlayerStatEntity(matchNumber, e))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        //Store in DB
        privateLeagueRepository.save(collect);
    }

    public List<PlayerLeagueStateEntity> getLeagueStats(String stat, int matchNumber) {
        List<PlayerLeagueStateEntity> currentMatchStats = privateLeagueRepository.findByMatchNumberAndStat(matchNumber, stat);
        boolean dataForPreviousMatch = privateLeagueRepository.findByMatchNumber(matchNumber - 1).size() != 0;
        if (!dataForPreviousMatch) {
            return currentMatchStats;
        } else {
            return computeDeltaWithPreviousMatch(stat, matchNumber, currentMatchStats);
        }
    }

    private List<PlayerLeagueStateEntity> computeDeltaWithPreviousMatch(String stat, int matchNumber, List<PlayerLeagueStateEntity> currentMatchStats) {
        List<PlayerLeagueStateEntity> previousMatchStats = privateLeagueRepository.findByMatchNumberAndStat(matchNumber - 1, stat);
        currentMatchStats.sort(Comparator.comparing(PlayerLeagueStateEntity::getTeam));
        previousMatchStats.sort(Comparator.comparing(PlayerLeagueStateEntity::getTeam));
        List<PlayerLeagueStateEntity> entitiesWithPointsDelta = new ArrayList<>();

        for (int i = 0; i < currentMatchStats.size(); i++) {
            entitiesWithPointsDelta.add(getPlayerLeagueStateWithPointsDelta(previousMatchStats.get(i), currentMatchStats.get(i)));
        }
        return entitiesWithPointsDelta;
    }

    private PlayerLeagueStateEntity getPlayerLeagueStateWithPointsDelta(PlayerLeagueStateEntity previousStats, PlayerLeagueStateEntity currentStats) {
        int currentPoints = currentStats.getPoints();
        int previousPoints = previousStats.getPoints();
        return new PlayerLeagueStateEntity(
                currentStats.getMatchNumber(),
                currentStats.getTeam(),
                currentStats.getStat(),
                currentPoints - previousPoints);
    }

    private List<PlayerLeagueStateEntity> generatePlayerStatEntity(int currentMatch, Map.Entry<PrivateLeagueCategory, Map<String, Integer>> e) {
        PrivateLeagueCategory category = e.getKey();
        return e.getValue().entrySet().stream().map(teamStat -> new PlayerLeagueStateEntity(currentMatch, teamStat.getKey(), category.toString(), teamStat.getValue())).collect(Collectors.toList());
    }
}
