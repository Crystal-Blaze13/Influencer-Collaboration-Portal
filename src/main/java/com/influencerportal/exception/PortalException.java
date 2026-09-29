package com.influencerportal.exception;

/** Base class for every error the portal reports to the user. Unchecked to keep signatures simple. */
public class PortalException extends RuntimeException {
    public PortalException(String message) {
        super(message);
    }

    public PortalException(String message, Throwable cause) {
        super(message, cause);
    }
}
