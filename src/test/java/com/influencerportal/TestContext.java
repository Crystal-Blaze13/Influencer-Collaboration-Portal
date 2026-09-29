package com.influencerportal;

import com.influencerportal.recommendation.RecommendationEngine;
import com.influencerportal.repository.CampaignRepository;
import com.influencerportal.repository.Database;
import com.influencerportal.repository.PaymentRepository;
import com.influencerportal.repository.UserRepository;
import com.influencerportal.service.AdminService;
import com.influencerportal.service.AuthService;
import com.influencerportal.service.CampaignService;
import com.influencerportal.service.PaymentService;
import com.influencerportal.service.UserService;
import com.influencerportal.ui.Services;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

/** Wires the whole application against one SQLite file. Tests pass a file inside a JUnit @TempDir. */
class TestContext {
    final Database database;
    final UserRepository users;
    final CampaignRepository campaigns;
    final PaymentRepository payments;
    final AuthService auth;
    final UserService userService;
    final CampaignService campaignService;
    final PaymentService paymentService;
    final AdminService adminService;

    TestContext(Path databaseFile) {
        database = new Database(databaseFile);
        users = new UserRepository(database);
        campaigns = new CampaignRepository(database);
        payments = new PaymentRepository(database);
        auth = new AuthService(users);
        userService = new UserService(users, campaigns);
        campaignService = new CampaignService(users, campaigns, new RecommendationEngine());
        paymentService = new PaymentService(users, campaigns, payments);
        adminService = new AdminService(users, campaigns, payments);
    }

    Services services() {
        return new Services(auth, userService, campaignService, paymentService, adminService);
    }

    /** Test setup shortcut: overwrite a brand manager's budget. */
    void brandManagerBudgetTo(long brandManagerId, String budget) {
        com.influencerportal.model.BrandManager manager = (com.influencerportal.model.BrandManager) users.getById(brandManagerId);
        manager.changeBudget(new BigDecimal(budget));
        users.update(manager);
    }

    static final String PASSWORD = "password123";

    long influencer(String username, String niche, long followers, double engagement, String fee) {
        return auth.registerInfluencer(username, PASSWORD, username, niche, followers, engagement,
                List.of("Instagram"), new BigDecimal(fee)).getId();
    }

    long advertiser(String username, String commission) {
        return auth.registerAdvertiser(username, PASSWORD, username, "Instagram", new BigDecimal(commission)).getId();
    }

    long brandManager(String username, String budget) {
        return auth.registerBrandManager(username, PASSWORD, username, "Sports", 1000, "Instagram",
                new BigDecimal(budget)).getId();
    }
}
