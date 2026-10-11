package com.rentatools.infrastructure.config;

import com.rentatools.domain.common.BusinessRuleValidationException;
import com.rentatools.domain.common.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso No Encontrado");
        problem.setType(URI.create("https://rentatools.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(BusinessRuleValidationException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleValidationException ex) {
        // Si el mensaje es de solapamiento o reserva, se usa 409 Conflict
        HttpStatus status = ex.getMessage().toLowerCase().contains("reserv") || ex.getMessage().toLowerCase().contains("disponib")
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setTitle(status == HttpStatus.CONFLICT ? "Conflicto de Disponibilidad" : "Regla de Negocio Inválida");
        problem.setType(URI.create("https://rentatools.com/errors/business-rule"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problem.setTitle("Error Interno del Servidor");
        problem.setType(URI.create("https://rentatools.com/errors/internal-error"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
