package org.amupoti.supermanager.parser.acb.exception;

import lombok.Getter;

/**
 * Created by amupoti on 13/09/2017.
 */
public class SmParserException extends RuntimeException {

    @Getter
    private final String code;

    public SmParserException(String message, String code, Throwable t) {
        super(message, t);
        this.code = code;
    }
}
