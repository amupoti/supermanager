package org.amupoti.sm.main.services.provider.team;

import org.amupoti.sm.main.services.PlayerPosition;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marcel on 17/08/2015.
 */
@Service
public class TeamDataProvider implements TeamDataService {

    private HtmlCleaner cleaner;

    /**
     * XPath expressions for team page
     */
    private static final String VAL_LOCAL = "//*[@id=\"sm_central\"]/div[1]/table/tbody/tr[8]/td[2]";
    private static final String VAL_LOCAL_RECEIVED = "//*[@id=\"sm_central\"]/div[1]/table/tbody/tr[8]/td[3]";
    private static final String VAL_VISITOR = "//*[@id=\"sm_central\"]/div[1]/table/tbody/tr[8]/td[4]";
    private static final String VAL_VISITOR_RECEIVED = "//*[@id=\"sm_central\"]/div[1]/table/tbody/tr[8]/td[5]";
    private static final String VAL = "//*[@id=\"sm_central\"]/div[1]/table/tbody/tr[8]/td[6]";
    private static final String VAL_RECEIVED = "//*[@id=\"sm_central\"]/div[1]/table/tbody/tr[8]/td[7]";
    public static final String TEAM_PAGE = "http://www.rincondelmanager.com/smgr/team.php?equipo=";
    /**
     * String containing the HTML code of the page
     */
    private HashMap<String,String> htmlForTeams;


    @PostConstruct
    public void init() {
        cleaner = new HtmlCleaner();
        htmlForTeams = new HashMap<>();

    }
    public String getTeamMean(String teamName,PlayerPosition position) {
        return getValue(teamName,position,VAL);
    }

    public String getTeamMeanReceived(String teamName,PlayerPosition position) {
        return getValue(teamName,position,VAL_RECEIVED);
    }

    public String getTeamMeanLocal(String teamName,PlayerPosition position) {
        return getValue(teamName,position,VAL_LOCAL);
    }

    public String getTeamMeanVisitor(String teamName,PlayerPosition position) {
        return getValue(teamName,position,VAL_VISITOR);
    }

    public String getTeamMeanLocalReceived(String teamName,PlayerPosition position) {
        return getValue(teamName,position,VAL_LOCAL_RECEIVED);
    }

    public String getTeamMeanVisitorReceived(String teamName,PlayerPosition position) {
        return getValue(teamName,position,VAL_VISITOR_RECEIVED);
    }

    /**
     * Obtains the value of applying the given XPATH to the provided HTML page
     * @param teamName
     * @param xPathExpression
     * @return
     * @throws XPatherException
     * @throws IOException
     */
    private String getValue(String teamName,PlayerPosition position,String xPathExpression) {
        try{
            String html = getHtmlForTeam(teamName,position);
            TagNode node = cleaner.clean(html);
            Object[] objects = node.evaluateXPath(xPathExpression);
            String s = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            return s;
        }
        catch (Exception e){
            //TODO: this is a poor way to handle any problem we may have during parsing.
            return "-1";
        }
    }

    private String getHtmlForTeam(String teamName, PlayerPosition position) throws IOException {
        String key = teamName+"-"+position;
        String html;
        if (htmlForTeams.get(key)==null){
            html = getTeamPage(teamName,position);
            htmlForTeams.put(key, html);
        }
        else{
            html= htmlForTeams.get(key);
        }

        return html;

    }

    /**
     * Returns a String containing the team page so it can be parsed to obtain the team values
     * @param teamId
     * @param position
     * @return
     * @throws IOException
     */
    private String getTeamPage(String teamId, PlayerPosition position) throws IOException {
        org.apache.http.client.HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost((TEAM_PAGE+teamId));
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("posi", position.getId()));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        HttpResponse response = client.execute(httpPost);
        return IOUtils.toString(response.getEntity().getContent());

    }

    /**
     * Returns an array with the team identifiers
     * @return
     */
    public String[] populateTeamIds() {

        String[] teamIds={"AND","BLB","CAI","CAN","EST","FCB","FUE","GBC","GCA","JOV","LAB","MAN","MUR","OBR","RMA","SEV","UNI","VBC"};
        return teamIds;
    }
}
