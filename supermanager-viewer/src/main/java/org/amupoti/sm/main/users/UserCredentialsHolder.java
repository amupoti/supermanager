package org.amupoti.sm.main.users;

import org.amupoti.sm.main.bean.SMUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by amupoti on 22/08/2017.
 */
public class UserCredentialsHolder {

    private Map<String, SMUser> credentialsMap = new HashMap<>();

    public void addCredentials(String key, SMUser smUser) {
        credentialsMap.put(key, smUser);
    }

    public Optional<SMUser> getCredentialsByKey(String key) {
        return Optional.ofNullable(credentialsMap.get(key));
    }
}
