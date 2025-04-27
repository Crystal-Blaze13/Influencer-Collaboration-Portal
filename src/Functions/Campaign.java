package Functions;//this class creates a campaign between an influencer/advertiser and a brand manager and manages the contract between them
import Users.*;
import java.util.*;

public class Campaign {
    private Influencer influencer;
    private BrandManager brandManager;
    private Advertiser advertiser;
    private double budget;
    private double performanceMetric;
    private boolean isActive;
    private Contract contract;
    double amount;

    public Campaign(Influencer influencer, BrandManager brandManager, Advertiser advertiser,int durationDays) {
        this.influencer = influencer;
        this.brandManager = brandManager;
        this.advertiser = advertiser;
        this.budget = brandManager.getBudget();
        this.influencer.updateCampaign(this);
        this.brandManager.updateCampaign(this);
        this.advertiser.updateCampaign(this);
        this.isActive = true;
        this.contract = new Contract(budget, durationDays);
        this.performanceMetric = calculateInitialPM();
        this.amount=this.advertiser.getNumOfContracts()*this.influencer.getEngRate()*1000;
        System.out.println("Campaign created with amount: " + amount);
    }
    public Campaign(Influencer influencer, BrandManager brandManager, Advertiser advertiser) {
        this.influencer = influencer;
        this.brandManager = brandManager;
        this.advertiser = advertiser;
        this.budget = brandManager.getBudget();
        this.influencer.updateCampaign(this);
        this.brandManager.updateCampaign(this);
        this.advertiser.updateCampaign(this);
        this.isActive = true;
        this.contract = new Contract(budget, 30); // Default duration of 30 days
        this.performanceMetric = calculateInitialPM();
    }
    public void display() {
        System.out.println("Campaign Details:");
        System.out.println("Influencer: " + influencer.getName());
        System.out.println("Brand Manager: " + brandManager.getName());
        System.out.println("Advertiser: " + advertiser.getName());
        System.out.println("Total Amount: " + amount);
        System.out.println("Performance Metrics: " + performanceMetric);
        System.out.println("Contract Signed: " + contract.isSigned());
        if (contract.isSigned()) {
            System.out.println("Contract Signed Date: " + contract.getSignedDate());
            System.out.println("Remaining Days: " + contract.getRemainingDays());
        }
    }
    private double calculateInitialPM() {
        double advertiserFactor = advertiser.getNumOfContracts() * 0.01;
        double influencerFactor = influencer.getEngRate() * influencer.getFollowers() * 0.00001;
        return advertiserFactor + influencerFactor;
    }

    public class Contract {
        private double totalAmount;
        private int durationDays;
        private boolean isSigned;
        private Date signedDate;

        public Contract(double totalAmount, int durationDays) {
            this.totalAmount = totalAmount;
            this.durationDays = durationDays;
            this.isSigned = false;
        }
        public void signContract() {
            if (!isSigned) {
                this.isSigned = true;
                this.signedDate = new Date();
                System.out.println("Contract signed on " + signedDate);
            }
        }

        public void makePayment() throws InsufficientBudgetException {//exception handling
            if (!isSigned) {
                System.out.println("Cannot make payment - contract not signed!");
                return;
            }
            if (this.totalAmount < amount) {
                throw new InsufficientBudgetException("Insufficient budget for payment");
            }
            
            else {
                this.totalAmount -= amount;
                brandManager.updateBudget(this.totalAmount);
                advertiser.updateEarnings(amount*advertiser.getCommission());
                double influencerEarnings = amount * (1 - advertiser.getCommission());
                influencer.updateEarnings(influencerEarnings);
                System.out.println("Payment of " + amount*advertiser.getCommission() + " made to "+advertiser.getName()+".");
                System.out.println("Payment of " + influencerEarnings + " made to "+influencer.getName()+".");
                System.out.println("Remaining budget: " + this.totalAmount);
            }
        }
        
        public void makePayment(double customAmount) throws InsufficientBudgetException { //Method overloading - 2
            if (!isSigned) {
                System.out.println("Cannot make payment - contract not signed!");
                return;
            }
            if (this.totalAmount < customAmount) {
                if (this.totalAmount < amount) {
                    throw new InsufficientBudgetException("Insufficient budget for payment");
            }
            this.totalAmount -= customAmount;
            brandManager.updateBudget(this.totalAmount);
            advertiser.updateEarnings(customAmount*advertiser.getCommission());
            influencer.updateEarnings(customAmount*(1-advertiser.getCommission()));
            System.out.println("Payment of " + customAmount + " made. Remaining budget: " + this.totalAmount);
            }
        }


        public void extendDuration(int additionalDays) {
            this.durationDays += additionalDays;
            System.out.println("Contract extended by " + additionalDays + " days");
        }

        // Getters
       
        public int getRemainingDays() {
             return durationDays; 
            }
        public boolean isSigned() { 
            return isSigned; 
        }
        public Date getSignedDate() { 
            return signedDate; 
        }
        public double getTotalAmount() { 
            return totalAmount; 
        }
    }

    public void updateReach(double newReach) {
        if (newReach >= 0) {
            this.performanceMetric = newReach;
        }
    }

    public void stopCampaign() {
        this.isActive = false;
        System.out.println("Campaign has been stopped");
    }

    public void changeInfluencer(Influencer newInfluencer) {
        if (newInfluencer != null) {
            this.influencer = newInfluencer;
            this.performanceMetric = calculateInitialPM();
        }
    }

    // Getters
    public double getBudget() { 
        return budget; 
    }
    public double getperformanceMetric() { 
        return performanceMetric; 
    }
    public boolean isActive() { 
        return isActive; 
    }
    public Contract getContract() { 
        return contract; 
    }
    public Influencer getInfluencer() { 
        return influencer; 
    }
    public BrandManager getBrandManager() { 
        return brandManager; 
    }
    public Advertiser getAdvertiser() { 
        return advertiser; 
    }

    
}