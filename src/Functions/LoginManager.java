package Functions;//this class manages the login process for all users and automatically checks the user type

import Users.*;
import java.util.Scanner;

public class LoginManager {

    private BrandManager[] brandManagers;
    private Influencer[] influencers;
    private Advertiser[] advertisers;
    private Admin admin; 

    public LoginManager(BrandManager[] brandManagers, Influencer[] influencers, Advertiser[] advertisers, Admin admin) {
        this.brandManagers = brandManagers;
        this.influencers = influencers;
        this.advertisers = advertisers;
        this.admin = admin;
    }

    public User login() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = sc.nextLine();

        
        for (BrandManager manager : brandManagers) {
            if (manager.getUsername().equals(username)) {  
                System.out.print("Enter password: ");
                String password = sc.nextLine();
                if(!manager.check(password)) {
                    System.out.println("Invalid password. Login failed.");
                    return null;
                }
                System.out.println("Logged in as Brand Manager: " + manager.getName());
                return manager;
            }
        }

        
        for (Influencer influencer : influencers) {
            if (influencer.getUsername().equals(username)) {
                System.out.print("Enter password: ");
                String password = sc.nextLine();
                if(!influencer.check(password)) {
                    System.out.println("Invalid password. Login failed.");
                    return null;
                }
                System.out.println("Logged in as Influencer: " + influencer.getName());
                return influencer;
            }
        }

       
        for (Advertiser advertiser : advertisers) {
            if (advertiser.getUsername().equals(username)) { 
                System.out.print("Enter password: ");
                String password = sc.nextLine();
                if(!advertiser.check(password)) {
                    System.out.println("Invalid password. Login failed.");
                    return null;
                }
                System.out.println("Logged in as Advertiser: " + advertiser.getName());
                return advertiser;
            }
        }

        
        if (admin != null && admin.getUsername().equals(username)) { 
            System.out.print("Enter password: ");
            String password = sc.nextLine();
            if(!admin.check(password)) {
                System.out.println("Invalid password. Login failed.");
                return null;
            }
            System.out.println("Logged in as Admin: " + admin.getUsername());
            return admin;
        }

        System.out.println("Invalid username. Login failed.");
        return null;
    }
}
