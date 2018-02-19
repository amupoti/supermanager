package org.amupoti.supermanager.parser.acb;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.beans.PlayerPosition;
import org.amupoti.supermanager.parser.acb.beans.SmPlayer;
import org.amupoti.supermanager.parser.acb.beans.SmPlayerStatus;
import org.amupoti.supermanager.parser.acb.beans.SmTeam;
import org.amupoti.supermanager.parser.acb.beans.market.MarketCategory;
import org.amupoti.supermanager.parser.acb.beans.market.PlayerMarketData;
import org.amupoti.supermanager.parser.acb.exception.ErrorCode;
import org.amupoti.supermanager.parser.acb.exception.SmException;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.amupoti.supermanager.parser.acb.beans.market.MarketCategory.*;

/**
 * Created by amupoti on 28/08/2017.
 */
@Slf4j
public class SmContentParser {

    private HtmlCleaner htmlCleaner;
    public static final String MARKET_REGEX = "//*[@id=\"posicion%d\"]/tbody";

    @PostConstruct
    public void init() {
        htmlCleaner = new HtmlCleaner();
    }

    public void populateTeam(String html, SmTeam team, PlayerMarketData playerMarketData) throws XPatherException {
        TagNode node = htmlCleaner.clean(html);
        addPlayers(team, node, playerMarketData);
        addTotalScore(team, node);

    }

    private void addPlayers(SmTeam team, TagNode node, PlayerMarketData playerMarketData) throws XPatherException {
        String xPathExpression = "//*[@id=\"puesto$row\"]/td[3]/span/a";
        String xPathExpressionScore = "//*[@id=\"puesto$row\"]/td[9]";
        String xPathExpressionIcons = "//*[@id=\"puesto$row\"]/td[1]";

        List<SmPlayer> players = new LinkedList<>();
        for (int i = 1; i <= 11; i++) {
            Object[] objects = getObjectsFromExpression(node, xPathExpression.replace("$row", "" + i));
            if (objects == null || objects.length == 0 || ((TagNode) objects[0]).getAllChildren().size() == 0) continue;

            ParsePlayerDataFromSmTeam playerParsedData = new ParsePlayerDataFromSmTeam(node, xPathExpressionScore, xPathExpressionIcons, i, objects).invoke();
            SmPlayerStatus statuses = playerParsedData.getStatus();
            String name = playerParsedData.getName();
            String score = playerParsedData.getScore();

            SmPlayer player = SmPlayer.builder()
                    .name(name)
                    .position(PlayerPosition.getFromRowId(i).name())
                    .score(score)
                    .status(statuses)
                    .marketData(playerMarketData.getPlayerMap(name))
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

        if (html == null) throw new SmException(ErrorCode.ERROR_PARSING_TEAMS);

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
        } catch (Exception e) {

            throw new SmException(ErrorCode.ERROR_PARSING_TEAMS, e);
        }
        return teamList;
    }


    public void checkGameStatus(String html) {
        String errorMessage = null;
        try {
            if (html != null && html.contains("mostrarMensajeModal")) {
                errorMessage = extractErrorMessage(html);
            }
        } catch (Exception ex) {
            throw new SmException(ErrorCode.ERROR_PARSING_TEAMS);
        }

        if (errorMessage != null) throw new SmException(errorMessage);
    }

    private String extractErrorMessage(String html) {
        return html.split("mostrarMensajeModal\\('")[1].split("'")[0];
    }

    @Cacheable("market")
    public PlayerMarketData providePlayerData(String html) {
        log.info("Requesting market data");
        PlayerMarketData playerMarketData = new PlayerMarketData();

        try {
            TagNode node = htmlCleaner.clean(html);
            for (int pos = 1; pos <= 5; pos += 2) {
                String xpath = String.format(MARKET_REGEX, pos);
                Object[] objects = node.evaluateXPath(xpath);
                int numPlayers = ((TagNode) objects[0]).getAllElements(false).length;
                log.debug("There are {} players for position {}", numPlayers, pos);
                for (int player = 0; player < numPlayers; player++) {
                    try {
                        int finalPlayer = player;
                        String name = getDataFromElementForCategory(objects, player, NAME);
                        playerMarketData.addPlayer(name);

                        List<MarketCategory> categoriesElem = Arrays.asList(PRICE, BUY_PCT, LAST_VAL);
                        categoriesElem.stream().forEach(c -> playerMarketData.addPlayerData(name, c.name(), getDataFromElementForCategory(objects, finalPlayer, c)));

                        List<MarketCategory> categoriesChildren = Arrays.asList(MEAN_VAL, LAST_THREE_VAL, KEEP_BROKER);
                        categoriesChildren.stream().forEach(c -> playerMarketData.addPlayerData(name, c.name(), getDataFromChildrenForCategory(objects, finalPlayer, c)));
                    } catch (Exception e) {
                        log.info("Could not process player with pos {} in row {}", pos, player);


                    }
                }
            }
        } catch (XPatherException e) {
            throw new SmException(ErrorCode.ERROR_PARSING_MARKET, e);
        }

        return playerMarketData;
    }

    private String getDataFromElementForCategory(Object[] objects, int p, MarketCategory category) {
        log.debug("Getting data from element for category {} and player {}", category, p);
        String value = ((TagNode) objects[0]).getAllElements(false)[p].getAllElements(false)[category.getColumn()].getAllElementsList(false).get(0).getAllChildren().get(0).toString();
        log.debug("Value is {}", value);
        return value;
    }

    private String getDataFromChildrenForCategory(Object[] objects, int p, MarketCategory category) {
        log.debug("Getting data from children for category {}  and player {}", category, p);
        String value = ((TagNode) objects[0]).getAllElements(false)[p].getAllElements(false)[category.getColumn()].getAllChildren().get(0).toString();
        log.debug("Value is {}", value);
        return value;
    }

    private class ParsePlayerDataFromSmTeam {
        private TagNode node;
        private String xPathExpressionScore;
        private String xPathExpressionIcons;
        private int i;
        private Object[] objects;
        private String name;
        private String score;
        private List<String> statuses;
        private SmPlayerStatus sps;

        public ParsePlayerDataFromSmTeam(TagNode node, String xPathExpressionScore, String xPathExpressionIcons, int i, Object... objects) {
            this.node = node;
            this.xPathExpressionScore = xPathExpressionScore;
            this.xPathExpressionIcons = xPathExpressionIcons;
            this.i = i;
            this.objects = objects;
        }

        public String getName() {
            return name;
        }

        public String getScore() {
            return score;
        }

        public SmPlayerStatus getStatus() {
            return sps;
        }

        public ParsePlayerDataFromSmTeam invoke() throws XPatherException {
            name = ((TagNode) objects[0]).getAllChildren().get(0).toString();

            objects = node.evaluateXPath(xPathExpressionScore.replace("$row", "" + i));
            score = ((TagNode) objects[0]).getAllChildren().get(0).toString();

            objects = node.evaluateXPath(xPathExpressionIcons.replace("$row", "" + i));
            statuses = getStatusesAsStrings(objects[0]);
            sps = parseStatuses(statuses);
            return this;
        }

        private List<String> getStatusesAsStrings(Object object) {
            return ((TagNode) object).getAllChildren().stream()
                    .filter(TagNode.class::isInstance)
                    .map(s -> {
                        TagNode t = (TagNode) s;
                        return t.getAttributeByName("alt");
                    }).collect(Collectors.toList());
        }
    }
}
