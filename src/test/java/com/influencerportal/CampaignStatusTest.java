package com.influencerportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.influencerportal.model.CampaignStatus;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class CampaignStatusTest {
    @Test
    void transitionTableMatchesTheDocumentedLifeCycle() {
        assertEquals(EnumSet.of(CampaignStatus.SIGNED, CampaignStatus.CANCELLED), CampaignStatus.CREATED.allowedNext());
        assertEquals(EnumSet.of(CampaignStatus.ACTIVE, CampaignStatus.CANCELLED), CampaignStatus.SIGNED.allowedNext());
        assertEquals(EnumSet.of(CampaignStatus.COMPLETED, CampaignStatus.CANCELLED), CampaignStatus.ACTIVE.allowedNext());
        assertTrue(CampaignStatus.COMPLETED.allowedNext().isEmpty());
        assertTrue(CampaignStatus.CANCELLED.allowedNext().isEmpty());
    }

    @Test
    void noStateMayMoveBackwardsOrToItself() {
        for (CampaignStatus from : CampaignStatus.values()) {
            assertFalse(from.canMoveTo(from), from + " -> itself");
        }
        assertFalse(CampaignStatus.ACTIVE.canMoveTo(CampaignStatus.CREATED));
        assertFalse(CampaignStatus.SIGNED.canMoveTo(CampaignStatus.CREATED));
    }
}
