package com.example.e_commerce.Exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler(ResourceAlreadyExistsException.class)
  public ResponseEntity<String> handleResourceAlreadyExists(
          ResourceAlreadyExistsException ex) {

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ex.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<String> handleDuplicate(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body("Data integrity violation: duplicate value or invalid reference");
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleUserNotFound(UsernameNotFoundException ex) {
    return "User not found";
  }

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public String handleBadCredentials(BadCredentialsException ex) {
    return "Invalid username or password";
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, String>> handleBadBody(HttpMessageNotReadableException ex) {
    return ResponseEntity.badRequest()
            .body(Map.of("error", "Request body is missing or invalid JSON"));
  }

  @ExceptionHandler(OutOfStockException.class)
  public ResponseEntity<Map<String, String>> handleOutOfStock(OutOfStockException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<Map<String, String>> handleConflict(ObjectOptimisticLockingFailureException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", "High demand, please try again"));
  }

  @ExceptionHandler(InvalidStatusTransitionException.class)
  public ResponseEntity<String> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity
            .badRequest()
            .body(ex.getMessage());
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<Map<String, String>> handleMissingHeader(MissingRequestHeaderException ex) {
    return ResponseEntity.badRequest()
            .body(Map.of("error", "Missing required header: " + ex.getHeaderName()));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", ex.getMessage()));
  }

}