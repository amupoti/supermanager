package org.amupoti.sm.main.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Marcel on 03/08/2015.
 */
@Controller
public class PlayerController {

    private final static Log LOG = LogFactory.getLog(PlayerController.class);

    @RequestMapping(value = "/")
    public String root(Model model) {

        return "index";
    }

    @RequestMapping(value = "/players/news.html")
    public String getPLayerNews(Model model) throws URISyntaxException, ExecutionException, XPatherException, InterruptedException, IOException {

        return "news";
    }


}

