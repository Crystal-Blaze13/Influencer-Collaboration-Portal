package com.influencerportal.exception;

/** A payment is larger than the brand manager's remaining budget. */
public class InsufficientBudgetException extends PortalException {
    public InsufficientBudgetException(String message) {
        super(message);
    }

}
