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
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marcel on 03/08/2015.
 */
@Controller
public class PlayerController {

    private final static Log LOG = LogFactory.getLog(PlayerController.class);

    @Autowired
    private PlayerDataService playerDataService;

    @RequestMapping(value = "/players/{id}")
    public String getPlayer(@PathVariable String id,Model model) throws IOException, XPatherException, URISyntaxException {
        PlayerId playerId = new PlayerId(id);
        LOG.info("Retrieving info for player:"+playerId);
        PlayerData playerData = playerDataService.getPlayerData(playerId);
        model.addAttribute("playerId",playerId);
        model.addAttribute("player",playerData);
        LOG.info("Retrieved info for player:" + playerId + "." +playerData);
        return "player";
    }

    @RequestMapping(value = "/players/all")
    public String getPlayers(Model model) throws IOException, XPatherException, URISyntaxException {
        LOG.info("Retrieving info for all players:");
        List<PlayerId> playerIdList = playerDataService.getAllPlayers();
        List<PlayerData> playerDataList = new LinkedList<>();
        for (int i=0;i<3;i++){
            LOG.info("Getting data for player "+playerIdList.get(i));
            PlayerData playerData = playerDataService.getPlayerData(playerIdList.get(i));
            playerDataList.add(playerData);
        }
        model.addAttribute("players",playerDataList);
        return "players";
    }


}

