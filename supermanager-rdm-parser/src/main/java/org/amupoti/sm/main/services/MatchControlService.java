package org.amupoti.sm.main.services;

import org.amupoti.sm.main.repository.ControlRepository;
import org.amupoti.sm.main.repository.entity.ControlEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Marcel on 03/11/2015.
 */
@Service
public class MatchControlService {

    private static final String CONTROL = "CONTROL_MATCH";
    @Autowired
    private ControlRepository controlRepository;

    private final static Log LOG = LogFactory.getLog(MatchControlService.class);

    /**
     * Updates the current match into the DB
     * @param match
     */
    public void setCurrentMatch(Integer match) {
        LOG.info("Setting current match number to "+match);
        ControlEntity controlEntity = controlRepository.findByName(CONTROL);
        if (controlEntity == null) {
            controlEntity = new ControlEntity();
            controlEntity.setName(CONTROL);
        }
        controlEntity.setCurrentMatch(match);
        controlRepository.save(controlEntity);
    }

    public int getMatchNumber() {
        ControlEntity controlEntity = controlRepository.findByName(CONTROL);
        if (controlEntity == null || controlEntity.getCurrentMatch()==null) {
            return 1;
        }
        else
            return controlEntity.getCurrentMatch();
    }
}
