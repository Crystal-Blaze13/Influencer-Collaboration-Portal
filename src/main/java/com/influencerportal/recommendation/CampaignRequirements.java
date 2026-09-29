package com.influencerportal.recommendation;

import java.math.BigDecimal;

/** What a brand manager is looking for. Built from the brand manager's profile. */
public record CampaignRequirements(String niche, int minFollowers, String platform, BigDecimal budget) {
}
