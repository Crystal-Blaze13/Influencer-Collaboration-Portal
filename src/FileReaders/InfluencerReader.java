package FileReaders;//this class reads the influencers from the file
import java.io.*;
import Users.Influencer;

public class InfluencerReader {
    public Influencer[] read(){
        Influencer[] influencers=null;
        try {
            FileReader fileReader = new FileReader("/Users/palakkshetrapal/Documents/FinalOops/src/influencers.txt");
            BufferedReader br = new BufferedReader(fileReader);

            int n = Integer.parseInt(br.readLine());
            influencers = new Influencer[n];

            for (int i = 0; i < n; i++) {
                String username = skipEmptyLines(br);
                String password = skipEmptyLines(br);
                String name = skipEmptyLines(br);
                int followers = Integer.parseInt(skipEmptyLines(br));
                String niche = skipEmptyLines(br); // then niche
                String[] platforms = skipEmptyLines(br).split(" ");

                influencers[i] = new Influencer(username, password, name,
                                                followers, niche, platforms);
            }

            br.close();

            
        } catch (IOException e) {
            System.out.println("Error "+ e.getMessage());
        }
        return influencers;
    }

    private static String skipEmptyLines(BufferedReader br) throws IOException {
        String line = br.readLine();
        while (line != null && line.trim().isEmpty()) {
            line = br.readLine();
        }
        return line;
    }
}