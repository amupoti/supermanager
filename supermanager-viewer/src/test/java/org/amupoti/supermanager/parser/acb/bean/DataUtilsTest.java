package org.amupoti.supermanager.parser.acb.bean;

import org.amupoti.supermanager.parser.acb.utils.DataUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by amupoti on 01/09/2017.
 */
public class DataUtilsTest {
    @Test
    public void getScoreFromStringValue() throws Exception {

        Assert.assertEquals((Float) 20.40f, DataUtils.getScoreFromStringValue("20,40 (+)"));
        Assert.assertEquals((Float) 11.20f, DataUtils.getScoreFromStringValue("11,20"));
    }

}