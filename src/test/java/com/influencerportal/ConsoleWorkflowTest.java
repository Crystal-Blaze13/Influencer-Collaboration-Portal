package com.influencerportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.influencerportal.model.Campaign;
import com.influencerportal.model.CampaignStatus;
import com.influencerportal.service.DemoDataSeeder;
import com.influencerportal.ui.ConsoleIO;
import com.influencerportal.ui.MainMenu;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Drives the real menus with scripted keyboard input, exactly like the README example workflow. */
class ConsoleWorkflowTest {
    @TempDir
    Path dir;

    private String runScript(TestContext ctx, String... lines) {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        ConsoleIO io = new ConsoleIO(new ByteArrayInputStream(String.join("\n", lines).getBytes(StandardCharsets.UTF_8)),
                new PrintStream(captured, true, StandardCharsets.UTF_8));
        new MainMenu(io, ctx.services()).run();
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    void brandManagerInfluencerWorkflowThroughTheMenus() {
        TestContext ctx = new TestContext(dir.resolve("console.db"));
        new DemoDataSeeder(ctx.database, ctx.auth, ctx.campaignService, ctx.paymentService).reset();
        int before = ctx.campaigns.findAll().size();

        // Nike (Sports, Instagram): create a campaign with the top recommendation and the cheapest advertiser.
        String brandOutput = runScript(ctx, "1", "nike", "demo1234", "4", "1", "1", "30", "0", "0");
        assertTrue(brandOutput.contains("Rule-based recommendations"));
        assertTrue(brandOutput.contains("1. Ananya Rao"));
        assertTrue(brandOutput.contains("Created campaign"));
        Campaign created = ctx.campaigns.findAll().get(before);
        assertEquals(CampaignStatus.CREATED, created.getStatus());

        // The influencer signs it, then Nike activates it and pays.
        long id = created.getId();
        String signOutput = runScript(ctx, "1", "ananya", "demo1234", "3", String.valueOf(id), "0", "0");
        assertTrue(signOutput.contains("Contract signed"));
        String payOutput = runScript(ctx, "1", "nike", "demo1234", "6", String.valueOf(id), "7", String.valueOf(id),
                "300.00", "0", "0");
        assertTrue(payOutput.contains("Paid 300.00"));
        assertEquals(CampaignStatus.ACTIVE, ctx.campaigns.getById(id).getStatus());
        assertEquals(new BigDecimal("300.00"), ctx.campaigns.getById(id).getTotalPaid());
    }

    @Test
    void invalidInputAndDomainErrorsDoNotCrashTheMenu() {
        TestContext ctx = new TestContext(dir.resolve("console2.db"));
        new DemoDataSeeder(ctx.database, ctx.auth, ctx.campaignService, ctx.paymentService).reset();

        String output = runScript(ctx, "abc", "1", "nike", "wrong-password", "1", "nike", "demo1234", "7", "999",
                "12.5", "0", "0", "0");

        assertTrue(output.contains("Please enter a whole number."));
        assertTrue(output.contains("Error: Invalid username or password."));
        assertTrue(output.contains("Error: "), "domain errors are shown, not thrown");
        assertTrue(output.contains("Goodbye."));
    }
}
