package com.influencerportal.repository;

import com.influencerportal.exception.NotFoundException;
import com.influencerportal.model.Campaign;
import com.influencerportal.model.CampaignStatus;
import com.influencerportal.model.Contract;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** All SQL for campaigns and their contracts. */
public class CampaignRepository {
    private static final String SELECT = """
            SELECT c.id, c.brand_manager_id, c.influencer_id, c.advertiser_id, c.status, c.created_at,
                   k.id AS contract_id, k.fee_cents, k.duration_days, k.signed_at,
                   COALESCE((SELECT SUM(p.amount_cents) FROM payments p WHERE p.campaign_id = c.id), 0) AS paid_cents,
                   bm.display_name AS brand_name, inf.display_name AS influencer_name,
                   adv.display_name AS advertiser_name
            FROM campaigns c
            JOIN contracts k ON k.campaign_id = c.id
            JOIN users bm ON bm.id = c.brand_manager_id
            JOIN users inf ON inf.id = c.influencer_id
            JOIN users adv ON adv.id = c.advertiser_id
            """;

    private final Database database;

    public CampaignRepository(Database database) {
        this.database = database;
    }

    /** Inserts the campaign and its unsigned contract together, then returns the stored campaign. */
    public Campaign insert(long brandManagerId, long influencerId, long advertiserId, BigDecimal fee,
                           int durationDays, Instant now) {
        long campaignId = database.inTransaction(connection -> {
            long id;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO campaigns (brand_manager_id, influencer_id, advertiser_id, status, created_at) "
                            + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, brandManagerId);
                statement.setLong(2, influencerId);
                statement.setLong(3, advertiserId);
                statement.setString(4, CampaignStatus.CREATED.name());
                statement.setString(5, now.toString());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getLong(1);
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO contracts (campaign_id, fee_cents, duration_days) VALUES (?, ?, ?)")) {
                statement.setLong(1, id);
                statement.setLong(2, Money.toCents(fee));
                statement.setInt(3, durationDays);
                statement.executeUpdate();
            }
            return id;
        });
        return getById(campaignId);
    }

    /** Writes the campaign's status and the contract's signature date in one transaction. */
    public void save(Campaign campaign) {
        database.inTransaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE campaigns SET status = ? WHERE id = ?")) {
                statement.setString(1, campaign.getStatus().name());
                statement.setLong(2, campaign.getId());
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE contracts SET signed_at = ? WHERE campaign_id = ?")) {
                Instant signedAt = campaign.getContract().getSignedAt();
                statement.setString(1, signedAt == null ? null : signedAt.toString());
                statement.setLong(2, campaign.getId());
                statement.executeUpdate();
            }
            return null;
        });
    }

    public Optional<Campaign> findById(long id) {
        return database.execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(SELECT + " WHERE c.id = ?")) {
                statement.setLong(1, id);
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next() ? Optional.of(map(rows)) : Optional.<Campaign>empty();
                }
            }
        });
    }

    public Campaign getById(long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("No campaign with id " + id + "."));
    }

    public List<Campaign> findAll() {
        return query(" ORDER BY c.id", null);
    }

    public List<Campaign> findByBrandManager(long userId) {
        return query(" WHERE c.brand_manager_id = ? ORDER BY c.id", userId);
    }

    public List<Campaign> findByInfluencer(long userId) {
        return query(" WHERE c.influencer_id = ? ORDER BY c.id", userId);
    }

    public List<Campaign> findByAdvertiser(long userId) {
        return query(" WHERE c.advertiser_id = ? ORDER BY c.id", userId);
    }

    /** Number of campaigns a user takes part in, in any role. Used to protect users from deletion. */
    public int countInvolving(long userId) {
        return database.execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM campaigns WHERE brand_manager_id = ? OR influencer_id = ? "
                            + "OR advertiser_id = ?")) {
                statement.setLong(1, userId);
                statement.setLong(2, userId);
                statement.setLong(3, userId);
                try (ResultSet rows = statement.executeQuery()) {
                    rows.next();
                    return rows.getInt(1);
                }
            }
        });
    }

    public Map<CampaignStatus, Integer> countByStatus() {
        return database.execute(connection -> {
            Map<CampaignStatus, Integer> counts = new EnumMap<>(CampaignStatus.class);
            for (CampaignStatus status : CampaignStatus.values()) {
                counts.put(status, 0);
            }
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT status, COUNT(*) FROM campaigns GROUP BY status")) {
                while (rows.next()) {
                    counts.put(CampaignStatus.valueOf(rows.getString(1)), rows.getInt(2));
                }
            }
            return counts;
        });
    }

    private List<Campaign> query(String tail, Long userId) {
        return database.execute(connection -> {
            List<Campaign> campaigns = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(SELECT + tail)) {
                if (userId != null) {
                    statement.setLong(1, userId);
                }
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        campaigns.add(map(rows));
                    }
                }
            }
            return campaigns;
        });
    }

    private Campaign map(ResultSet row) throws SQLException {
        String signedAt = row.getString("signed_at");
        Contract contract = new Contract(row.getLong("contract_id"), row.getLong("id"),
                Money.fromCents(row.getLong("fee_cents")), row.getInt("duration_days"),
                signedAt == null ? null : Instant.parse(signedAt));
        return new Campaign(row.getLong("id"), row.getLong("brand_manager_id"), row.getLong("influencer_id"),
                row.getLong("advertiser_id"), CampaignStatus.valueOf(row.getString("status")),
                Instant.parse(row.getString("created_at")), contract, Money.fromCents(row.getLong("paid_cents")),
                row.getString("brand_name"), row.getString("influencer_name"), row.getString("advertiser_name"));
    }
}
