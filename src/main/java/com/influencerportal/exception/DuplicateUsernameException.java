package com.influencerportal.exception;

/** A user with the requested username already exists. */
public class DuplicateUsernameException extends PortalException {
    public DuplicateUsernameException(String message) {
        super(message);
    }

}
