package com.influencerportal.model;

import java.math.BigDecimal;
import java.time.Instant;

/** The agreed terms of a campaign. It becomes binding once the influencer signs it. */
public class Contract {
    private final long id;
    private final long campaignId;
    private final BigDecimal fee;
    private final int durationDays;
    private Instant signedAt; // null until signed

    public Contract(long id, long campaignId, BigDecimal fee, int durationDays, Instant signedAt) {
        this.id = id;
        this.campaignId = campaignId;
        this.fee = Validate.money(fee, "Fee");
        this.durationDays = Validate.nonNegative(durationDays, "Duration");
        this.signedAt = signedAt;
    }

    public boolean isSigned() {
        return signedAt != null;
    }

    public void sign(Instant when) {
        signedAt = when;
    }

    public long getId() {
        return id;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public Instant getSignedAt() {
        return signedAt;
    }
}
