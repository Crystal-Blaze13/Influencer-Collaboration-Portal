package com.influencerportal.model;

import com.influencerportal.exception.InvalidStateTransitionException;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * One collaboration between a brand manager, an influencer and an advertiser.
 * The state machine lives here so an illegal transition is rejected before anything reaches the database.
 * The three display names are filled in by the repository's join and are read-only.
 */
public class Campaign {
    private final long id;
    private final long brandManagerId;
    private long influencerId;
    private final long advertiserId;
    private CampaignStatus status;
    private final Instant createdAt;
    private final Contract contract;
    private final BigDecimal totalPaid;
    private final String brandName;
    private String influencerName;
    private final String advertiserName;

    public Campaign(long id, long brandManagerId, long influencerId, long advertiserId, CampaignStatus status,
                    Instant createdAt, Contract contract, BigDecimal totalPaid, String brandName,
                    String influencerName, String advertiserName) {
        this.id = id;
        this.brandManagerId = brandManagerId;
        this.influencerId = influencerId;
        this.advertiserId = advertiserId;
        this.status = status;
        this.createdAt = createdAt;
        this.contract = contract;
        this.totalPaid = totalPaid;
        this.brandName = brandName;
        this.influencerName = influencerName;
        this.advertiserName = advertiserName;
    }

    /** Moves to {@code next} or throws if the life cycle does not allow it. */
    public void moveTo(CampaignStatus next) {
        if (!status.canMoveTo(next)) {
            throw new InvalidStateTransitionException(
                    "Campaign " + id + " cannot go from " + status + " to " + next
                            + (status.allowedNext().isEmpty() ? " (it is final)." : "; allowed: " + status.allowedNext() + "."));
        }
        status = next;
    }

    /** Signing is only possible while the campaign is CREATED; it moves the campaign to SIGNED. */
    public void signContract(Instant when) {
        moveTo(CampaignStatus.SIGNED);
        contract.sign(when);
    }

    public BigDecimal outstandingFee() {
        return contract.getFee().subtract(totalPaid);
    }

    public void replaceInfluencer(long newInfluencerId, String newName) {
        if (status != CampaignStatus.CREATED) {
            throw new InvalidStateTransitionException(
                    "The influencer can only be changed before the contract is signed (status is " + status + ").");
        }
        influencerId = newInfluencerId;
        influencerName = newName;
    }

    public long getId() {
        return id;
    }

    public long getBrandManagerId() {
        return brandManagerId;
    }

    public long getInfluencerId() {
        return influencerId;
    }

    public long getAdvertiserId() {
        return advertiserId;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Contract getContract() {
        return contract;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getInfluencerName() {
        return influencerName;
    }

    public String getAdvertiserName() {
        return advertiserName;
    }

    @Override
    public String toString() {
        return "#" + id + " " + brandName + " x " + influencerName + " via " + advertiserName
                + " [" + status + "] fee " + contract.getFee() + ", paid " + totalPaid
                + (contract.isSigned() ? ", signed " + contract.getSignedAt() : ", unsigned");
    }
}
