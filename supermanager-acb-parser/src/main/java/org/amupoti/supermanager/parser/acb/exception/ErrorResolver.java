package org.amupoti.supermanager.parser.acb.exception;

import java.util.HashMap;
import java.util.Map;

import static org.amupoti.supermanager.parser.acb.exception.ErrorCode.*;

/**
 * Created by amupoti on 13/09/2017.
 */
public class ErrorResolver {

    private static Map<ErrorCode, String> errors = new HashMap();

    static {
        errors.put(ERROR_PARSING_TEAMS, "No se han podido obtener tus equipos. Es posible que la página supermanager.acb.com " +
                "no funcione correctamente");
        errors.put(DEFAULT_ERROR, "Se ha producido un error, pruébalo de nuevo más tarde.");
        errors.put(INCORRECT_SESSION_ID, "Tu sesión ha caducado, prueba a hacer login de nuevo.");
        errors.put(ERROR_PARSING_MARKET, "No se ha podido recuperar la información del mercado");
    }

    public static String getMessageFromCode(ErrorCode code) {
        return errors.getOrDefault(code, "Se ha producido un error desconocido");
    }


}
