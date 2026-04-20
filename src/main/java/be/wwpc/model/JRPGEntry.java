package be.wwpc.model;

import java.util.List;

public record JRPGEntry(String name, int releaseYear, List<String> platforms, Integer userScore, Integer selected, String femaleSexualizedInfo, String maleSexualizedInfo) {

}
