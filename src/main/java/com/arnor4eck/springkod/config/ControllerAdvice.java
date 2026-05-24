package com.arnor4eck.springkod.config;

import com.arnor4eck.springkod.util.exception.NotFoundException;
import com.arnor4eck.springkod.util.factory.ExceptionResponseFactory;
import com.arnor4eck.springkod.util.response.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Iterator;
import java.util.List;

@Slf4j
@RestControllerAdvice
@AllArgsConstructor
public class ControllerAdvice {

    private final ExceptionResponseFactory exceptionResponseFactory;

    @ExceptionHandler({Exception.class})
    public ExceptionResponse handleAllExceptions(Exception e,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        StringBuilder headers = new StringBuilder();

        for (Iterator<String> it = request.getHeaderNames().asIterator(); it.hasNext(); ) {
            String header = it.next();
            headers.append(String.format("%s: %s; ", header, request.getHeader(header)));
        }

        log.warn("Ошибка сервера '{}': {};\nДанные запроса: RequestURI: {}; Method: {}; QueryString: {};\nЗаголовки запроса: {}",
                e.getClass(), e.getMessage(),
                request.getRequestURI(),
                request.getMethod(),
                request.getQueryString(),
                headers);

        return exceptionResponseFactory.create("Неизвестная ошибка сервера. Свяжитесь с разработчиком.",
                response,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ExceptionResponse handleAccessDeniedException(AccessDeniedException e,
                                                         HttpServletResponse response) {
        return exceptionResponseFactory.create(e.getMessage(),
                response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ExceptionResponse handleAuthenticationException(AuthenticationException e,
                                                           HttpServletResponse response) {
        return exceptionResponseFactory.create("Авторизируйтесь для доступа к ресурсу.",
                response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ExceptionResponse methodArgumentNotValidException(MethodArgumentNotValidException e,
                                                             HttpServletResponse response){

        List<String> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(field ->
                        field.getField() + ": " + field.getDefaultMessage())
                .toList();

        return exceptionResponseFactory.create(errors, response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ExceptionResponse handleValidationException(HandlerMethodValidationException ex,
                                                       HttpServletResponse response) {
        List<String> messages = ex.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .toList();

        return exceptionResponseFactory.create(messages,
                response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ResponseStatusException.class})
    public ExceptionResponse responseStatusException(ResponseStatusException e,
                                                     HttpServletResponse response){
        return exceptionResponseFactory.create(e.getReason(),
                response, HttpStatus.valueOf(e.getStatusCode().value()));
    }

    @ExceptionHandler({NotFoundException.class})
    public ExceptionResponse notFoundException(NotFoundException e,
                                                 HttpServletResponse response){
        return exceptionResponseFactory.create(e.getMessage(),
                response, HttpStatus.NOT_FOUND);
    }
}
