package Users;
import Functions.*;
//brand manager  class
public class BrandManager extends User implements Authenticate,Update{
    String brandName;
    String reqNiche;
    int minFollowers;
    Influencer[] influencers;
    Advertiser[] advertisers;
    double budget;
    Campaign campaign;

     public BrandManager(String username, String password, String brandName,String reqNiche,int minFollowers,Influencer []influencers,Advertiser [] advertisers,double budget)
    {
        super(username,password);
        this.brandName = brandName;
        this.reqNiche=reqNiche;
        this.minFollowers=minFollowers;
        this.influencers=influencers;
        this.advertisers=advertisers;
        this.budget=budget;
        this.campaign=null;
    }
    public BrandManager(String username, String password, String brandName,String reqNiche,int minFollowers,double budget)
    {
        super(username,password);
        this.brandName = brandName;
        this.reqNiche=reqNiche;
        this.minFollowers=minFollowers;
        this.budget=budget;
    }
    public void display()
    {
        System.out.println("Brand Manager");
        System.out.println("Brand Name - "+brandName);
        System.out.println("Required Niche - "+reqNiche);
        System.out.println("Minimum Followers - "+minFollowers);
        System.out.println("Budget - "+budget);
    }
    public void updatePW(String newpassword)
    {
        super.setPassword(newpassword);
    }
    @Override
    public boolean check(String word_of_pass)
    {
        if(super.getPassword().equals(word_of_pass))
        return true;
        return false;
    }

    public void updateCampaign(Campaign campaign)
    {
        this.campaign=campaign;
    }
    public Campaign getCampaign()
    {
        return campaign;
    }
    
    public void displayCampaign()
    {
        campaign.display();
    }
    
    public void createCampaign() {
        // Select the influencer based on brand manager's criteria
        Influencer selectedInfluencer = SelectionHelper.selectInfluencer(this, influencers);
        
        if (selectedInfluencer == null) {
            System.out.println("No influencer selected, campaign cannot be created.");
            return;
        }

        // Select the advertiser based on selected influencer
        Advertiser selectedAdvertiser = SelectionHelper.selectAdvertiser(selectedInfluencer, advertisers);

        if (selectedAdvertiser == null) {
            System.out.println("No advertiser selected, campaign cannot be created.");
            return;
        }

        // Proceed to create a campaign
        System.out.println("Creating Campaign with selected Influencer and Advertiser...");
        Campaign newCampaign = new Campaign(selectedInfluencer, this, selectedAdvertiser, 30); // Example: Duration = 30 days

        // Assign the created campaign to the brand manager
        updateCampaign(newCampaign);
        selectedAdvertiser.updateCampaign(newCampaign);
        selectedInfluencer.updateCampaign(newCampaign);
        // Display campaign details
        displayCampaign();
    }
    public void updateBudget(double budget)
    {
        this.budget=budget;
    }

    public String getUsername()
    {
        return username;
    }
    public String getPassword() {
        return super.getPassword();
    }
    public String getName()
    {
        return brandName;
    }
    public String getReqNiche()
    {
        return reqNiche;
    }
    public int getMinFollowers()
    {
        return minFollowers;
    }
    public double getBudget()
    {
        return budget;
    }
}

