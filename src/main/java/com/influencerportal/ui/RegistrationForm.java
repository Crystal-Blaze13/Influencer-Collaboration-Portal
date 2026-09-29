package com.influencerportal.ui;

/** Asks for the details of a new influencer, brand manager or advertiser. Used by the main and admin menus. */
class RegistrationForm {
    private final ConsoleIO io;
    private final Services services;

    RegistrationForm(ConsoleIO io, Services services) {
        this.io = io;
        this.services = services;
    }

    void run() {
        io.println("Register as: 1. Influencer  2. Brand manager  3. Advertiser");
        int type = io.askInt("Type");
        if (type < 1 || type > 3) {
            io.println("Invalid choice.");
            return;
        }
        String username = io.ask("Username");
        String password = io.ask("Password (min 8 characters)");
        switch (type) {
            case 1 -> {
                String name = io.ask("Display name");
                String niche = io.ask("Niche (e.g. Sports)");
                long followers = io.askLong("Followers");
                double engagement = io.askDouble("Engagement rate in percent (0-100)");
                var platforms = io.askList("Platforms (comma-separated)");
                var fee = io.askMoney("Your fee per campaign");
                services.auth().registerInfluencer(username, password, name, niche, followers, engagement,
                        platforms, fee);
            }
            case 2 -> {
                String brand = io.ask("Brand name");
                String niche = io.ask("Required influencer niche");
                long minFollowers = io.askLong("Minimum followers");
                String platform = io.ask("Target platform");
                var budget = io.askMoney("Budget");
                services.auth().registerBrandManager(username, password, brand, niche, minFollowers, platform, budget);
            }
            default -> {
                String name = io.ask("Advertiser name");
                String platform = io.ask("Platform");
                var commission = io.askMoney("Commission as a fraction (e.g. 0.15 for 15%)");
                services.auth().registerAdvertiser(username, password, name, platform, commission);
            }
        }
        io.println("Registered '" + username + "' successfully.");
    }
}
