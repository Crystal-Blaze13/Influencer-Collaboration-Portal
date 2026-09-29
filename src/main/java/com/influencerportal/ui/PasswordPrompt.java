package com.influencerportal.ui;

/** The change-password dialogue, identical for all four roles. */
final class PasswordPrompt {
    private PasswordPrompt() {
    }

    static void change(ConsoleIO io, Services services, long userId) {
        String current = io.ask("Current password");
        String next = io.ask("New password (min 8 characters)");
        services.auth().changePassword(userId, current, next);
        io.println("Password changed.");
    }
}
