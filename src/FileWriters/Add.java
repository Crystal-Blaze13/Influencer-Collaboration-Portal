package FileWriters;//This class is used to write the data of all the user to their files after a new user is added or a user is removed

import java.io.FileWriter;
import java.io.IOException;

import Users.Advertiser;
import Users.BrandManager;
import Users.Influencer;

public class Add {
    private Influencer[] influencers;
    private BrandManager[] brandManagers;
    private Advertiser[] advertisers;
    public Add(Influencer[] influencers, BrandManager[] brandManagers, Advertiser[] advertisers) {
        this.influencers = influencers;
        this.brandManagers = brandManagers;
        this.advertisers = advertisers;
    }
    public void addInfluencerFile() {
        try (FileWriter fw = new FileWriter("/Users/palakkshetrapal/Documents/FinalOops/src/influencers.txt")) {
            fw.write(influencers.length + "\n");
            for (Influencer influencer : influencers) {
                fw.write(influencer.getUsername() + "\n");
                fw.write(influencer.getPassword() + "\n");
                fw.write(influencer.getName() + "\n");
                fw.write(influencer.getFollowers() + "\n");
                fw.write(influencer.getNiche() + "\n");
                String[] platforms = influencer.getPlatforms();
                for (String platform : platforms) {
                    fw.write(platform + " ");
                }
                fw.write("\n");
            }
        } catch (IOException e) {
            System.out.println("Error updating influencer file: " + e.getMessage());
        }
    }
    public void addBrandManagerFile() {
        try (FileWriter fw = new FileWriter("/Users/palakkshetrapal/Documents/FinalOops/src/brandmanagers.txt")) {
            fw.write(brandManagers.length + "\n");
            for (BrandManager brandManager : brandManagers) {
                fw.write(brandManager.getUsername() + "\n");
                fw.write(brandManager.getPassword() + "\n");
                fw.write(brandManager.getName() + "\n");
                fw.write(brandManager.getReqNiche() + "\n");
                fw.write(brandManager.getMinFollowers() + "\n");
                fw.write(brandManager.getBudget() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error updating brand manager file: " + e.getMessage());
        }
    }
    public void addAdvertiserFile() {
        try (FileWriter fw = new FileWriter("/Users/palakkshetrapal/Documents/FinalOops/src/advertisers.txt")) {
            fw.write(advertisers.length + "\n");
            for (Advertiser advertiser : advertisers) {
                fw.write(advertiser.getUsername() + "\n");
                fw.write(advertiser.getPassword() + "\n");
                fw.write(advertiser.getName() + "\n");
                fw.write(advertiser.getCommission() + "\n");
                fw.write(advertiser.getNumOfContracts() + "\n");
                fw.write(advertiser.getPlatform() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error updating advertiser file: " + e.getMessage());
        }
    }    
}
