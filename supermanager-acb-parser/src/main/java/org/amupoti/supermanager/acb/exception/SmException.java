package org.amupoti.supermanager.acb.exception;

import org.springframework.web.client.HttpClientErrorException;

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
