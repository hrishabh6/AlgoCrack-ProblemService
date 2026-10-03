package com.hrishabh.problemservice.dailychallenge.exception;

public class PotdNotScheduledException extends RuntimeException {

    public static final String CODE = "POTD_NOT_SCHEDULED";

    public PotdNotScheduledException() {
        super("No published daily challenge for the requested date");
    }
}
