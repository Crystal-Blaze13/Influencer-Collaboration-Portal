package com.influencerportal.exception;

/** A requested record does not exist. */
public class NotFoundException extends PortalException {
    public NotFoundException(String message) {
        super(message);
    }

}
