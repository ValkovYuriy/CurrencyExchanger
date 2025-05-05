package yuriy.dev.exchangeservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yuriy.dev.exchangeservice.dto.ResponseDto;
import yuriy.dev.exchangeservice.exception.AuthenticationMismatchException;
import yuriy.dev.exchangeservice.exception.NotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseDto<Object>> handleNotFoundException(NotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity.ok(new ResponseDto<>(e.getMessage(), null));
    }

    @ExceptionHandler(AuthenticationMismatchException.class)
    public ResponseEntity<ResponseDto<Object>> handleAuthenticationMismatchException(AuthenticationMismatchException e) {
        log.error(e.getMessage());
        return ResponseEntity.ok(new ResponseDto<>(e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.ok(new ResponseDto<>("Ошибка валидации",errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Object>> handleException(Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.ok(new ResponseDto<>(e.getMessage(),null));
    }
}
