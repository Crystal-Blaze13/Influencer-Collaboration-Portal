package com.influencerportal.exception;

/** A database operation failed (wraps SQLException). */
public class PersistenceException extends PortalException {
    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
