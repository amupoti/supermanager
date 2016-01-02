package org.amupoti.sm.main.services.exception;

/**
 * Created by Marcel on 12/11/2015.
 */
public class PlayerException extends Throwable {
    public PlayerException(Exception e) {
        super(e);
    }

    public PlayerException(String message, Exception e) {
        super(message,e);
    }
}
