package org.amupoti.supermanager.parser.acb.beans.market;

import lombok.Getter;

/**
 * Created by amupoti on 03/10/2017.
 */
@Getter
public enum MarketCategory {

    NAME(2), PRICE(5), BUY_PCT(6), LAST_VAL(7), MEAN_VAL(4), LAST_THREE_VAL(8), KEEP_BROKER(10);

    private int column;

    MarketCategory(int column) {

        this.column = column;
    }
}
