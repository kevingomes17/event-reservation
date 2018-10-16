package com.twa.evtreg.exception;

import java.util.Date;

public class CustomError {
    private Date timestamp;
    private String message;
    private String details;

    public CustomError(Date timestamp, String message, String details) {
        super();
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }
}
