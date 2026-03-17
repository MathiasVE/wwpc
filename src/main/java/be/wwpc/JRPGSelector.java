package be.wwpc;

import be.wwpc.csv.JRPGCsvReader;
import be.wwpc.csv.JRPGCsvWriter;
import be.wwpc.model.JRPGEntry;
import com.opencsv.exceptions.CsvValidationException;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JRPGSelector {

    static void main() throws CsvValidationException, IOException {
        Path path =  Paths.get("jrpgs.csv");
        List<JRPGEntry> jrpgsEntries = new ArrayList<>();
        try {
            jrpgsEntries = JRPGCsvReader.parse(path);
        } catch (IOException | CsvValidationException e) {
            System.out.println("Missing or invalid jrpgs.csv");
            throw e;
        }

        Map<Integer, List<JRPGEntry>> jrpgEntriesPerReleaseYear = jrpgsEntries.stream().collect(Collectors.groupingBy(JRPGEntry::releaseYear, Collectors.mapping(jrpgEntry -> jrpgEntry, Collectors.toList())));

//        List<List<JRPGEntry>> filteredTop3 = jrpgEntriesPerReleaseYear.entrySet()
//                .stream()
//                .sorted(Comparator.comparingInt(Map.Entry::getKey))
//                .map(integerListEntry -> integerListEntry.getValue().stream()
//                        .sorted(Comparator.comparingInt(value -> value instanceof JRPGEntry jrpgEntry ? jrpgEntry.userScore() != null ? jrpgEntry.userScore() : 0 : 0).reversed())
//                        .filter(jrpgEntry -> jrpgEntry.userScore() != null && jrpgEntry.userScore() > 0)
//                        .limit(3)
//                        .toList()).toList();
        Random r = new Random();

        for (Map.Entry<Integer, List<JRPGEntry>> entry : jrpgEntriesPerReleaseYear.entrySet()) {
            if(entry.getKey() > 0) {
                System.out.println("YEAR " + entry.getKey());
                List<JRPGEntry> entriesForYear = entry.getValue();
                List<JRPGEntry> filteredTop3 = entry.getValue()
                        .stream()
                        .sorted(Comparator.comparingInt(value -> value instanceof JRPGEntry jrpgEntry ? jrpgEntry.userScore() != null ? jrpgEntry.userScore() : 0 : 0).reversed())
                        .filter(jrpgEntry -> jrpgEntry.userScore() != null && jrpgEntry.userScore() > 0)
                        .limit(3)
                        .toList();
                int count = 0;
                for (JRPGEntry jrpgEntry : filteredTop3) {
                    System.out.println("  TOP " + jrpgEntry.name());
                    updateJrpgEntryInList(jrpgsEntries, createJrpgEntrySelected(jrpgEntry, ++count));
                }
                for (int i = 0; i < 6 - Math.min(3, filteredTop3.size()) && !entriesForYear.isEmpty(); i++) {
                    JRPGEntry jrpgEntry = entriesForYear.get(r.nextInt(entriesForYear.size()));
                    if (jrpgEntry.selected() == null) {
                        System.out.println("  RAND " + jrpgEntry.name());
                        updateJrpgEntryInList(jrpgsEntries, createJrpgEntrySelected(jrpgEntry, ++count));
                    } else {
                        i--;
                    }
                    entriesForYear.remove(jrpgEntry);
                }
            }
        }

        try {
            JRPGCsvWriter.write(path, jrpgsEntries);
        } catch (IOException e) {
            System.out.println("Failed to write jrpgs.csv");
            throw e;
        }
    }

    private static void updateJrpgEntryInList(List<JRPGEntry> entries, JRPGEntry newEntry) {
        for(int i=0; i<entries.size(); i++) {
            if(entries.get(i).name().equals(newEntry.name())) {
                entries.set(i, newEntry);
            }
        }
    }

    private static @NonNull JRPGEntry createJrpgEntrySelected(JRPGEntry jrpgEntry, int selected) {
        // TODO: add selected column to CSV
        return new JRPGEntry(
                jrpgEntry.name(),
                jrpgEntry.releaseYear(),
                jrpgEntry.platforms(),
                jrpgEntry.mainProtagonistGender(),
                jrpgEntry.femaleCharactersSexualised(),
                jrpgEntry.userScore(),
                selected
                );
    }
}
