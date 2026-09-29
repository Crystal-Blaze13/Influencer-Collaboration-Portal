package com.influencerportal.recommendation;

import com.influencerportal.model.Influencer;

/** One ranked candidate with the reasons behind the score. */
public record Recommendation(int rank, Influencer influencer, double totalScore, ScoreBreakdown breakdown) {
}
