package org.amupoti.supermanager.parser.acb.bean;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by Marcel on 15/12/2015.
 */
public class DataUtils {

    public static String format(Number ranking) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        DecimalFormat df = new DecimalFormat("#0.00", otherSymbols);
        return df.format(ranking);
    }

}
