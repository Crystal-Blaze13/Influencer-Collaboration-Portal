package com.influencerportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.influencerportal.exception.PersistenceException;
import com.influencerportal.model.Advertiser;
import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Influencer;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Proves the payment is atomic. A trigger in the TEST database makes the last statement of the payment
 * transaction (inserting the payment row) fail after the budget and both earnings were already updated.
 * If the transaction did not roll back, the budget and earnings would have changed.
 */
class PaymentRollbackTest {
    @TempDir
    Path dir;

    @Test
    void failureInTheLastStepRollsBackEveryEarlierStep() {
        TestContext ctx = new TestContext(dir.resolve("test.db"));
        long brand = ctx.brandManager("nike", "1000.00");
        long influencer = ctx.influencer("alice", "Sports", 5000, 5.0, "800.00");
        long advertiser = ctx.advertiser("adco", "0.15");
        long campaign = ctx.campaignService.createCampaign(brand, influencer, advertiser, 30).getId();
        ctx.campaignService.signContract(campaign, influencer);
        ctx.campaignService.activate(campaign, brand);

        ctx.database.execute(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TRIGGER fail_payment BEFORE INSERT ON payments "
                        + "BEGIN SELECT RAISE(ABORT, 'simulated failure'); END");
            }
            return null;
        });

        assertThrows(PersistenceException.class, () -> ctx.paymentService.pay(campaign, brand, new BigDecimal("200.00")));

        assertEquals(new BigDecimal("1000.00"), ((BrandManager) ctx.users.getById(brand)).getBudget());
        assertEquals(new BigDecimal("0.00"), ((Advertiser) ctx.users.getById(advertiser)).getEarnings());
        assertEquals(new BigDecimal("0.00"), ((Influencer) ctx.users.getById(influencer)).getEarnings());
        assertEquals(0, ctx.paymentService.history(campaign).size());

        // Control: with the trigger removed the very same payment succeeds, so the failure above was the trigger.
        ctx.database.execute(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP TRIGGER fail_payment");
            }
            return null;
        });
        ctx.paymentService.pay(campaign, brand, new BigDecimal("200.00"));
        assertEquals(new BigDecimal("800.00"), ((BrandManager) ctx.users.getById(brand)).getBudget());
    }
}
