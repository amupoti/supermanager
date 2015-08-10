package org.amupoti.sm.main.services;

import org.amupoti.sm.main.repository.PlayerRepository;
import org.amupoti.sm.main.repository.TeamRepository;
import org.amupoti.sm.main.repository.entity.PlayerEntity;
import org.amupoti.sm.main.repository.entity.PlayerId;
import org.amupoti.sm.main.repository.entity.TeamEntity;
import org.amupoti.sm.main.repository.entity.ValueEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Recovers data from the player page
 * Created by Marcel on 04/08/2015.
 */
@Service
public class DataProviderService {

    //TODO: this XPATH expressions are not valid since the page changes depending on the number of matches played by player

    private static final String VAL_MEDIA_LOCAL = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[38]/td[9]/b";
    private static final String VAL_MEDIA_VISITANTE = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[39]/td[9]/b";
    private static final String VAL_MANTENER_BROKER = "//*[@id=\"sm_central\"]/div[3]/table/tbody/tr[6]/td[2]";
    private static final String ALL_PLAYERS = "//*[@id=\"sm_central\"]/div/table/tbody/tr/td[1]/a";
    private static final String TEAM_PAGE = "http://www.rincondelmanager.com/smgr/team.php?equipo=";
    private static final String BASE = "B";
    private static final String ALERO = "A";
    private static final String PIVOT = "P";
    private static final String LOCAL_VAL = "//*[@id=\"sm_central\"]/div[1]/table/tbody/tr[8]/td[2]";


    private HtmlCleaner cleaner;
    private final static Log LOG = LogFactory.getLog(DataProviderService.class);


    @Autowired
    private HTMLProviderService htmlProviderService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @PostConstruct
    public void init() {
        cleaner = new HtmlCleaner();

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
        PlayerEntity playerData = new PlayerEntity();
        String html = htmlProviderService.getPlayerURL(playerId);
        String localMean = getValue(html, VAL_MEDIA_LOCAL);
        String visitorMean = getValue(html, VAL_MEDIA_VISITANTE);
        String keepBroker = getValue(html, VAL_MANTENER_BROKER);

        playerData.setId(playerId);
        playerData.setLocalMean(Float.parseFloat(localMean));
        playerData.setVisitorMean(Float.parseFloat(visitorMean));
        playerData.setKeepBroker(Float.parseFloat(keepBroker));
        return new AsyncResult<>(playerData);
    }


    /**
     * Obtains the value of applying the given XPATH to the provided HTML page
     * @param html
     * @param xPathExpression
     * @return
     * @throws XPatherException
     * @throws IOException
     */
    private String getValue(String html, String xPathExpression) throws XPatherException, IOException {
        try{
            TagNode node = cleaner.clean(html);
            Object[] objects = node.evaluateXPath(xPathExpression);
            String s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            return s;
        }
        catch (Exception e){
            return "-1";
        }
    }

    //*[@id="sm_central"]/div/table/tbody/tr[3]/td[1]/a
    ////*[@id="sm_central"]/div/table/tbody/tr[286]/td[1]/a
    private List<PlayerId> populatePlayerIds() throws IOException, XPatherException {
        String html = htmlProviderService.getAllPlayersURL();
        TagNode node = cleaner.clean(html);
        Object[] objects = node.evaluateXPath(ALL_PLAYERS);
        List<PlayerId> playerIds = new LinkedList<>();
        for (int i = 0; i < objects.length; i++) {
            TagNode tagNode = (TagNode) objects[i];
            String name = tagNode.getAllChildren().get(0).toString();
            if (name.contains(",")) {
                playerIds.add(new PlayerId(name));
            }
        }
        //TODO: change, just retrieving fist 5 players for tests
        return playerIds.subList(0,5);
    }

    public void storeDummyData(){
        PlayerEntity playerEntity = new PlayerEntity();
        playerEntity.setId(new PlayerId("Marcel"));
        playerEntity.setLocalMean(22.2f);
        playerEntity.setVisitorMean(22.2f);
        playerEntity.setKeepBroker(22.2f);
        playerRepository.save(playerEntity);
    }

    public PlayerEntity getDummyData(){
        return playerRepository.findOne(new PlayerId("Marcel"));
    }

    public void populate() throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {
        List<PlayerId> playerIdList = populatePlayerIds();
        populatePlayerData(playerIdList);
        //TODO: service should return player list
        //TODO: endpoint to load all the data into the cache: start with players, then we can retrieve by any kind of players
        //Add to model
        populateTeams();

    }

    /**
     * Populates data from player pages
     * @param playerIdList
     * @throws IOException
     * @throws XPatherException
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private void populatePlayerData(List<PlayerId> playerIdList) throws IOException, XPatherException, URISyntaxException, InterruptedException, ExecutionException {
        List<Future<PlayerEntity>> futurePlayerDataList = new LinkedList<>();

        //Fire up all URL retrieval and value computation in new threads
        for (int i=0;i<playerIdList.size();i++){
            LOG.info("Getting data for player "+playerIdList.get(i));
            Future<PlayerEntity> playerData = populatePlayerData(playerIdList.get(i));
            futurePlayerDataList.add(playerData);
            Thread.sleep(100);
        }

        for (int i=0;i<playerIdList.size();i++){
            if (!futurePlayerDataList.get(i).isDone()){
                LOG.info("Player "+i+" not ready, waiting...");
                Thread.sleep(1000);
                i--;
            }
        }

        for (int i=0;i<playerIdList.size();i++){
            PlayerEntity playerData = futurePlayerDataList.get(i).get();
            playerRepository.save(playerData);
        }
    }

    public void populateTeams() throws IOException, XPatherException {
        //Load from static data
        String[] teamIds = populateTeamIds();
        //Load from web, load players + calendar
        populateTeamData(teamIds);
    }

    private String[] populateTeamIds() {

//        String[] teamIds={"AND","BLB","CAI","CAN","CLA","EST","FCB","FUE","GBC","GCA","JOV","MAN","MUR","OBR","RMA","SEV","UNI","VBC"};
        return new String[]{"AND", "BLB"};
    }


    private void populateTeamData(String ...teamIds) throws IOException, XPatherException {
        //Load all team data from every team page
        for (int i = 0; i < teamIds.length; i++) {
            LOG.info("Populating team " + teamIds[i]);
            populateTeamValues(teamIds[i], BASE);

            populateTeamValues(teamIds[i], ALERO);
            populateTeamValues(teamIds[i], PIVOT);
        }
    }



    private void populateTeamValues(String teamId, String position) throws IOException, XPatherException {

        TeamEntity teamEntity = new TeamEntity();
        teamEntity.setId(teamId);
        ValueEntity valueB = new ValueEntity();

        String html = getPageAsPost(teamId,position);
        String valueLocal = getValue(html, LOCAL_VAL);
        valueB.setVal((valueLocal));
        LOG.info("Value for "+position+" is :"+valueLocal);
        if (position.equals(BASE)){
            teamEntity.setValB(valueB);
        }
        else if(position.equals(ALERO)){
            teamEntity.setValA(valueB);

        }
        else{
            teamEntity.setValP(valueB);

        }



        teamRepository.save(teamEntity);


    }
    private String getPageAsPost(String teamId, String position) throws IOException {
        org.apache.http.client.HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost((TEAM_PAGE+teamId));
        List <NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("posi", position));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        HttpResponse response = client.execute(httpPost);
        return IOUtils.toString(response.getEntity().getContent());

    }

}

