package com.influencerportal.recommendation;

/** Points earned per criterion. Each value is already multiplied by its weight; total is their sum (max 100). */
public record ScoreBreakdown(double niche, double engagement, double audienceFit, double budgetFit) {
    public double total() {
        return round2(niche + engagement + audienceFit + budgetFit);
    }

    static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return String.format("niche %.2f + engagement %.2f + audience fit %.2f + budget fit %.2f",
                niche, engagement, audienceFit, budgetFit);
    }
}
