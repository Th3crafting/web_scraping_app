package com.utilities;

import com.opencsv.CSVWriter;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class LinkedInScraper {
    private WebDriver driver;

    private WebDriverWait wait;

    private CSVWriter writer;
    private ProgressBar progressBar;

    public LinkedInScraper(ProgressBar progressBar) {
        this.progressBar = progressBar;
        System.setProperty("webdriver.chrome.driver", "./chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        //options.addArguments("--headless");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(3));
    }

    public void setProgressBar(ProgressBar progressBar) { this.progressBar = progressBar; }

    public void scrape(String url) {
        try {
            System.out.println(navigateToPage(url));

            System.out.println(createArchive());

            startScrap();

            for (int i=1;i>0;i++){
                Thread.sleep(20000);
                if ((i%2) == 0) {
                    System.out.println(i);
                }
            }

            writer.close();
            driver.quit();
        } catch (Exception e) {
            System.out.println("Error Rama principal -> " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    @NotNull
    private String navigateToPage(String url) {
        try{
            driver.get(url);
            return "Página encontrada.";
        } catch (Exception e) {
            return "Error al encontrar la página || " + e.getMessage();
        }
    }

    @NotNull
    private String createArchive() throws IOException {
        Date fechaActual = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy - HH-mm");
        String fechaFormateada = formato.format(fechaActual);

        String carpeta = "scrapFiles";

        File archivoCSV = new File(carpeta + File.separator + "LinkedInScrap - " + fechaFormateada + ".csv");

        if (!archivoCSV.getParentFile().exists()) {
            archivoCSV.getParentFile().mkdirs();
        }

        FileWriter fileWriter = new FileWriter(archivoCSV);
        this.writer = new CSVWriter(fileWriter, ';', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        return "Archivo creado correctamente.";
    }

    private void startScrap() {
        try {
            String tituloPagina = driver.getTitle();
            System.out.println("Título de la página " + tituloPagina);
        } catch (Exception e) {
            System.out.println("Error durante el scraping " + e.getMessage());
        }
    }

    private void updateProgressBar(int currentPos, int maxPos) {
        Platform.runLater(() -> {
            double progress;
            if (currentPos <= maxPos) {
                progress = (double) currentPos / maxPos;
            } else {
                progress = 1.0;
            }
            progressBar.setProgress(progress);
        });
    }

    public void cancelScraper() {
        System.out.println("Cancelado.");
    }
}
