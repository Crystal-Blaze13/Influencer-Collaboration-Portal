package FileReaders;//this class reads the advertisers from the file

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import Users.Advertiser;

public class AdvertiserReader {
   public Advertiser[] read() {
        Advertiser[] advertisers = null;
    try {
            FileReader fileReader = new FileReader("/Users/palakkshetrapal/Documents/FinalOops/src/advertisers.txt");
            BufferedReader br = new BufferedReader(fileReader);

            int n = Integer.parseInt(br.readLine());
            advertisers = new Advertiser[n];

            for (int i = 0; i < n; i++) {
                String username = skipEmptyLines(br);
                String password = skipEmptyLines(br);
                String brandName = skipEmptyLines(br);
                double rate = Double.parseDouble(skipEmptyLines(br)); // then niche
                int deals = Integer.parseInt(skipEmptyLines(br));
                String platform=skipEmptyLines(br);
                advertisers[i] = new Advertiser(username,password, brandName, rate, deals,platform);
            }

            br.close();

            
        } catch (IOException e) {
            System.out.println("Error "+ e.getMessage());
        }
        return advertisers;
    }

    private static String skipEmptyLines(BufferedReader br) throws IOException {
        String line = br.readLine();
        while (line != null && line.trim().isEmpty()) {
            line = br.readLine();
        }
        return line;
    }
}
