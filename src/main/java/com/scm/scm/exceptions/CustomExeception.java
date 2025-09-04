package com.scm.scm.exceptions;

public class CustomExeception extends RuntimeException {
    private String message;
    public CustomExeception(String message) {
        super(message);
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
