package org.amupoti.supermanager.parser.acb.exception;

/**
 * Created by amupoti on 13/09/2017.
 */
public class SmException extends RuntimeException {

    public SmException(ErrorCode code, Throwable t) {
        super(ErrorResolver.getMessageFromCode(code), t);
    }

    public SmException(ErrorCode code) {
        super(ErrorResolver.getMessageFromCode(code));
    }

    public SmException(String message) {
        super(message);
    }
}
