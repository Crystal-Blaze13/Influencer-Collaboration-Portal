package com.influencerportal.repository;

import com.influencerportal.exception.DuplicateUsernameException;
import com.influencerportal.exception.NotFoundException;
import com.influencerportal.model.Admin;
import com.influencerportal.model.Advertiser;
import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Influencer;
import com.influencerportal.model.Role;
import com.influencerportal.model.User;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** All SQL for users and their role-specific profile tables. */
public class UserRepository {
    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    /** Inserts the user and its profile in one transaction and stores the generated id on the object. */
    public void insert(User user) {
        try {
            database.inTransaction(connection -> {
                long id;
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO users (username, password_hash, role, display_name) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, user.getUsername());
                    statement.setString(2, user.getPasswordHash());
                    statement.setString(3, user.getRole().name());
                    statement.setString(4, user.getDisplayName());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getLong(1);
                    }
                }
                user.assignId(id);
                insertProfile(connection, user);
                return null;
            });
        } catch (com.influencerportal.exception.PersistenceException e) {
            if (e.getCause() != null && String.valueOf(e.getCause().getMessage()).contains("users.username")) {
                throw new DuplicateUsernameException("Username '" + user.getUsername() + "' is already taken.");
            }
            throw e;
        }
    }

    public boolean existsByUsername(String username) {
        return database.execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 FROM users WHERE username = ?")) {
                statement.setString(1, username);
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next();
                }
            }
        });
    }

    public Optional<User> findByUsername(String username) {
        return database.execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM users WHERE username = ?")) {
                statement.setString(1, username);
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next() ? load(connection, rows.getLong(1)) : Optional.<User>empty();
                }
            }
        });
    }

    public Optional<User> findById(long id) {
        return database.execute(connection -> load(connection, id));
    }

    public User getById(long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("No user with id " + id + "."));
    }

    public List<User> findAll(Role role) {
        return database.execute(connection -> {
            List<Long> ids = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM users WHERE role = ? ORDER BY username")) {
                statement.setString(1, role.name());
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        ids.add(rows.getLong(1));
                    }
                }
            }
            List<User> users = new ArrayList<>();
            for (long id : ids) {
                load(connection, id).ifPresent(users::add);
            }
            return users;
        });
    }

    /**
     * Saves the editable profile fields of an existing user. Earnings and budget spending are deliberately
     * not written here; they change only inside {@link PaymentRepository}'s transaction.
     */
    public void update(User user) {
        database.inTransaction(connection -> {
            switch (user.getRole()) {
                case INFLUENCER -> updateInfluencer(connection, (Influencer) user);
                case BRAND_MANAGER -> updateBrandManager(connection, (BrandManager) user);
                case ADVERTISER -> updateAdvertiser(connection, (Advertiser) user);
                case ADMIN -> { }
            }
            return null;
        });
    }

    public void updatePasswordHash(long userId, String newHash) {
        database.execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET password_hash = ? WHERE id = ?")) {
                statement.setString(1, newHash);
                statement.setLong(2, userId);
                statement.executeUpdate();
            }
            return null;
        });
    }

    public void delete(long userId) {
        database.execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
                statement.setLong(1, userId);
                statement.executeUpdate();
            }
            return null;
        });
    }

    public Map<Role, Integer> countByRole() {
        return database.execute(connection -> {
            Map<Role, Integer> counts = new EnumMap<>(Role.class);
            for (Role role : Role.values()) {
                counts.put(role, 0);
            }
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT role, COUNT(*) FROM users GROUP BY role")) {
                while (rows.next()) {
                    counts.put(Role.valueOf(rows.getString(1)), rows.getInt(2));
                }
            }
            return counts;
        });
    }

    // ---- inserts --------------------------------------------------------------------------------------

    private void insertProfile(Connection connection, User user) throws SQLException {
        switch (user.getRole()) {
            case INFLUENCER -> {
                Influencer influencer = (Influencer) user;
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO influencer_profiles (user_id, niche, followers, engagement_rate, fee_cents, "
                                + "earnings_cents) VALUES (?, ?, ?, ?, ?, ?)")) {
                    statement.setLong(1, influencer.getId());
                    statement.setString(2, influencer.getNiche());
                    statement.setInt(3, influencer.getFollowers());
                    statement.setDouble(4, influencer.getEngagementRate());
                    statement.setLong(5, Money.toCents(influencer.getFee()));
                    statement.setLong(6, Money.toCents(influencer.getEarnings()));
                    statement.executeUpdate();
                }
                replacePlatforms(connection, influencer);
            }
            case BRAND_MANAGER -> {
                BrandManager manager = (BrandManager) user;
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO brand_manager_profiles (user_id, required_niche, min_followers, "
                                + "target_platform, budget_cents) VALUES (?, ?, ?, ?, ?)")) {
                    statement.setLong(1, manager.getId());
                    statement.setString(2, manager.getRequiredNiche());
                    statement.setInt(3, manager.getMinFollowers());
                    statement.setString(4, manager.getTargetPlatform());
                    statement.setLong(5, Money.toCents(manager.getBudget()));
                    statement.executeUpdate();
                }
            }
            case ADVERTISER -> {
                Advertiser advertiser = (Advertiser) user;
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO advertiser_profiles (user_id, platform, commission_basis_points, "
                                + "earnings_cents) VALUES (?, ?, ?, ?)")) {
                    statement.setLong(1, advertiser.getId());
                    statement.setString(2, advertiser.getPlatform());
                    statement.setInt(3, advertiser.getCommission().movePointRight(4).intValueExact());
                    statement.setLong(4, Money.toCents(advertiser.getEarnings()));
                    statement.executeUpdate();
                }
            }
            case ADMIN -> { }
        }
    }

    private void replacePlatforms(Connection connection, Influencer influencer) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM influencer_platforms WHERE user_id = ?")) {
            delete.setLong(1, influencer.getId());
            delete.executeUpdate();
        }
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO influencer_platforms (user_id, platform) VALUES (?, ?)")) {
            for (String platform : influencer.getPlatforms()) {
                insert.setLong(1, influencer.getId());
                insert.setString(2, platform);
                insert.executeUpdate();
            }
        }
    }

    // ---- updates --------------------------------------------------------------------------------------

    private void updateInfluencer(Connection connection, Influencer influencer) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE influencer_profiles SET niche = ?, followers = ?, fee_cents = ? WHERE user_id = ?")) {
            statement.setString(1, influencer.getNiche());
            statement.setInt(2, influencer.getFollowers());
            statement.setLong(3, Money.toCents(influencer.getFee()));
            statement.setLong(4, influencer.getId());
            statement.executeUpdate();
        }
        replacePlatforms(connection, influencer);
    }

    private void updateBrandManager(Connection connection, BrandManager manager) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE brand_manager_profiles SET required_niche = ?, min_followers = ?, target_platform = ?, "
                        + "budget_cents = ? WHERE user_id = ?")) {
            statement.setString(1, manager.getRequiredNiche());
            statement.setInt(2, manager.getMinFollowers());
            statement.setString(3, manager.getTargetPlatform());
            statement.setLong(4, Money.toCents(manager.getBudget()));
            statement.setLong(5, manager.getId());
            statement.executeUpdate();
        }
    }

    private void updateAdvertiser(Connection connection, Advertiser advertiser) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE advertiser_profiles SET platform = ?, commission_basis_points = ? WHERE user_id = ?")) {
            statement.setString(1, advertiser.getPlatform());
            statement.setInt(2, advertiser.getCommission().movePointRight(4).intValueExact());
            statement.setLong(3, advertiser.getId());
            statement.executeUpdate();
        }
    }

    // ---- loading --------------------------------------------------------------------------------------

    private Optional<User> load(Connection connection, long id) throws SQLException {
        String username;
        String hash;
        String displayName;
        Role role;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT username, password_hash, role, display_name FROM users WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) {
                    return Optional.empty();
                }
                username = row.getString("username");
                hash = row.getString("password_hash");
                role = Role.valueOf(row.getString("role"));
                displayName = row.getString("display_name");
            }
        }
        return Optional.of(switch (role) {
            case INFLUENCER -> loadInfluencer(connection, id, username, displayName, hash);
            case BRAND_MANAGER -> loadBrandManager(connection, id, username, displayName, hash);
            case ADVERTISER -> loadAdvertiser(connection, id, username, displayName, hash);
            case ADMIN -> new Admin(id, username, displayName, hash);
        });
    }

    private Influencer loadInfluencer(Connection connection, long id, String username, String displayName,
                                      String hash) throws SQLException {
        List<String> platforms = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT platform FROM influencer_platforms WHERE user_id = ? ORDER BY platform")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    platforms.add(rows.getString(1));
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT niche, followers, engagement_rate, fee_cents, earnings_cents "
                        + "FROM influencer_profiles WHERE user_id = ?")) {
            statement.setLong(1, id);
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                return new Influencer(id, username, displayName, hash, row.getString("niche"),
                        row.getLong("followers"), row.getDouble("engagement_rate"), platforms,
                        Money.fromCents(row.getLong("fee_cents")), Money.fromCents(row.getLong("earnings_cents")));
            }
        }
    }

    private BrandManager loadBrandManager(Connection connection, long id, String username, String displayName,
                                          String hash) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT required_niche, min_followers, target_platform, budget_cents "
                        + "FROM brand_manager_profiles WHERE user_id = ?")) {
            statement.setLong(1, id);
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                return new BrandManager(id, username, displayName, hash, row.getString("required_niche"),
                        row.getLong("min_followers"), row.getString("target_platform"),
                        Money.fromCents(row.getLong("budget_cents")));
            }
        }
    }

    private Advertiser loadAdvertiser(Connection connection, long id, String username, String displayName,
                                      String hash) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT platform, commission_basis_points, earnings_cents FROM advertiser_profiles "
                        + "WHERE user_id = ?")) {
            statement.setLong(1, id);
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                return new Advertiser(id, username, displayName, hash, row.getString("platform"),
                        BigDecimal.valueOf(row.getLong("commission_basis_points"), 4),
                        Money.fromCents(row.getLong("earnings_cents")));
            }
        }
    }
}
