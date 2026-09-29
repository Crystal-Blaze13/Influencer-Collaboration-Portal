package com.influencerportal.ui;

import com.influencerportal.model.Campaign;
import com.influencerportal.model.Influencer;
import java.util.List;

class InfluencerMenu extends Menu {
    private final Services services;
    private final long userId;

    InfluencerMenu(ConsoleIO io, Services services, long userId) {
        super(io);
        this.services = services;
        this.userId = userId;
    }

    @Override
    protected String title() {
        return "Influencer menu";
    }

    @Override
    protected List<String> options() {
        return List.of("View my profile and earnings", "View my campaigns", "Sign a contract", "Add a platform",
                "Add followers", "Change niche", "Change my fee", "Change password");
    }

    @Override
    protected void handle(int choice) {
        Influencer me = services.users().getInfluencer(userId);
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
                long id = io.askLong("Campaign id to sign");
                Campaign signed = services.campaigns().signContract(id, userId);
                io.println("Contract signed. " + signed);
            }
            case 4 -> {
                me.addPlatform(io.ask("Platform to add"));
                services.users().save(me);
                io.println("Platform added.");
            }
            case 5 -> {
                me.addFollowers(io.askLong("Followers to add"));
                services.users().save(me);
                io.println("Followers are now " + me.getFollowers() + ".");
            }
            case 6 -> {
                me.changeNiche(io.ask("New niche"));
                services.users().save(me);
                io.println("Niche updated.");
            }
            case 7 -> {
                me.changeFee(io.askMoney("New fee per campaign"));
                services.users().save(me);
                io.println("Fee updated to " + me.getFee() + ".");
            }
            case 8 -> PasswordPrompt.change(io, services, userId);
            default -> io.println("Invalid choice.");
        }
    }
}
