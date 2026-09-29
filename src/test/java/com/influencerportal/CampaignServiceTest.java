package com.influencerportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.influencerportal.exception.InsufficientBudgetException;
import com.influencerportal.exception.InvalidStateTransitionException;
import com.influencerportal.exception.ValidationException;
import com.influencerportal.model.Campaign;
import com.influencerportal.model.CampaignStatus;
import java.math.BigDecimal;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CampaignServiceTest {
    @TempDir
    Path dir;
    TestContext ctx;
    long brand;
    long influencer;
    long advertiser;

    @BeforeEach
    void setUp() {
        ctx = new TestContext(dir.resolve("test.db"));
        brand = ctx.brandManager("nike", "10000.00");
        influencer = ctx.influencer("alice", "Sports", 5000, 5.0, "1500.00");
        advertiser = ctx.advertiser("adco", "0.10");
    }

    private Campaign newCampaign() {
        return ctx.campaignService.createCampaign(brand, influencer, advertiser, 30);
    }

    @Test
    void createCampaignStartsInCreatedStateWithUnsignedContractAtTheInfluencersFee() {
        Campaign campaign = newCampaign();

        assertEquals(CampaignStatus.CREATED, campaign.getStatus());
        assertFalse(campaign.getContract().isSigned());
        assertEquals(new BigDecimal("1500.00"), campaign.getContract().getFee());
        assertEquals(30, campaign.getContract().getDurationDays());
        assertEquals(1, ctx.campaignService.listFor(ctx.users.getById(brand)).size());
        assertEquals(1, ctx.campaignService.listFor(ctx.users.getById(influencer)).size());
    }

    @Test
    void createCampaignRejectsBadInputs() {
        assertThrows(ValidationException.class, () -> ctx.campaignService.createCampaign(brand, influencer, advertiser, 0));
        assertThrows(ValidationException.class, () -> ctx.campaignService.createCampaign(brand, advertiser, advertiser, 5));
        long youtubeOnly = ctx.auth.registerInfluencer("ytube", TestContext.PASSWORD, "YT", "Sports", 9000, 5,
                java.util.List.of("YouTube"), new BigDecimal("10.00")).getId();
        assertThrows(ValidationException.class, () -> ctx.campaignService.createCampaign(brand, youtubeOnly, advertiser, 5));
        long expensive = ctx.influencer("pricey", "Sports", 9000, 5, "10000.01");
        assertThrows(InsufficientBudgetException.class, () -> ctx.campaignService.createCampaign(brand, expensive, advertiser, 5));
        assertEquals(0, ctx.campaigns.findAll().size());
    }

    @Test
    void contractSigningMovesCampaignToSignedAndRecordsTheDate() {
        Campaign campaign = newCampaign();

        ctx.campaignService.signContract(campaign.getId(), influencer);

        Campaign stored = ctx.campaigns.getById(campaign.getId());
        assertEquals(CampaignStatus.SIGNED, stored.getStatus());
        assertTrue(stored.getContract().isSigned());
        assertNotNull(stored.getContract().getSignedAt());
    }

    @Test
    void onlyTheCampaignsOwnInfluencerCanSign() {
        Campaign campaign = newCampaign();
        long other = ctx.influencer("bob", "Sports", 5000, 5.0, "10.00");

        assertThrows(ValidationException.class, () -> ctx.campaignService.signContract(campaign.getId(), other));
        assertEquals(CampaignStatus.CREATED, ctx.campaigns.getById(campaign.getId()).getStatus());
    }

    @Test
    void fullHappyPathCreatedSignedActiveCompleted() {
        Campaign campaign = newCampaign();
        ctx.campaignService.signContract(campaign.getId(), influencer);
        ctx.campaignService.activate(campaign.getId(), brand);
        assertEquals(CampaignStatus.ACTIVE, ctx.campaigns.getById(campaign.getId()).getStatus());
        ctx.campaignService.complete(campaign.getId(), brand);
        assertEquals(CampaignStatus.COMPLETED, ctx.campaigns.getById(campaign.getId()).getStatus());
    }

    @Test
    void invalidTransitionsAreRejectedAndLeaveTheStoredStateUntouched() {
        Campaign campaign = newCampaign();
        long id = campaign.getId();

        // CREATED cannot skip ahead
        assertThrows(InvalidStateTransitionException.class, () -> ctx.campaignService.activate(id, brand));
        assertThrows(InvalidStateTransitionException.class, () -> ctx.campaignService.complete(id, brand));

        ctx.campaignService.signContract(id, influencer);
        // cannot sign twice, cannot complete before activation
        assertThrows(InvalidStateTransitionException.class, () -> ctx.campaignService.signContract(id, influencer));
        assertThrows(InvalidStateTransitionException.class, () -> ctx.campaignService.complete(id, brand));
        assertEquals(CampaignStatus.SIGNED, ctx.campaigns.getById(id).getStatus());

        ctx.campaignService.cancel(id, brand);
        // CANCELLED is final
        assertThrows(InvalidStateTransitionException.class, () -> ctx.campaignService.activate(id, brand));
        assertThrows(InvalidStateTransitionException.class, () -> ctx.campaignService.cancel(id, brand));
        assertThrows(InvalidStateTransitionException.class, () -> ctx.campaignService.signContract(id, influencer));
        assertEquals(CampaignStatus.CANCELLED, ctx.campaigns.getById(id).getStatus());
    }

    @Test
    void completedCampaignIsFinal() {
        Campaign campaign = newCampaign();
        ctx.campaignService.signContract(campaign.getId(), influencer);
        ctx.campaignService.activate(campaign.getId(), brand);
        ctx.campaignService.complete(campaign.getId(), brand);

        assertThrows(InvalidStateTransitionException.class, () -> ctx.campaignService.cancel(campaign.getId(), brand));
        assertEquals(CampaignStatus.COMPLETED, ctx.campaigns.getById(campaign.getId()).getStatus());
    }

    @Test
    void anotherBrandManagerCannotChangeTheCampaign() {
        Campaign campaign = newCampaign();
        long rival = ctx.brandManager("puma", "500.00");

        assertThrows(ValidationException.class, () -> ctx.campaignService.cancel(campaign.getId(), rival));
    }
}
