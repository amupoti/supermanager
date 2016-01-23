package org.amupoti.supermanager.parser.acb;

import org.amupoti.supermanager.parser.acb.beans.ACBPlayer;
import org.amupoti.supermanager.parser.acb.beans.ACBSupermanagerTeam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marcel on 02/01/2016.
 */
public class ACBTeamServiceDefault implements ACBTeamService {

    private static final String URL_FORM = "http://supermanager.acb.com/index/identificar";
    private static final String BASE_URL = "http://supermanager.acb.com";
    Log log = LogFactory.getLog(ACBTeamServiceDefault.class);
    private static final String URL_ENTRY = "http://supermanager.acb.com/";
    private static final String URL_LOGGED_IN = "http://supermanager.acb.com/equipos/listado";

    @Autowired
    private RestTemplate restTemplate;

    private HtmlCleaner htmlCleaner;

    @PostConstruct
    public void init() {
        htmlCleaner = new HtmlCleaner();

    }
    @Override
    public List<ACBSupermanagerTeam> getTeamsByCredentials(String user, String password) throws XPatherException {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Host","supermanager.acb.com");
        httpHeaders.add(HttpHeaders.ACCEPT,"*/*");
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add(HttpHeaders.REFERER,"http://supermanager.acb.com/index/identificar");

        restTemplate.getMessageConverters().add(new FormHttpMessageConverter() );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email",user);
        params.add("clave",password);
        params.add("entrar", "Entrar");

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(params, httpHeaders);

        ResponseEntity<String> exchange = restTemplate.postForEntity(URL_FORM, httpEntity, String.class,params);
        log.info("Post to "+URL_FORM+ " with headers "+httpHeaders);
        log.info(exchange.getStatusCode());
        log.info(exchange.getHeaders());
        httpHeaders.add("Cookie", exchange.getHeaders().get("Set-Cookie").toString().replace("[","").split(";")[0]);
        httpEntity = new HttpEntity<MultiValueMap<String, String>>(params, httpHeaders);
        exchange = restTemplate.exchange(URL_LOGGED_IN, HttpMethod.GET, httpEntity, String.class);
        log.info("Get to "+ URL_LOGGED_IN);
        log.info(exchange.getStatusCode());
        log.info(exchange.getHeaders());
        String pageBody = exchange.getBody();

        List<ACBSupermanagerTeam> teams = getTeams(pageBody);
        for (ACBSupermanagerTeam team:teams){
            exchange = restTemplate.exchange(BASE_URL+team.getUrl(), HttpMethod.GET, httpEntity, String.class);
            team.setPlayers(getPlayers(exchange.getBody()));
        }


        log.debug("Teams found: "+teams);
        return teams;
    }

    private List<ACBPlayer> getPlayers(String html) throws XPatherException {
        TagNode node = htmlCleaner.clean(html);
        String xPathExpression = "//*[@id=\"puesto$row\"]/td[3]/span/a";
        List<ACBPlayer> players = new LinkedList<>();
        for (int i = 1; i <= 11; i++) {
            Object[] objects = node.evaluateXPath(xPathExpression.replace("$row",""+i));
            String name = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            ACBPlayer player = new ACBPlayer();
            player.setName(name);
            player.setPosition(""+i);
            players.add(player);
        }
    return players;
    }

    private List<ACBSupermanagerTeam> getTeams(String html) {

        List<ACBSupermanagerTeam> teamList = new LinkedList<>();

        String xPathExpression = "//*[@id=\"contentmercado\"]/section/table[2]/tbody/tr";
        try{
            TagNode node = htmlCleaner.clean(html);
            Object[] objects = node.evaluateXPath(xPathExpression);
            int rows= objects.length;
            for (int i = 1; i <= rows; i++) {
                Object[] names = node.evaluateXPath(xPathExpression+"["+i+"]/td[2]");
                String name = ((TagNode) names[0]).getChildTagList().get(0).getAllChildren().get(0).toString();
                String url = ((TagNode) names[0]).getChildTagList().get(0).getAttributeByName("href");
                ACBSupermanagerTeam team = new ACBSupermanagerTeam();
                team.setName(name);
                team.setUrl(url);
                log.info("Found team for user. Team: "+team);
                teamList.add(team);
            }

            return teamList;
        }
        catch (Exception e){
            //TODO: this is a poor way to handle any problem we may have during parsing.
            log.warn("Could not get value from html with xPathExpression: " + xPathExpression);
            return null;
        }
    }
}

