package org.amupoti.supermanager.acb.adapter.out.acbapi;

import org.amupoti.supermanager.acb.application.port.out.MarketDataPort;
import org.amupoti.supermanager.acb.domain.model.MarketData;
import org.amupoti.supermanager.parser.acb.SmContentParser;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.springframework.stereotype.Component;

/**
 * Out-adapter: fetches and parses market (broker) data from the ACB API.
 */
@Component
public class AcbMarketDataAdapter implements MarketDataPort {

    private final SmContentProvider smContentProvider;
    private final SmContentParser smContentParser;

    public AcbMarketDataAdapter(SmContentProvider smContentProvider, SmContentParser smContentParser) {
        this.smContentProvider = smContentProvider;
        this.smContentParser = smContentParser;
    }

    @Override
    public MarketData getMarketData(String token) {
        return smContentParser.providePlayerData(smContentProvider.getMarketPage(token));
    }
}
