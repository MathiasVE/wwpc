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
    public static String callGemini(String question) {
        try (Client client = new Client()) {
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-3-flash-preview",
                    question,
                    null);
            return response.text();
        }
    }

    public static String getGenderMainProtagonist(String jrpgName, int year, List<String> platforms) {
        if(platforms.isEmpty()) {
            return "not applicable";
        }
        return callGemini("Is the main protagonist in " + jrpgName + " from " + year + " on " + platforms.get(0) + " male or female? answer only with \"male\", \"female\", \"player choice\" or \"not applicable\"");
    }

    public static String getFemaleCharactersSexualised(String jrpgName, int year, List<String> platforms) {
        if(platforms.isEmpty()) {
            return "not applicable";
        }
        return callGemini("Are the female characters sexualised in " + jrpgName + " from " + year + " on " + platforms.get(0) + "? Answer only with \"yes\", \"no\" or \"unclear\". Don't provide an explanation.");
    }

    static void main() throws IOException {
        Path path =  Paths.get("jrpgs.csv");
        List<JRPGEntry> jrpgsEntries = new ArrayList<>();
        try {
            jrpgsEntries = JRPGCsvReader.parse(path);
        } catch (IOException | CsvValidationException e) {
            // ignore
        }
        try {
            int callCount = 0;
            for(int i=0; i<jrpgsEntries.size(); i++) {
                JRPGEntry entry = jrpgsEntries.get(i);
                boolean calledGemini = false;
                if(entry.mainProtagonistGender().isEmpty()) {
                    System.out.println("Get gender main protagonist: " + i);
                    jrpgsEntries.set(i, new JRPGEntry(
                            entry.name(),
                            entry.releaseYear(),
                            entry.platforms(),
                            getGenderMainProtagonist(entry.name(), entry.releaseYear(), entry.platforms()),
                            entry.femaleCharactersSexualised(),
                            entry.userScore(),
                            entry.selected()));
                    calledGemini = true;
                }
                entry = jrpgsEntries.get(i);
                if(entry.femaleCharactersSexualised().isEmpty()) {
                    System.out.println("Get Female Characters Sexualised: " + i);
                    String femaleCharactersSexualised = getFemaleCharactersSexualised(entry.name(), entry.releaseYear(), entry.platforms());
                    if(!(femaleCharactersSexualised.equalsIgnoreCase("yes") ||
                            femaleCharactersSexualised.equalsIgnoreCase("no") ||
                            femaleCharactersSexualised.equalsIgnoreCase("unclear"))) {
                        System.out.println(entry.name() + ": " + femaleCharactersSexualised);
                        femaleCharactersSexualised = "unclear";
                    }
                    jrpgsEntries.set(i, new JRPGEntry(
                            entry.name(),
                            entry.releaseYear(),
                            entry.platforms(),
                            entry.mainProtagonistGender(),
                            femaleCharactersSexualised,
                            entry.userScore(),
                            entry.selected()));
                    calledGemini = true;
                }
                if(calledGemini) {
                    callCount++;
                    if(callCount % 100 == 0) {
                        JRPGCsvWriter.write(path, jrpgsEntries);
                    }
                }
            }
        } finally {
            JRPGCsvWriter.write(path, jrpgsEntries);
        }
    }


}
