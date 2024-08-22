package org.example;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Scraper {

    private static final Logger logger = LoggerFactory.getLogger(Scraper.class);
    private static final int MAX_THREADS = 8;
    private ExecutorService executorService;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStreamCaptor;

    @Before
    public void setUp() {
        // Capture System.out
        originalOut = System.out;
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        executorService = Executors.newFixedThreadPool(MAX_THREADS);
        // Set up the WebDriver, assuming ChromeDriver is in the system PATH
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\markl\\IdeaProjects\\ScrapingProject\\src\\main\\java\\org\\example\\chromedriver.exe");
    }

    @Test
    public void test() {
        // Path to the input CSV file
        java.lang.String inputFilePath = "C:\\Users\\markl\\IdeaProjects\\ScrapingProject\\src\\main\\java\\org\\example\\listofnames2.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            // Skip the header line

            // Process each line in the CSV file
            while ((line = br.readLine()) != null) {
                final String currentLine = line;
                executorService.submit(() -> processRow(currentLine));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Wait for all tasks to complete
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Print all captured output
        System.out.println("Captured output:\n" + outputStreamCaptor.toString());
    }

    private void processRow(String line) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-web-security");
        options.addArguments("--remote-allow-origins=*");

        WebDriver driver = new ChromeDriver(options);

        String[] values = line.split(",");
        String url = values[0].trim();
        String nameString = values[1].trim();
        driver.manage().window().maximize();
        // Open the page
        driver.get(url);

        // Additional logic here (e.g., checking for missing email, cost, etc.)

        // Process the row
        String name = nameString.substring(0, nameString.indexOf("(")).trim();
        boolean missingEmail = nameString.contains("(missing email)");
        boolean missingCost = nameString.contains("(missing cost)");
        boolean missingSessionType = nameString.contains("(missing session type)");
        boolean missingWebsite = nameString.contains("(missing website)");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(1000));
        WebElement nameElement = null;

        try {
            nameElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'therapist-intro')]//h1")));
        } catch (Exception e) {
            System.out.println("Element not found within 1 second");
        }

        String email = "";
        if (missingEmail) {
            if (driver.findElements(By.xpath("//div[contains(@class, 'therapist-contacts')]//a[text()='Email Therapist']"))
                    .size() > 0) {
                email = driver
                        .findElement(By.xpath("//div[contains(@class, 'therapist-contacts')]//a[text()='Email Therapist']"))
                        .getAttribute("href").replace("mailto:", "");
            } else {
                email = "NO EMAIL AFTER SCRAPE";
            }
        }

        String costText = "";
        if (missingCost) {
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='profile-locations' and //h4[text()='Cost:']]//span")));
            } catch (Exception e) {
                System.out.println("Element not found within time frame");
            }

            List<WebElement> costElements = driver.findElements(By.xpath("//div[@class='profile-locations' and //h4[text()='Cost:']]//span"));

            if (!costElements.isEmpty()) {
                for (WebElement element : costElements) {
                    costText = element.getAttribute("textContent").trim().replace("\n", "").replace(',', ';');
                }
            } else {
                costText = "NO COST AFTER SCRAPE";
            }
//            costText = "2";
        }


        String sessionTypes = "";
        if (missingSessionType) {
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//section[h3[text()='Types of sessions']]//li")));
            } catch (Exception e) {
                System.out.println("Element not found within time frame");
            }
            List<WebElement> listOfSessionTypes = driver
                    .findElements(By.xpath("//section[h3[text()='Types of sessions']]//li"));
            if (listOfSessionTypes.size() > 0) {
                for (WebElement element : listOfSessionTypes) {
                    sessionTypes += element.getText() + ";";
                }
                sessionTypes = sessionTypes.trim();
            } else {
                sessionTypes = "NO SESSION TYPES AFTER SCRAPE";
            }
        }

        String websiteString = "";
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (missingWebsite) {
            try {
                WebElement ele = driver.findElement(By.xpath("//div[contains(@class, 'therapist-header')]//div[contains(@class, 'block')]//button[text()=\"Show Contact Details\"]"));
//                WebElement ele = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class, 'therapist-header')]//div[contains(@class, 'block')]//button[text()=\"Show Contact Details\"]")));
//                Actions actions = new Actions(driver);
//                actions.moveToElement(ele).click().build().perform();
                ele.click();
            } catch (Exception e) {

                websiteString = e.getMessage();
                System.out.println("Element not found within time frame");
            }
//            websiteString += wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class, 'therapist-header')]//div[h2[text()=\"Website\"]]/a"))).getAttribute("href").trim();
        } else {
            websiteString = "NO WEBSITE AFTER SCRAPE";
        }


        appendDataToCSV(name, email, costText, sessionTypes, websiteString);

        driver.quit();
    }

    private synchronized void appendDataToCSV(String name, String email, String cost, String sessionTypes, String websiteString) {
        String outputFilePath = "C:\\Users\\markl\\IdeaProjects\\ScrapingProject\\src\\main\\java\\org\\example\\retrieved_info.csv"; // Replace with actual path

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true))) {
            writer.write(name + "," + email + "," + cost + "," + sessionTypes + "," + websiteString);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        // Restore the original System.out
        System.setOut(originalOut);

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
