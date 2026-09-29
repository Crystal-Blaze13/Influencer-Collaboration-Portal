package com.influencerportal.exception;

/** Username or password is wrong. */
public class AuthenticationException extends PortalException {
    public AuthenticationException(String message) {
        super(message);
    }

}
