package com.influencerportal.ui;

import com.influencerportal.model.Campaign;
import com.influencerportal.model.CampaignStatus;
import com.influencerportal.model.Role;
import com.influencerportal.model.User;
import java.util.List;
import java.util.Map;

class AdminMenu extends Menu {
    private final Services services;
    private final long userId;

    AdminMenu(ConsoleIO io, Services services, long userId) {
        super(io);
        this.services = services;
        this.userId = userId;
    }

    @Override
    protected String title() {
        return "Admin menu";
    }

    @Override
    protected List<String> options() {
        return List.of("Overview", "List users by role", "Add a user", "Remove a user", "View all campaigns",
                "Dashboard", "Change password");
    }

    @Override
    protected void handle(int choice) {
        switch (choice) {
            case 1 -> overview();
            case 2 -> listUsers();
            case 3 -> new RegistrationForm(io, services).run();
            case 4 -> {
                services.users().remove(io.ask("Username to remove"));
                io.println("User removed.");
            }
            case 5 -> listCampaigns();
            case 6 -> dashboard();
            case 7 -> PasswordPrompt.change(io, services, userId);
            default -> io.println("Invalid choice.");
        }
    }

    private void overview() {
        Map<Role, Integer> users = services.admin().userCounts();
        io.println("Influencers: " + users.get(Role.INFLUENCER));
        io.println("Brand managers: " + users.get(Role.BRAND_MANAGER));
        io.println("Advertisers: " + users.get(Role.ADVERTISER));
        io.println("Admins: " + users.get(Role.ADMIN));
        io.println("Campaigns: " + services.admin().campaignCounts().values().stream().mapToInt(Integer::intValue).sum());
    }

    private void listUsers() {
        io.println("1. Influencers  2. Brand managers  3. Advertisers");
        int type = io.askInt("Type");
        Role role = switch (type) {
            case 1 -> Role.INFLUENCER;
            case 2 -> Role.BRAND_MANAGER;
            case 3 -> Role.ADVERTISER;
            default -> null;
        };
        if (role == null) {
            io.println("Invalid choice.");
            return;
        }
        for (User user : services.users().list(role)) {
            io.println("- " + user.getUsername() + " (" + user.getDisplayName() + ")");
        }
    }

    private void listCampaigns() {
        List<Campaign> campaigns = services.campaigns().listFor(services.users().get(userId));
        if (campaigns.isEmpty()) {
            io.println("No campaigns yet.");
        }
        campaigns.forEach(c -> io.println(c.toString()));
    }

    private void dashboard() {
        io.println("--- Dashboard ---");
        for (Map.Entry<CampaignStatus, Integer> entry : services.admin().campaignCounts().entrySet()) {
            io.println(String.format("%-10s %d", entry.getKey(), entry.getValue()));
        }
        io.println("Total paid across all campaigns: " + services.admin().totalPaid());
    }
}
