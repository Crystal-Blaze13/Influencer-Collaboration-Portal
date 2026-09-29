package Users;
//influencer class
import java.util.Arrays;
import Functions.*;

public class Influencer extends User implements Authenticate,Update {
    private String name;
    private int followers;
    private String niche;
    private double engRate;
    private String[] platforms;
    private int noOfPlatforms;
    private double earnings;
    private Campaign campaign;

    public Influencer(String username, String password, String name, int followers, String niche, String[] platforms) {
        super(username, password );
        this.name = name;
        this.followers = followers;
        this.niche = niche;
        this.platforms = platforms;
        this.noOfPlatforms = platforms.length;
        this.engRate = ((0.4 * noOfPlatforms) + (0.6 * followers))/10000;
        this.earnings = 0;
        this.campaign = null;
    }
    public Influencer(String username, String password) {
        super(username, password);
        this.name = "";
        this.followers = 0;
        this.niche = "";
        this.platforms = new String[0];
        this.noOfPlatforms = 0;
        this.engRate = 0;
        this.earnings = 0;
        this.campaign = null;
    }

    public void display() {
        System.out.println("Influencer");
        System.out.println("Name - " + name);
        System.out.println("Followers - " + followers);
        System.out.println("Niche - " + niche);
        System.out.println("Engagement Rate - " + engRate);
        System.out.println("Platforms - ");
        for (int i = 0; i < noOfPlatforms; i++) {
            System.out.print(platforms[i] + " ");
        }
        System.out.println(); // Added newline for clean output
    }
    public void updatePW(String newPassword) {
        super.setPassword(newPassword);
    }

    @Override
    public boolean check(String wordOfPass) {
        return super.getPassword().equals(wordOfPass);
    }

    public void addPlatform(String... platform) {
        int len = platform.length;
        String[] newPlatforms = Arrays.copyOf(platforms, platforms.length + len);
        for (int i = 0; i < len; i++) {
            newPlatforms[platforms.length + i] = platform[i];
        }
        platforms = newPlatforms; 
        noOfPlatforms = platforms.length;
        updateEngRate(); 
    }
    

    public void addPlatform(String platform) {
        String[] newPlatforms = Arrays.copyOf(platforms, platforms.length + 1);
        newPlatforms[platforms.length] = platform;
        platforms = newPlatforms;
        noOfPlatforms++;
        updateEngRate();
    }

    public void updateCampaign(Campaign campaign) {
        this.campaign = campaign;
        System.out.println("Campaign updated successfully.");
    }


    public void displayCampaign() {
        if (campaign == null) {
            System.out.println("No Campaigns assigned");
            return;
        }
        campaign.display();
    }

    public void updateEarnings(double earnings) {
        this.earnings += earnings;
    }

    public void addFollowers(int add) {
        this.followers += add;
        updateEngRate(); 
    }

    public void addFollowers(int... adds) {//varag overloading - 2
        if (adds.length == 0) {
            System.out.println("No followers added.");
            return;
        }
        for (int add : adds) {
            this.followers += add;
        }
        updateEngRate();
    }

    private void updateEngRate() {
        this.engRate = (0.04 * noOfPlatforms) + (0.06 * followers);
    }
    

    public void changeNiche(String niche) {
        this.niche = niche;
    }

    public String getPassword() {
        return super.getPassword();
    }
    public Campaign getCampaign() {
        return campaign;
    }

    public String getUsername() {
        return username;
    }

    public double getEngRate() {
        return engRate;
    }

    public String getNiche() {
        return niche;
    }

    public String getName() {
        return name;
    }

    public int getFollowers() {
        return followers;
    }

    public String[] getPlatforms() {
        return platforms;
    }
    public double getEarnings() {
        return earnings;
    }
    
}
