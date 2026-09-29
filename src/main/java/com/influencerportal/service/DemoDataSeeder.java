package com.influencerportal.service;

import com.influencerportal.model.Advertiser;
import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Campaign;
import com.influencerportal.model.Influencer;
import com.influencerportal.repository.Database;
import java.math.BigDecimal;
import java.util.List;

/**
 * Wipes the database and fills it with invented demonstration data. Everything goes through the normal
 * services, so the seeded passwords are BCrypt-hashed like any other and the seeded campaigns obey the rules.
 */
public class DemoDataSeeder {
    /** Shared password of every demonstration account (documented in the README; stored only as a hash). */
    public static final String DEMO_PASSWORD = "demo1234";
    public static final String ADMIN_USERNAME = "admin";

    private final Database database;
    private final AuthService auth;
    private final CampaignService campaigns;
    private final PaymentService payments;

    public DemoDataSeeder(Database database, AuthService auth, CampaignService campaigns, PaymentService payments) {
        this.database = database;
        this.auth = auth;
        this.campaigns = campaigns;
        this.payments = payments;
    }

    public void reset() {
        database.reset();
        auth.registerAdmin(ADMIN_USERNAME, DEMO_PASSWORD, "Portal Admin");

        Influencer ananya = influencer("ananya", "Ananya Rao", "Sports", 45_000, 6.8, "Instagram,YouTube", "1500.00");
        Influencer dev = influencer("dev", "Dev Mehta", "Sports", 120_000, 4.2, "YouTube,Instagram", "4000.00");
        influencer("meera", "Meera Iyer", "Fitness", 80_000, 7.5, "Instagram", "2500.00");
        influencer("karan", "Karan Shah", "Sports", 75_000, 3.1, "YouTube", "2000.00");
        Influencer rhea = influencer("rhea", "Rhea Kapoor", "Education", 25_000, 8.9, "YouTube", "800.00");
        influencer("yash", "Yash Verma", "Education", 150_000, 2.8, "YouTube,Facebook", "3500.00");
        influencer("aditi", "Aditi Nair", "Technology", 120_000, 5.4, "Instagram,YouTube", "3000.00");
        influencer("sana", "Sana Khan", "Comedy", 20_000, 9.6, "Instagram,X", "600.00");
        influencer("neha", "Neha Joshi", "Comedy", 35_000, 5.0, "YouTube", "900.00");
        influencer("aryan", "Aryan Gill", "Comedy", 60_000, 3.6, "Facebook,Instagram", "1200.00");
        influencer("tiny", "Tiny Creator", "Sports", 2_000, 12.0, "Instagram", "100.00");

        Advertiser insta = advertiser("instabuzz", "InstaBuzz", "Instagram", "0.10");
        advertiser("metaads", "MetaAds", "Instagram", "0.13");
        advertiser("brandone", "BrandOne", "Instagram", "0.15");
        Advertiser tube = advertiser("tubestar", "TubeStar", "YouTube", "0.18");
        advertiser("facefun", "FaceFun", "Facebook", "0.14");
        advertiser("xclusive", "XclusiveAds", "X", "0.16");

        BrandManager nike = brand("nike", "Nike", "Sports", 40_000, "Instagram", "50000.00");
        BrandManager allen = brand("allen", "Allen", "Education", 20_000, "YouTube", "5000.00");
        brand("funburst", "Funburst", "Comedy", 15_000, "Instagram", "3000.00");

        // A finished campaign, an active one with a part payment, and a fresh one.
        Campaign done = campaigns.createCampaign(nike.getId(), dev.getId(), insta.getId(), 30);
        campaigns.signContract(done.getId(), dev.getId());
        campaigns.activate(done.getId(), nike.getId());
        payments.pay(done.getId(), nike.getId(), new BigDecimal("4000.00"));
        campaigns.complete(done.getId(), nike.getId());

        Campaign active = campaigns.createCampaign(nike.getId(), ananya.getId(), insta.getId(), 45);
        campaigns.signContract(active.getId(), ananya.getId());
        campaigns.activate(active.getId(), nike.getId());
        payments.pay(active.getId(), nike.getId(), new BigDecimal("500.00"));

        campaigns.createCampaign(allen.getId(), rhea.getId(), tube.getId(), 20);
    }

    private Influencer influencer(String username, String name, String niche, long followers, double engagement,
                                  String platforms, String fee) {
        return auth.registerInfluencer(username, DEMO_PASSWORD, name, niche, followers, engagement,
                List.of(platforms.split(",")), new BigDecimal(fee));
    }

    private Advertiser advertiser(String username, String name, String platform, String commission) {
        return auth.registerAdvertiser(username, DEMO_PASSWORD, name, platform, new BigDecimal(commission));
    }

    private BrandManager brand(String username, String name, String niche, long minFollowers, String platform,
                               String budget) {
        return auth.registerBrandManager(username, DEMO_PASSWORD, name, niche, minFollowers, platform,
                new BigDecimal(budget));
    }
}
