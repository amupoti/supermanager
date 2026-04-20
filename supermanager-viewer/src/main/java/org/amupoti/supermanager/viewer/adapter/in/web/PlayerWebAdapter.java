package org.amupoti.supermanager.viewer.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

@Controller
public class PlayerWebAdapter {

    @RequestMapping(value = "/")
    public String root(Model model) {
        return "index";
    }

    @RequestMapping(value = "/players/news.html")
    public String getPlayerNews(Model model) throws URISyntaxException, ExecutionException, InterruptedException, IOException {
        return "news";
    }
}
