package com.influencerportal.model;

import java.math.BigDecimal;
import java.util.List;

/** A brand manager owns a budget and the requirements a suitable influencer must meet. */
public class BrandManager extends User {
    private String requiredNiche;
    private int minFollowers;
    private String targetPlatform;
    private BigDecimal budget;

    public BrandManager(long id, String username, String brandName, String passwordHash, String requiredNiche,
                        long minFollowers, String targetPlatform, BigDecimal budget) {
        super(id, username, brandName, passwordHash);
        this.requiredNiche = Validate.text(requiredNiche, "Required niche");
        this.minFollowers = Validate.nonNegative(minFollowers, "Minimum followers");
        this.targetPlatform = Validate.text(targetPlatform, "Target platform");
        this.budget = Validate.money(budget, "Budget");
    }

    public void changeRequirements(String niche, long minFollowers, String platform) {
        this.requiredNiche = Validate.text(niche, "Required niche");
        this.minFollowers = Validate.nonNegative(minFollowers, "Minimum followers");
        this.targetPlatform = Validate.text(platform, "Target platform");
    }

    public void changeBudget(BigDecimal newBudget) {
        budget = Validate.money(newBudget, "Budget");
    }

    /** Adds money to the budget. The amount must be greater than zero. */
    public void topUpBudget(BigDecimal amount) {
        BigDecimal added = Validate.money(amount, "Top-up amount");
        if (added.signum() == 0) {
            throw new com.influencerportal.exception.ValidationException("Top-up amount must be greater than zero.");
        }
        budget = budget.add(added);
    }

    @Override
    public Role getRole() {
        return Role.BRAND_MANAGER;
    }

    @Override
    public List<String> describe() {
        return List.of("Brand manager: " + getDisplayName() + " (" + getUsername() + ")",
                "Required niche: " + requiredNiche,
                "Minimum followers: " + minFollowers,
                "Target platform: " + targetPlatform,
                "Remaining budget: " + budget);
    }

    public String getRequiredNiche() {
        return requiredNiche;
    }

    public int getMinFollowers() {
        return minFollowers;
    }

    public String getTargetPlatform() {
        return targetPlatform;
    }

    public BigDecimal getBudget() {
        return budget;
    }
}
