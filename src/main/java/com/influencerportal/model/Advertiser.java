package com.influencerportal.model;

import java.math.BigDecimal;
import java.util.List;

/** An advertiser publishes on one platform and keeps a commission from every payment. */
public class Advertiser extends User {
    private String platform;
    private BigDecimal commission; // fraction, e.g. 0.15 = 15 percent
    private BigDecimal earnings;

    public Advertiser(long id, String username, String displayName, String passwordHash, String platform,
                      BigDecimal commission, BigDecimal earnings) {
        super(id, username, displayName, passwordHash);
        this.platform = Validate.text(platform, "Platform");
        this.commission = Validate.commission(commission);
        this.earnings = Validate.money(earnings, "Earnings");
    }

    public void changePlatform(String newPlatform) {
        platform = Validate.text(newPlatform, "Platform");
    }

    public void changeCommission(BigDecimal newCommission) {
        commission = Validate.commission(newCommission);
    }

    @Override
    public Role getRole() {
        return Role.ADVERTISER;
    }

    @Override
    public List<String> describe() {
        return List.of("Advertiser: " + getDisplayName() + " (" + getUsername() + ")",
                "Platform: " + platform,
                "Commission: " + commission.movePointRight(2).stripTrailingZeros().toPlainString() + "%",
                "Earnings: " + earnings);
    }

    public String getPlatform() {
        return platform;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public BigDecimal getEarnings() {
        return earnings;
    }
}
