package org.amupoti.supermanager.acb.exception;

import java.util.Map;

import static org.amupoti.supermanager.acb.exception.ErrorCode.*;

public class ErrorResolver {

    private static final Map<ErrorCode, String> errors = Map.of(
            ERROR_PARSING_TEAMS, "No se han podido obtener tus equipos. Es posible que la página supermanager.acb.com no funcione correctamente",
            DEFAULT_ERROR, "Se ha producido un error, pruébalo de nuevo más tarde.",
            INCORRECT_SESSION_ID, "Tu sesión ha caducado, prueba a hacer login de nuevo.",
            ERROR_PARSING_MARKET, "No se ha podido recuperar la información del mercado",
            TEAM_PAGE_ERROR, "No se ha podido recuperar la información de los equipos",
            INVALID_CREDENTIALS, "Ha habido un error durante el login, comprueba tu usuario y contraseña"
    );

    public static String getMessageFromCode(ErrorCode code) {
        return errors.getOrDefault(code, "Se ha producido un error desconocido");
    }
}
