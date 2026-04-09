package org.amupoti.supermanager.parser.acb.bean;

import org.amupoti.supermanager.parser.acb.utils.DataUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by amupoti on 01/09/2017.
 */
public class DataUtilsTest {
    @Test
    public void getScoreFromStringValue() throws Exception {
        assertEquals((Float) 20.40f, DataUtils.getScoreFromStringValue("20,40 (+)"));
        assertEquals((Float) 11.20f, DataUtils.getScoreFromStringValue("11,20"));
    }
}
