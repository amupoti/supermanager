package org.amupoti.sm.rdm.parser.services;

import org.amupoti.sm.rdm.parser.bean.SMPlayerDataBean;
import org.amupoti.sm.rdm.parser.repository.entity.PlayerEntity;
import org.amupoti.sm.rdm.parser.repository.entity.PlayerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by amupoti on 20/10/2017.
 */
@Service
public class PlayerCompleteDataService {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ComputePlayerValuesService computePlayerValuesService;

    @Cacheable("playerData")
    public Optional<SMPlayerDataBean> getPlayerCompleteData(String playerName) {
        PlayerEntity player = playerService.getPlayer(new PlayerId(playerName));
        if (player == null) return Optional.empty();
        return Optional.of(computePlayerValuesService.addPlayerData(player));
    }
}
