package org.amupoti.supermanager.parser.acb.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by amupoti on 13/09/2017.
 */
public class ErrorCodes {

    public static String ERROR_PARSING_TEAMS = "exception.parsing.teams";
    public static final String DEFAULT_ERROR = "default.error";
    private static Map<String, String> errors = new HashMap();


    static {
        errors.put(ERROR_PARSING_TEAMS, "No se han podido obtener tus equipos. Es probable que la jornada esté cerrada temporalmente, pruébalo de nuevo más tarde.");
        errors.put(DEFAULT_ERROR, "Se ha producido un error, pruébalo de nuevo más tarde.");
    }

    public static String getMessageFromCode(String code) {
        return errors.getOrDefault(code, "Se ha producido un error desconocido");
    }
}
