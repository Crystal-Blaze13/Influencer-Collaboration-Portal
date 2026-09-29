package com.influencerportal.exception;

/** A campaign was asked to move to a state that is not allowed from its current state. */
public class InvalidStateTransitionException extends PortalException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }

}
