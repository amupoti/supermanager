package org.amupoti.sm.main.services.provider;

import org.amupoti.sm.main.repository.entity.MatchEntity;
import org.amupoti.sm.main.services.HTMLProviderService;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by Marcel on 17/08/2015.
 */
@Service

public class MatchDataProvider {

    @Autowired
    private HTMLProviderService htmlProviderService;
    private HtmlCleaner cleaner;


    @PostConstruct
    public void init() {
        cleaner = new HtmlCleaner();

    }


    /**
     * Returns the name of the team which plays in the given match number as local or visitor
     * @param matchNumber
     * @param local
     * @return
     */
    public String getTeamNameByMatchNumber(int matchNumber, boolean local) {
        //TODOO: bind with obtained page and parse data
        return null;

    }

    public String getTeamPage(String teamId, String position) throws IOException {
        return null;
    }

    public Iterable<MatchEntity> getTeamMatches(String teamName) throws IOException {
        String html = htmlProviderService.getTeamURLBody(teamName);
        TagNode node = cleaner.clean(html);
        //Object[] objects = node.evaluateXPath(xPathExpression);
        //String s = ((TagNode) objects[0]).getAllChildren().get(0).toString();

        for (int i=0;i<34;i++){
//   //*[@id="sm_izquierda"]/div[1]/table/tbody/tr[2]/td[2]
        }

        // //*[@id="sm_izquierda"]/div[1]/table/tbody/tr[2]/td[4]

        // //*[@id="sm_izquierda"]/div[1]/table/tbody/tr[3]/td[2]

        //*[@id="sm_izquierda"]/div[1]/table/tbody/tr[3]/td[4]
        return null;
    }
}
