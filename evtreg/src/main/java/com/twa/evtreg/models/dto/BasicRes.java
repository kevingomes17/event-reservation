package com.twa.evtreg.models.dto;

public class BasicRes {
    private Boolean status;
    private String message;
    private String code; // Error Code when status is false

    public BasicRes(Boolean status, String message, String code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
