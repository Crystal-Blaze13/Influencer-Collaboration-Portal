package Functions;//THIS IS THE MAIN CLASS. IT RUNS THE ENTIRE PROGRAM AND BRINGS ALL THE COMPONENTS TOGETHER

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import FileReaders.AdvertiserReader;
import FileReaders.BrandManagerReader;
import FileReaders.InfluencerReader;
import Users.*;

public class Platform {
    static Admin admin;    
    static Influencer[] influencers;
    static Advertiser[] advertisers;
    static BrandManager[] brandManagers;
    static{
        InfluencerReader influencerReader = new InfluencerReader();
        AdvertiserReader advertiserReader = new AdvertiserReader();
        BrandManagerReader brandManagerReader = new BrandManagerReader();
        influencers = influencerReader.read();
        advertisers = advertiserReader.read();
        brandManagers = brandManagerReader.read();
        try {
            FileReader fileReader = new FileReader("/Users/palakkshetrapal/Documents/FinalOops/src/admin.txt");
            BufferedReader br = new BufferedReader(fileReader);
            admin = new Admin(br.readLine(), br.readLine(), influencers, brandManagers, advertisers, null);
            br.close();
        } catch (IOException e) {
            System.out.println("Error " + e.getMessage());
        }
    }
    public static void main(String[] args) throws InsufficientBudgetException {

        System.out.println("Welcome to the Social Media Influencer Collaboration and Sponsorship  Platform!");
        System.out.println("1. Existing User");
        System.out.println("2. New User");
        System.out.print("Enter your choice: ");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        scanner.nextLine(); 
        if (choice == 2) {
            System.out.println("===New User Login===");
            System.out.println("1. Influencer");
            System.out.println("2. Advertiser");
            System.out.println("3. Brand Manager");
            System.out.print("Choose the type of user: ");
            int choice1 = scanner.nextInt();
            scanner.nextLine();
            if (choice1 == 1) {
                System.out.print("Enter username: ");
                String username = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                System.out.print("Enter name: ");
                String name = scanner.nextLine();
                System.out.print("Enter number of followers: ");
                int followers = scanner.nextInt();
                scanner.nextLine(); 
                System.out.print("Enter niche: ");
                String niche = scanner.nextLine();
                System.out.print("Enter platforms (comma-separated): ");
                String platformsInput = scanner.nextLine();
                String[] platforms = platformsInput.split(","); // Split by commas
                Influencer newInfluencer = new Influencer(username, password, name, followers, niche, platforms);
                admin.addUser(newInfluencer);
                influencers=admin.getInfluencers();
                System.out.println("Influencer added successfully.");
            } else if (choice1 == 2) {
                System.out.print("Enter username: ");
                String username = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                System.out.print("Enter advertiser name: ");
                String advName = scanner.nextLine();
                System.out.print("Enter commission rate: ");
                double commission = scanner.nextDouble();
                System.out.print("Enter number of contracts: ");
                int numOfContracts = scanner.nextInt();
                scanner.nextLine(); 
                System.out.print("Enter platform: ");
                String platform = scanner.nextLine();
                Advertiser newAdvertiser = new Advertiser(username, password, advName, commission, numOfContracts, platform);
                admin.addUser(newAdvertiser);
                advertisers=admin.getAdvertisers();
                System.out.println("Advertiser added successfully.");
            } else if (choice1 == 3) {
                System.out.print("Enter username: ");
                String username  =scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                System.out.print("Enter brand name: ");
                String brandName = scanner.nextLine();
                System.out.print("Enter required niche: ");
                String reqNiche = scanner.nextLine();
                System.out.print("Enter minimum followers: ");
                int minFollowers = scanner.nextInt();
                System.out.print("Enter budget: ");
                double budget = scanner.nextDouble();
                scanner.nextLine(); 
                BrandManager newBrandManager = new BrandManager(username, password, brandName, reqNiche, minFollowers, budget);
                admin.addUser(newBrandManager);
                brandManagers=admin.getBrandManagers();
                System.out.println("Brand Manager added successfully.");
            } else {
                System.out.println("Invalid choice.");
            }
        }
        
        LoginManager loginManager = new LoginManager(brandManagers, influencers, advertisers, admin);
        
        User loggedInUser = null;
        while (loggedInUser == null) {
            loggedInUser = loginManager.login();

            if (loggedInUser != null) {
                if (loggedInUser instanceof BrandManager) {
                    showBrandManagerMenu((BrandManager) loggedInUser);
                    loggedInUser = null; 
                } else if (loggedInUser instanceof Influencer) {    
                    showInfluencerMenu((Influencer) loggedInUser);
                    loggedInUser = null; 
                } else if (loggedInUser instanceof Advertiser) {
                    showAdvertiserMenu((Advertiser) loggedInUser);
                    loggedInUser = null;
                } else if (loggedInUser instanceof Admin) {
                    showAdminMenu();
                    loggedInUser = null; 
                } else {
                    System.out.println("Invalid user type.");
                }
            } else {
                System.out.println("Invalid login credentials, please try again.");
            }
        }
    }
    public static void showAdvertiserMenu(Advertiser advertiser) {
        Scanner sc = new Scanner(System.in);
        int choice = -1;
    
        while (choice != 0) {
            System.out.println("\n===== Advertiser Menu =====");
            System.out.println("1. Display Details");
            System.out.println("2. Update Password");
            System.out.println("3. Update Platform");
            System.out.println("4. Display Campaign");
            System.out.println("5. Update Earnings");
            System.out.println("6. View Number of Contracts and Commission");
            System.out.println("7. Logout");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine(); 
    
            switch (choice) {
                case 1:
                    advertiser.display();
                    break;
                case 2:
                    System.out.print("Enter new password: ");
                    String newPw = sc.nextLine();
                    advertiser.updatePW(newPw);
                    System.out.println("Password updated successfully.");
                    break;
                case 3:
                    System.out.print("Enter new platform: ");
                    String platform = sc.nextLine();
                    advertiser.updatePlatform(platform);
                    System.out.println("Platform updated successfully.");
                    break;
                case 4:
                    if (advertiser.getCampaign() == null) {
                        System.out.println("No Campaign Assigned Yet.");
                    } else {
                        advertiser.displayCampaign();
                    }
                    break;
                case 5:
                    System.out.print("Enter earnings to add: ");
                    double earn = sc.nextDouble();
                    advertiser.updateEarnings(earn);
                    System.out.println("Earnings updated.");
                    break;
                case 6:
                    System.out.println("Number of Contracts: " + advertiser.getNumOfContracts());
                    System.out.println("Current Commission Rate: " + advertiser.getCommission());
                    break;
                case 7:
                    System.out.println("Logging out...");
                    break;
                case 8:
                    System.out.println("Exiting the application...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    public static void showInfluencerMenu(Influencer influencer) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
    
        while (!exit) {
            System.out.println("\n=== Influencer Menu ===");
            System.out.println("1. View Stats");
            System.out.println("2. View Campaign");
            System.out.println("3. Update Password");
            System.out.println("4. Add Platform");
            System.out.println("5. Add Followers");
            System.out.println("6. Change Niche");
            System.out.println("7. View Earnings");
            System.out.println("8. Sign Contract");
            System.out.println("9. Logout");
            System.out.println("10. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); 
    
            switch (choice) {
                case 1:
                    
                    influencer.display();
                    break;
                case 2:
                    
                    influencer.displayCampaign();
                    break;
                case 3:
                    {
                    System.out.print("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    influencer.updatePW(newPassword);
                    System.out.println("Password updated successfully.");
                    }
                    break;
                case 4:
                    {
                    System.out.print("Enter new platform: ");
                    String platform = scanner.nextLine();
                    influencer.addPlatform(platform);
                    System.out.println("Platform added successfully.");
                    }
                    break;
                case 5:
                    {
                    System.out.print("Enter number of followers to add: ");
                    int addFollowers = scanner.nextInt();
                    influencer.addFollowers(addFollowers);
                    System.out.println("Followers added successfully.");
                    }
                    break;
                case 6:
                   {
                    System.out.print("Enter new niche: ");
                    String niche = scanner.nextLine();
                    influencer.changeNiche(niche);
                    System.out.println("Niche updated successfully.");
                    break;
                   }
                case 7:
                    System.out.println("Current earnings: " + influencer.getEarnings());
                    break;
                case 8:{
                    System.out.println("Signing contract...");
                    Campaign campaign = influencer.getCampaign();
                    if (campaign != null) {
                        campaign.getContract().signContract();
                        System.out.println("Contract signed successfully.");
                    } else {
                        System.out.println("No campaign assigned.");
                    }
                }
                    break;
                case 9:{
                    exit = true;
                    System.out.println("Logging out...");
                    return;
                }
                case 10:
                    {
                    System.out.println("Exiting the application...");
                    System.exit(0);
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }
    
    public static void showBrandManagerMenu(BrandManager brandManager) throws InsufficientBudgetException {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n=== Brand Manager Menu ===");
            System.out.println("1. View Stats");
            System.out.println("2. Create Campaign");
            System.out.println("3. View Campaign");
            System.out.println("4. Update Password");
            System.out.println("5. Update Budget");
            System.out.println("6. Make Payment");
            System.out.println("7. Stop Campaign");
            System.out.println("8. Change Campaign Influencer");
            System.out.println("9. Logout");
            System.out.println("10. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();  

            switch (choice) {
                case 1:
                    brandManager.display();
                    break;
                case 2:
                    {
                        Influencer selectedInfluencer = SelectionHelper.selectInfluencer(brandManager, influencers);
        
                        if (selectedInfluencer == null) {
                            System.out.println("No influencer selected, campaign cannot be created.");
                            return;
                        }
                
                        Advertiser selectedAdvertiser = SelectionHelper.selectAdvertiser(selectedInfluencer, advertisers);
                
                        if (selectedAdvertiser == null) {
                            System.out.println("No advertiser selected, campaign cannot be created.");
                            return;
                        }
                
                        System.out.println("Creating Campaign with selected Influencer and Advertiser...");
                        Campaign newCampaign = new Campaign(selectedInfluencer, brandManager, selectedAdvertiser, 30); // Example: Duration = 30 days
                
                        brandManager.updateCampaign(newCampaign);
                        for (int i = 0; i < influencers.length; i++) {
                            if (influencers[i].getUsername().equals(selectedInfluencer.getUsername())) {
                            influencers[i].updateCampaign(newCampaign);
                            break;
                            } 
                    }

                        for (int i = 0; i < advertisers.length; i++) {
                            if (advertisers[i].getUsername().equals(selectedAdvertiser.getUsername())) {
                            advertisers[i].updateCampaign(newCampaign);
                            advertisers[i].update();
                            break;
                            }
                        }
                    }
                    break;
                case 3:
                    Campaign campaign = brandManager.getCampaign();
                    if (campaign != null) {
                        brandManager.displayCampaign();
                    } else {
                        System.out.println("No campaign found.");
                    }
                    break;
                case 4:
                    System.out.print("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    brandManager.updatePW(newPassword);
                    System.out.println("Password updated successfully.");
                    break;
                case 5:
                    System.out.print("Enter new budget: ");
                    double newBudget = scanner.nextDouble();
                    brandManager.updateBudget(newBudget);
                    System.out.println("Budget updated successfully.");
                    break;
                case 6:
                    Campaign campaign1 = brandManager.getCampaign();
                    if(campaign1 == null) {
                    System.out.println("No campaign found");
                    break;
                    }
                    try{
                    campaign1.getContract().makePayment();  
                    }catch(InsufficientBudgetException e) {
                        System.out.println("Insufficient budget to make payment.");
                    }
                    break;
                case 7:
                    Campaign campaign2 = brandManager.getCampaign();
                    if (campaign2 != null) {
                        campaign2.stopCampaign();
                        System.out.println("Campaign stopped successfully.");
                    } else {
                        System.out.println("No campaign found.");
                    }
                    break;
                case 8:
                    System.out.println("Changing campaign influencer...");
                    Campaign campaign3 = brandManager.getCampaign();
                    if (campaign3 != null) {
                        Influencer newInfluencer = SelectionHelper.selectInfluencer(brandManager, influencers);
                        if (newInfluencer != null) {
                            campaign3.changeInfluencer(newInfluencer);
                            System.out.println("Campaign influencer changed successfully.");
                        } else {
                            System.out.println("No influencer selected.");
                        }
                    } else {
                        System.out.println("No campaign found.");
                    }
                    break;
                case 9:
                    exit = true;
                    System.out.println("Logging out...");
                    return;
                case 10: 
                    System.out.println("Exiting the application...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }
    public static void showAdminMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. View Stats");
            System.out.println("2. List Users");
            System.out.println("3. Add User");
            System.out.println("4. Remove User");
            System.out.println("5. View Campaigns");
            System.out.println("6. Update Password");
            System.out.println("7. View Dashboard");
            System.out.println("8. Logout");
            System.out.println("9. Exit");
            System.out.print("Enter your choice: ");
            int ch = scanner.nextInt();
            scanner.nextLine(); 

            switch (ch) {
                case 1:
                    admin.display();
                    break;
                case 2:
                    System.out.print("Enter user type (1: Influencer, 2: Brand Manager, 3: Advertiser): ");
                    int userType = scanner.nextInt();
                    scanner.nextLine(); 
                    if (userType == 1) {
                        admin.listUsers(admin.getInfluencers());
                    } else if (userType == 2) {
                        admin.listUsers(admin.getBrandManagers());
                    } else if (userType == 3) {
                        admin.listUsers(admin.getAdvertisers());
                    } else {
                        System.out.println("Invalid choice!");
                    }
                    break;
                case 3:
                    // Add User
                    System.out.println("\n=== Add New User ===");
                    System.out.println("1. Add Influencer");
                    System.out.println("2. Add Advertiser");
                    System.out.println("3. Add Brand Manager");
                    System.out.print("Choose the type of user to add: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();  

                    if (choice == 1) {
                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        System.out.print("Enter name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter number of followers: ");
                        int followers = scanner.nextInt();
                        scanner.nextLine(); 
                        System.out.print("Enter niche: ");
                        String niche = scanner.nextLine();
                        System.out.print("Enter platforms (comma-separated): ");
                        String platformsInput = scanner.nextLine();
                        String[] platforms = platformsInput.split(","); // Split by commas
                        Influencer newInfluencer = new Influencer(username, password, name, followers, niche, platforms);
                        admin.addUser(newInfluencer);
                        System.out.println("Influencer added successfully.");
                    } else if (choice == 2) {
                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        System.out.print("Enter advertiser name: ");
                        String advName = scanner.nextLine();
                        System.out.print("Enter commission rate: ");
                        double commission = scanner.nextDouble();
                        System.out.print("Enter number of contracts: ");
                        int numOfContracts = scanner.nextInt();
                        scanner.nextLine(); 
                        System.out.print("Enter platform: ");
                        String platform = scanner.nextLine();
                        Advertiser newAdvertiser = new Advertiser(username, password, advName, commission, numOfContracts, platform);
                        admin.addUser(newAdvertiser);
                        System.out.println("Advertiser added successfully.");
                    } else if (choice == 3) {
                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        System.out.print("Enter brand name: ");
                        String brandName = scanner.nextLine();
                        System.out.print("Enter required niche: ");
                        String reqNiche = scanner.nextLine();
                        System.out.print("Enter minimum followers: ");
                        int minFollowers = scanner.nextInt();
                        System.out.print("Enter budget: ");
                        double budget = scanner.nextDouble();
                        scanner.nextLine(); 
                        BrandManager newBrandManager = new BrandManager(username, password, brandName, reqNiche, minFollowers, budget);
                        admin.addUser(newBrandManager);
                        System.out.println("Brand Manager added successfully.");
                    } else {
                        System.out.println("Invalid choice.");
                    }
                    break;

                case 4:
                    System.out.println("Enter user type to remove (1: Influencer, 2: Brand Manager, 3: Advertiser): ");
                    int removeType = scanner.nextInt();
                    scanner.nextLine(); 
                    System.out.print("Enter user name to remove: ");
                    String userName = scanner.nextLine();
                    User userToRemove = null;

                    if (removeType == 1) {
                        for (Influencer influencer : influencers) {
                            if (influencer.getUsername().equals(userName)) {
                                userToRemove = influencer;
                                break;
                            }
                        }
                    } else if (removeType == 2) {
                        for (BrandManager manager : brandManagers) {
                            if (manager.getUsername().equals(userName)) {
                                userToRemove = manager;
                                break;
                            }
                        }
                    } else if (removeType == 3) {
                        for (Advertiser advertiser : advertisers) {
                            if (advertiser.getUsername().equals(userName)) {
                                userToRemove = advertiser;
                                break;
                            }
                        }
                    }

                    if (userToRemove != null) {
                        admin.removeUser(userToRemove);
                        System.out.println("User removed successfully!");
                    } else {
                        System.out.println("User not found!");
                    }
                    break;
                
                case 5:
                    Campaign[] campaigns = admin.getCampaigns();
                    if (campaigns != null) {
                        System.out.println("Campaigns: ");
                        for (Campaign campaign : campaigns) {
                            campaign.display();
                        }
                    } else {
                        System.out.println("No campaigns found!");
                    }
                    break;
                case 6:
                    System.out.print("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    admin.updatePW(newPassword);
                    break;
                case 7:
                    {
                        Dashboard.showPerformance(admin.getInfluencers(), admin.getAdvertisers(), admin.getCampaigns());
                        break;
                    }
                case 8:
                    exit = true;
                    System.out.println("Logging out...");
                    break;
                case 9:
                    System.out.println("Exiting the application...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }
}
