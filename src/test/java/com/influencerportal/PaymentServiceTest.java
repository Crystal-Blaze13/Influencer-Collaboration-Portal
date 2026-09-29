package com.influencerportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.influencerportal.exception.InsufficientBudgetException;
import com.influencerportal.exception.InvalidStateTransitionException;
import com.influencerportal.exception.ValidationException;
import com.influencerportal.model.Advertiser;
import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Campaign;
import com.influencerportal.model.Influencer;
import com.influencerportal.model.Payment;
import java.math.BigDecimal;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PaymentServiceTest {
    @TempDir
    Path dir;
    TestContext ctx;
    long brand;
    long influencer;
    long advertiser;
    long campaignId;

    @BeforeEach
    void setUp() {
        ctx = new TestContext(dir.resolve("test.db"));
        brand = ctx.brandManager("nike", "1000.00");
        influencer = ctx.influencer("alice", "Sports", 5000, 5.0, "800.00");
        advertiser = ctx.advertiser("adco", "0.15");
        Campaign campaign = ctx.campaignService.createCampaign(brand, influencer, advertiser, 30);
        campaignId = campaign.getId();
        ctx.campaignService.signContract(campaignId, influencer);
        ctx.campaignService.activate(campaignId, brand);
    }

    BigDecimal budget() {
        return ((BrandManager) ctx.users.getById(brand)).getBudget();
    }

    BigDecimal influencerEarnings() {
        return ((Influencer) ctx.users.getById(influencer)).getEarnings();
    }

    BigDecimal advertiserEarnings() {
        return ((Advertiser) ctx.users.getById(advertiser)).getEarnings();
    }

    @Test
    void successfulPaymentUpdatesBudgetEarningsAndHistoryExactly() {
        Payment payment = ctx.paymentService.pay(campaignId, brand, new BigDecimal("200.00"));

        assertEquals(new BigDecimal("30.00"), payment.advertiserCut());
        assertEquals(new BigDecimal("170.00"), payment.influencerCut());
        assertEquals(new BigDecimal("800.00"), budget());
        assertEquals(new BigDecimal("30.00"), advertiserEarnings());
        assertEquals(new BigDecimal("170.00"), influencerEarnings());
        assertEquals(1, ctx.paymentService.history(campaignId).size());
        assertEquals(new BigDecimal("200.00"), ctx.campaigns.getById(campaignId).getTotalPaid());
    }

    @Test
    void commissionSplitRoundsToCentsAndNeverLosesOrCreatesMoney() {
        // 0.15 * 33.33 = 4.9995 -> 5.00 ; influencer gets 28.33 ; together exactly 33.33
        Payment payment = ctx.paymentService.pay(campaignId, brand, new BigDecimal("33.33"));

        assertEquals(new BigDecimal("5.00"), payment.advertiserCut());
        assertEquals(new BigDecimal("28.33"), payment.influencerCut());
        assertEquals(payment.amount(), payment.advertiserCut().add(payment.influencerCut()));
    }

    @Test
    void paymentLargerThanTheRemainingBudgetIsRefusedAndChangesNothing() {
        ctx.brandManagerBudgetTo(brand, "100.00");

        assertThrows(InsufficientBudgetException.class,
                () -> ctx.paymentService.pay(campaignId, brand, new BigDecimal("100.01")));

        assertEquals(new BigDecimal("100.00"), budget());
        assertEquals(new BigDecimal("0.00"), advertiserEarnings());
        assertEquals(new BigDecimal("0.00"), influencerEarnings());
        assertEquals(0, ctx.paymentService.history(campaignId).size());
    }

    @Test
    void budgetCanBeSpentDownToExactlyZeroButNotBelow() {
        ctx.brandManagerBudgetTo(brand, "300.00");

        ctx.paymentService.pay(campaignId, brand, new BigDecimal("300.00"));
        assertEquals(new BigDecimal("0.00"), budget());
        assertThrows(InsufficientBudgetException.class,
                () -> ctx.paymentService.pay(campaignId, brand, new BigDecimal("0.01")));
    }

    @Test
    void invalidAmountsAreRejected() {
        assertThrows(ValidationException.class, () -> ctx.paymentService.pay(campaignId, brand, new BigDecimal("-5.00")));
        assertThrows(ValidationException.class, () -> ctx.paymentService.pay(campaignId, brand, BigDecimal.ZERO));
        assertThrows(ValidationException.class, () -> ctx.paymentService.pay(campaignId, brand, new BigDecimal("1.001")));
        // more than the contract fee of 800.00
        assertThrows(ValidationException.class, () -> ctx.paymentService.pay(campaignId, brand, new BigDecimal("800.01")));
        assertEquals(new BigDecimal("1000.00"), budget());
    }

    @Test
    void paymentsRequireAnActiveCampaign() {
        ctx.campaignService.complete(campaignId, brand);

        assertThrows(InvalidStateTransitionException.class,
                () -> ctx.paymentService.pay(campaignId, brand, new BigDecimal("10.00")));
        assertEquals(new BigDecimal("1000.00"), budget());
    }
}
