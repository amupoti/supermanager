package org.amupoti.sm.rdm.parser.services.scraper.player;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.sm.rdm.parser.bean.DataUtils;
import org.amupoti.sm.rdm.parser.bean.PlayerPositionRdm;
import org.amupoti.sm.rdm.parser.provider.HTMLProviderServiceV2;
import org.amupoti.sm.rdm.parser.repository.PlayerRepository;
import org.amupoti.sm.rdm.parser.repository.entity.PlayerEntity;
import org.amupoti.sm.rdm.parser.repository.entity.PlayerId;
import org.amupoti.sm.rdm.parser.repository.entity.TeamEntity;
import org.amupoti.sm.rdm.parser.services.MatchControlService;
import org.amupoti.sm.rdm.parser.services.TeamService;
import org.amupoti.sm.rdm.parser.services.exception.DataParsingException;
import org.amupoti.sm.rdm.parser.services.exception.PlayerException;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.BaseToken;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Loads data from www.elrincondelmanager.es so it can be loaded in the local database
 * Data is obtained by retrieveing the full HTML page and performing Xpath transformations
 * Created by Marcel on 17/08/2015.
 */
@Slf4j
public class RdmPlayerDataScraperV2 implements PlayerDataService {

    public static final int ROW_VAL_MEDIA_LOCAL = 1;
    public static final int ROW_VAL_MEDIA_VISITANTE = 2;
    private static final int OFFSET_ROW_TABLE = 0;
    public static final int MEAN_LAST_MATCHES = 5;

    public static final String BASE_VAL_MEDIA = "//*[@id=\"news1\"]/div/table/tbody/tr[row]/td[10]";
    //*[@id="news1"]/div/table/tbody/tr[7]/td[12]
    public static final String LAST_STATS = "//*[@id=\"news1\"]/div/table/tbody/tr[row]/td[12]";
    public static final String VAL_MANTENER_BROKER = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[4]/td[2]";
    private static final String BROKER = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[4]/td[4]";
    private static final String FIRST_GAME_PLAYED = "//*[@id=\"news1\"]/div/table/tbody/tr[1]/td[1]";
    public static final String PLAYER_TEAM = "/body/div[5]/div/div[1]/h2";

    private static final String PLAYERS_BASES = "//*[@id=\"tBases\"]/tbody/*";
    private static final String PLAYERS_ALEROS = "//*[@id=\"tAleros\"]/tbody/*";
    private static final String PLAYERS_PIVOTS = "//*[@id=\"tPívots\"]/tbody/*";

    private HtmlCleaner cleaner;

    @Autowired
    private HTMLProviderServiceV2 htmlProviderService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchControlService matchControlService;

    public RdmPlayerDataScraperV2() {
        cleaner = new HtmlCleaner();
    }

    private HashMap<PlayerId, PlayerPositionRdm> playerPositionMap = new HashMap<>();


    /**
     * Returns the list of player Ids from the web
     *
     * @return
     * @throws IOException
     * @throws XPatherException
     */
    public Set<PlayerId> getPlayerIds() throws IOException, XPatherException {

        Set<PlayerId> playerIds = new LinkedHashSet<>();

        PlayerPositionRdm[] positions = {PlayerPositionRdm.BASE, PlayerPositionRdm.ALERO, PlayerPositionRdm.PIVOT};
        String html = htmlProviderService.getAllPlayersURL();
        for (PlayerPositionRdm playerPosition : positions) {
            log.info("Getting playerIds for position " + playerPosition);
            playerIds.addAll(getPlayerIdsPerPosition(html, playerPosition));
        }
        return playerIds;
    }

    private List<PlayerId> getPlayerIdsPerPosition(String html, PlayerPositionRdm playerPosition) throws IOException, XPatherException {
        List<PlayerId> playerIds = new LinkedList<>();
        TagNode node = cleaner.clean(html);
        String regex = getRegexForPosition(playerPosition);

        Object[] objects = node.evaluateXPath(regex);
        for (int i = 0; i < objects.length; i++) {
            List<? extends BaseToken> children = ((TagNode) objects[i]).getAllElementsList(false).get(0).getChildren().get(0).getChildren().get(0).getAllChildren();
            String name = children.get(children.size() - 1).toString();
            if (name.contains(",")) {
                PlayerId playerId = new PlayerId(name);
                playerIds.add(playerId);
                playerPositionMap.put(playerId, playerPosition);
            } else {
                log.info("Could not add name {}", name);
            }
        }
        return playerIds;
    }

    private String getRegexForPosition(PlayerPositionRdm playerPosition) {
        String regex = null;
        if (playerPosition.equals(PlayerPositionRdm.BASE)) {
            regex = PLAYERS_BASES;
        } else if (playerPosition.equals(PlayerPositionRdm.ALERO)) {
            regex = PLAYERS_ALEROS;
        } else if (playerPosition.equals(PlayerPositionRdm.PIVOT)) {
            regex = PLAYERS_PIVOTS;
        }
        return regex;
    }

