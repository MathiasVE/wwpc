package be.wwpc;

import be.wwpc.csv.JRPGCsvReader;
import be.wwpc.csv.JRPGCsvWriter;
import be.wwpc.model.JRPGEntry;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GeminiRequestor {
    public static String callGemini(String jrpgName, int year, List<String> platforms) {
        if(platforms.isEmpty()) {
            return "not applicable";
        }
        try (Client client = new Client()) {
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-3-flash-preview",
                    "Is the main protagonist in " + jrpgName + " from " + year + " on " + platforms.get(0) + " male or female? answer only with \"male\", \"female\", \"player choice\" or \"not applicable\"",
                    null);
            return response.text();
        }
    }

    static void main() {
        Path path =  Paths.get("jrpgs.csv");
        List<JRPGEntry> jrpgsEntries = new ArrayList<>();
        try {
            jrpgsEntries = JRPGCsvReader.parse(path);
        } catch (IOException | CsvValidationException e) {
            // ignore
        }
        try {
            for(int i=0; i<jrpgsEntries.size(); i++) {
                JRPGEntry entry = jrpgsEntries.get(i);
                if(entry.mainProtagonistGender().equals("")) {
                    jrpgsEntries.set(i, new JRPGEntry(
                        entry.name(),
                        entry.releaseYear(),
                        entry.platforms(),
                        callGemini(entry.name(), entry.releaseYear(), entry.platforms())));
                    JRPGCsvWriter.write(path, jrpgsEntries);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
