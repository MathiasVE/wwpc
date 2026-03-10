package be.wwpc;

import be.wwpc.csv.JRPGCsvReader;
import be.wwpc.csv.JRPGCsvWriter;
import be.wwpc.model.JRPGEntry;
import com.opencsv.exceptions.CsvValidationException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JRPGCScraper {
    static void main() {
        Path path =  Paths.get("jrpgs.csv");
        List<JRPGEntry> jrpgsEntries = new ArrayList<>();
        try {
            jrpgsEntries = JRPGCsvReader.parse(path);
        } catch (IOException | CsvValidationException e) {
            // ignore
        }

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            driver.get("https://jrpgc.com/jrpg-database/");

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("facetwp-template")));

            int readEntriesOffset = jrpgsEntries.size();
            int loadedOffset = 0;

            WebElement mainCollection = driver.findElement(By.className("facetwp-template"));

            List<WebElement> entries = mainCollection.findElements(By.className("card"));

            while(loadedOffset < entries.size()) {
                for (int i = readEntriesOffset; i < entries.size(); i++) {
                    WebElement entry = entries.get(i);

                    WebElement h6 = entry.findElement(By.cssSelector("h6"));
                    WebElement a = h6.findElement(By.cssSelector("a"));

                    int releaseYear = 0;
                    try {
                        WebElement abbr = entry.findElement(By.cssSelector("abbr"));
                        WebElement span = abbr.findElement(By.cssSelector("span"));
                        releaseYear = Integer.parseInt(span.getText());
                    } catch(NoSuchElementException e) {
                        // ignore take default 0 value
                    }
                    List<WebElement> sub = entry.findElements(By.cssSelector("sub"));
                    List<WebElement> aList = sub.get(1).findElements(By.cssSelector("a"));

                    String jrpgTitle = a.getText();
                    List<String> platforms = aList.stream().map(webElement -> webElement.getText()).collect(Collectors.toUnmodifiableList());
                    String mainProtagonistGender = "";
                    String femaleCharactersSexualised = "";
                    jrpgsEntries.add(new JRPGEntry(jrpgTitle, releaseYear, platforms, mainProtagonistGender, femaleCharactersSexualised, null));
                }
                loadedOffset = entries.size();
                if(readEntriesOffset < entries.size()) {
                    readEntriesOffset = entries.size();
                }
                WebElement element = driver.findElement(By.className("facetwp-load-more"));
                try {
                    element.click();
                    wait.until(webDriver -> !webDriver.findElement(By.className("facetwp-load-more")).getText().equals("Loading..."));
                    entries = mainCollection.findElements(By.className("card"));
                } catch (ElementNotInteractableException e) {
                    // ignore (end of list), while loop will finish and result written
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            driver.quit();
            try {
                JRPGCsvWriter.write(path, jrpgsEntries);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
