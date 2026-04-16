package org.amupoti.supermanager.acb.application.port.out;

/**
 * Driven port: authenticate a user against the ACB API and return a bearer token.
 */
public interface AuthenticationPort {
    String authenticate(String user, String password);
}
