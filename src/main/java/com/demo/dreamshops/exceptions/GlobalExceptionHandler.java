package com.demo.dreamshops.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.expression.AccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends RuntimeException  {


    @ExceptionHandler(AccessException.class)
    public ResponseEntity<String> handleAccessDeniedException(Exception ex, HttpServletRequest request){
       String message = "You do not have permission to this action";
       return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
    }
}
