package at.ac.tuwien.sepr.groupphase.backend.endpoint.exceptionhandler;

import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Handles exceptions that only have one message.
     */
    @ExceptionHandler(value = {NotFoundException.class, BadCredentialsException.class, UsernameNotFoundException.class, SecurityException.class})
    protected ResponseEntity<Object> handleNotFound(Exception ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();

        //Get all errors
        body.put("errors", Collections.singletonList(ex.getMessage()));

        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {ConflictException.class})
    protected ResponseEntity<Object> handleConflict(ConflictException ex, WebRequest request) {
        LOGGER.warn("Conflict exception: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();

        //Get all errors
        body.put("errors", ex.getErrors());

        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    /**
     * Override methods from ResponseEntityExceptionHandler to send a customized HTTP response for a know exception
     * from e.g. Spring
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        //Get all errors
        List<String> errors = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.toList());
        body.put("errors", errors);

        LOGGER.warn("Argument not valid exception: {}", String.join(", ", errors));

        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * Handles access denial scenarios, such as forbidden actions for specific users (e.g., admin password change).
     * Returns HTTP 403 with a descriptive message.
     *
     * @param ex      the thrown AccessDeniedException
     * @param request the web request
     * @return a {@link ResponseEntity} with 403 status and custom message
     */
    @ExceptionHandler(value = {AccessDeniedException.class})
    protected ResponseEntity<Object> handleAccessDenied(RuntimeException ex, WebRequest request) {
        LOGGER.warn("Access denied: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();

        //Get all errors
        body.put("errors", Collections.singletonList(ex.getMessage()));

        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    /**
     * Handles illegal arguments in service or controller layers (e.g., invalid request states).
     * Returns HTTP 400 BAD REQUEST with the cause message.
     *
     * @param ex      the thrown IllegalArgumentException
     * @param request the web request
     * @return a {@link ResponseEntity} with 400 status and explanation message
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleIllegalArgument(RuntimeException ex, WebRequest request) {
        LOGGER.warn("Illegal argument: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();

        //Get all errors
        body.put("errors", Collections.singletonList(ex.getMessage()));

        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }


}
