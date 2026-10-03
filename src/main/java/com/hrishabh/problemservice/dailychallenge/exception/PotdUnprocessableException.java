package com.hrishabh.problemservice.dailychallenge.exception;

public class PotdUnprocessableException extends RuntimeException {
    public PotdUnprocessableException(String message) {
        super(message);
    }
}
