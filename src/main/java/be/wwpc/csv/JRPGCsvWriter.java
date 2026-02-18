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
                    "JRPG Name",
                    "Release date",
                    "Platforms",
                    "Main Protagonist Gender"
            });
            for(JRPGEntry entry : entries) {
                csvWriter.writeNext(new String[] {
                        entry.name(),
                        "" + entry.releaseYear(),
                        entry.platforms().stream().reduce("", (s, s2) -> s + ";" + s2),
                        entry.mainProtagonistGender()
                });
            }
        }
    }
}
