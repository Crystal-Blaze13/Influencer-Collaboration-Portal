package com.influencerportal.recommendation;

import com.influencerportal.model.Influencer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * A rule-based recommendation engine (no machine learning): hard filters remove unsuitable influencers,
 * then a weighted formula scores the rest. It is deterministic: the same inputs always give the same
 * ranking, because scores are pure arithmetic and ties are broken by username.
 *
 * <pre>
 * Filters (all must pass):  niche exact or related  |  followers >= minimum  |  uses the target platform
 * Score (0-100) = 30 * nicheMatch + 30 * engagement + 20 * audienceFit + 20 * budgetFit
 *   nicheMatch  = 1.0 exact niche, 0.5 related niche
 *   engagement  = min(engagementRate / 10, 1)                   (10% or more counts as full marks)
 *   audienceFit = min(1, 3 * minFollowers / followers)          (penalises far larger, pricier audiences)
 *   budgetFit   = max(0, 1 - fee / budget)                      (cheaper relative to the budget is better)
 * </pre>
 */
public class RecommendationEngine {
    public static final double NICHE_WEIGHT = 30;
    public static final double ENGAGEMENT_WEIGHT = 30;
    public static final double AUDIENCE_WEIGHT = 20;
    public static final double BUDGET_WEIGHT = 20;
    public static final double FULL_MARKS_ENGAGEMENT_PERCENT = 10;
    public static final double AUDIENCE_HEADROOM = 3;

    private static final double RELATED_NICHE_SCORE = 0.5;
    private static final List<Set<String>> RELATED_NICHES = List.of(
            Set.of("sports", "fitness", "health"),
            Set.of("education", "technology"),
            Set.of("comedy", "entertainment"),
            Set.of("fashion", "beauty"),
            Set.of("food", "travel"));

    /** Returns the eligible influencers, best first. Empty if nobody passes the filters. */
    public List<Recommendation> rank(CampaignRequirements requirements, List<Influencer> candidates) {
        List<Scored> scored = new ArrayList<>();
        for (Influencer candidate : candidates) {
            if (isEligible(requirements, candidate)) {
                scored.add(new Scored(candidate, score(requirements, candidate)));
            }
        }
        scored.sort(Comparator.comparingDouble((Scored s) -> s.breakdown.total()).reversed()
                .thenComparing(s -> s.influencer.getUsername().toLowerCase()));
        List<Recommendation> result = new ArrayList<>();
        for (int i = 0; i < scored.size(); i++) {
            Scored s = scored.get(i);
            result.add(new Recommendation(i + 1, s.influencer, s.breakdown.total(), s.breakdown));
        }
        return result;
    }

    public boolean isEligible(CampaignRequirements requirements, Influencer influencer) {
        return nicheMatch(requirements.niche(), influencer.getNiche()) > 0
                && influencer.getFollowers() >= requirements.minFollowers()
                && influencer.usesPlatform(requirements.platform());
    }

    public ScoreBreakdown score(CampaignRequirements requirements, Influencer influencer) {
        double niche = NICHE_WEIGHT * nicheMatch(requirements.niche(), influencer.getNiche());
        double engagement = ENGAGEMENT_WEIGHT
                * Math.min(influencer.getEngagementRate() / FULL_MARKS_ENGAGEMENT_PERCENT, 1.0);
        double audience = AUDIENCE_WEIGHT * audienceFit(requirements.minFollowers(), influencer.getFollowers());
        double budget = BUDGET_WEIGHT * budgetFit(requirements.budget(), influencer.getFee());
        return new ScoreBreakdown(ScoreBreakdown.round2(niche), ScoreBreakdown.round2(engagement),
                ScoreBreakdown.round2(audience), ScoreBreakdown.round2(budget));
    }

    static double nicheMatch(String required, String actual) {
        String a = required.trim().toLowerCase();
        String b = actual.trim().toLowerCase();
        if (a.equals(b)) {
            return 1.0;
        }
        for (Set<String> group : RELATED_NICHES) {
            if (group.contains(a) && group.contains(b)) {
                return RELATED_NICHE_SCORE;
            }
        }
        return 0.0;
    }

    static double audienceFit(int minFollowers, int followers) {
        if (followers <= 0 || minFollowers <= 0) {
            return 1.0;
        }
        return Math.min(1.0, AUDIENCE_HEADROOM * minFollowers / followers);
    }

    static double budgetFit(BigDecimal budget, BigDecimal fee) {
        if (budget.signum() <= 0) {
            return 0.0;
        }
        double ratio = fee.divide(budget, 6, RoundingMode.HALF_UP).doubleValue();
        return Math.max(0.0, 1.0 - ratio);
    }

    private record Scored(Influencer influencer, ScoreBreakdown breakdown) {
    }
}
