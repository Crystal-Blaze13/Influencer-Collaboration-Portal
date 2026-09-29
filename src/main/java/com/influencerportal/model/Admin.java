package com.influencerportal.model;

import java.util.List;

public class Admin extends User {
    public Admin(long id, String username, String displayName, String passwordHash) {
        super(id, username, displayName, passwordHash);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public List<String> describe() {
        return List.of("Admin: " + getDisplayName());
    }
}
