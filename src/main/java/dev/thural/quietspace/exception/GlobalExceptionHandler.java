package dev.thural.quietspace.exception;

import dev.thural.quietspace.model.response.ErrorResponse;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
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

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(RuntimeException e) {
        HttpStatus status = NOT_FOUND;
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.name())
                        .message(e.getMessage())
                        .timestamp(new Date())
                        .build());
    }

    @ExceptionHandler(TransactionSystemException.class)
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(RuntimeException e) {
        HttpStatus status = BAD_REQUEST;
        return new ResponseEntity<>(ErrorResponse.builder()
                .status(status.name())
                .message(e.getMessage())
                .build(), status);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(RuntimeException e) {
        HttpStatus status = UNAUTHORIZED;
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(status.name())
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(RuntimeException e) {
        HttpStatus status = UNAUTHORIZED;
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.name())
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(RuntimeException e) {
        HttpStatus status = FORBIDDEN;
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.name())
                        .message(e.getMessage())
                        .timestamp(new Date())
                        .build());
    }


    @ExceptionHandler(CustomDataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomDataNotFoundExceptions(RuntimeException e) {
        HttpStatus status = NOT_FOUND;
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.name())
                        .message(e.getMessage())
                        .timestamp(new Date())
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundExceptions(RuntimeException e) {
        HttpStatus status = NOT_FOUND;
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.name())
                        .message(e.getMessage())
                        .timestamp(new Date())
                        .build());
    }

    @ExceptionHandler(CustomParameterConstraintException.class)
    public ResponseEntity<ErrorResponse> handleCustomParameterConstraintExceptions(RuntimeException e) {
        HttpStatus status = BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.name())
                        .message(e.getMessage())
                        .timestamp(new Date())
                        .build());
    }

    @ExceptionHandler(CustomErrorException.class)
    public ResponseEntity<ErrorResponse> handleCustomErrorExceptions(CustomErrorException e) {
        HttpStatus status = e.getStatus();
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.name())
                        .message(e.getMessage())
                        .timestamp(new Date())
                        .build());
    }

    @ExceptionHandler(ActivationTokenException.class)
    public ResponseEntity<ErrorResponse> handleActivationTokenExceptions(RuntimeException e) {
        HttpStatus status = BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.name())
                        .message(e.getMessage())
                        .timestamp(new Date())
                        .build());
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ErrorResponse> handleMailingException(RuntimeException e) {
        HttpStatus status = INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.name())
                        .message(e.getMessage())
                        .timestamp(new Date())
                        .build());
    }

}
