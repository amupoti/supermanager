package org.amupoti.supermanager.acb.adapter.out.acbapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.adapter.out.acbapi.dto.MarketPlayerResponse;
import org.amupoti.supermanager.acb.application.port.out.MarketDataPort;
import org.amupoti.supermanager.acb.domain.model.MarketCategory;
import org.amupoti.supermanager.acb.domain.model.MarketData;
import org.amupoti.supermanager.acb.domain.model.PlayerPosition;
import org.amupoti.supermanager.acb.exception.ErrorCode;
import org.amupoti.supermanager.acb.exception.SmException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.amupoti.supermanager.acb.domain.model.MarketCategory.*;

/**
 * Out-adapter: fetches and parses market (broker) data from the ACB API.
 */
@Component
@Slf4j
public class AcbMarketDataAdapter implements MarketDataPort {

    private static final String MARKET_PAGE_FIELDS =
            "[{\"field\":\"competition.idCompetition\",\"value\":1,\"operator\":\"=\",\"condition\":\"AND\"}," +
            "{\"field\":\"edition.isActive\",\"value\":true,\"operator\":\"=\",\"condition\":\"AND\"}]";

    @Value("${acb.url.market:https://supermanager.acb.com/api/basic/player?_filters={fields}&_page=1&_perPage=300}")
    private String marketPageUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AcbMarketDataAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable("marketPage")
    public MarketData getMarketData(String token) {
        log.debug("Requesting market page");
        ResponseEntity<String> exchange = restTemplate.exchange(
                marketPageUrl, HttpMethod.GET, new HttpEntity<>(authHeader(token)), String.class, MARKET_PAGE_FIELDS);
        return parseMarketData(exchange.getBody());
    }

    private MarketData parseMarketData(String response) {
        try {
            MarketData marketData = new MarketData();
            List<MarketPlayerResponse> playerList = objectMapper.readValue(
                    response, new TypeReference<List<MarketPlayerResponse>>() {});
            log.debug("Market API returned {} players", playerList.size());
            if (!playerList.isEmpty()) {
                MarketPlayerResponse sample = playerList.get(0);
                log.debug("Market sample player: name={} spanish={} foreign={} position={} idPlayer={}",
                        sample.getShortName(), sample.isSpanish(), sample.isForeign(),
                        sample.getPosition(), sample.getIdPlayer());
                if (!sample.getUnknownFields().isEmpty()) {
                    log.debug("Market first player unknown fields (not yet mapped): {}", sample.getUnknownFields());
                }
            }
            long spanishInMarket = playerList.stream().filter(MarketPlayerResponse::isSpanish).count();
            long foreignInMarket = playerList.stream().filter(MarketPlayerResponse::isForeign).count();
            log.debug("Market nationality summary: {} Spanish, {} foreign out of {} total",
                    spanishInMarket, foreignInMarket, playerList.size());
            playerList.stream()
                    .filter(p -> !p.isSpanish())
                    .limit(10)
                    .forEach(p -> log.debug("Non-national player: name={} license={} nationality={}",
                            p.getShortName(), p.getLicense(), p.getNationality()));
            playerList.forEach(player -> fillMarketData(player, marketData));
            return marketData;
        } catch (Exception e) {
            throw new SmException(ErrorCode.ERROR_PARSING_MARKET, e);
        }
    }

    private void fillMarketData(MarketPlayerResponse player, MarketData marketData) {
        if (player.getShortName() == null) {
            log.warn("ACB API: market player has null shortName — skipping");
            return;
        }
        if (player.getPrice() == null) {
            log.warn("ACB API: market player {} has null price — API field may have changed", player.getShortName());
        }
        String playerName = player.getShortName();
        marketData.addPlayer(playerName);
        marketData.addPlayerData(playerName, PRICE.name(), player.getPrice());
        marketData.addPlayerData(playerName, PRICE_FORMATTED.name(), formatPrice(player.getPrice()));
        marketData.addPlayerData(playerName, PLUS_15_BROKER.name(), player.getUp15());
        marketData.addPlayerData(playerName, KEEP_BROKER.name(), player.getKeep());
        marketData.addPlayerData(playerName, MEAN_VAL.name(), player.getCompetitionAverage());
        marketData.addPlayerData(playerName, TEAM.name(), player.getNameTeam());
        marketData.addPlayerData(playerName, FISIC_STATUS.name(), player.getFisicStatus());
        marketData.addPlayerData(playerName, ID_PLAYER.name(), String.valueOf(player.getIdPlayer()));
        marketData.addPlayerData(playerName, POSITION.name(), toPositionName(player.getPosition()));
        marketData.addPlayerData(playerName, IS_SPANISH.name(), String.valueOf(player.isSpanish()));
        marketData.addPlayerData(playerName, IS_FOREIGN.name(), String.valueOf(player.isForeign()));
        marketData.addPlayerData(playerName, IS_BLOCKED.name(), String.valueOf(player.isBlocked()));
    }

    private String toPositionName(String position) {
        if (position == null) return null;
        try {
            PlayerPosition pos = PlayerPosition.getFromNum(position);
            return pos != null ? pos.getName() : null;
        } catch (Exception e) {
            log.warn("Unknown position value from market API: {}", position);
            return null;
        }
    }

    private String formatPrice(String price) {
        if (price == null) return "0k";
        return (Float.valueOf(price).intValue() / 1000) + "k";
    }

    private MultiValueMap<String, String> authHeader(String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }
}
