package org.amupoti.sm.main.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.parser.acb.exception.ErrorCodes;
import org.amupoti.supermanager.parser.acb.exception.SmParserException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static org.amupoti.supermanager.parser.acb.exception.ErrorCodes.DEFAULT_ERROR;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String DEFAULT_ERROR_VIEW = "error";

    @ExceptionHandler(SmParserException.class)
    public ModelAndView handleSmParserException(HttpServletRequest request, SmParserException ex) {
        log.error("Exception ocurred", ex);
        ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        mav.addObject("message", ErrorCodes.getMessageFromCode(ex.getCode()));
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(HttpServletRequest request, Exception ex) {
        log.error("Exception ocurred", ex);
        ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        mav.addObject("message", ErrorCodes.getMessageFromCode(DEFAULT_ERROR));
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("stacktrace", ExceptionUtils.getFullStackTrace(ex));
        return mav;

    }
}