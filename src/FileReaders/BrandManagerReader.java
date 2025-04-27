package FileReaders;//this class reads the brand managers from the file

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import Users.*;

public class BrandManagerReader {
    public BrandManager[] read() {
        AdvertiserReader advertiserReader = new AdvertiserReader();
        InfluencerReader influencerReader = new InfluencerReader();
        Influencer[] influencers = influencerReader.read();
        Advertiser[] advertisers = advertiserReader.read();
        BrandManager[] brandManagers = null;
        try {
            FileReader fileReader = new FileReader("/Users/palakkshetrapal/Documents/FinalOops/src/brandmanagers.txt");
            BufferedReader br = new BufferedReader(fileReader);

            int n = Integer.parseInt(br.readLine());
           brandManagers = new BrandManager[n];

            for (int i = 0; i < n; i++) {
                String username = skipEmptyLines(br);
                String password = skipEmptyLines(br);
                String brandName = skipEmptyLines(br);
                String reqNiche = skipEmptyLines(br); // then niche
                int minFollowers = Integer.parseInt(skipEmptyLines(br));
                double budget = Double.parseDouble(skipEmptyLines(br));
                brandManagers[i] = new BrandManager(username, password, brandName, reqNiche, minFollowers,influencers,advertisers,budget);
            }

            br.close();
            //brandManagers[0].display();
        } catch (IOException e) {
            System.out.println("Error "+ e.getMessage());
        }
        return brandManagers;
    }

    private static String skipEmptyLines(BufferedReader br) throws IOException {
        String line = br.readLine();
        while (line != null && line.trim().isEmpty()) {
            line = br.readLine();
        }
        return line;
    }    
}

