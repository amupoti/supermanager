package org.amupoti.supermanager.parser.acb.market;

import org.amupoti.supermanager.parser.acb.beans.market.PlayerMarketData;

import java.util.Map;

/**
 * Created by amupoti on 29/09/2017.
 */
public interface SmMarketService {

    Map<String, PlayerMarketData> getPlayerMarketData();
}
