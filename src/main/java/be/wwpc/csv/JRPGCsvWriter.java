package be.wwpc.csv;

import be.wwpc.model.JRPGEntry;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JRPGCsvWriter {
    public static void write(Path csv, List<JRPGEntry> entries) throws IOException {
        try(FileWriter fileWriter = new FileWriter(csv.toFile());
            CSVWriter csvWriter = new CSVWriter(fileWriter)) {
            csvWriter.writeNext(new String[] {
                    "JRPG name",
                    "Release date",
                    "Platforms",
                    "Main protagonist gender",
                    "Female characters sexualised",
                    "User score",
                    "Selected"
            });
            for(JRPGEntry entry : entries) {
                csvWriter.writeNext(new String[] {
                        entry.name(),
                        "" + entry.releaseYear(),
                        entry.platforms().stream().reduce("", (s, s2) -> s.isBlank() ? s2 : s + ";" + s2),
                        entry.mainProtagonistGender(),
                        entry.femaleCharactersSexualised(),
                        entry.userScore() != null ? "" + entry.userScore() : "",
                        entry.selected() != null ? "" + entry.selected() : ""
                });
            }
        }
    }
}
