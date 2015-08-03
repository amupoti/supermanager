package org.amupoti.sm.main;

import lombok.Getter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Marcel on 03/08/2015.
 */
@Controller
@Getter
public class PlayerController {


    private static final String VAL_MEDIA_LOCAL = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[26]/td[9]/b";
    private static final String VAL_MEDIA_VISITANTE = "//*[@id=\"sm_central\"]/div[2]/table/tbody/tr[27]/td[9]/b";
    private static final String NOMBRE = "//*[@id=\"sm_izquierda\"]/div[2]/table/tbody/tr[2]/td[2]/select/option[1]";

    private final String PLAYER_URL = "http://www.rincondelmanager.com/smgr/stats.php?nombre=Jawai,%20Nathan";
    private final static Log LOG = LogFactory.getLog(PlayerController.class);
    private HtmlCleaner cleaner;

    @RequestMapping(value = "/log")
    public String getPlayer() throws IOException, XPatherException {
        // create an instance of HtmlCleaner
        cleaner = new HtmlCleaner();

// take default cleaner properties
        CleanerProperties props = cleaner.getProperties();
// customize cleaner's behaviour with property setters

// Clean HTML taken from simple string, file, URL, input stream,
// input source or reader. Result is root node of created
// tree-like structure. Single cleaner instance may be safely used
// multiple times.

        String localMean = getValue(PLAYER_URL, VAL_MEDIA_LOCAL);
        String visitorMean = getValue(PLAYER_URL,VAL_MEDIA_VISITANTE);
      //  String name = getValue(PLAYER_URL,NOMBRE);

        LOG.info("Player: "+". L: "+localMean + " v: "+visitorMean);
        return "player";
    }
    
    private String getValue(String url, String xPathExpression) throws XPatherException, IOException {
        TagNode node = cleaner.clean(new URL(url));
        Object[] objects = node.evaluateXPath(xPathExpression);
        return ((TagNode) objects[0]).getAllChildren().get(0).toString();
    }
    
}

