package com.influencerportal.model;

import com.influencerportal.exception.ValidationException;
import java.util.List;

/** Base class of the four roles. The password is only ever held as a BCrypt hash. */
public abstract class User {
    private long id;
    private final String username;
    private final String displayName;
    private final String passwordHash;

    protected User(long id, String username, String displayName, String passwordHash) {
        String name = Validate.text(username, "Username");
        if (!name.matches("[A-Za-z0-9_.-]{3,30}")) {
            throw new ValidationException("Username must be 3-30 characters: letters, digits, '_', '.' or '-'.");
        }
        this.id = id;
        this.username = name;
        this.displayName = Validate.text(displayName, "Name");
        this.passwordHash = passwordHash;
    }

    public abstract Role getRole();

    /** Human-readable profile lines for the console. */
    public abstract List<String> describe();

    public long getId() {
        return id;
    }

    /** Called by the repository after the row has been inserted. */
    public void assignId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
