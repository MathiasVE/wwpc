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

    public static String getFemaleSexualizeInfo(String jrpgName, int year, List<String> platforms) {
        if(platforms.isEmpty()) {
            return "not applicable";
        }
        return callGemini("Overdreven slanke taille en groot contrast in lichaamsverhoudingen en poseer houdingen die het lichaam accentueren 20%\n" +
                "\"Anime-fysica\" en Borstanimatie 7,5%\n" +
                "Haarstijlen die de aandacht naar het lichaam trekken of make‑up of stylisatie die sensualiteit benadrukt 20%\n" +
                "Het \"Moe\"-gezicht vs. Volwassen Lichaam 5%\n" +
                "De \"Shizuku\" (Natte Huid) Esthetiek 5%\n" +
                "Kleding die veel huid toont, strak aansluitende of doorschijnende kleding 20%\n" +
                "Onpraktische harnassen of vechtkledij 7,5%\n" +
                "De \"Zettai Ryōiki\" (Absolute Zone) 5%\n" +
                "De \"Detached Sleeves\" (Losse Mouwen) 5%\n" +
                "Focus op \"Garter Belts\" en Dij-accessoires 5%\n\n" +
                "Gegeven deze percentages volgens prioriteit, wat is de sexualisatie van vrouwen in " + jrpgName + " van " + year + " op " + platforms.get(0) + ". Antwoord met een uiteenzetting van de percentages en wijs een concreet percentage toe.");
    }

    public static String getMaleSexualizeInfo(String jrpgName, int year, List<String> platforms) {
        if(platforms.isEmpty()) {
            return "not applicable";
        }
        return callGemini("perfect atletische lichamen, perfect proportionele lichaamsbouw of Slanke Spierbouw 20%\n" +
                "Androgyn of bishōnen‑geïnspireerde gezichten 10%\n" +
                "Verzorgde, opvallende kapsels 7,5%\n" +
                "De \"Melancholische Blik\" 5%\n" +
                "Minimale of strak aansluitende kleding 20%\n" +
                "Accessoires die sensualiteit suggereren, overdaad aan Bandjes en Ritsen 7,5%\n" +
                "Nonchalante “cool” poses 5%\n" +
                "Tatoeages of littekens als esthetiek 12,5%\n" +
                "Handschoenen en Hand-fetisjisme 7,5%\n" +
                "De \"Capes\" en \"Long Coats\" zonder Shirt 5%\n\n" +
                "Gegeven deze percentages volgens prioriteit, wat is de sexualisatie van mannen in " + jrpgName + " van " + year + " op " + platforms.get(0) + ". Antwoord met een uiteenzetting van de percentages en wijs een concreet percentage toe.");
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
                if(entry.selected() != null && entry.selected() > 0) {
                    boolean calledGemini = false;
                    if (entry.femaleSexualizedInfo().isEmpty()) {
                        System.out.println("Get female sexualisation info: " + i);
                        jrpgsEntries.set(i, new JRPGEntry(
                                entry.name(),
                                entry.releaseYear(),
                                entry.platforms(),
                                entry.userScore(),
                                entry.selected(),
                                getFemaleSexualizeInfo(entry.name(), entry.releaseYear(), entry.platforms()),
                                entry.maleSexualizedInfo()));
                        calledGemini = true;
                    }
                    entry = jrpgsEntries.get(i);
                    if (entry.maleSexualizedInfo().isEmpty()) {
                        System.out.println("Get male sexualisation info: " + i);
                        jrpgsEntries.set(i, new JRPGEntry(
                                entry.name(),
                                entry.releaseYear(),
                                entry.platforms(),
                                entry.userScore(),
                                entry.selected(),
                                entry.femaleSexualizedInfo(),
                                getMaleSexualizeInfo(entry.name(), entry.releaseYear(), entry.platforms())));
                        calledGemini = true;
                    }
                    if (calledGemini) {
                        callCount++;
                        if (callCount % 20 == 0) {
                            JRPGCsvWriter.write(path, jrpgsEntries);
                        }
                    }
                }
            }
        } finally {
            JRPGCsvWriter.write(path, jrpgsEntries);
        }
    }


}
