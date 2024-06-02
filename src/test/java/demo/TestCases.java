package demo;

import org.openqa.selenium.chrome.ChromeDriver;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestCases {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String OUTPUT_FILE_PATH = "output/hockey-team-data.json";

    @BeforeClass
    public void setUp() {
        System.out.println("Constructor: TestCases");
        WebDriverManager.chromedriver().timeout(30).setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

    }

    @Test(priority = 1)
    public void testCase01() throws JsonGenerationException, JsonMappingException, IOException, InterruptedException {
        System.out.println("Start Test case: testCase01");
        driver.get("https://www.scrapethissite.com/pages/");

        // Ensure the page is fully loaded before finding the element
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));


        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Hockey Teams: Forms, Searching and Pagination"))).click();
        Thread.sleep(3000);

        ArrayList<HashMap<String, Object>> teamsData = new ArrayList<>();
        int pagesToScrape = 4;

        for (int i = 0; i < pagesToScrape; i++) {
            List<WebElement> rows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#hockey tbody tr")));
            System.out.println("Number of rows found: " + rows.size());

           // List<WebElement> rows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id='hockey']/div/table/tbody/tr")));
            for (WebElement row : rows) {
                //List<WebElement> columns = row.findElements(By.xpath("//*[@id='hockey']/div/table/tbody/tr/td"));
                List<WebElement> columns = row.findElements(By.tagName("td"));
                System.out.println("Number of columns found: " + columns.size());



                // Check if the row has the expected number of columns
                if (columns.size() >= 6) {
                    String teamName = columns.get(0).getText();
                    int year = Integer.parseInt(columns.get(1).getText());
                    double winPercentage = Double.parseDouble(columns.get(5).getText());

                    if (winPercentage < 0.40) {
                        HashMap<String, Object> teamData = new HashMap<>();
                        teamData.put("epochTime", Instant.now().getEpochSecond());
                        teamData.put("teamName", teamName);
                        teamData.put("year", year);
                        teamData.put("winPercentage", winPercentage);
                        teamsData.add(teamData);
                        System.out.println("Added team data: " + teamData);

                    }
                } else {
                    System.out.println("Row does not have the expected columns: " + row.getText());
                }
            }

            // Click the "Next" button to go to the next page
            WebElement nextButton = driver.findElement(By.xpath("//*[@id='hockey']/div/div[5]/div[1]/ul/li[last()]/a"));
            if (nextButton != null && nextButton.isEnabled()) {
                nextButton.click();
                // Wait for the next page to load
                wait.until(ExpectedConditions.stalenessOf(rows.get(0)));
            } else {
                break;
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        File outputFile = new File(OUTPUT_FILE_PATH);
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs(); // Create directories if they don't exist
        }
        objectMapper.writeValue(outputFile, teamsData);

        Assert.assertTrue(outputFile.exists() && outputFile.length() > 0, "Output file is missing or empty");

        System.out.println("End Test case: testCase01");
    }

    @Test(priority = 2)
    public void scrapeOscarWinningFilms() throws IOException, InterruptedException {

        System.out.println("Start Test case: testCase02");
        driver.get("https://www.scrapethissite.com/pages/");

         // Ensure the page is fully loaded before finding the element
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Oscar Winning Films: AJAX and Javascript"))).click();
        Thread.sleep(3000);

        List<WebElement> yearLinks = driver.findElements(By.xpath("//a[@class='year-link']"));
        ArrayList<HashMap<String, Object>> oscarData = new ArrayList<>();

        for (WebElement yearLink : yearLinks) {
            String year = yearLink.getText();
            yearLink.click();

            List<WebElement> movies = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id='table-body']")));

            

            //*[@id='table-body']

            for (int i = 0; i < Math.min(5, movies.size()); i++) {
                WebElement movieElement = movies.get(i);
                HashMap<String, Object> movieData = new HashMap<>();

                String title = movieElement.findElement(By.xpath("//*[@id='oscars']/div/div[5]/div/table/thead/tr/th[1]")).getText();
                String nomination = movieElement.findElement(By.xpath("//*[@id='oscars']/div/div[5]/div/table/thead/tr/th[2]")).getText();
                String awards = movieElement.findElement(By.xpath("//*[@id='oscars']/div/div[5]/div/table/thead/tr/th[3]")).getText();
                boolean isWinner = movieElement.getAttribute("class").contains("film-best-picture");

                movieData.put("epochTime", Instant.now().getEpochSecond());
                movieData.put("year", year);
                movieData.put("title", title);
                movieData.put("nomination", nomination);
                movieData.put("awards", awards);
                movieData.put("isWinner", isWinner);

                oscarData.add(movieData);
                System.out.println("Added movie data: " + movieData);
            }
            driver.navigate().back();
            yearLinks = driver.findElements(By.cssSelector(".col-md-6 a"));  // Re-fetch the year links after navigation
        }

        saveDataToFile(oscarData);
        Assert.assertTrue(new File(OUTPUT_FILE_PATH).exists() && new File(OUTPUT_FILE_PATH).length() > 0, "Output file is missing or empty");

        System.out.println("End Test: scrapeOscarWinningFilms");
    }

    private void saveDataToFile(ArrayList<HashMap<String, Object>> data) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File outputFile = new File(OUTPUT_FILE_PATH);
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();  // Create directories if they don't exist
        }
        objectMapper.writeValue(outputFile, data);

        System.out.println("End Test case: testCase02");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}