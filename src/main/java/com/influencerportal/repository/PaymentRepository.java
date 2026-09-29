package com.influencerportal.repository;

import com.influencerportal.exception.InsufficientBudgetException;
import com.influencerportal.model.Payment;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Payments touch four tables, so the whole operation is one transaction. */
public class PaymentRepository {
    private final Database database;

    public PaymentRepository(Database database) {
        this.database = database;
    }

    /**
     * Atomically: subtract the amount from the brand manager's budget, credit the advertiser and influencer,
     * and record the payment. If any step fails nothing is saved. The budget update is conditional
     * ({@code budget >= amount}), so the budget can never go negative even if two payments race.
     */
    public Payment record(long campaignId, long brandManagerId, long advertiserId, long influencerId,
                          BigDecimal amount, BigDecimal advertiserCut, BigDecimal influencerCut, Instant now) {
        return database.inTransaction(connection -> {
            long amountCents = Money.toCents(amount);
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE brand_manager_profiles SET budget_cents = budget_cents - ? "
                            + "WHERE user_id = ? AND budget_cents >= ?")) {
                statement.setLong(1, amountCents);
                statement.setLong(2, brandManagerId);
                statement.setLong(3, amountCents);
                if (statement.executeUpdate() != 1) {
                    throw new InsufficientBudgetException("Insufficient budget for a payment of " + amount + ".");
                }
            }
            credit(connection, "advertiser_profiles", advertiserId, advertiserCut);
            credit(connection, "influencer_profiles", influencerId, influencerCut);
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO payments (campaign_id, amount_cents, advertiser_cut_cents, influencer_cut_cents, "
                            + "paid_at) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, campaignId);
                statement.setLong(2, amountCents);
                statement.setLong(3, Money.toCents(advertiserCut));
                statement.setLong(4, Money.toCents(influencerCut));
                statement.setString(5, now.toString());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    return new Payment(keys.getLong(1), campaignId, amount, advertiserCut, influencerCut, now);
                }
            }
        });
    }

    public List<Payment> findByCampaign(long campaignId) {
        return database.execute(connection -> {
            List<Payment> payments = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, amount_cents, advertiser_cut_cents, influencer_cut_cents, paid_at "
                            + "FROM payments WHERE campaign_id = ? ORDER BY id")) {
                statement.setLong(1, campaignId);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        payments.add(new Payment(rows.getLong("id"), campaignId,
                                Money.fromCents(rows.getLong("amount_cents")),
                                Money.fromCents(rows.getLong("advertiser_cut_cents")),
                                Money.fromCents(rows.getLong("influencer_cut_cents")),
                                Instant.parse(rows.getString("paid_at"))));
                    }
                }
            }
            return payments;
        });
    }

    public BigDecimal totalPaid() {
        return database.execute(connection -> {
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT COALESCE(SUM(amount_cents), 0) FROM payments")) {
                rows.next();
                return Money.fromCents(rows.getLong(1));
            }
        });
    }

    // table is one of two constant names chosen in this class, never user input
    private void credit(Connection connection, String table, long userId, BigDecimal amount) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + table + " SET earnings_cents = earnings_cents + ? WHERE user_id = ?")) {
            statement.setLong(1, Money.toCents(amount));
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }
}
