package Functions;//this class is an admin function to present the performance of the app

import Users.*;

public class Dashboard {

    public static void showPerformance(Influencer[] influencers, Advertiser[] advertisers, Campaign[] campaigns) {
        System.out.println("---APP DASHBOARD---\n");

        System.out.println("--INFLUENCERS--");
        for (Influencer inf : influencers) {
            if (inf != null) {
                System.out.println("Name: " + inf.getName() + 
                                   " | Followers: " + inf.getFollowers() + 
                                   " | Engagement Rate: " + inf.getEngRate() + 
                                   " | In Campaign: " + (inf.getCampaign() != null ? "Yes" : "No"));
            }
        }
        
        System.out.println("\n--ADVERTISER--");
        for (Advertiser adv : advertisers) {
            if (adv != null) {
                System.out.println("Name: " + adv.getName() + 
                                   " | Platform: " + adv.getPlatform() + 
                                   " | Commission: " + adv.getCommission() + 
                                   " | In Campaign: " + (adv.getCampaign() != null ? "Yes" : "No"));
            }
        }
        if(campaigns == null)
        {
            System.out.println("NO CAMPAIGNS AVAILABLE.");
            return;
        }
        System.out.println("\n--CAMPAIGNS--");
        for (Campaign camp : campaigns) {
            if (camp != null) {
                camp.display();
            }
        }

        System.out.println("\n--- END ---\n");
    }
}
