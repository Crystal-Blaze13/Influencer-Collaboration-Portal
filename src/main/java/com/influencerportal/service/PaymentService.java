package com.influencerportal.service;

import com.influencerportal.exception.InsufficientBudgetException;
import com.influencerportal.exception.InvalidStateTransitionException;
import com.influencerportal.exception.ValidationException;
import com.influencerportal.model.Advertiser;
import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Campaign;
import com.influencerportal.model.CampaignStatus;
import com.influencerportal.model.Payment;
import com.influencerportal.model.Validate;
import com.influencerportal.repository.CampaignRepository;
import com.influencerportal.repository.PaymentRepository;
import com.influencerportal.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

/** Validates a payment, splits it between advertiser and influencer, and hands it to one transaction. */
public class PaymentService {
    private final UserRepository users;
    private final CampaignRepository campaigns;
    private final PaymentRepository payments;

    public PaymentService(UserRepository users, CampaignRepository campaigns, PaymentRepository payments) {
        this.users = users;
        this.campaigns = campaigns;
        this.payments = payments;
    }

    /**
     * Pays {@code amount} towards an ACTIVE campaign. The advertiser receives amount x commission (rounded to
     * cents) and the influencer receives the rest, so no cent is lost.
     */
    public Payment pay(long campaignId, long brandManagerId, BigDecimal amount) {
        Campaign campaign = campaigns.getById(campaignId);
        if (campaign.getBrandManagerId() != brandManagerId) {
            throw new ValidationException("Campaign " + campaignId + " does not belong to you.");
        }
        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new InvalidStateTransitionException(
                    "Payments are only allowed while the campaign is ACTIVE (it is " + campaign.getStatus() + ").");
        }
        Validate.money(amount, "Payment amount");
        if (amount.signum() == 0) {
            throw new ValidationException("Payment amount must be greater than zero.");
        }
        if (amount.compareTo(campaign.outstandingFee()) > 0) {
            throw new ValidationException("Payment " + amount + " exceeds the outstanding contract fee "
                    + campaign.outstandingFee() + ".");
        }
        BrandManager manager = (BrandManager) users.getById(brandManagerId);
        if (amount.compareTo(manager.getBudget()) > 0) {
            throw new InsufficientBudgetException(
                    "Insufficient budget: payment " + amount + ", remaining budget " + manager.getBudget() + ".");
        }
        Advertiser advertiser = (Advertiser) users.getById(campaign.getAdvertiserId());
        BigDecimal advertiserCut = amount.multiply(advertiser.getCommission()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal influencerCut = amount.subtract(advertiserCut);
        return payments.record(campaignId, brandManagerId, advertiser.getId(), campaign.getInfluencerId(),
                amount.setScale(2, RoundingMode.UNNECESSARY), advertiserCut, influencerCut, Instant.now());
    }

    public List<Payment> history(long campaignId) {
        return payments.findByCampaign(campaignId);
    }
}
