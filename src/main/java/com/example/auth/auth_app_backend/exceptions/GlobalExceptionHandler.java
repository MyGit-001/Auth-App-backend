package com.example.auth.auth_app_backend.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.auth.auth_app_backend.dtos.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //resource not found exception handler
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception){
       ErrorResponse errorResponse = new ErrorResponse(exception.getMessage() , HttpStatus.NOT_FOUND
               , "Resource Not Found" );
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}
