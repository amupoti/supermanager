package org.amupoti.supermanager.parser.acb.utils;

import org.apache.commons.lang.math.NumberUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by Marcel on 15/12/2015.
 */
public class DataUtils {

    static DecimalFormat df;

    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        df = new DecimalFormat("#0.00", otherSymbols);
    }

    public static String format(Number number) {

        return df.format(round(number));
    }

    public static String format(String value) {

        if (NumberUtils.isNumber(value)) {
            return df.format(round(NumberUtils.toFloat(value)));
        } else return value;
    }

    public static Float toFloat(Number number) {
        return round(number);
    }

    public static Float toFloat(String number) {
        return round(Float.parseFloat(number));
    }

    private static float round(Number number) {
        return Math.round(number.floatValue() * 100.0) / 100.0f;
    }

    public static Float getScoreFromStringValue(String scoreWithBonus) {
        String score = scoreWithBonus.replace("(+)", "").replace(",", ".");
        return toFloat(score);
    }
}
