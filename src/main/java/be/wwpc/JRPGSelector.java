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

        List<List<JRPGEntry>> listStream = jrpgEntriesPerReleaseYear.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(integerListEntry -> integerListEntry.getValue().stream()
                        .sorted(Comparator.comparingInt(value -> value instanceof JRPGEntry jrpgEntry ? jrpgEntry.userScore() != null ? jrpgEntry.userScore() : 0 : 0).reversed())
                        .filter(jrpgEntry -> jrpgEntry.userScore() != null && jrpgEntry.userScore() > 0)
                        .limit(3)
                        .toList())
                .filter(jrpgEntries -> !jrpgEntries.isEmpty()).toList();
        Random r = new Random();

        for(List<JRPGEntry> entries : listStream) {
            System.out.println("Year: " + entries.getFirst().releaseYear());
            int count = 1;
            for (JRPGEntry jrpgEntry : entries) {
                System.out.println("  TOP " + jrpgEntry.name());
                updateJrpgEntryInList(jrpgsEntries, createJrpgEntrySelected(jrpgEntry, count));
                count++;
            }
            List<JRPGEntry> jrpgEntriesForYear = new ArrayList<>(jrpgsEntries.stream().filter(jrpgEntry -> jrpgEntry.releaseYear() == entries.getFirst().releaseYear()).toList());

            for (int i = 0; i < 3 && !jrpgEntriesForYear.isEmpty(); i++) {
                JRPGEntry jrpgEntry = jrpgEntriesForYear.get(r.nextInt(jrpgEntriesForYear.size()));
                if (jrpgEntry.selected() == null) {
                    System.out.println("  RAND " + jrpgEntry.name());
                    updateJrpgEntryInList(jrpgsEntries, createJrpgEntrySelected(jrpgEntry, 4 + i));
                } else {
                      i--;
                }
                jrpgEntriesForYear.remove(jrpgEntry);
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
