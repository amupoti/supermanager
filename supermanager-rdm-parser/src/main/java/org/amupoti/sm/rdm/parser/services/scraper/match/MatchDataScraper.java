package org.amupoti.sm.rdm.parser.services.scraper.match;

import lombok.Setter;
import org.amupoti.sm.rdm.parser.provider.HTMLProviderServiceV2;
import org.amupoti.sm.rdm.parser.repository.entity.MatchEntity;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.LinkedHashSet;

import static org.amupoti.sm.rdm.parser.config.SMConstants.MAX_GAMES;

/**
 * Created by Marcel on 17/08/2015.
 */
@Service
public class MatchDataScraper {

    @Autowired
    @Setter
    private HTMLProviderServiceV2 htmlProviderService;
    private HtmlCleaner cleaner;


    @PostConstruct
    public void init() {
        cleaner = new HtmlCleaner();

    }

    public Iterable<MatchEntity> getTeamMatches(String teamName) throws IOException, XPatherException {
        String html = htmlProviderService.getTeamURLBody(teamName);
        TagNode node = cleaner.clean(html);

        LinkedHashSet<MatchEntity> matchEntityList = new LinkedHashSet<>();
        for (int i = 0; i < MAX_GAMES; i++) {
            int current = (i + 2);
            Object[] objects = node.evaluateXPath("//*[@id=\"sm_izquierda\"]/div[1]/table/tbody/tr[" + current + "]/td[2]");
            String local = getTeamName(objects[0]);
            objects = node.evaluateXPath("//*[@id=\"sm_izquierda\"]/div[1]/table/tbody/tr[" + current + "]/td[4]");
            String visitor = getTeamName(objects[0]);
            MatchEntity matchEntity = new MatchEntity();
            matchEntity.setNumber(i + 1);
            matchEntity.setLocal(local);
            matchEntity.setVisitor(visitor);
            matchEntityList.add(matchEntity);
        }

        return matchEntityList;
    }

    private String getTeamName(Object object) {
        TagNode tagNode = (TagNode) object;
        String local = tagNode.getAllChildren().get(0).toString();
        return local;
    }
}
