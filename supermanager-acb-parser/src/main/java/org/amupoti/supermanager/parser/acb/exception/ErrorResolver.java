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
        errors.put(ERROR_PARSING_TEAMS, "No se han podido obtener tus equipos. Es posible que la contraseña no sea correcta" +
                " o que la jornada esté cerrada temporalmente, pruébalo de nuevo más tarde.");
        errors.put(DEFAULT_ERROR, "Se ha producido un error, pruébalo de nuevo más tarde.");
        errors.put(INCORRECT_SESSION_ID, "Tu sesión ha caducado, prueba a hacer login de nuevo.");
        errors.put(INVALID_USER_PASS, "El usuario o la contraseña no son válidos");
    }

    public static String getMessageFromCode(ErrorCode code) {
        return errors.getOrDefault(code, "Se ha producido un error desconocido");
    }


}
