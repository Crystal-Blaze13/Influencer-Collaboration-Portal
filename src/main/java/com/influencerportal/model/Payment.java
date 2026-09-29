package com.influencerportal.model;

import java.math.BigDecimal;
import java.time.Instant;

/** One payment: {@code amount} is split into the advertiser's commission and the influencer's share. */
public record Payment(long id, long campaignId, BigDecimal amount, BigDecimal advertiserCut,
                      BigDecimal influencerCut, Instant paidAt) {
}
