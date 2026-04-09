package org.amupoti.supermanager.parser.rdm;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by amupoti on 28/08/2017.
 */
@Service
public class RdmContentParser {

    private HtmlCleaner htmlCleaner;
    private static String xpathMatches = "body/div[5]/div/div[2]/div/div[2]/table/tbody/tr[%s]/td[%s]/a/span";

    private static String xPathCurrentGame = "//div[@class='proxima-table-header']/h4";

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
            // /html/body/div[5]/div/div[2]/div/div[2]/table/tbody/tr[1]/td[2]/a/span
            // /html/body/div[5]/div/div[2]/div/div[2]/table/tbody/tr[1]/td[4]/a/span

            String homeTeam = getTeamFromXpath(node, String.format(xpathMatches, row, 2));
            String awayTeam = getTeamFromXpath(node, String.format(xpathMatches, row, 4));
            String againstTeam = homeTeam.equals(rdmTeam.name()) ? awayTeam : homeTeam;
            return Match.builder()
                    .againstTeam(RdmTeam.valueOf(againstTeam))
                    .local(homeTeam.equals(rdmTeam.name()))
                    .build();
        } catch (XPatherException e) {
            throw new RdmException("Could not read matches for team " + rdmTeam.name());
        }
    }

    private String getTeamFromXpath(TagNode node, String homeTeamXpath) throws XPatherException {
        Object[] objects = node.evaluateXPath(homeTeamXpath);
        if (objects == null || objects.length == 0) {
            throw new RdmException("XPath returned no results: " + homeTeamXpath);
        }
        List<?> children = ((TagNode) objects[0]).getAllChildren();
        if (children.isEmpty()) {
            throw new RdmException("No children found for XPath node: " + homeTeamXpath);
        }
        return children.get(0).toString();
    }

    public String getMatchNumber(String page) {
        try {
            TagNode node = htmlCleaner.clean(page);
            Object[] objects = node.evaluateXPath(xPathCurrentGame);
            if (objects == null || objects.length == 0) {
                throw new RdmException("Could not find current match number: XPath returned no results");
            }
            TagNode h4Node = (TagNode) objects[0];
            String text = h4Node.getText().toString();
            return text.split("Jornada ")[1];
        } catch (XPatherException e) {
            throw new RdmException("Could not determine current match number: " + e.getMessage());
        }
    }
}