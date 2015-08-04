package org.amupoti.sm.main;

import org.amupoti.sm.main.services.PlayerDataService;
import org.amupoti.sm.main.services.bean.PlayerData;
import org.amupoti.sm.main.services.bean.PlayerId;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * Created by Marcel on 03/08/2015.
 */
@Controller
public class PlayerController {

    private final static Log LOG = LogFactory.getLog(PlayerController.class);

    @Autowired
    private PlayerDataService playerDataService;

    @RequestMapping(value = "/player/{id}")
    public String getPlayer(@PathVariable String id,Model model) throws IOException, XPatherException {
        PlayerId playerId = new PlayerId(id);
        LOG.info("Retrieving info for player:"+playerId);
        PlayerData playerData = playerDataService.getPlayerData(playerId);
        model.addAttribute("playerId",playerId);
        model.addAttribute("player",playerData);
        LOG.info("Retrieved info for player:" + playerId + "." +playerData);
        return "player";
    }
    

}

