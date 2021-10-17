package org.amupoti.supermanager.parser.acb.exception;

import org.springframework.web.client.HttpClientErrorException;

/**
 * Created by amupoti on 13/09/2017.
 */
public class SmException extends RuntimeException {

    public SmException(ErrorCode code, Throwable t) {
        super(ErrorResolver.getMessageFromCode(code) + buildMessage(t), t);
    }

    private static String buildMessage(Throwable t) {
        if (t instanceof HttpClientErrorException) {
            return ".\n" + ((HttpClientErrorException) t).getResponseBodyAsString();
        }
        return "";
    }

    public SmException(ErrorCode code) {
        super(ErrorResolver.getMessageFromCode(code));
    }

}
