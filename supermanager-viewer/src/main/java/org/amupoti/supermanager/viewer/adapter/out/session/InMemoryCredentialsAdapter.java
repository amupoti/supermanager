package org.amupoti.supermanager.viewer.adapter.out.session;

import org.amupoti.supermanager.viewer.application.port.out.CredentialsStorePort;
import org.amupoti.supermanager.viewer.domain.model.SMUser;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Out-adapter: in-memory session credential store.
 */
@Component
public class InMemoryCredentialsAdapter implements CredentialsStorePort {

    private final Map<String, SMUser> store = new HashMap<>();

    @Override
    public void store(String key, SMUser user) {
        store.put(key, user);
    }

    @Override
    public Optional<SMUser> find(String key) {
        return Optional.ofNullable(store.get(key));
    }
}
