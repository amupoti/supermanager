package org.amupoti.supermanager.viewer.application.port.out;

import org.amupoti.supermanager.viewer.domain.model.SMUser;

import java.util.Optional;

/**
 * Driven port: store and retrieve user credentials by session key.
 */
public interface CredentialsStorePort {
    void store(String key, SMUser user);
    Optional<SMUser> find(String key);
}
