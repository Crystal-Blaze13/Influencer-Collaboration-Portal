package Users;
import Functions.*;
//advertiser class
public class Advertiser extends User implements Authenticate, Update{

    private String advName;
    private double commission;
    int numOfContracts; 
    String platform;
    double earnings;
    Campaign campaign;

    public Advertiser(String username, String password, String advName, double commission, int numOfContracts, String platform )
    {
        super(username, password);
        this.advName=advName;
        this.commission = commission;
        this.numOfContracts = numOfContracts;
        this.platform=platform;
        this.campaign=null;
        this.earnings=0;

    }
    public Advertiser(String username, String password)
    {
        super(username, password);
        this.advName="";
        this.commission=0;
        this.numOfContracts=0;
        this.platform="";
        this.campaign=null;
        this.earnings=0;
    }
    public void display()
    {
        System.out.println("Advertiser");
        System.out.println("Name - "+advName);
        System.out.println("Commission Rate - "+commission);
        System.out.println("Number Of Contracts - "+numOfContracts);
        System.out.println("Platform - "+platform);
    }
    public void updatePW(String newpassword)
    {
        super.setPassword(newpassword);
    }
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
    public void updatePlatform(String platform)
    {
        this.platform=platform;
    }
    public Campaign getCampaign()
    {
        return campaign;
    }
    public void displayCampaign()
    {
        campaign.display();
    }
    public void update()///Method Overloading - 1
    {
        numOfContracts++;
        commission+=(0.01*numOfContracts);
    }
    void update(int numOfContracts)
    {
        this.numOfContracts+=numOfContracts;
        commission+=(0.01*numOfContracts);
    }
    void update(int numOfContracts,double commission)
    {
        this.numOfContracts+=numOfContracts;
        this.commission=commission;
    }
    
    public void updateEarnings(double earnings)
    {
        this.earnings+=earnings;
    }
    public String getPassword() {
        return super.getPassword();
    }
    public String getUsername()
    {
        return username;
    }
    public String getName()
    {
        return advName;
    }
    public String getPlatform()
    {
        return platform;
    }
    public double getCommission()
    {
        return commission;
    }
    public int getNumOfContracts()
    {
        return numOfContracts;
    }
}