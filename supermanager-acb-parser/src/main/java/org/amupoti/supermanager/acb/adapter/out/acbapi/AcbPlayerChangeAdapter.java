package org.amupoti.supermanager.acb.adapter.out.acbapi;

import org.amupoti.supermanager.acb.application.port.out.PlayerChangePort;
import org.amupoti.supermanager.parser.acb.SmContentProvider;
import org.springframework.stereotype.Component;

/**
 * Out-adapter: buy, sell, cancel, and query pending player changes via the ACB API.
 */
@Component
public class AcbPlayerChangeAdapter implements PlayerChangePort {

    private final SmContentProvider smContentProvider;

    public AcbPlayerChangeAdapter(SmContentProvider smContentProvider) {
        this.smContentProvider = smContentProvider;
    }

    @Override
    public void buyPlayer(String userTeamId, long playerId, String token) {
        smContentProvider.buyPlayer(userTeamId, playerId, token);
    }

    @Override
    public void sellPlayer(String userTeamId, long playerId, String token) {
        smContentProvider.removePlayer(userTeamId, playerId, token);
    }

    @Override
    public void cancelChange(long changeId, String token) {
        smContentProvider.cancelPlayerChange(changeId, token);
    }

    @Override
    public void cancelAllChanges(String userTeamId, String token) {
        smContentProvider.cancelAllChanges(userTeamId, token);
    }

    @Override
    public String getPendingChanges(String userTeamId, String token) {
        return smContentProvider.getPendingChanges(userTeamId, token);
    }
}
