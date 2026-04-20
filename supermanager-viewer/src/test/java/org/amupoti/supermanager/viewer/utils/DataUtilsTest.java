package org.amupoti.supermanager.viewer.utils;

import org.amupoti.supermanager.acb.utils.DataUtils;
import org.junit.Assert;
import org.junit.Test;

public class DataUtilsTest {
    @Test
    public void getScoreFromStringValue() throws Exception {
        Assert.assertEquals((Float) 20.40f, DataUtils.getScoreFromStringValue("20,40 (+)"));
        Assert.assertEquals((Float) 11.20f, DataUtils.getScoreFromStringValue("11,20"));
    }
}