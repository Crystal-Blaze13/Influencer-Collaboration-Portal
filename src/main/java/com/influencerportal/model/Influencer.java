package com.influencerportal.model;

import com.influencerportal.exception.ValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Influencer extends User {
    private String niche;
    private int followers;
    private final double engagementRate; // percent of followers who interact, 0-100
    private final List<String> platforms = new ArrayList<>();
    private BigDecimal fee; // asking price per campaign
    private BigDecimal earnings;

    public Influencer(long id, String username, String displayName, String passwordHash, String niche,
                      long followers, double engagementRate, List<String> platforms, BigDecimal fee,
                      BigDecimal earnings) {
        super(id, username, displayName, passwordHash);
        this.niche = Validate.text(niche, "Niche");
        this.followers = Validate.nonNegative(followers, "Followers");
        this.engagementRate = Validate.percentage(engagementRate, "Engagement rate");
        for (String platform : platforms) {
            addPlatform(platform);
        }
        if (this.platforms.isEmpty()) {
            throw new ValidationException("An influencer needs at least one platform.");
        }
        this.fee = Validate.money(fee, "Fee");
        this.earnings = Validate.money(earnings, "Earnings");
    }

    public void addPlatform(String platform) {
        String name = Validate.text(platform, "Platform");
        boolean known = platforms.stream().anyMatch(p -> p.equalsIgnoreCase(name));
        if (!known) {
            platforms.add(name);
        }
    }

    public void addFollowers(long extra) {
        if (extra < 0) {
            throw new ValidationException("Followers to add must not be negative.");
        }
        followers = Validate.nonNegative((long) followers + extra, "Followers");
    }

    public void changeNiche(String newNiche) {
        niche = Validate.text(newNiche, "Niche");
    }

    public void changeFee(BigDecimal newFee) {
        fee = Validate.money(newFee, "Fee");
    }

    public boolean usesPlatform(String platform) {
        return platforms.stream().anyMatch(p -> p.equalsIgnoreCase(platform));
    }

    @Override
    public Role getRole() {
        return Role.INFLUENCER;
    }

    @Override
    public List<String> describe() {
        return List.of("Influencer: " + getDisplayName() + " (" + getUsername() + ")",
                "Niche: " + niche,
                "Followers: " + followers,
                "Engagement rate: " + engagementRate + "%",
                "Platforms: " + String.join(", ", platforms),
                "Fee per campaign: " + fee,
                "Earnings: " + earnings);
    }

    public String getNiche() {
        return niche;
    }

    public int getFollowers() {
        return followers;
    }

    public double getEngagementRate() {
        return engagementRate;
    }

    public List<String> getPlatforms() {
        return Collections.unmodifiableList(platforms);
    }

    public BigDecimal getFee() {
        return fee;
    }

    public BigDecimal getEarnings() {
        return earnings;
    }
}
