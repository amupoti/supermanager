package org.amupoti.sm.rdm.parser.services.exception;

/**
 * Created by Marcel on 05/11/2016.
 */
public class DataParsingException extends Exception {
    public DataParsingException(Throwable e) {
        super(e);
    }

    public DataParsingException(String message, Throwable e) {
        super(message, e);
    }

}
