package org.amupoti.sm.main.services.provider.player;

import org.amupoti.sm.main.bean.PlayerPosition;
import org.amupoti.sm.main.repository.PlayerRepository;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.services.MatchControlService;
import org.amupoti.sm.main.services.TeamService;
import org.amupoti.sm.main.services.exception.PlayerException;
import org.amupoti.sm.main.services.provider.HTMLProviderService;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Loads data from www.elrincondelmanager.es so it can be loaded in the local database
 * Data is obtained by retrieveing the full HTML page and performing Xpath transformations
 * Created by Marcel on 17/08/2015.
 */

public class RDMPlayerDataService implements PlayerDataService {

    public static final int ROW_VAL_MEDIA_LOCAL = 1;
    public static final int ROW_VAL_MEDIA_VISITANTE = 2;
    private static final int OFFSET_ROW_TABLE = 3;
    public static final String BASE_VAL_MEDIA = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[row]/td[10]/b";
    public static final String VAL_MANTENER_BROKER = "//*[@id=\"sm_central\"]/div[3]/table/tbody/tr[6]/td[2]";
    public static final String ALL_PLAYERS = "//*[@id=\"sm_central\"]/div/table/tbody/tr/td[1]/a";
    private static final String BROKER = "//*[@id=\"sm_central\"]/div[3]/table/tbody/tr[6]/td[4]";

    private static final String FIRST_GAME_PLAYED = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[3]/td[1]/b";
    //*[@id="pr"]/div/table/tbody/tr[4]/td[2]/a
    //public static final String ALL_PLAYERS = "//*[@id=\"sm_central\"]/div/table/tbody/tr/td[1]/a";
    public static final String PLAYER_TEAM = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[1]/td/b";


    public static final String BASE = "B";
    public static final String ALERO = "A";
    public static final String PIVOT = "P";



    private HtmlCleaner cleaner;

    private static final Log LOG = LogFactory.getLog(RDMPlayerDataService.class);

    @Autowired
    private HTMLProviderService htmlProviderService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchControlService matchControlService;

    public RDMPlayerDataService() {
        cleaner = new HtmlCleaner();

    }

    private HashMap<PlayerId,PlayerPosition> playerPositionMap =new HashMap<>();


    /**
     * Returns the list of player Ids from the web
     * @return
     * @throws IOException
     * @throws XPatherException
     */
    public Set<PlayerId> getPlayerIds() throws IOException, XPatherException {

        Set<PlayerId> playerIds = new LinkedHashSet<>();

        PlayerPosition[] positions = {PlayerPosition.BASE,PlayerPosition.ALERO,PlayerPosition.PIVOT};
        for (PlayerPosition playerPosition:positions) {
            LOG.info("Getting playerIds for position "+playerPosition);
            String html = htmlProviderService.getAllPlayersURL(playerPosition);
            playerIds.addAll(getPlayerIdsPerPosition(html, playerPosition));
        }
        return playerIds;
    }

