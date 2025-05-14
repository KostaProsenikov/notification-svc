package app.web;

import app.errors.CustomErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvice {

    // Handle all general exceptions
    @ExceptionHandler (Exception.class)
    public String handleGeneralException(Exception e) {
        return "Some text for the client: " + e.getMessage();
    }

    // Handle RuntimeException specifically
    @ResponseStatus (HttpStatus.NOT_FOUND)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CustomErrorResponse> handleNoResourceFoundException() {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                HttpStatus.NOT_FOUND.value(), "Not supported endpoint.");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }
}