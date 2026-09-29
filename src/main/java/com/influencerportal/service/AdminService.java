package com.influencerportal.service;

import com.influencerportal.model.CampaignStatus;
import com.influencerportal.model.Role;
import com.influencerportal.repository.CampaignRepository;
import com.influencerportal.repository.PaymentRepository;
import com.influencerportal.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Map;

/** Numbers for the admin overview and dashboard. */
public class AdminService {
    private final UserRepository users;
    private final CampaignRepository campaigns;
    private final PaymentRepository payments;

    public AdminService(UserRepository users, CampaignRepository campaigns, PaymentRepository payments) {
        this.users = users;
        this.campaigns = campaigns;
        this.payments = payments;
    }

    public Map<Role, Integer> userCounts() {
        return users.countByRole();
    }

    public Map<CampaignStatus, Integer> campaignCounts() {
        return campaigns.countByStatus();
    }

    public BigDecimal totalPaid() {
        return payments.totalPaid();
    }
}
