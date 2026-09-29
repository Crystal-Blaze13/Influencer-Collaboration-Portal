package com.influencerportal.model;

import com.influencerportal.exception.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Small input checks shared by the domain classes and services. */
public final class Validate {
    private Validate() {
    }

    public static String text(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " must not be empty.");
        }
        return value.trim();
    }

    public static int nonNegative(long value, String field) {
        if (value < 0 || value > Integer.MAX_VALUE) {
            throw new ValidationException(field + " must be between 0 and " + Integer.MAX_VALUE + ".");
        }
        return (int) value;
    }

    public static double percentage(double value, String field) {
        if (Double.isNaN(value) || value < 0 || value > 100) {
            throw new ValidationException(field + " must be between 0 and 100.");
        }
        return value;
    }

    /** Money must be non-negative and have at most two decimal places. Returned with scale 2. */
    public static BigDecimal money(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) {
            throw new ValidationException(field + " must not be negative.");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new ValidationException(field + " must have at most two decimal places.");
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    /** A commission is a fraction such as 0.15 for 15 percent. */
    public static BigDecimal commission(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new ValidationException("Commission must be a fraction between 0 and 1 (for example 0.15).");
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
