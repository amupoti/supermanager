package org.amupoti.supermanager.parser.acb.market;

import org.amupoti.supermanager.acb.domain.model.MarketData;

import java.util.Map;

public interface SmMarketService {
    Map<String, MarketData> getPlayerMarketData();
}
