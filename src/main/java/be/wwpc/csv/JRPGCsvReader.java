package be.wwpc.csv;

import be.wwpc.model.JRPGEntry;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JRPGCsvReader {
    public static List<JRPGEntry> parse(Path csv) throws IOException, CsvValidationException {
        List<JRPGEntry> entries = new ArrayList<>();
        try(FileReader filereader = new FileReader(csv.toFile());
            CSVReader csvReader = new CSVReader(filereader);) {
            String[] nextRecord = csvReader.readNext(); // skip first line
            while((nextRecord = csvReader.readNext()) != null) {
                entries.add(parseEntry(nextRecord));

            }
        }
        return entries;
    }

    private static JRPGEntry parseEntry(String[] nextRecord) {
        String[] split = nextRecord[2].split(";");
        ArrayList<String> platforms = new ArrayList<>(Arrays.asList(split));
        return new JRPGEntry(nextRecord[0],
                Integer.parseInt(nextRecord[1]),
                platforms,
                nextRecord.length > 3 ? nextRecord[3].isEmpty() ? null : Integer.valueOf(nextRecord[3]) : null,
                nextRecord.length > 4 ? nextRecord[4].isEmpty() ? null : Integer.valueOf(nextRecord[4]) : null,
                nextRecord.length > 5 ? nextRecord[5] : null,
                nextRecord.length > 6 ? nextRecord[6] : null
                );
    }
}
