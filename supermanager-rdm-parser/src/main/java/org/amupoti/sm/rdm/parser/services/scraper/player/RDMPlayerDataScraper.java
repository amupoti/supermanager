package org.amupoti.sm.rdm.parser.services.scraper.player;

import org.amupoti.sm.rdm.parser.bean.DataUtils;
import org.amupoti.sm.rdm.parser.bean.PlayerPositionRdm;
import org.amupoti.sm.rdm.parser.provider.HTMLProviderService;
import org.amupoti.sm.rdm.parser.repository.PlayerRepository;
import org.amupoti.sm.rdm.parser.repository.entity.PlayerEntity;
import org.amupoti.sm.rdm.parser.repository.entity.PlayerId;
import org.amupoti.sm.rdm.parser.repository.entity.TeamEntity;
import org.amupoti.sm.rdm.parser.services.MatchControlService;
import org.amupoti.sm.rdm.parser.services.TeamService;
import org.amupoti.sm.rdm.parser.services.exception.DataParsingException;
import org.amupoti.sm.rdm.parser.services.exception.PlayerException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class RDMPlayerDataScraper implements PlayerDataService {

    public static final int ROW_VAL_MEDIA_LOCAL = 1;
    public static final int ROW_VAL_MEDIA_VISITANTE = 2;
    private static final int OFFSET_ROW_TABLE = 3;
    public static final int MEAN_LAST_MATCHES = 5;

    public static final String BASE_VAL_MEDIA = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[row]/td[10]/b";
    public static final String LAST_STATS = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[row]/td[12]/b";
    public static final String VAL_MANTENER_BROKER = "//*[@id=\"sm_central\"]/div[3]/table/tbody/tr[6]/td[2]";
    public static final String ALL_PLAYERS = "//*[@id=\"sm_central\"]/div/table/tbody/tr/td[1]/a";
    private static final String BROKER = "//*[@id=\"sm_central\"]/div[3]/table/tbody/tr[6]/td[4]";
    private static final String FIRST_GAME_PLAYED = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[3]/td[1]/b";
    public static final String PLAYER_TEAM = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[1]/td/b";

    private HtmlCleaner cleaner;

    private static final Log log = LogFactory.getLog(RDMPlayerDataScraper.class);

    @Autowired
    private HTMLProviderService htmlProviderService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchControlService matchControlService;

    public RDMPlayerDataScraper() {
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
        for (PlayerPositionRdm playerPosition : positions) {
            log.info("Getting playerIds for position " + playerPosition);
            String html = htmlProviderService.getAllPlayersURL(playerPosition);
            playerIds.addAll(getPlayerIdsPerPosition(html, playerPosition));
        }
        return playerIds;
    }

    private List<PlayerId> getPlayerIdsPerPosition(String html, PlayerPositionRdm playerPosition) throws IOException, XPatherException {
        List<PlayerId> playerIds = new LinkedList<>();
        TagNode node = cleaner.clean(html);
        Object[] objects = node.evaluateXPath(RDMPlayerDataScraper.ALL_PLAYERS);
        for (int i = 0; i < objects.length; i++) {
            TagNode tagNode = (TagNode) objects[i];
            String name = tagNode.getAllChildren().get(0).toString();
            if (name.contains(",")) {
                PlayerId playerId = new PlayerId(name);
                playerIds.add(playerId);
                playerPositionMap.put(playerId, playerPosition);
            }
        }
        return playerIds;
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

            String localMean = getValueViaLabelForPlayerMeans(html, RDMPlayerDataScraper.ROW_VAL_MEDIA_LOCAL);
            String visitorMean = getValueViaLabelForPlayerMeans(html, RDMPlayerDataScraper.ROW_VAL_MEDIA_VISITANTE);
            String keepBroker = getValueViaXPath(html, RDMPlayerDataScraper.VAL_MANTENER_BROKER);
            String broker = getValueViaXPath(html, RDMPlayerDataScraper.BROKER).replace(",", "");
            String meanLastMatches = getValueViaLabelForLastResults(html);
            log.debug("Mean of lastMatches for player " + playerId + " is " + meanLastMatches);

            //Parse team and store in player data
            String team = getValueViaXPath(html, RDMPlayerDataScraper.PLAYER_TEAM);
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
                int rowMean = matchesPlayed + OFFSET_ROW_TABLE - i - 1;
                String pathExpression = LAST_STATS.replace("row", "" + rowMean);
                objects = node.evaluateXPath(pathExpression);
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
            int matchNumber = matchControlService.getMatchNumber() - firstGamePlayed + OFFSET_ROW_TABLE + row + 1;
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
        String s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
        return Integer.parseInt(s);
    }


}
