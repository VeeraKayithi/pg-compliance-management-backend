package com.pgcompliance.exception;

import com.pgcompliance.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(
                        ResourceNotFoundException ex) {

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("NOT FOUND")
                                .message(ex.getMessage())
                                .build();

                return new ResponseEntity<>(
                                response,
                                HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleException(Exception ex) {

                ErrorResponse response = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error("INTERNAL SERVER ERROR")
                                .message(ex.getMessage())
                                .build();

                return new ResponseEntity<>(
                                response,
                                HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidationExceptions(
                        MethodArgumentNotValidException ex) {

                Map<String, String> errors = new HashMap<>();

                ex.getBindingResult()
                                .getFieldErrors()
                                .forEach(error -> errors.put(error.getField(),
                                                error.getDefaultMessage()));

                return ResponseEntity.badRequest().body(errors);
        }

}