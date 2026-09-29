package com.influencerportal.exception;

/** An input value is missing, malformed or out of range. */
public class ValidationException extends PortalException {
    public ValidationException(String message) {
        super(message);
    }

}
