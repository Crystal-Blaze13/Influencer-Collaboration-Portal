package com.influencerportal.repository;

import com.influencerportal.exception.PersistenceException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.sqlite.SQLiteConfig;

/**
 * Owns the SQLite file: opens connections, creates the schema on first use, and runs transactions.
 * Every repository call opens a short-lived connection, so nothing is shared between calls.
 */
public class Database {
    /** A piece of database work that may throw SQLException. */
    @FunctionalInterface
    public interface Work<T> {
        T run(Connection connection) throws SQLException;
    }

    private static final List<String> TABLES_IN_DROP_ORDER = List.of(
            "payments", "contracts", "campaigns", "influencer_platforms", "influencer_profiles",
            "brand_manager_profiles", "advertiser_profiles", "users");

    private static final List<String> SCHEMA = List.of(
            """
            CREATE TABLE IF NOT EXISTS users (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                username      TEXT NOT NULL UNIQUE COLLATE NOCASE,
                password_hash TEXT NOT NULL,
                role          TEXT NOT NULL CHECK (role IN ('INFLUENCER','BRAND_MANAGER','ADVERTISER','ADMIN')),
                display_name  TEXT NOT NULL
            )""",
            """
            CREATE TABLE IF NOT EXISTS influencer_profiles (
                user_id         INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                niche           TEXT NOT NULL,
                followers       INTEGER NOT NULL CHECK (followers >= 0),
                engagement_rate REAL NOT NULL CHECK (engagement_rate BETWEEN 0 AND 100),
                fee_cents       INTEGER NOT NULL CHECK (fee_cents >= 0),
                earnings_cents  INTEGER NOT NULL DEFAULT 0 CHECK (earnings_cents >= 0)
            )""",
            """
            CREATE TABLE IF NOT EXISTS influencer_platforms (
                user_id  INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                platform TEXT NOT NULL COLLATE NOCASE,
                PRIMARY KEY (user_id, platform)
            )""",
            """
            CREATE TABLE IF NOT EXISTS brand_manager_profiles (
                user_id         INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                required_niche  TEXT NOT NULL,
                min_followers   INTEGER NOT NULL CHECK (min_followers >= 0),
                target_platform TEXT NOT NULL,
                budget_cents    INTEGER NOT NULL CHECK (budget_cents >= 0)
            )""",
            """
            CREATE TABLE IF NOT EXISTS advertiser_profiles (
                user_id          INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                platform         TEXT NOT NULL,
                commission_basis_points INTEGER NOT NULL CHECK (commission_basis_points BETWEEN 0 AND 10000),
                earnings_cents   INTEGER NOT NULL DEFAULT 0 CHECK (earnings_cents >= 0)
            )""",
            """
            CREATE TABLE IF NOT EXISTS campaigns (
                id               INTEGER PRIMARY KEY AUTOINCREMENT,
                brand_manager_id INTEGER NOT NULL REFERENCES users(id),
                influencer_id    INTEGER NOT NULL REFERENCES users(id),
                advertiser_id    INTEGER NOT NULL REFERENCES users(id),
                status           TEXT NOT NULL CHECK (status IN ('CREATED','SIGNED','ACTIVE','COMPLETED','CANCELLED')),
                created_at       TEXT NOT NULL
            )""",
            """
            CREATE TABLE IF NOT EXISTS contracts (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                campaign_id   INTEGER NOT NULL UNIQUE REFERENCES campaigns(id),
                fee_cents     INTEGER NOT NULL CHECK (fee_cents >= 0),
                duration_days INTEGER NOT NULL CHECK (duration_days > 0),
                signed_at     TEXT
            )""",
            """
            CREATE TABLE IF NOT EXISTS payments (
                id                    INTEGER PRIMARY KEY AUTOINCREMENT,
                campaign_id           INTEGER NOT NULL REFERENCES campaigns(id),
                amount_cents          INTEGER NOT NULL CHECK (amount_cents > 0),
                advertiser_cut_cents  INTEGER NOT NULL CHECK (advertiser_cut_cents >= 0),
                influencer_cut_cents  INTEGER NOT NULL CHECK (influencer_cut_cents >= 0),
                paid_at               TEXT NOT NULL
            )""");

    private final String url;
    private final Path file;

    /** Opens (creating if needed) the database at {@code file} and makes sure the schema exists. */
    public Database(Path file) {
        this.file = file.toAbsolutePath();
        this.url = "jdbc:sqlite:" + this.file;
        createSchema();
    }

    public Path getFile() {
        return file;
    }

    public Connection open() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setBusyTimeout(5000);
        return DriverManager.getConnection(url, config.toProperties());
    }

    /** Runs a single read or write without a surrounding transaction. */
    public <T> T execute(Work<T> work) {
        try (Connection connection = open()) {
            return work.run(connection);
        } catch (SQLException e) {
            throw new PersistenceException("Database error: " + e.getMessage(), e);
        }
    }

    /** Runs all the work in one transaction: either every statement is committed or none is. */
    public <T> T inTransaction(Work<T> work) {
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Database error: " + e.getMessage(), e);
        }
    }

    /** Drops every table and recreates the empty schema. Used by the demo reset command. */
    public void reset() {
        execute(connection -> {
            try (Statement statement = connection.createStatement()) {
                for (String table : TABLES_IN_DROP_ORDER) {
                    statement.executeUpdate("DROP TABLE IF EXISTS " + table);
                }
            }
            return null;
        });
        createSchema();
    }

    private void createSchema() {
        execute(connection -> {
            try (Statement statement = connection.createStatement()) {
                for (String ddl : SCHEMA) {
                    statement.executeUpdate(ddl);
                }
            }
            return null;
        });
    }
}