    private List<PlayerId> getPlayerIdsPerPosition(String html, PlayerPosition playerPosition) throws IOException, XPatherException {
        List<PlayerId> playerIds = new LinkedList<>();
        TagNode node = cleaner.clean(html);
        Object[] objects = node.evaluateXPath(RDMPlayerDataService.ALL_PLAYERS);
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

    public List<PlayerEntity> getPlayersData(Set<PlayerId> playerIds)  {
        List<Future<PlayerEntity>> futurePlayerDataList = new LinkedList<>();
        List<PlayerEntity> playerDataList = new LinkedList<>();


        //Fire up all URL retrieval and value computation in new threads
        int num =0;
        for (PlayerId playerId:playerIds){
            num++;
            LOG.info("Getting data for player "+playerId+ ". "+num+" out of "+playerIds.size()+" processed.");

            Future<PlayerEntity> playerData = null;
            try {
                playerData = populatePlayerData(playerId);
                futurePlayerDataList.add(playerData);
                sleep(50);
            } catch (PlayerException e) {
                LOG.error("Could not get data for player " + playerId);
            }
        }

        for (int i=0;i<futurePlayerDataList.size();i++){
            if (!futurePlayerDataList.get(i).isDone()){
                LOG.info("Player "+i+" not ready, waiting...");
                sleep(50);
                i--;
            }
            else{
                try {
                    playerDataList.add(futurePlayerDataList.get(i).get());
                    LOG.info("Processed " + (i + 1) + " players out of " + futurePlayerDataList.size());
                } catch (ExecutionException |InterruptedException e) {
                    LOG.error("Could not process player number "+(i+1));
                    e.printStackTrace();
                }
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
    @Cacheable("playerData")
    @Async
    private Future<PlayerEntity> populatePlayerData(PlayerId playerId) throws PlayerException {

        PlayerEntity playerEntity = playerRepository.findByPlayerId(playerId);
        if (playerEntity==null){
            playerEntity = new PlayerEntity();
        }
        String html = null;
        try {
            html = htmlProviderService.getPlayerURL(playerId);

            String localMean = getValueViaLabel(html, RDMPlayerDataService.ROW_VAL_MEDIA_LOCAL);
            String visitorMean = getValueViaLabel(html, RDMPlayerDataService.ROW_VAL_MEDIA_VISITANTE);
            String keepBroker = getValueViaXPath(html, RDMPlayerDataService.VAL_MANTENER_BROKER);
            String broker = getValueViaXPath(html, RDMPlayerDataService.BROKER).replace(",","");

            //Parse team and store in player data
            String team = getValueViaXPath(html, RDMPlayerDataService.PLAYER_TEAM);
            team=parseTeam(team);
            TeamEntity teamEntity = teamService.getTeam(team);

            playerEntity.setPlayerId(playerId);
            playerEntity.setLocalMean(Float.parseFloat(localMean));
            playerEntity.setVisitorMean(Float.parseFloat(visitorMean));
            playerEntity.setKeepBroker(Float.parseFloat(keepBroker));
            playerEntity.setPlayerPosition(playerPositionMap.get(playerId));
            playerEntity.setBroker(Float.parseFloat(broker));
            playerEntity.setTeam(teamEntity);
        } catch (IOException | URISyntaxException | XPatherException e) {
            throw new PlayerException("A problem ocurred during HTML parsing",e);
        }
        return new AsyncResult<>(playerEntity);
    }


    private String parseTeam(final String team) throws PlayerException {
        String teamName = null;
        try {
            String[] split = team.split("\\(");
            teamName = split[1].substring(0,3);
        }catch (StringIndexOutOfBoundsException e){
            throw new PlayerException("Invalid team name: "+team, e);
        }
        return teamName;

    }

    private String getValueViaXPath(String html, String xPathExpression) {
        try{
            TagNode node = cleaner.clean(html);
            Object[] objects = node.evaluateXPath(xPathExpression);
            String s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            return s;
        }
        catch (Exception e){
            //TODO: this is a poor way to handle any problem we may have during parsing.
            LOG.warn("Could not get value from html with xPathExpression: " + xPathExpression);
            return "-1";
        }
    }

    public String getValueViaLabel(String html, int row) throws XPatherException {

        try {


            TagNode node = cleaner.clean(html);
            Object[] objects = node.evaluateXPath(FIRST_GAME_PLAYED);
            //We need to add here the number of the current match to get the
            String s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            int firstGamePlayed = Integer.parseInt(s);
            //current row to apply xpath is: last game with stats - first game with stats +1 (since we want to obtain the next row)
            int matchNumber = matchControlService.getMatchNumber() - firstGamePlayed + OFFSET_ROW_TABLE + row;
            String pathExpression = BASE_VAL_MEDIA.replace("row", "" + matchNumber);
            objects = node.evaluateXPath(pathExpression);
            s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            return s;
        }
        catch (Exception e){
            //TODO: this is a poor way to handle any problem we may have during parsing.
            LOG.warn("Could not get value via label for row "+row);
            return "-1";
        }
    }


}
