package com.helpflow.support.infrastructure.adapter.in.rest;

import com.helpflow.support.domain.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTicketNotFound(TicketNotFoundException ex, WebRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, "Ticket Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler({UserNotFoundException.class, TechnicianNotFoundException.class})
    public ResponseEntity<ProblemDetail> handleNotFound(DomainException ex, WebRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidTicketTransitionException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTransition(InvalidTicketTransitionException ex, WebRequest request) {
        return buildProblem(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid State Transition", ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleException ex, WebRequest request) {
        return buildProblem(HttpStatus.UNPROCESSABLE_ENTITY, "Business Rule Violation", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return buildProblem(HttpStatus.BAD_REQUEST, "Validation Failed", detail, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return buildProblem(HttpStatus.FORBIDDEN, "Access Denied", "No tiene permisos para esta operación", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuth(AuthenticationException ex, WebRequest request) {
        return buildProblem(HttpStatus.UNAUTHORIZED, "Authentication Failed", "Credenciales inválidas", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneral(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Error interno del servidor", request);
    }

    private ResponseEntity<ProblemDetail> buildProblem(HttpStatus status, String title, String detail, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setType(URI.create("about:blank"));
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("traceId", UUID.randomUUID().toString());
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(pd);
    }
}
