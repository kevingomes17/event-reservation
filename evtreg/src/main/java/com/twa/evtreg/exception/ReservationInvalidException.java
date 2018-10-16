package com.twa.evtreg.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReservationInvalidException extends RuntimeException {
    public ReservationInvalidException(String exception) {
        super(exception);
    }
}
