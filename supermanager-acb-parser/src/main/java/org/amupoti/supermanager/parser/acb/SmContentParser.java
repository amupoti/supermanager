package org.amupoti.supermanager.parser.acb;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.beans.PlayerPosition;
import org.amupoti.supermanager.parser.acb.beans.SmPlayer;
import org.amupoti.supermanager.parser.acb.beans.SmPlayerStatus;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by amupoti on 28/08/2017.
 */
@Slf4j
public class SmContentParser {

    private HtmlCleaner htmlCleaner;

    @PostConstruct
    public void init() {
        htmlCleaner = new HtmlCleaner();
    }

    public void populateTeam(String html, SmTeam team) throws XPatherException {
        TagNode node = htmlCleaner.clean(html);
        addPlayers(team, node);
        addTotalScore(team, node);

    }

    private void addPlayers(SmTeam team, TagNode node) throws XPatherException {
        String xPathExpression = "//*[@id=\"puesto$row\"]/td[3]/span/a";
        String xPathExpressionScore = "//*[@id=\"puesto$row\"]/td[9]";
        String xPathExpressionIcons = "//*[@id=\"puesto$row\"]/td[1]";

        List<SmPlayer> players = new LinkedList<>();
        for (int i = 1; i <= 11; i++) {
            Object[] objects = getObjectsFromExpression(node, xPathExpression.replace("$row", "" + i));
            String name = ((TagNode) objects[0]).getAllChildren().get(0).toString();

            objects = node.evaluateXPath(xPathExpressionScore.replace("$row", "" + i));
            String score = ((TagNode) objects[0]).getAllChildren().get(0).toString();

            objects = node.evaluateXPath(xPathExpressionIcons.replace("$row", "" + i));
            List<String> statuses = getStatusesAsStrings(objects[0]);
            SmPlayerStatus sps = parseStatuses(statuses);

            //Build object
            SmPlayer player = SmPlayer.builder()
                    .name(name)
                    .position(PlayerPosition.getFromRowId(i).name())
                    .score(score)
                    .status(sps)
                    .build();

            players.add(player);
        }
        team.setPlayerList(players);
    }

    private SmPlayerStatus parseStatuses(List<String> statuses) {
        boolean active = true;
        if (statuses.contains("Icono de inactivo")) active = false;
        boolean spanish = false;
        if (statuses.contains("Icono de español")) spanish = true;
        boolean foreign = false;
        if (statuses.contains("Icono de extracomunitario")) foreign = true;
        boolean info = false;
        if (statuses.contains("Icono de más información")) info = true;
        boolean injured = false;
        if (statuses.contains("Icono de lesionado")) injured = true;

        return SmPlayerStatus.builder()
                .active(active)
                .spanish(spanish)
                .foreign(foreign)
                .injured(injured)
                .info(info)
                .build();

    }

    private List<String> getStatusesAsStrings(Object object) {
        return ((TagNode) object).getAllChildren().stream()
                .filter(TagNode.class::isInstance)
                .map(s -> {
                    TagNode t = (TagNode) s;
                    return t.getAttributeByName("alt");
                }).collect(Collectors.toList());
    }

    private Object[] getObjectsFromExpression(TagNode node, String $row) throws XPatherException {
        return node.evaluateXPath($row);
    }

    private void addTotalScore(SmTeam team, TagNode node) throws XPatherException {
        String xpathScore = "//*[@id=\"valoracion_total\"]";
        Object[] objects = node.evaluateXPath(xpathScore);
        String score = ((TagNode) objects[0]).getAllChildren().get(0).toString();
        try {
            //TODO: use DataUtils, which needs to be moved into a util package
            team.setScore(Float.parseFloat(score.replace(",", ".")));

        } catch (NumberFormatException e) {
            team.setScore(-1.0f);
        }
    }

    public List<SmTeam> getTeams(String html) {

        List<SmTeam> teamList = new LinkedList<>();

        String xPathExpression = "//*[@id=\"contentmercado\"]/section/table[2]/tbody/tr";
        try {
            TagNode node = htmlCleaner.clean(html);
            Object[] objects = node.evaluateXPath(xPathExpression);
            int rows = objects.length;
            for (int i = 1; i <= rows; i++) {
                Object[] names = node.evaluateXPath(xPathExpression + "[" + i + "]/td[2]");
                String name = ((TagNode) names[0]).getChildTagList().get(0).getAllChildren().get(0).toString();
                String url = ((TagNode) names[0]).getChildTagList().get(0).getAttributeByName("href");
                SmTeam team = new SmTeam();
                team.setName(name);
                team.setUrl(url);
                log.info("Found team for user. Team: " + team);
                teamList.add(team);
            }

            return teamList;
        } catch (Exception e) {
            //TODO: this is a poor way to handle any problem we may have during parsing.
            log.warn("Could not get value from html with xPathExpression: " + xPathExpression);
            return null;
        }
    }

}
