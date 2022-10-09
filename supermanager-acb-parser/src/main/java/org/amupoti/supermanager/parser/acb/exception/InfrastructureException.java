package org.amupoti.supermanager.parser.acb.exception;


public class InfrastructureException extends RuntimeException {


    public InfrastructureException(String message, Exception e) {
        super(message, e);
    }
}
