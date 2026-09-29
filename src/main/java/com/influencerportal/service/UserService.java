package com.influencerportal.service;

import com.influencerportal.exception.ValidationException;
import com.influencerportal.model.Advertiser;
import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Influencer;
import com.influencerportal.model.Role;
import com.influencerportal.model.User;
import com.influencerportal.repository.CampaignRepository;
import com.influencerportal.repository.UserRepository;
import java.util.List;

/** Reads users and saves profile edits. The edit rules themselves live in the domain classes. */
public class UserService {
    private final UserRepository users;
    private final CampaignRepository campaigns;

    public UserService(UserRepository users, CampaignRepository campaigns) {
        this.users = users;
        this.campaigns = campaigns;
    }

    public User get(long id) {
        return users.getById(id);
    }

    public Influencer getInfluencer(long id) {
        return (Influencer) users.getById(id);
    }

    public BrandManager getBrandManager(long id) {
        return (BrandManager) users.getById(id);
    }

    public Advertiser getAdvertiser(long id) {
        return (Advertiser) users.getById(id);
    }

    public List<User> list(Role role) {
        return users.findAll(role);
    }

    /** Persists changes made through the user's own methods, such as addPlatform or changeBudget. */
    public void save(User user) {
        users.update(user);
    }

    /** Removes a user unless they appear in a campaign, which would leave that campaign dangling. */
    public void remove(String username) {
        User user = users.findByUsername(username == null ? "" : username.trim())
                .orElseThrow(() -> new ValidationException("No user named '" + username + "'."));
        if (user.getRole() == Role.ADMIN) {
            throw new ValidationException("Admin accounts cannot be removed from the menu.");
        }
        int involved = campaigns.countInvolving(user.getId());
        if (involved > 0) {
            throw new ValidationException(
                    "Cannot remove '" + username + "': the user is part of " + involved + " campaign(s).");
        }
        users.delete(user.getId());
    }
}
