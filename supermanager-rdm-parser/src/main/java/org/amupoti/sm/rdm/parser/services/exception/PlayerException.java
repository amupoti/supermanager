package org.amupoti.sm.rdm.parser.services.exception;

/**
 * Created by Marcel on 12/11/2015.
 */
public class PlayerException extends Exception {
    public PlayerException(Throwable e) {
        super(e);
    }

    public PlayerException(String message, Throwable e) {
        super(message, e);
    }
}
