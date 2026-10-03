package com.hrishabh.problemservice.dailychallenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.hrishabh.problemservice.dailychallenge")
public class DailyChallengeExceptionHandler {

    @ExceptionHandler(PotdNotScheduledException.class)
    public ResponseEntity<Map<String, Object>> handleNotScheduled(PotdNotScheduledException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("code", PotdNotScheduledException.CODE);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
