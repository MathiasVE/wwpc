package be.wwpc;

import be.wwpc.csv.JRPGCsvReader;
import be.wwpc.csv.JRPGCsvWriter;
import be.wwpc.model.JRPGEntry;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UniqueFilter {
    static void main() throws CsvValidationException, IOException {
        Path path = Paths.get("jrpgs.csv");
        List<JRPGEntry> jrpgsEntries = new ArrayList<>();
        try {
            jrpgsEntries = JRPGCsvReader.parse(path);
        } catch (IOException | CsvValidationException e) {
            System.out.println("Missing or invalid jrpgs.csv");
            throw e;
        }

        // this is not the fastest way to unique a list but it keeps the sorting.
        List<JRPGEntry> unique = new ArrayList<>();
        for(JRPGEntry entry : jrpgsEntries) {
            if(!unique.contains(entry)) {
                unique.add(entry);
            } else {
                System.out.println("DOUBLE " + entry.name());
            }
        }
        try {
            JRPGCsvWriter.write(path, unique);
        } catch (IOException e) {
            System.out.println("Failed to write jrpgs.csv");
            throw e;
        }
    }
}
