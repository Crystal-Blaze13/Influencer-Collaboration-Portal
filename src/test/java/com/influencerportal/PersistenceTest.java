package com.influencerportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Campaign;
import com.influencerportal.model.CampaignStatus;
import com.influencerportal.model.Influencer;
import com.influencerportal.model.Role;
import com.influencerportal.service.DemoDataSeeder;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PersistenceTest {
    @TempDir
    Path dir;

    @Test
    void schemaIsCreatedAutomaticallyOnFirstRun() {
        Path file = dir.resolve("fresh.db");
        assertFalse(Files.exists(file));

        TestContext ctx = new TestContext(file);

        assertTrue(Files.exists(file));
        List<String> tables = ctx.database.execute(connection -> {
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
                List<String> names = new java.util.ArrayList<>();
                while (rows.next()) {
                    names.add(rows.getString(1));
                }
                return names;
            }
        });
        assertTrue(tables.containsAll(List.of("users", "influencer_profiles", "influencer_platforms",
                "brand_manager_profiles", "advertiser_profiles", "campaigns", "contracts", "payments")));
    }

    @Test
    void dataSurvivesAnApplicationRestart() {
        Path file = dir.resolve("app.db");
        long campaignId;
        {
            TestContext first = new TestContext(file);
            long brand = first.brandManager("nike", "1000.00");
            long influencer = first.influencer("alice", "Sports", 5000, 5.0, "800.00");
            long advertiser = first.advertiser("adco", "0.15");
            campaignId = first.campaignService.createCampaign(brand, influencer, advertiser, 30).getId();
            first.campaignService.signContract(campaignId, influencer);
            first.campaignService.activate(campaignId, brand);
            first.paymentService.pay(campaignId, brand, new BigDecimal("200.00"));
        } // "application exits": every object above is discarded

        TestContext second = new TestContext(file); // "application starts again" on the same file

        assertEquals(Role.BRAND_MANAGER, second.auth.login("nike", TestContext.PASSWORD).getRole());
        Influencer alice = (Influencer) second.auth.login("alice", TestContext.PASSWORD);
        assertEquals(new BigDecimal("170.00"), alice.getEarnings());
        assertEquals(List.of("Instagram"), alice.getPlatforms());
        assertEquals(new BigDecimal("800.00"), ((BrandManager) second.users.findByUsername("nike").orElseThrow()).getBudget());
        Campaign campaign = second.campaigns.getById(campaignId);
        assertEquals(CampaignStatus.ACTIVE, campaign.getStatus());
        assertTrue(campaign.getContract().isSigned());
        assertEquals(new BigDecimal("200.00"), campaign.getTotalPaid());
        assertEquals(1, second.paymentService.history(campaignId).size());
    }

    @Test
    void profileEditsAndPasswordChangesArePersisted() {
        Path file = dir.resolve("edits.db");
        TestContext first = new TestContext(file);
        long id = first.influencer("alice", "Sports", 5000, 5.0, "800.00");
        Influencer alice = first.userService.getInfluencer(id);
        alice.addPlatform("YouTube");
        alice.addFollowers(250);
        alice.changeNiche("Fitness");
        first.userService.save(alice);
        first.auth.changePassword(id, TestContext.PASSWORD, "another-pass-1");

        TestContext second = new TestContext(file);
        Influencer reloaded = (Influencer) second.auth.login("alice", "another-pass-1");
        assertEquals(List.of("Instagram", "YouTube"), reloaded.getPlatforms());
        assertEquals(5250, reloaded.getFollowers());
        assertEquals("Fitness", reloaded.getNiche());
    }

    @Test
    void seedingCreatesHashedDemoAccountsAndIsRepeatable() {
        TestContext ctx = new TestContext(dir.resolve("seed.db"));
        DemoDataSeeder seeder = new DemoDataSeeder(ctx.database, ctx.auth, ctx.campaignService, ctx.paymentService);

        seeder.reset();
        int usersAfterFirst = ctx.users.countByRole().values().stream().mapToInt(Integer::intValue).sum();
        seeder.reset();
        int usersAfterSecond = ctx.users.countByRole().values().stream().mapToInt(Integer::intValue).sum();

        assertEquals(usersAfterFirst, usersAfterSecond);
        assertEquals(Role.ADMIN, ctx.auth.login("admin", DemoDataSeeder.DEMO_PASSWORD).getRole());
        assertEquals(Role.BRAND_MANAGER, ctx.auth.login("nike", DemoDataSeeder.DEMO_PASSWORD).getRole());
        long plaintextRows = ctx.database.execute(connection -> {
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM users WHERE password_hash NOT LIKE '$2%' "
                         + "OR password_hash LIKE '%" + DemoDataSeeder.DEMO_PASSWORD + "%'")) {
                rows.next();
                return rows.getLong(1);
            }
        });
        assertEquals(0, plaintextRows);
        assertEquals(3, ctx.campaigns.findAll().size());
    }

    @Test
    void deletingAUserInACampaignIsRefusedButOthersCanBeRemoved() {
        TestContext ctx = new TestContext(dir.resolve("remove.db"));
        long brand = ctx.brandManager("nike", "1000.00");
        long influencer = ctx.influencer("alice", "Sports", 5000, 5.0, "800.00");
        long advertiser = ctx.advertiser("adco", "0.15");
        ctx.influencer("loner", "Sports", 5000, 5.0, "800.00");
        ctx.campaignService.createCampaign(brand, influencer, advertiser, 30);

        org.junit.jupiter.api.Assertions.assertThrows(com.influencerportal.exception.ValidationException.class,
                () -> ctx.userService.remove("alice"));
        ctx.userService.remove("loner");

        assertFalse(ctx.users.existsByUsername("loner"));
        assertTrue(ctx.users.existsByUsername("alice"));
    }
}
