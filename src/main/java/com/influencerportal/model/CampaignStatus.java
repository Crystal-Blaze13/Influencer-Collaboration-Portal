package com.influencerportal.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Campaign life cycle:
 * CREATED -> SIGNED -> ACTIVE -> COMPLETED, and CANCELLED from any non-final state.
 * COMPLETED and CANCELLED are final.
 */
public enum CampaignStatus {
    CREATED, SIGNED, ACTIVE, COMPLETED, CANCELLED;

    public Set<CampaignStatus> allowedNext() {
        return switch (this) {
            case CREATED -> EnumSet.of(SIGNED, CANCELLED);
            case SIGNED -> EnumSet.of(ACTIVE, CANCELLED);
            case ACTIVE -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(CampaignStatus.class);
        };
    }

    public boolean canMoveTo(CampaignStatus next) {
        return allowedNext().contains(next);
    }

    public boolean isOpen() {
        return this != COMPLETED && this != CANCELLED;
    }
}
