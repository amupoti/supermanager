package org.amupoti.sm.main.services.provider.player;

import org.amupoti.sm.main.repository.PlayerRepository;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.services.PlayerPosition;
import org.amupoti.sm.main.services.TeamService;
import org.amupoti.sm.main.services.provider.HTMLProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import javax.annotation.PostConstruct;
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

    public static final String VAL_MEDIA_LOCAL = "//*[@id=\"sm_central\"]/div[2]";
    public static final String VAL_MEDIA_VISITANTE = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[6]/td[9]/b";
    public static final String VAL_MANTENER_BROKER = "//*[@id=\"sm_central\"]/div[3]/table/tbody/tr[6]/td[2]";
    public static final String ALL_PLAYERS = "//*[@id=\"sm_central\"]/div/table/tbody/tr/td[1]/a";
    private static final String BROKER = "//*[@id=\"sm_central\"]/div[3]/table/tbody/tr[6]/td[4]";

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


    @PostConstruct
    public void init() {
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

    public List<PlayerEntity> getPlayersData(Set<PlayerId> playerIds) throws XPatherException, IOException, URISyntaxException, InterruptedException, ExecutionException {
        List<Future<PlayerEntity>> futurePlayerDataList = new LinkedList<>();
        List<PlayerEntity> playerDataList = new LinkedList<>();


        //Fire up all URL retrieval and value computation in new threads
        int num =0;
        for (PlayerId playerId:playerIds){
            num++;
            LOG.info("Getting data for player "+playerId+ ". "+num+" out of "+playerIds.size()+" processed.");

            Future<PlayerEntity> playerData = populatePlayerData(playerId);
            futurePlayerDataList.add(playerData);
            Thread.sleep(100);
        }

        for (int i=0;i<playerIds.size();i++){
            if (!futurePlayerDataList.get(i).isDone()){
                LOG.info("Player "+i+" not ready, waiting...");
                Thread.sleep(1000);
                i--;
            }
            else{
                playerDataList.add(futurePlayerDataList.get(i).get());
                LOG.info("Processed "+(i+1)+" players out of "+playerIds.size());
            }
        }
        return playerDataList;
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
    private Future<PlayerEntity> populatePlayerData(PlayerId playerId) throws IOException, XPatherException, URISyntaxException {

        PlayerEntity playerEntity = playerRepository.findByPlayerId(playerId);
        if (playerEntity==null){
            playerEntity = new PlayerEntity();
        }
        String html = htmlProviderService.getPlayerURL(playerId);
        String localMean = getValueViaLabel(html, RDMPlayerDataService.VAL_MEDIA_LOCAL);
        String visitorMean = getValueViaLabel(html, RDMPlayerDataService.VAL_MEDIA_VISITANTE);
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

        return new AsyncResult<>(playerEntity);
    }


    private String parseTeam(final String team) {
        String teamName = null;
        try {
            String[] split = team.split("\\(");
            teamName = split[1].substring(0,3);
        }catch (StringIndexOutOfBoundsException e){
            LOG.error("Invalid team name: "+team);
            throw e;
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

    public String getValueViaLabel(String html, String expression) {
    //TODO: get data properly, must check where "media local" is
        TagNode node = cleaner.clean(html);
        //Object[] objects = node.getElementsByAttValue()evaluateXPath(xPathExpression);

        return "-1";
    }


}
