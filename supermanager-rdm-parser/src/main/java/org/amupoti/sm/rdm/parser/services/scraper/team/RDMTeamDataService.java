package org.amupoti.sm.rdm.parser.services.scraper.team;

import org.amupoti.sm.rdm.parser.bean.PlayerPositionRdm;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Marcel on 17/08/2015.
 */
public class RDMTeamDataService implements TeamDataService {

    public static final String INVALID_VALUE = "-100";
    private HtmlCleaner cleaner;

    /**
     * XPath expressions for team page
     */
    private static final String VAL_LOCAL = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[1]/td[2]";
    private static final String VAL_LOCAL_RECEIVED = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[1]/td[3]";
    private static final String VAL_VISITOR = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[1]/td[4]";
    private static final String VAL_VISITOR_RECEIVED = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[1]/td[5]";
    private static final String VAL = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[1]/td[6]";
    private static final String VAL_RECEIVED = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[1]/td[7]";

    private static final String POINTS_LOCAL = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[2]/td[2]";
    private static final String POINTS_LOCAL_RECEIVED = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[2]/td[3]";
    private static final String POINTS_VISITOR = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[2]/td[4]";
    private static final String POINTS_VISITOR_RECEIVED = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[2]/td[5]";
    private static final String POINTS = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[2]/td[6]";
    private static final String POINTS_RECEIVED = "/body/div[5]/div/div[1]/div[2]/div/table/tbody/tr[2]/td[7]";

    @Value("${url.team}")
    public String TEAM_PAGE;

    /**
     * String containing the HTML code of the page
     */
    private HashMap<String, String> htmlForTeams;


    @PostConstruct
    public void init() {
        cleaner = new HtmlCleaner();
        htmlForTeams = new HashMap<>();

    }

    public String getTeamMean(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, VAL);
    }

    public String getTeamMeanReceived(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, VAL_RECEIVED);
    }

    public String getTeamMeanLocal(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, VAL_LOCAL);
    }

    public String getTeamMeanVisitor(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, VAL_VISITOR);
    }

    public String getTeamMeanLocalReceived(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, VAL_LOCAL_RECEIVED);
    }

    public String getTeamMeanVisitorReceived(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, VAL_VISITOR_RECEIVED);
    }

    @Override
    public String getTeamMeanPoints(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, POINTS);
    }

    @Override
    public String getTeamMeanPointsReceived(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, POINTS_RECEIVED);
    }

    @Override
    public String getTeamMeanPointsLocal(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, POINTS_LOCAL);
    }

    @Override
    public String getTeamMeanPointsVisitor(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, POINTS_VISITOR);
    }

    @Override
    public String getTeamMeanPointsLocalReceived(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, POINTS_LOCAL_RECEIVED);
    }

    @Override
    public String getTeamMeanPointsVisitorReceived(String teamName, PlayerPositionRdm position) {
        return getValue(teamName, position, POINTS_VISITOR_RECEIVED);
    }


    /**
     * Obtains the value of applying the given XPATH to the provided HTML page
     *
     * @param teamName
     * @param xPathExpression
     * @return
     * @throws XPatherException
     * @throws IOException
     */
    private String getValue(String teamName, PlayerPositionRdm position, String xPathExpression) {
        try {
            String html = getHtmlForTeam(teamName, position);
            TagNode node = cleaner.clean(html);
            Object[] objects = node.evaluateXPath(xPathExpression);
            String value = ((TagNode) objects[0]).getAllChildren().get(0).toString();
            if (value.contains("(")) {
                value = parseVal(value);
            }
            if (value.contains(",")) {
                value = value.replace(",", ".");
            }
            return value;
        } catch (Exception e) {
            //TODO: this is a poor way to handle any problem we may have during parsing, but won't break data
            return INVALID_VALUE;
        }
    }

    private String parseVal(String valRec) {
        String[] split = valRec.split(" \\(");
        return split[0];

    }

    private String getHtmlForTeam(String teamName, PlayerPositionRdm position) throws IOException {
        String key = teamName + "-" + position;
        String html;
        if (htmlForTeams.get(key) == null) {
            html = getTeamPage(teamName, position);
            htmlForTeams.put(key, html);
        } else {
            html = htmlForTeams.get(key);
        }

        return html;

    }

    /**
     * Returns a String containing the team page so it can be parsed to obtain the team values
     *
     * @param teamId
     * @param position
     * @return
     * @throws IOException
     */
    private String getTeamPage(String teamId, PlayerPositionRdm position) throws IOException {
        org.apache.http.client.HttpClient client = HttpClients.createDefault();
        HttpGet httpPost = new HttpGet((TEAM_PAGE + teamId));
        //TODO: wait until page allows results per position
        HttpResponse response = client.execute(httpPost);
        return IOUtils.toString(response.getEntity().getContent());

    }

}
