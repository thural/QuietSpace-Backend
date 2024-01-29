package dev.thural.quietspacebackend.controller;

import dev.thural.quietspacebackend.model.response.ErrorResponse;
import dev.thural.quietspacebackend.exception.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFoundException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler
    ResponseEntity<?> handleJPAViolations(TransactionSystemException exception) {
        ResponseEntity.BodyBuilder responseEntity = ResponseEntity.badRequest();

        if (exception.getCause().getCause() instanceof ConstraintViolationException violationException) {

            List<Map<String, String>> errors = violationException.getConstraintViolations().stream()
                    .map(constraintViolation -> {
                        Map<String, String> errorMap = new HashMap<>();
                        errorMap.put(constraintViolation.getPropertyPath().toString(),
                                constraintViolation.getMessage());
                        return errorMap;
                    }).collect(Collectors.toList());

            return responseEntity.body(errors);
        }

        return responseEntity.build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> handleBindErrors(MethodArgumentNotValidException exception) {
        List<Map<String, String>> errorList = exception.getFieldErrors().stream()
                .map(fieldError -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
                    return errorMap;
                }).collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errorList);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(RuntimeException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        return new ResponseEntity<>(ErrorResponse.builder()
                .code(401)
                .status(status.name())
                .message(e.getMessage())
                .build(), status);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(RuntimeException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        return new ResponseEntity<>(ErrorResponse.builder()
                .code(401)
                .status(status.name())
                .message(e.getMessage())
                .build(), status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(RuntimeException e) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        return new ResponseEntity<>(ErrorResponse.builder()
                .code(403)
                .status(status.name())
                .message(e.getMessage())
                .build(), status);
    }


    @ExceptionHandler(CustomDataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomDataNotFoundExceptions(RuntimeException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        return new ResponseEntity<>(ErrorResponse.builder()
                .code(404)
                .status(status.name())
                .message(e.getMessage())
                .build(), status);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundExceptions(RuntimeException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .status(status.name())
                        .message("the requested resource not found: " + e.getMessage())
                        .code(404)
                        .timestamp(new Date())
                        .build(), status);
    }

    @ExceptionHandler(CustomParameterConstraintException.class)
    public ResponseEntity<ErrorResponse> handleCustomParameterConstraintExceptions(RuntimeException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .code(400)
                        .message("A parameter constraint error occurred: " + e.getMessage())
                        .status(status.name())
                        .build(),
                status);
    }

    @ExceptionHandler(CustomErrorException.class)
    public ResponseEntity<ErrorResponse> handleCustomErrorExceptions(RuntimeException e) {
        CustomErrorException customErrorException = (CustomErrorException) e;

        HttpStatus status = customErrorException.getStatus();

        return ResponseEntity.internalServerError()
                .body(ErrorResponse.builder()
                        .code(500)
                        .status(status.name())
                        .message("An unexpected error occurred: " + e.getMessage())
                        .timestamp(new Date())
                        .build()
                );
    }

    @ExceptionHandler(Exception.class) // exception handled
    public ResponseEntity<ErrorResponse> handleExceptions(RuntimeException exception) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.internalServerError()
                .body(ErrorResponse.builder()
                        .code(500)
                        .status(status.name())
                        .message("An unexpected error occurred: " + exception.getMessage())
                        .timestamp(new Date())
                        .build()
                );
    }


}
