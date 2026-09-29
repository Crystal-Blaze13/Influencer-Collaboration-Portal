package Users;
//Admin class
import java.util.Arrays;
import Functions.Campaign;
import FileWriters.*;

public class Admin extends User implements Authenticate, Update{
    int totalCampaigns;
    String name;
    String totalCampaignAmount;
    Influencer[] influencers;
    BrandManager[] brandManagers;
    Advertiser[] advertisers;
    Campaign[] campaigns;

    public Admin(String username, String password, Influencer[] influencers, BrandManager[] brandManagers, Advertiser[] advertisers, Campaign[] campaigns)
    {
        super(username,password);
        this.influencers=influencers;
        this.brandManagers=brandManagers; 
        this.advertisers=advertisers;
        this.campaigns=campaigns;
    }

    
    public Admin(String username, String password)
    {
        super(username,password);
        this.name="";
        this.totalCampaignAmount="";
        this.influencers=null;
        this.brandManagers=null; 
        this.advertisers=null;
        this.campaigns=null;
    }

    public boolean check(String word_of_pass)
    {
        if(this.password.equals(word_of_pass))
        return true;
        return false;
    }
    public void updatePW(String newpassword)
    {
        super.setPassword(newpassword);
    }
    public void addCampaign(Campaign campaign)
    {
        if(campaigns==null)
        {
            campaigns=new Campaign[1];
            campaigns[0]=campaign;
            return;
        }
        Campaign[] newcampaigns=Arrays.copyOf(campaigns,campaigns.length+1);
        newcampaigns[newcampaigns.length-1]=campaign;
        campaigns=newcampaigns;
    }
    public Campaign[] getCampaigns()
    {
        return campaigns;
    }
    public void display() {
        System.out.println("\n=== Admin Overview ===");
        System.out.println("Total Influencers: " + (influencers != null ? influencers.length : 0));
        System.out.println("Total Brand Managers: " + (brandManagers != null ? brandManagers.length : 0));
        System.out.println("Total Advertisers: " + (advertisers != null ? advertisers.length : 0));
        System.out.println("Total Campaigns: " + (campaigns != null ? campaigns.length : 0));
        System.out.println("======================\n");
    }
    
    public void listUsers(User[] users)
    {
        User x=users[0];
        if(x instanceof Influencer){
            System.out.println("List of Influencers:");
            for(int i=0;i<users.length;i++)
            {
                System.out.println((i+1)+". "+((Influencer)users[i]).getName());
            }
        }
        else if(x instanceof BrandManager){
            System.out.println("List of Brand Managers:");
            for(int i=0;i<users.length;i++)
            {
                System.out.println((i+1)+". "+((BrandManager)users[i]).getName());
            }
        }
        else if (x instanceof Advertiser){
            System.out.println("List of Advertisers:");
            for(int i=0;i<users.length;i++)
            {
                System.out.println((i+1)+". "+((Advertiser)users[i]).getName());
            }
        }
        
    }
    public User[] addUser(User user)
    {
        if(user instanceof Influencer)
        {
            Influencer[] newinf=Arrays.copyOf(influencers,influencers.length+1);
            newinf[newinf.length-1]=(Influencer) user;
            influencers=newinf;
            Add add = new Add(influencers, brandManagers, advertisers);
            add.addInfluencerFile();
            return influencers;
        }
        else if(user instanceof Advertiser)
        {
            Advertiser[] newadv=Arrays.copyOf(advertisers,advertisers.length+1);
            newadv[newadv.length-1]=(Advertiser) user;
            advertisers=newadv;
            Add add = new Add(influencers, brandManagers, advertisers);
            add.addAdvertiserFile();
            return advertisers;
        }
        else if(user instanceof BrandManager)
        {
            BrandManager[] newbrand=Arrays.copyOf(brandManagers,brandManagers.length+1);
            newbrand[newbrand.length-1]=(BrandManager) user;
            brandManagers=newbrand;
            Add add = new Add(influencers, brandManagers, advertisers);
            add.addBrandManagerFile();
            return brandManagers;
        }
        return null;
    }
    public User[] addUser(User... user)
    {
        for(User u : user)
        {
            if(u instanceof Influencer)
            {
                Influencer[] newinf=Arrays.copyOf(influencers,influencers.length+1);
                newinf[newinf.length-1]=(Influencer) u;
                influencers=newinf;
                Add add = new Add(influencers, brandManagers, advertisers);
                add.addInfluencerFile();
            }
            else if(u instanceof Advertiser)
            {
                Advertiser[] newadv=Arrays.copyOf(advertisers,advertisers.length+1);
                newadv[newadv.length-1]=(Advertiser) u;
                advertisers=newadv;
                Add add = new Add(influencers, brandManagers, advertisers);
                add.addAdvertiserFile();
            }
            else if(u instanceof BrandManager)
            {
                BrandManager[] newbrand=Arrays.copyOf(brandManagers,brandManagers.length+1);
                newbrand[newbrand.length-1]=(BrandManager) u;
                brandManagers=newbrand;
                Add add = new Add(influencers, brandManagers, advertisers);
                add.addBrandManagerFile();
            }
        }
        return null;
    }
    

    public User[] removeUser(User user)
    {
        boolean found=false;
        if(user instanceof Influencer)
        {
            Influencer newarray[]=new Influencer[influencers.length-1];
            int k=0;
            for(int i=0;i<influencers.length;i++)
            {
                if(influencers[i].equals(user))
                {
                    found=true;
                    continue;
                }
                newarray[k++]=this.influencers[i];
            }
            if(found){
            influencers=newarray;
            Add add = new Add(influencers, brandManagers, advertisers);
            add.addInfluencerFile();
            }
            return influencers; 
        }
        else if(user instanceof BrandManager)
        {
            BrandManager newarray[]=new BrandManager[brandManagers.length-1];
            int k=0;
            for(int i=0;i<brandManagers.length;i++)
            {
                if(brandManagers[i].equals(user))
                {
                    found=true;
                    continue;
                }
                newarray[k++]=this.brandManagers[i];
            } 
            if(found){
            brandManagers=newarray;
            Add add = new Add(influencers, brandManagers, advertisers);
            add.addBrandManagerFile();
            }
            return brandManagers;
        }
        else if(user instanceof Advertiser)
        {
            Advertiser newarray[]=new Advertiser[advertisers.length-1];
            int k=0;
            for(int i=0;i<advertisers.length;i++)
            {
                if(advertisers[i].equals(user))
                {
                    found=true;
                    continue;
                }
                newarray[k++]=this.advertisers[i];
            } 
            if(found){
            advertisers=newarray;
            Add add = new Add(influencers, brandManagers, advertisers);
            add.addAdvertiserFile();
            }
            return advertisers;
        }
        return null;
    }

    
    public Influencer[] getInfluencers()
    {
        return influencers;
    }
    public BrandManager[] getBrandManagers()
    {
        return brandManagers;
    }
    public Advertiser[] getAdvertisers()
    {
        return advertisers;
    }
}
