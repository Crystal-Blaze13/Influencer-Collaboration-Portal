package com.influencerportal.ui;

import com.influencerportal.model.Advertiser;
import com.influencerportal.model.Campaign;
import java.util.List;

class AdvertiserMenu extends Menu {
    private final Services services;
    private final long userId;

    AdvertiserMenu(ConsoleIO io, Services services, long userId) {
        super(io);
        this.services = services;
        this.userId = userId;
    }

    @Override
    protected String title() {
        return "Advertiser menu";
    }

    @Override
    protected List<String> options() {
        return List.of("View my profile and earnings", "View my campaigns", "Change platform", "Change commission",
                "Change password");
    }

    @Override
    protected void handle(int choice) {
        Advertiser me = services.users().getAdvertiser(userId);
        switch (choice) {
            case 1 -> me.describe().forEach(io::println);
            case 2 -> {
                List<Campaign> campaigns = services.campaigns().listFor(me);
                if (campaigns.isEmpty()) {
                    io.println("You have no campaigns yet.");
                }
                campaigns.forEach(c -> io.println(c.toString()));
            }
            case 3 -> {
                me.changePlatform(io.ask("New platform"));
                services.users().save(me);
                io.println("Platform updated.");
            }
            case 4 -> {
                me.changeCommission(io.askMoney("New commission as a fraction (e.g. 0.15)"));
                services.users().save(me);
                io.println("Commission updated.");
            }
            case 5 -> PasswordPrompt.change(io, services, userId);
            default -> io.println("Invalid choice.");
        }
    }
}
