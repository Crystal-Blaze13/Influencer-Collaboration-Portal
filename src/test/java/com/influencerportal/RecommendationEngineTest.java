package com.influencerportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.influencerportal.model.Influencer;
import com.influencerportal.recommendation.CampaignRequirements;
import com.influencerportal.recommendation.Recommendation;
import com.influencerportal.recommendation.RecommendationEngine;
import com.influencerportal.recommendation.ScoreBreakdown;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Pure unit tests: the engine needs no database. */
class RecommendationEngineTest {
    private final RecommendationEngine engine = new RecommendationEngine();
    private final CampaignRequirements requirements =
            new CampaignRequirements("Sports", 10_000, "Instagram", new BigDecimal("1000.00"));

    private Influencer influencer(String username, String niche, long followers, double engagement,
                                  String platforms, String fee) {
        return new Influencer(0, username, username, "hash", niche, followers, engagement,
                List.of(platforms.split(",")), new BigDecimal(fee), BigDecimal.ZERO);
    }

    @Test
    void filtersByNicheMinimumFollowersAndPlatform() {
        List<Influencer> all = List.of(
                influencer("good", "Sports", 20_000, 5, "Instagram", "100.00"),
                influencer("wrongniche", "Comedy", 20_000, 5, "Instagram", "100.00"),
                influencer("tooSmall", "Sports", 9_999, 5, "Instagram", "100.00"),
                influencer("wrongplatform", "Sports", 20_000, 5, "YouTube", "100.00"),
                influencer("exactlyMin", "sports", 10_000, 5, "youtube,INSTAGRAM", "100.00"));

        List<Recommendation> ranked = engine.rank(requirements, all);

        assertEquals(List.of("exactlyMin", "good"),
                ranked.stream().map(r -> r.influencer().getUsername()).sorted().toList());
    }

    @Test
    void scoreBreakdownMatchesTheDocumentedFormula() {
        // niche exact 1.0 -> 30 | engagement 5/10 -> 15 | audience min(1, 3*10000/20000)=1 -> 20 | budget 1-250/1000 -> 15
        Influencer candidate = influencer("aaa", "Sports", 20_000, 5.0, "Instagram", "250.00");

        ScoreBreakdown breakdown = engine.score(requirements, candidate);

        assertEquals(30.0, breakdown.niche(), 1e-9);
        assertEquals(15.0, breakdown.engagement(), 1e-9);
        assertEquals(20.0, breakdown.audienceFit(), 1e-9);
        assertEquals(15.0, breakdown.budgetFit(), 1e-9);
        assertEquals(80.0, breakdown.total(), 1e-9);
    }

    @Test
    void relatedNicheEarnsHalfTheNichePoints() {
        ScoreBreakdown breakdown = engine.score(requirements, influencer("aaa", "Fitness", 20_000, 5, "Instagram", "250.00"));

        assertEquals(15.0, breakdown.niche(), 1e-9);
    }

    @Test
    void audienceFitPenalisesAudiencesMuchLargerThanNeededAndBudgetFitPenalisesExpensiveFees() {
        // followers 60000 -> min(1, 30000/60000) = 0.5 -> 10 points; fee 1000 of budget 1000 -> 0 points
        ScoreBreakdown breakdown = engine.score(requirements, influencer("aaa", "Sports", 60_000, 10, "Instagram", "1000.00"));

        assertEquals(10.0, breakdown.audienceFit(), 1e-9);
        assertEquals(0.0, breakdown.budgetFit(), 1e-9);
        assertEquals(30.0, breakdown.engagement(), 1e-9);
    }

    @Test
    void ranksByTotalScoreDescendingWithRankNumbers() {
        List<Recommendation> ranked = engine.rank(requirements, List.of(
                influencer("low", "Sports", 20_000, 1, "Instagram", "900.00"),
                influencer("high", "Sports", 20_000, 9, "Instagram", "100.00"),
                influencer("mid", "Sports", 20_000, 5, "Instagram", "500.00")));

        assertEquals(List.of("high", "mid", "low"), ranked.stream().map(r -> r.influencer().getUsername()).toList());
        assertEquals(List.of(1, 2, 3), ranked.stream().map(Recommendation::rank).toList());
        assertTrue(ranked.get(0).totalScore() > ranked.get(1).totalScore());
        assertEquals(ranked.get(0).breakdown().total(), ranked.get(0).totalScore(), 1e-9);
    }

    @Test
    void identicalInputsAlwaysGiveIdenticalRankingIncludingTies() {
        List<Influencer> candidates = new ArrayList<>(List.of(
                influencer("zoe", "Sports", 20_000, 5, "Instagram", "300.00"),
                influencer("amy", "Sports", 20_000, 5, "Instagram", "300.00"), // exact tie with zoe
                influencer("mia", "Sports", 20_000, 5, "Instagram", "300.00"),
                influencer("top", "Sports", 20_000, 8, "Instagram", "300.00")));
        List<String> expected = engine.rank(requirements, candidates).stream()
                .map(r -> r.influencer().getUsername()).toList();

        assertEquals(List.of("top", "amy", "mia", "zoe"), expected); // ties broken by username
        Random random = new Random(42);
        for (int run = 0; run < 50; run++) {
            Collections.shuffle(candidates, random);
            List<String> actual = engine.rank(requirements, candidates).stream()
                    .map(r -> r.influencer().getUsername()).toList();
            assertEquals(expected, actual);
        }
    }

    @Test
    void returnsEmptyListWhenNobodyIsEligible() {
        assertTrue(engine.rank(requirements, List.of(influencer("xxx", "Comedy", 50_000, 5, "Instagram", "10.00"))).isEmpty());
    }
}
