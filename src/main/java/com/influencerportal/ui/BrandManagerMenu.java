package com.influencerportal.ui;

import com.influencerportal.model.Advertiser;
import com.influencerportal.model.BrandManager;
import com.influencerportal.model.Campaign;
import com.influencerportal.model.Influencer;
import com.influencerportal.model.Payment;
import com.influencerportal.recommendation.Recommendation;
import java.util.List;

class BrandManagerMenu extends Menu {
    private final Services services;
    private final long userId;

    BrandManagerMenu(ConsoleIO io, Services services, long userId) {
        super(io);
        this.services = services;
        this.userId = userId;
    }

    @Override
    protected String title() {
        return "Brand manager menu";
    }

    @Override
    protected List<String> options() {
        return List.of("View my profile and budget", "Update campaign requirements", "Add to budget",
                "Create a campaign (recommendations)", "View my campaigns", "Activate a signed campaign",
                "Make a payment", "Complete a campaign", "Cancel a campaign", "Payment history for a campaign",
                "Change password");
    }

    @Override
    protected void handle(int choice) {
        BrandManager me = services.users().getBrandManager(userId);
        switch (choice) {
            case 1 -> me.describe().forEach(io::println);
            case 2 -> {
                me.changeRequirements(io.ask("Required niche"), io.askLong("Minimum followers"),
                        io.ask("Target platform"));
                services.users().save(me);
                io.println("Requirements updated.");
            }
            case 3 -> {
                me.topUpBudget(io.askMoney("Amount to add"));
                services.users().save(me);
                io.println("Budget is now " + me.getBudget() + ".");
            }
            case 4 -> createCampaign();
            case 5 -> {
                List<Campaign> campaigns = services.campaigns().listFor(me);
                if (campaigns.isEmpty()) {
                    io.println("You have no campaigns yet.");
                }
                campaigns.forEach(c -> io.println(c.toString()));
            }
            case 6 -> io.println("Now " + services.campaigns().activate(io.askLong("Campaign id"), userId));
            case 7 -> {
                long id = io.askLong("Campaign id");
                Payment payment = services.payments().pay(id, userId, io.askMoney("Payment amount"));
                io.println("Paid " + payment.amount() + " (advertiser commission " + payment.advertiserCut()
                        + ", influencer " + payment.influencerCut() + ").");
                io.println("Remaining budget: " + services.users().getBrandManager(userId).getBudget());
            }
            case 8 -> io.println("Now " + services.campaigns().complete(io.askLong("Campaign id"), userId));
            case 9 -> io.println("Now " + services.campaigns().cancel(io.askLong("Campaign id"), userId));
            case 10 -> {
                List<Payment> history = services.payments().history(io.askLong("Campaign id"));
                if (history.isEmpty()) {
                    io.println("No payments yet.");
                }
                history.forEach(p -> io.println("Payment #" + p.id() + ": " + p.amount() + " at " + p.paidAt()));
            }
            case 11 -> PasswordPrompt.change(io, services, userId);
            default -> io.println("Invalid choice.");
        }
    }

    private void createCampaign() {
        BrandManager me = services.users().getBrandManager(userId);
        io.println("Looking for: niche " + me.getRequiredNiche() + ", at least " + me.getMinFollowers()
                + " followers, on " + me.getTargetPlatform() + ", budget " + me.getBudget());
        List<Recommendation> ranked = services.campaigns().recommendInfluencers(userId);
        if (ranked.isEmpty()) {
            io.println("No influencer meets these requirements.");
            return;
        }
        io.println("Rule-based recommendations (score out of 100):");
        for (Recommendation r : ranked) {
            Influencer i = r.influencer();
            io.println(r.rank() + ". " + i.getDisplayName() + " - " + String.format("%.2f", r.totalScore())
                    + "  [" + r.breakdown() + "]");
            io.println("     " + i.getFollowers() + " followers, " + i.getEngagementRate() + "% engagement, fee "
                    + i.getFee());
        }
        int pick = io.askInt("Pick an influencer by rank (0 to cancel)");
        if (pick == 0) {
            return;
        }
        if (pick < 1 || pick > ranked.size()) {
            io.println("Invalid rank.");
            return;
        }
        Influencer chosen = ranked.get(pick - 1).influencer();

        List<Advertiser> advertisers = services.campaigns().advertisersForTargetPlatform(userId);
        if (advertisers.isEmpty()) {
            io.println("No advertiser publishes on " + me.getTargetPlatform() + ".");
            return;
        }
        io.println("Advertisers on " + me.getTargetPlatform() + " (lowest commission first):");
        for (int n = 0; n < advertisers.size(); n++) {
            Advertiser a = advertisers.get(n);
            io.println((n + 1) + ". " + a.getDisplayName() + " - commission " + a.getCommission().stripTrailingZeros().toPlainString());
        }
        int adv = io.askInt("Pick an advertiser (0 to cancel)");
        if (adv < 1 || adv > advertisers.size()) {
            io.println("Cancelled.");
            return;
        }
        int days = io.askInt("Contract duration in days");
        Campaign campaign = services.campaigns().createCampaign(userId, chosen.getId(),
                advertisers.get(adv - 1).getId(), days);
        io.println("Created campaign " + campaign);
    }
}
