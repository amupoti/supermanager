package org.amupoti.supermanager.acb.application.port.out;

import org.amupoti.supermanager.acb.domain.model.MarketData;

/**
 * Driven port: retrieve market (broker) data for all players.
 */
public interface MarketDataPort {
    MarketData getMarketData(String token);
}