    public List<PlayerEntity> getPlayersData(Set<PlayerId> playerIds) {
        List<PlayerEntity> playerDataList = new LinkedList<>();


        //Fire up all URL retrieval and value computation in new threads
        int num = 0;
        for (PlayerId playerId : playerIds) {
            num++;
            log.info("Getting data for player " + playerId + ". " + num + " out of " + playerIds.size() + " processed.");

            try {
                PlayerEntity playerData = populatePlayerData(playerId);
                playerDataList.add(playerData);
            } catch (PlayerException e) {
                log.error("Could not get data for player " + playerId);
                log.info("Processed " + num + " players out of " + playerIds.size());
            }
        }

        return playerDataList;
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the data available for a player by checking the player page
     *
     * @param playerId
     * @return
     * @throws IOException
     * @throws XPatherException
     */
    private PlayerEntity populatePlayerData(PlayerId playerId) throws PlayerException {

        PlayerEntity playerEntity = playerRepository.findByPlayerId(playerId);
        if (playerEntity == null) {
            playerEntity = new PlayerEntity();
        }
        String html = null;
        try {
            html = htmlProviderService.getPlayerURL(playerId);

            String localMean = getValueViaLabelForPlayerMeans(html, RdmPlayerDataScraperV2.ROW_VAL_MEDIA_LOCAL).replace(",", ".");
            String visitorMean = getValueViaLabelForPlayerMeans(html, RdmPlayerDataScraperV2.ROW_VAL_MEDIA_VISITANTE).replace(",", ".");
            String keepBroker = getValueViaXPath(html, RdmPlayerDataScraperV2.VAL_MANTENER_BROKER).replace(",", ".");
            String broker = getValueViaXPath(html, RdmPlayerDataScraperV2.BROKER).replace(".", "");
            String meanLastMatches = getValueViaLabelForLastResults(html);
            log.debug("Mean of lastMatches for player " + playerId + " is " + meanLastMatches);

            //Parse team and store in player data
            String team = getValueViaXPath(html, RdmPlayerDataScraperV2.PLAYER_TEAM);
            team = parseTeam(team);
            TeamEntity teamEntity = teamService.getTeam(team);

            playerEntity.setPlayerId(playerId);
            playerEntity.setLocalMean(Float.parseFloat(localMean));
            playerEntity.setVisitorMean(Float.parseFloat(visitorMean));
            playerEntity.setKeepBroker(Float.parseFloat(keepBroker));
            playerEntity.setPlayerPosition(playerPositionMap.get(playerId));
            playerEntity.setBroker(Float.parseFloat(broker));
            playerEntity.setMeanLastMatches(Float.parseFloat(meanLastMatches));
            playerEntity.setTeam(teamEntity);
        } catch (DataParsingException | URISyntaxException | IOException e) {
            throw new PlayerException("A problem ocurred while populating player " + playerId, e);
        }
        return playerEntity;
    }


    private String parseTeam(final String team) throws PlayerException {
        String teamName = null;
        try {
            String[] split = team.split("\\(");
            teamName = split[1].substring(0, 3);
        } catch (StringIndexOutOfBoundsException e) {
            throw new PlayerException("Invalid team name: " + team, e);
        }
        return teamName;

    }

    private String getValueViaXPath(String html, String xPathExpression) throws DataParsingException {
        try {
            TagNode node = cleaner.clean(html);
            Object[] objects = node.evaluateXPath(xPathExpression);
            String s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            return DataUtils.format(s);
        } catch (Exception e) {
            throw new DataParsingException("Could not get value from html with xPathExpression: " + xPathExpression, e);
        }
    }

    public String getValueViaLabelForLastResults(String html) throws DataParsingException {
        try {


            TagNode node = cleaner.clean(html);
            int firstGamePlayed = getFirstGamePlayed(node);
            //Get how many matches the player played so we can know how many we can include in the mean of last stats
            int matchesPlayed = matchControlService.getMatchNumber() - firstGamePlayed;

            int matchesForMean;
            if (matchesPlayed > MEAN_LAST_MATCHES)
                matchesForMean = MEAN_LAST_MATCHES;
            else
                matchesForMean = matchesPlayed;

            float total = 0.0f;
            Object[] objects;
            String s;
            int computedMatches = 0;
            for (int i = 0; i < matchesForMean; i++) {
                int rowMean = matchesPlayed + OFFSET_ROW_TABLE - i;
                String pathExpression = LAST_STATS.replace("row", "" + rowMean);
                objects = node.evaluateXPath(pathExpression);
                //Skip rows without data
                if (((TagNode) objects[0]).getAllChildren().size() == 0) continue;

                s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
                float matchValue;
                if (NumberUtils.isNumber(s)) {
                    matchValue = Float.parseFloat(s);
                    computedMatches++;
                    total += matchValue;
                }
            }
            Float result;
            if (computedMatches > 0) {
                result = (total / computedMatches);
            } else {
                result = -1.0f;
            }
            String formattedResult = DataUtils.format(result);
            return formattedResult;
        } catch (Exception e) {
            throw new DataParsingException("Could not get value via label for last results", e);
        }

    }

    public String getValueViaLabelForPlayerMeans(String html, int row) throws DataParsingException {

        try {

            TagNode node = cleaner.clean(html);
            int firstGamePlayed = getFirstGamePlayed(node);
            Object[] objects;
            String s;
            //current row to apply xpath is: last game with stats - first game with stats +1 (since we want to obtain the next row)
            int matchNumber = matchControlService.getMatchNumber() - firstGamePlayed + OFFSET_ROW_TABLE + row;
            String pathExpression = BASE_VAL_MEDIA.replace("row", "" + matchNumber);
            objects = node.evaluateXPath(pathExpression);
            s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            return DataUtils.format(s);
        } catch (Exception e) {
            throw new DataParsingException("Could not get value via label for row " + row, e);
        }
    }

    private int getFirstGamePlayed(TagNode node) throws XPatherException {
        Object[] objects = node.evaluateXPath(FIRST_GAME_PLAYED);
        //We need to add here the number of the current match to get the
        if (((TagNode) objects[0]).getAllChildren().size() == 0) return 1;
        else {
            String s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            return Integer.parseInt(s);
        }


    }


}
