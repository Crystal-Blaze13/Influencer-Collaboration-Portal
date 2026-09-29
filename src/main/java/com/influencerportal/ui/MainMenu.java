package com.influencerportal.ui;

import com.influencerportal.exception.PortalException;
import com.influencerportal.model.User;

/** Entry menu: log in or register, then hand over to the menu for the user's role. */
public class MainMenu {
    private final ConsoleIO io;
    private final Services services;

    public MainMenu(ConsoleIO io, Services services) {
        this.io = io;
        this.services = services;
    }

    public void run() {
        io.println("Welcome to the Influencer Collaboration Portal");
        while (true) {
            io.println();
            io.println("1. Log in");
            io.println("2. Register a new account");
            io.println("0. Exit");
            int choice = io.askInt("Choice");
            try {
                switch (choice) {
                    case 0 -> {
                        io.println("Goodbye.");
                        return;
                    }
                    case 1 -> logIn();
                    case 2 -> new RegistrationForm(io, services).run();
                    default -> io.println("Invalid choice.");
                }
            } catch (PortalException e) {
                io.println("Error: " + e.getMessage());
            }
        }
    }

    private void logIn() {
        String username = io.ask("Username");
        String password = io.ask("Password");
        User user = services.auth().login(username, password);
        io.println("Logged in as " + user.getDisplayName() + " (" + user.getRole() + ").");
        Menu menu = switch (user.getRole()) {
            case INFLUENCER -> new InfluencerMenu(io, services, user.getId());
            case BRAND_MANAGER -> new BrandManagerMenu(io, services, user.getId());
            case ADVERTISER -> new AdvertiserMenu(io, services, user.getId());
            case ADMIN -> new AdminMenu(io, services, user.getId());
        };
        menu.run();
        io.println("Logged out.");
    }
}
