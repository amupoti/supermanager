package org.amupoti.supermanager.parser.rdm;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by amupoti on 28/08/2017.
 */
@Service
public class RdmContentParser {

    private HtmlCleaner htmlCleaner;
    private static String xpathMatches = "body/div[5]/div/div[2]/div/div[1]/div/table/tbody/tr[%s]/td[%s]/a";

    @PostConstruct
    public void init() {
        htmlCleaner = new HtmlCleaner();
    }


    List<Match> getTeamMatches(String html, RdmTeam rdmTeam) {

        TagNode node = htmlCleaner.clean(html);
        return IntStream
                .range(1, 35)
                .mapToObj(row -> getMatch(rdmTeam, node, row))
                .collect(Collectors.toList());
    }

    private Match getMatch(RdmTeam rdmTeam, TagNode node, int row) {
        try {
            String homeTeam = getTeamFromXpath(node, String.format(xpathMatches, row, 2));
            String awayTeam = getTeamFromXpath(node, String.format(xpathMatches, row, 4));
            String againstTeam = homeTeam.equals(rdmTeam.name()) ? awayTeam : homeTeam;
            return Match.builder()
                    .againstTeam(RdmTeam.valueOf(againstTeam))
                    .local(homeTeam.equals(rdmTeam.name()))
                    .build();
        } catch (XPatherException e) {
            throw new RuntimeException("Could not read matches for team " + rdmTeam.name());
        }
    }

    private String getTeamFromXpath(TagNode node, String homeTeamXpath) throws XPatherException {
        Object[] objects = node.evaluateXPath(homeTeamXpath);
        return ((TagNode) objects[0]).getAllChildren().get(0).toString();
    }
}