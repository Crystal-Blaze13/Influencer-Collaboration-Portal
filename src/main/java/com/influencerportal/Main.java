package com.influencerportal;

import com.influencerportal.exception.PortalException;
import com.influencerportal.recommendation.RecommendationEngine;
import com.influencerportal.repository.CampaignRepository;
import com.influencerportal.repository.Database;
import com.influencerportal.repository.PaymentRepository;
import com.influencerportal.repository.UserRepository;
import com.influencerportal.service.AdminService;
import com.influencerportal.service.AuthService;
import com.influencerportal.service.CampaignService;
import com.influencerportal.service.DemoDataSeeder;
import com.influencerportal.service.PaymentService;
import com.influencerportal.service.UserService;
import com.influencerportal.ui.ConsoleIO;
import com.influencerportal.ui.EndOfInputException;
import com.influencerportal.ui.MainMenu;
import com.influencerportal.ui.Services;
import java.nio.file.Path;

/**
 * Usage: java -jar influencer-portal.jar [run | seed] [--db path/to/file.db]
 * <ul>
 *   <li>run (default): start the console application</li>
 *   <li>seed: DELETE all data in the database and load the demonstration data</li>
 * </ul>
 * The database path defaults to ./influencer-portal.db; the schema is created automatically.
 */
public class Main {
    private static final String DEFAULT_DB = "influencer-portal.db";

    public static void main(String[] args) {
        String command = "run";
        String dbPath = System.getenv().getOrDefault("PORTAL_DB", DEFAULT_DB);
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "run", "seed" -> command = args[i];
                case "--db" -> {
                    if (i + 1 >= args.length) {
                        usage();
                        return;
                    }
                    dbPath = args[++i];
                }
                default -> {
                    usage();
                    return;
                }
            }
        }

        Database database = new Database(Path.of(dbPath));
        UserRepository users = new UserRepository(database);
        CampaignRepository campaigns = new CampaignRepository(database);
        PaymentRepository payments = new PaymentRepository(database);
        AuthService auth = new AuthService(users);
        CampaignService campaignService = new CampaignService(users, campaigns, new RecommendationEngine());
        PaymentService paymentService = new PaymentService(users, campaigns, payments);

        if (command.equals("seed")) {
            new DemoDataSeeder(database, auth, campaignService, paymentService).reset();
            System.out.println("Database " + database.getFile() + " reset and loaded with demonstration data.");
            System.out.println("Demo login: username 'admin', 'nike', 'ananya' or 'instabuzz'; password '"
                    + DemoDataSeeder.DEMO_PASSWORD + "'.");
            return;
        }

        Services services = new Services(auth, new UserService(users, campaigns), campaignService, paymentService,
                new AdminService(users, campaigns, payments));
        ConsoleIO io = new ConsoleIO(System.in, System.out);
        try {
            if (users.countByRole().values().stream().mapToInt(Integer::intValue).sum() == 0) {
                io.println("The database is empty. Register an account, or quit and run: java -jar <jar> seed");
            }
            new MainMenu(io, services).run();
        } catch (EndOfInputException e) {
            System.out.println("\nInput closed. Goodbye.");
        } catch (PortalException e) {
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void usage() {
        System.out.println("Usage: java -jar influencer-portal.jar [run | seed] [--db path/to/file.db]");
    }
}
