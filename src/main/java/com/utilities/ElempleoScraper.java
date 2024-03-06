package com.utilities;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class ElempleoScraper {
    private WebDriver driver;
    private WebDriverWait wait;
    private Set<String> botonesProcesados;

    public ElempleoScraper() {
        System.setProperty("webdriver.chrome.driver", "./chromedriver.exe");

        ChromeOptions options = new ChromeOptions();

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        botonesProcesados = new HashSet<>();
    }

    public void scrape(String url) {
        try {
            driver.get(url);

            int posicion = 1;

            while (true) {
                WebElement boton = null;
                WebElement contenedorPadreCerrar = null;
                WebElement botonCerrar = null;
                WebElement iconoBoton = null;

                try {
                    boton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@datalayertag, 'position') and contains(@datalayertag, ':" + posicion + ",')]")));
                } catch (NoSuchElementException e){
                    break;
                }

                String jobOfferId = boton.getAttribute("data-joboffer");

                if(!botonesProcesados.contains(jobOfferId)) {
                    iconoBoton = wait.until(ExpectedConditions.elementToBeClickable(boton.findElement(By.xpath(".//i[contains(@class, 'fa-eye')]"))));

                    iconoBoton.click();

                    int lecturas = 0;

                    while (lecturas < 3) {
                        try{
                            Thread.sleep(1000);
                            WebElement tituloOfertaElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='modal-header ee-mod']//h3[@class='modal-title offer-title ee-mod js-quick-result-title']//a")));
                            WebElement empresaElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='media']//span[@class='company-name']")));
                            WebElement salarioElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='media-body media-middle']//span[@class='text-primary']")));
                            WebElement lugarElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='data-offer-wrapper']//span[@class='info-city']")));
                            WebElement fechaElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='data-offer-wrapper']//span[@class='pull-right info-publish-date']")));
                            WebElement descripElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[@class='js-description']")));
                            WebElement cargosRelacionadosElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='ee-modal-equivalent-position']//span")));

                            String tituloOferta = tituloOfertaElement.getText().trim();
                            String empresa = empresaElement.getText().trim();
                            String salario = salarioElement.getText().trim();
                            String lugar = lugarElement.getText().trim();
                            String fecha = fechaElement.getText().trim();
                            String descripcion1 = descripElement.getText().trim();
                            String descripcion2 = descripElement.getAttribute("title").trim();
                            String cargosRelacionados = cargosRelacionadosElement.getText().trim();

                            String lineaOferta = String.format("%s;%s;%s;%s;%s;%s;%s;%s", tituloOferta, empresa, salario, lugar, fecha, descripcion1, descripcion2, cargosRelacionados);

                            System.out.println(lineaOferta);

                            break;
                        } catch (StaleElementReferenceException e){
                            System.out.println("Elemento no encontrado. Reintentando...(" + lecturas+1 + "/3)");
                            lecturas++;
                            Thread.sleep(500);
                        }
                    }

                    int intentos = 0;

                    while (intentos < 3) {
                        try{
                            Thread.sleep(2000);
                            contenedorPadreCerrar = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".modal-header.ee-mod")));
                            botonCerrar = wait.until(ExpectedConditions.elementToBeClickable(contenedorPadreCerrar.findElement(By.cssSelector("button.close"))));

                            botonCerrar.click();
                            break;
                        } catch (StaleElementReferenceException e) {
                            System.out.println("Elemento no encontrado. Reintentando...(" + intentos+1 + "/3)");
                            intentos++;
                            Thread.sleep(500);
                        }
                    }
                    botonesProcesados.add(jobOfferId);
                }
                posicion++;
            }
            driver.quit();
        } catch (Exception e){
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}

