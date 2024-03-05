package com.utilities;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ElempleoScraper {
    private WebDriver driver;
    private WebDriverWait wait;

    public ElempleoScraper() {
        System.setProperty("webdriver.chrome.driver", "./chromedriver.exe");

        ChromeOptions options = new ChromeOptions();

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void scrape(String url) {
        try {
            driver.get(url);

            int posicion = 1;

            while (true) {
                WebElement boton = null;
                try {
                    boton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@datalayertag, 'position') and contains(@datalayertag, '" + posicion + "')]")));
                } catch (NoSuchElementException e){
                    break;
                }

                WebElement iconoBoton = boton.findElement(By.xpath(".//i[contains(@class, 'fa-eye')]"));

                iconoBoton.click();

                System.out.println("Botón: " + posicion);

                WebElement contenedorPadreCerrar = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".modal-header.ee-mod")));
                WebElement botonCerrar = wait.until(ExpectedConditions.elementToBeClickable(contenedorPadreCerrar.findElement(By.cssSelector("button.close"))));

                botonCerrar.click();

                posicion++;
            }
        } catch (Exception e){
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
