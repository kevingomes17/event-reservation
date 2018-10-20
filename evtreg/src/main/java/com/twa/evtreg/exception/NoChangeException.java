package com.twa.evtreg.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class NoChangeException extends RuntimeException {
    public NoChangeException(String exception) {
        super(exception);
    }
}
