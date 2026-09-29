package com.influencerportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.influencerportal.exception.AuthenticationException;
import com.influencerportal.exception.DuplicateUsernameException;
import com.influencerportal.exception.ValidationException;
import com.influencerportal.model.Role;
import com.influencerportal.model.User;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuthServiceTest {
    @TempDir
    Path dir;
    TestContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new TestContext(dir.resolve("test.db"));
    }

    @Test
    void registeredUserCanLogInAndPasswordIsStoredAsBcryptHash() {
        long id = ctx.influencer("alice", "Sports", 5000, 5.0, "100.00");

        User loggedIn = ctx.auth.login("alice", TestContext.PASSWORD);

        assertEquals(id, loggedIn.getId());
        assertEquals(Role.INFLUENCER, loggedIn.getRole());
        assertNotEquals(TestContext.PASSWORD, loggedIn.getPasswordHash());
        assertTrue(loggedIn.getPasswordHash().startsWith("$2"), "expected a BCrypt hash");
    }

    @Test
    void wrongPasswordAndUnknownUserAreRejectedWithTheSameMessage() {
        ctx.influencer("alice", "Sports", 5000, 5.0, "100.00");

        AuthenticationException wrong = assertThrows(AuthenticationException.class,
                () -> ctx.auth.login("alice", "not-the-password"));
        AuthenticationException unknown = assertThrows(AuthenticationException.class,
                () -> ctx.auth.login("nobody", TestContext.PASSWORD));

        assertEquals(wrong.getMessage(), unknown.getMessage());
    }

    @Test
    void duplicateUsernameIsRejectedEvenWithDifferentCaseOrRole() {
        ctx.influencer("alice", "Sports", 5000, 5.0, "100.00");

        assertThrows(DuplicateUsernameException.class, () -> ctx.influencer("alice", "Sports", 1, 1.0, "1.00"));
        assertThrows(DuplicateUsernameException.class, () -> ctx.advertiser("ALICE", "0.10"));
        assertEquals(1, ctx.users.countByRole().get(Role.INFLUENCER));
        assertEquals(0, ctx.users.countByRole().get(Role.ADVERTISER));
    }

    @Test
    void invalidRegistrationDataIsRejected() {
        assertThrows(ValidationException.class, () -> ctx.auth.registerInfluencer("bob", "short", "Bob", "Sports",
                10, 1.0, List.of("Instagram"), BigDecimal.ONE));
        assertThrows(ValidationException.class, () -> ctx.auth.registerInfluencer("b", TestContext.PASSWORD, "Bob",
                "Sports", 10, 1.0, List.of("Instagram"), BigDecimal.ONE));
        assertFalse(ctx.users.existsByUsername("bob"));
    }

    @Test
    void changePasswordRequiresCurrentPassword() {
        long id = ctx.influencer("alice", "Sports", 5000, 5.0, "100.00");

        assertThrows(AuthenticationException.class, () -> ctx.auth.changePassword(id, "wrong-old", "newpassword1"));
        ctx.auth.changePassword(id, TestContext.PASSWORD, "newpassword1");

        assertThrows(AuthenticationException.class, () -> ctx.auth.login("alice", TestContext.PASSWORD));
        assertEquals(id, ctx.auth.login("alice", "newpassword1").getId());
    }
}
