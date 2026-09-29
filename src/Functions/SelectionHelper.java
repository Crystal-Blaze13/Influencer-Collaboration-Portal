package Functions;//this is the class for a simple AI recommendation system with a formula to select the best influencer and advertiser come up by us. 
// it also displays the list of available influencers and advertisers to the brand manager and allows them to select one of them

import Users.*;
import java.util.Scanner;

public class SelectionHelper {

    public static Influencer selectInfluencer(BrandManager manager, Influencer[] influencers) {
        String reqNiche = manager.getReqNiche();
        int minFollowers = manager.getMinFollowers();
        Influencer best = null;
        double highestEngRate = -1;
        
        // Prepare array to store matching influencers
        Influencer[] matched = new Influencer[influencers.length];
        int matchedCount = 0;

        System.out.println("Available Influencers with niche: " + reqNiche + " and minimum followers: " + minFollowers);

        for (Influencer influencer : influencers) {
            if (influencer.getNiche().equalsIgnoreCase(reqNiche) && influencer.getFollowers() >= minFollowers) {
                matched[matchedCount++] = influencer; 
                if (influencer.getEngRate() > highestEngRate) {
                    highestEngRate = influencer.getEngRate();
                    best = influencer;
                }
            }
        }

        if (matchedCount == 0) {
            System.out.println("No influencers found matching the criteria.");
            return null;
        }

       
        System.out.println("\nList of Available Influencers:");
        for (int i = 0; i < matchedCount; i++) {
            System.out.println((i + 1) + ". " + matched[i].getName());
            matched[i].display();
        }

        Scanner scanner = new Scanner(System.in);

        if (best != null) {
            System.out.println("\nRecommended Influencer: " + best.getName() + " with engagement rate: " + best.getEngRate());
            System.out.print("Would you like to go with the recommended influencer? (yes/no): ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.equals("yes")) {
                return best;
            } else {
                System.out.print("Enter the number of the influencer you want to select: ");
                int selectedIndex = scanner.nextInt();
                if (selectedIndex >= 1 && selectedIndex <= matchedCount) {
                    return matched[selectedIndex - 1];
                }
            }
        }

        System.out.println("No influencer selected.");
        return null;
    }

    public static Advertiser selectAdvertiser(Influencer influencer, Advertiser[] advertisers) {
        if (influencer == null) {
            System.out.println("No influencer selected, cannot choose advertiser.");
            return null;
        }

        Advertiser best = null;
        double minCommission = Double.MAX_VALUE;
        
        // Prepare array to store matching advertisers
        Advertiser[] matched = new Advertiser[advertisers.length];
        int matchedCount = 0;

        System.out.println("Available Advertisers for platforms: ");
        for (String platform : influencer.getPlatforms()) {
            System.out.print(platform + " ");
        }
        System.out.println();

        for (Advertiser advertiser : advertisers) {
            for (String platform : influencer.getPlatforms()) {
                if (advertiser.getPlatform().equalsIgnoreCase(platform)) {
                    matched[matchedCount++] = advertiser; // store matching advertiser
                    if (advertiser.getCommission() < minCommission) {
                        minCommission = advertiser.getCommission();
                        best = advertiser;
                    }
                    break;
                }
            }
        }

        if (matchedCount == 0) {
            System.out.println("No advertisers found matching the influencer's platforms.");
            return null;
        }

        // Display matched advertisers
        System.out.println("\nList of Available Advertisers:");
        for (int i = 0; i < matchedCount; i++) {
            System.out.println((i + 1) + ". " + matched[i].getName() + " - Platform: " + matched[i].getPlatform() + ", Commission: " + matched[i].getCommission());
            matched[i].display();
        }

        Scanner scanner = new Scanner(System.in);

        if (best != null) {
            System.out.println("\nRecommended Advertiser: " + best.getName() + " with commission: " + best.getCommission());
            System.out.print("Would you like to go with the recommended advertiser? (yes/no): ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.equals("yes")) {
                return best;
            } else {
                System.out.print("Enter the number of the advertiser you want to select: ");
                int selectedIndex = scanner.nextInt();
                if (selectedIndex >= 1 && selectedIndex <= matchedCount) {
                    return matched[selectedIndex - 1];
                }
            }
        }

        System.out.println("No advertiser selected.");
        return null;
    }
}
