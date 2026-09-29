package com.influencerportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.influencerportal.exception.ValidationException;
import com.influencerportal.model.Advertiser;
import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Influencer;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainValidationTest {
    private Influencer validInfluencer() {
        return new Influencer(0, "alice", "Alice", "hash", "Sports", 1000, 5.0, List.of("Instagram"),
                new BigDecimal("10.00"), BigDecimal.ZERO);
    }

    @Test
    void negativeValuesAreRejected() {
        assertThrows(ValidationException.class, () -> new Influencer(0, "alice", "Alice", "h", "Sports", -1, 5.0,
                List.of("Instagram"), BigDecimal.TEN, BigDecimal.ZERO));
        assertThrows(ValidationException.class, () -> new Influencer(0, "alice", "Alice", "h", "Sports", 10, 5.0,
                List.of("Instagram"), new BigDecimal("-0.01"), BigDecimal.ZERO));
        assertThrows(ValidationException.class, () -> new BrandManager(0, "nike", "Nike", "h", "Sports", 10,
                "Instagram", new BigDecimal("-5")));
        assertThrows(ValidationException.class, () -> validInfluencer().addFollowers(-5));
    }

    @Test
    void outOfRangeValuesAreRejected() {
        assertThrows(ValidationException.class, () -> new Influencer(0, "alice", "Alice", "h", "Sports", 10, 100.5,
                List.of("Instagram"), BigDecimal.TEN, BigDecimal.ZERO));
        assertThrows(ValidationException.class, () -> new Advertiser(0, "adco", "AdCo", "h", "Instagram",
                new BigDecimal("1.5"), BigDecimal.ZERO));
        assertThrows(ValidationException.class, () -> new Influencer(0, "alice", "Alice", "h", "Sports", 10, 5.0,
                List.of(), BigDecimal.TEN, BigDecimal.ZERO));
    }

    @Test
    void moneyWithMoreThanTwoDecimalsIsRejectedRatherThanSilentlyRounded() {
        assertThrows(ValidationException.class, () -> validInfluencer().changeFee(new BigDecimal("1.005")));
        BigDecimal ok = new BigDecimal("1.50");
        Influencer influencer = validInfluencer();
        influencer.changeFee(ok);
        assertEquals(new BigDecimal("1.50"), influencer.getFee());
    }

    @Test
    void budgetTopUpMustBePositive() {
        BrandManager manager = new BrandManager(0, "nike", "Nike", "h", "Sports", 10, "Instagram", new BigDecimal("100"));
        assertThrows(ValidationException.class, () -> manager.topUpBudget(BigDecimal.ZERO));
        manager.topUpBudget(new BigDecimal("50.25"));
        assertEquals(new BigDecimal("150.25"), manager.getBudget());
    }

    @Test
    void platformsAreDeduplicatedIgnoringCase() {
        Influencer influencer = validInfluencer();
        influencer.addPlatform("instagram");
        influencer.addPlatform("YouTube");
        assertEquals(List.of("Instagram", "YouTube"), influencer.getPlatforms());
    }
}
