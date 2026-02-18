package be.wwpc.model;

import java.time.LocalDate;
import java.util.List;

public record JRPGEntry(String name, int releaseYear, List<String> platforms, String mainProtagonistGender) {

}
