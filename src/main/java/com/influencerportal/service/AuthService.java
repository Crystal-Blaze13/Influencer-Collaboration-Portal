package com.influencerportal.service;

import com.influencerportal.exception.AuthenticationException;
import com.influencerportal.exception.DuplicateUsernameException;
import com.influencerportal.exception.ValidationException;
import com.influencerportal.model.Admin;
import com.influencerportal.model.Advertiser;
import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Influencer;
import com.influencerportal.model.User;
import com.influencerportal.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/** Registration, login and password changes. Plaintext passwords never leave this class. */
public class AuthService {
    static final int MIN_PASSWORD_LENGTH = 8;
    private static final int BCRYPT_COST = 10;
    // Checked when the username is unknown, so a missing user takes as long as a wrong password.
    private static final String DUMMY_HASH = BCrypt.hashpw("not-a-real-password", BCrypt.gensalt(BCRYPT_COST));

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public Influencer registerInfluencer(String username, String password, String name, String niche, long followers,
                                         double engagementRate, List<String> platforms, BigDecimal fee) {
        String hash = hashPassword(password);
        return save(new Influencer(0, username, name, hash, niche, followers, engagementRate, platforms, fee,
                BigDecimal.ZERO));
    }

    public BrandManager registerBrandManager(String username, String password, String brandName,
                                             String requiredNiche, long minFollowers, String targetPlatform,
                                             BigDecimal budget) {
        String hash = hashPassword(password);
        return save(new BrandManager(0, username, brandName, hash, requiredNiche, minFollowers, targetPlatform,
                budget));
    }

    public Advertiser registerAdvertiser(String username, String password, String name, String platform,
                                         BigDecimal commission) {
        String hash = hashPassword(password);
        return save(new Advertiser(0, username, name, hash, platform, commission, BigDecimal.ZERO));
    }

    /** Admins cannot self-register in the menu; they are created by the demo seeder. */
    public Admin registerAdmin(String username, String password, String name) {
        String hash = hashPassword(password);
        return save(new Admin(0, username, name, hash));
    }

    public User login(String username, String password) {
        User user = username == null ? null : users.findByUsername(username.trim()).orElse(null);
        String hash = user == null ? DUMMY_HASH : user.getPasswordHash();
        boolean matches = password != null && BCrypt.checkpw(password, hash);
        if (user == null || !matches) {
            throw new AuthenticationException("Invalid username or password.");
        }
        return user;
    }

    public void changePassword(long userId, String currentPassword, String newPassword) {
        User user = users.getById(userId);
        if (!BCrypt.checkpw(currentPassword == null ? "" : currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is wrong.");
        }
        users.updatePasswordHash(userId, hashPassword(newPassword));
    }

    private String hashPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    private <T extends User> T save(T user) {
        if (users.existsByUsername(user.getUsername())) {
            throw new DuplicateUsernameException("Username '" + user.getUsername() + "' is already taken.");
        }
        users.insert(user);
        return user;
    }
}
