package com.influencerportal.service;

import com.influencerportal.exception.InsufficientBudgetException;
import com.influencerportal.exception.ValidationException;
import com.influencerportal.model.Advertiser;
import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Campaign;
import com.influencerportal.model.CampaignStatus;
import com.influencerportal.model.Influencer;
import com.influencerportal.model.Role;
import com.influencerportal.model.User;
import com.influencerportal.recommendation.CampaignRequirements;
import com.influencerportal.recommendation.Recommendation;
import com.influencerportal.recommendation.RecommendationEngine;
import com.influencerportal.repository.CampaignRepository;
import com.influencerportal.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Campaign creation and the CREATED -> SIGNED -> ACTIVE -> COMPLETED / CANCELLED life cycle. */
public class CampaignService {
    public static final int MAX_DURATION_DAYS = 365;

    private final UserRepository users;
    private final CampaignRepository campaigns;
    private final RecommendationEngine engine;

    public CampaignService(UserRepository users, CampaignRepository campaigns, RecommendationEngine engine) {
        this.users = users;
        this.campaigns = campaigns;
        this.engine = engine;
    }

    /** Ranked influencers for this brand manager's requirements. */
    public List<Recommendation> recommendInfluencers(long brandManagerId) {
        BrandManager manager = (BrandManager) users.getById(brandManagerId);
        List<Influencer> influencers = new ArrayList<>();
        for (User user : users.findAll(Role.INFLUENCER)) {
            influencers.add((Influencer) user);
        }
        return engine.rank(requirementsOf(manager), influencers);
    }

    /** Advertisers on the manager's target platform, lowest commission first. */
    public List<Advertiser> advertisersForTargetPlatform(long brandManagerId) {
        BrandManager manager = (BrandManager) users.getById(brandManagerId);
        List<Advertiser> matching = new ArrayList<>();
        for (User user : users.findAll(Role.ADVERTISER)) {
            Advertiser advertiser = (Advertiser) user;
            if (advertiser.getPlatform().equalsIgnoreCase(manager.getTargetPlatform())) {
                matching.add(advertiser);
            }
        }
        matching.sort(Comparator.comparing(Advertiser::getCommission).thenComparing(Advertiser::getUsername));
        return matching;
    }

    /** Creates a campaign in state CREATED with an unsigned contract whose fee is the influencer's fee. */
    public Campaign createCampaign(long brandManagerId, long influencerId, long advertiserId, int durationDays) {
        BrandManager manager = requireRole(brandManagerId, Role.BRAND_MANAGER, BrandManager.class);
        Influencer influencer = requireRole(influencerId, Role.INFLUENCER, Influencer.class);
        Advertiser advertiser = requireRole(advertiserId, Role.ADVERTISER, Advertiser.class);

        if (durationDays < 1 || durationDays > MAX_DURATION_DAYS) {
            throw new ValidationException("Duration must be between 1 and " + MAX_DURATION_DAYS + " days.");
        }
        if (!influencer.usesPlatform(manager.getTargetPlatform())) {
            throw new ValidationException(influencer.getDisplayName() + " is not on " + manager.getTargetPlatform() + ".");
        }
        if (!advertiser.getPlatform().equalsIgnoreCase(manager.getTargetPlatform())) {
            throw new ValidationException(advertiser.getDisplayName() + " does not publish on "
                    + manager.getTargetPlatform() + ".");
        }
        if (influencer.getFee().compareTo(manager.getBudget()) > 0) {
            throw new InsufficientBudgetException("The fee " + influencer.getFee()
                    + " is larger than the remaining budget " + manager.getBudget() + ".");
        }
        return campaigns.insert(brandManagerId, influencerId, advertiserId, influencer.getFee(), durationDays,
                Instant.now());
    }

    /** The influencer signs the contract: CREATED -> SIGNED. Only the campaign's own influencer may sign. */
    public Campaign signContract(long campaignId, long influencerId) {
        Campaign campaign = campaigns.getById(campaignId);
        if (campaign.getInfluencerId() != influencerId) {
            throw new ValidationException("Campaign " + campaignId + " does not belong to you.");
        }
        campaign.signContract(Instant.now());
        campaigns.save(campaign);
        return campaign;
    }

    /** SIGNED -> ACTIVE, by the owning brand manager. */
    public Campaign activate(long campaignId, long brandManagerId) {
        return transition(campaignId, brandManagerId, CampaignStatus.ACTIVE);
    }

    /** ACTIVE -> COMPLETED, by the owning brand manager. */
    public Campaign complete(long campaignId, long brandManagerId) {
        return transition(campaignId, brandManagerId, CampaignStatus.COMPLETED);
    }

    /** Any open state -> CANCELLED, by the owning brand manager. */
    public Campaign cancel(long campaignId, long brandManagerId) {
        return transition(campaignId, brandManagerId, CampaignStatus.CANCELLED);
    }

    public Campaign get(long campaignId) {
        return campaigns.getById(campaignId);
    }

    public List<Campaign> listFor(User user) {
        return switch (user.getRole()) {
            case BRAND_MANAGER -> campaigns.findByBrandManager(user.getId());
            case INFLUENCER -> campaigns.findByInfluencer(user.getId());
            case ADVERTISER -> campaigns.findByAdvertiser(user.getId());
            case ADMIN -> campaigns.findAll();
        };
    }

    private Campaign transition(long campaignId, long brandManagerId, CampaignStatus next) {
        Campaign campaign = campaigns.getById(campaignId);
        if (campaign.getBrandManagerId() != brandManagerId) {
            throw new ValidationException("Campaign " + campaignId + " does not belong to you.");
        }
        campaign.moveTo(next);
        campaigns.save(campaign);
        return campaign;
    }

    private CampaignRequirements requirementsOf(BrandManager manager) {
        return new CampaignRequirements(manager.getRequiredNiche(), manager.getMinFollowers(),
                manager.getTargetPlatform(), manager.getBudget());
    }

    private <T extends User> T requireRole(long id, Role role, Class<T> type) {
        User user = users.getById(id);
        if (user.getRole() != role) {
            throw new ValidationException("User " + user.getUsername() + " is not a " + role + ".");
        }
        return type.cast(user);
    }
}
