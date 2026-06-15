package com.planpormexico.curp_validator.exception;

public class RegistrationLimitException extends RuntimeException {

    public RegistrationLimitException(String message) {
        super(message);
    }
}
