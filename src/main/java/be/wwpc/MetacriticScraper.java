package be.wwpc;

import be.wwpc.csv.JRPGCsvReader;
import be.wwpc.csv.JRPGCsvWriter;
import be.wwpc.model.JRPGEntry;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.text.similarity.FuzzyScore;
import org.jspecify.annotations.NonNull;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

public class MetacriticScraper {

    static void main() {
        Path path =  Paths.get("jrpgs.csv");
        List<JRPGEntry> jrpgsEntries = new ArrayList<>();
        try {
            jrpgsEntries = JRPGCsvReader.parse(path);
        } catch (IOException | CsvValidationException e) {
            // ignore
        }

        ChromeOptions options = new ChromeOptions();

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        boolean cookiesIgnored = false;

        try {
            for(int i=0; i<jrpgsEntries.size(); i++) {
                JRPGEntry jrpgEntry = jrpgsEntries.get(i);
                if(jrpgEntry.userScore() == null) {
                    String simplifiedName = getSimplifiedName(jrpgEntry.name());

                    driver.get("https://www.metacritic.com/search/" + simplifiedName);
                    if (!cookiesIgnored) {
                        try {
                            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("onetrust-reject-all-handler")));
                            WebElement element = driver.findElement(By.id("onetrust-reject-all-handler"));
                            element.click();
                            cookiesIgnored = true;
                        } catch (Exception e) {

                        }
                    }
                    try {
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text() = 'No Results Found']")));
                        System.out.println(i + ". No results found for: " + jrpgEntry.name() + " (" + jrpgEntry.releaseYear() + ")");
                        jrpgsEntries.set(i, createJrpgEntryWithUserScore(jrpgEntry, 0));
                        continue; // no results found
                    } catch (Exception e) {
                        // ignore
                    }

                    List<WebElement> elements = driver.findElements(By.className("g-grid-container"));
                    elements = elements.stream().filter(webElement -> {
                        try {
                            WebElement element = webElement.findElement(By.cssSelector("ul[data-testid='tag-list']"));
                            return element.getText().equals("game");
                        } catch(NoSuchElementException e) {
                            return false;
                        }
                    }).toList();

                    Optional<WebElement> matchingElement = elements.stream().filter(webElement -> {
                        String title = webElement.findElement(By.cssSelector("p[data-testid='product-title']")).getText();
                        return getSimplifiedName(jrpgEntry.name()).replaceAll(" ", "").equalsIgnoreCase(getSimplifiedName(title).replaceAll(" ", ""));
                    }).findFirst();

                    if(matchingElement.isPresent()) {
                        matchingElement.get().click();
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-testid='product-score']")));
                        String userScoreText = driver.findElements(By.cssSelector("div[data-testid='product-score']")).get(1).findElement(By.className("c-siteReviewScore")).getText();
                        double userScore = 0;
                        if(!userScoreText.equalsIgnoreCase("tbd")) {
                            userScore = Double.parseDouble(userScoreText);
                        }
                        System.out.println(i + ". Result found for: " + jrpgEntry.name() + " (" + jrpgEntry.releaseYear() + "): " + userScore);
                        jrpgsEntries.set(i, createJrpgEntryWithUserScore(jrpgEntry, (int) (userScore * 10)));
                    } else {
                        System.out.println(i + ". No results found for: " + jrpgEntry.name() + " (" + jrpgEntry.releaseYear() + ")");
                        jrpgsEntries.set(i, createJrpgEntryWithUserScore(jrpgEntry, 0));
                    }

                    if(i % 100==0) {
                        // write result every 100
                        try {
                            JRPGCsvWriter.write(path, jrpgsEntries);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } finally {
            driver.quit();
            try {
                JRPGCsvWriter.write(path, jrpgsEntries);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static @NonNull JRPGEntry createJrpgEntryWithUserScore(JRPGEntry jrpgEntry, int userScore) {
        return new JRPGEntry(
                jrpgEntry.name(),
                jrpgEntry.releaseYear(),
                jrpgEntry.platforms(),
                jrpgEntry.mainProtagonistGender(),
                jrpgEntry.femaleCharactersSexualised(),
                userScore);
    }

    private static @NonNull String getSimplifiedName(String name) {
        return Arrays.stream(name
                        .chars()
                        .filter(value -> value == 32
                                || value >= 48 && value <= 57
                                || value >= 65 && value <= 90
                                || value >= 97 && value <= 122)
                        .toArray())
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}
