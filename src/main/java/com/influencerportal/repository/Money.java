package com.influencerportal.repository;

import java.math.BigDecimal;

/** Money is stored as whole cents (INTEGER) so SQLite never rounds it; in Java it is a BigDecimal. */
final class Money {
    private Money() {
    }

    static long toCents(BigDecimal amount) {
        return amount.movePointRight(2).longValueExact();
    }

    static BigDecimal fromCents(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }
}
