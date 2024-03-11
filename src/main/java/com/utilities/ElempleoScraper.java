package com.utilities;

import com.opencsv.CSVWriter;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ElempleoScraper {
    private WebDriver driver;
    private WebDriverWait wait;
    private Set<String> botonesProcesados;

    public ElempleoScraper() {
        System.setProperty("webdriver.chrome.driver", "./chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        //options.addArguments("--headless");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        botonesProcesados = new HashSet<>();
    }

    public void scrape(String url) {
        try {
            driver.get(url);

            Date fechaActual = new Date();
            SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy - HH-mm");
            String fechaFormateada = formato.format(fechaActual);

            File archivoCSV = new File("ElempleoScrap - " + fechaFormateada + ".csv");
            FileWriter fileWriter = new FileWriter(archivoCSV);
            CSVWriter csvWriter = new CSVWriter(fileWriter, ';', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            csvWriter.writeNext(new String[]{"tituloOferta", "empresa", "salario", "lugar", "fecha", "descripcion1", "descripcion2", "cargosRelacionados"});

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver driver) {
                    return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                }
            });

            int posicion = 1;
            boolean firstTime = true;

            while (true) {
                WebElement boton = null;
                WebElement contenedorPadreCerrar = null;
                WebElement botonCerrar = null;
                WebElement iconoBoton = null;
                WebElement botonCookies = null;
                WebElement iconoCookies = null;

                if (firstTime == true) {
                    try {
                        botonCookies = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("buttons-politics")));

                        iconoCookies = wait.until(ExpectedConditions.elementToBeClickable(botonCookies.findElement(By.xpath("//a[contains(@class, 'btn btn-default submit-politics btnAcceptPolicyNavigationCO')]"))));

                        iconoCookies.click();
                        firstTime = false;
                    } catch (NoSuchElementException e) {
                        break;
                    }
                }

                try {
                    boton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'btn btn-ghost-reverse btn-block')]")));
                } catch (NoSuchElementException e){
                    break;
                }

                String jobOfferId = boton.getAttribute("data-joboffer");

                if(!botonesProcesados.contains(jobOfferId)) {
                    iconoBoton = wait.until(ExpectedConditions.elementToBeClickable(boton.findElement(By.xpath(".//i[contains(@class, 'fa-eye')]"))));

                    iconoBoton.click();

                    int lecturas = 0;

                    while (lecturas < 3) {
                        String tituloOferta = null;
                        String empresa = null;
                        String salario = null;
                        String lugar = null;
                        String fecha = null;
                        String descripcion1 = null;
                        String descripcion2 = null;
                        String cargosRelacionados = null;

                        try{
                            Thread.sleep(1000);

                            WebElement tituloOfertaElement = null;
                            try {
                                tituloOfertaElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='modal-header ee-mod']//h3[@class='modal-title offer-title ee-mod js-quick-result-title']//a")));
                            } catch (Exception e) {
                                System.err.println("Error inesperado: " + e.getMessage());
                            }

                            WebElement empresaElement = null;
                            try {
                                empresaElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='media']//span[@class='company-name']")));
                            } catch (Exception e) {
                                System.err.println("Error inesperado: " + e.getMessage());
                            }

                            WebElement salarioElement = null;
                            try {
                                salarioElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='media-body media-middle']//span[@class='text-primary']")));
                            } catch (Exception e) {
                                System.err.println("Error inesperado: " + e.getMessage());
                            }

                            WebElement lugarElement = null;
                            try {
                                lugarElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='data-offer-wrapper']//span[@class='info-city']")));
                            } catch (Exception e) {
                                System.err.println("Error inesperado: " + e.getMessage());
                            }

                            WebElement fechaElement = null;
                            try {
                                fechaElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='data-offer-wrapper']//span[@class='pull-right info-publish-date']")));
                            } catch (Exception e) {
                                System.err.println("Error inesperado: " + e.getMessage());
                            }

                            WebElement descripElement = null;
                            try {
                                descripElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[@class='js-description' and @title]")));
                            } catch (Exception e) {
                                System.err.println("Error inesperado: " + e.getMessage());
                            }

                            WebElement cargosRelacionadosElement = null;
                            try {
                                cargosRelacionadosElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='ee-modal-equivalent-position']//span")));
                            } catch (Exception e) {
                                System.err.println("Error inesperado: " + e.getMessage());
                            }

                            tituloOferta = tituloOfertaElement != null ? tituloOfertaElement.getText().trim() : "";
                            empresa = empresaElement != null ? empresaElement.getText().trim() : "";
                            salario = salarioElement != null ? salarioElement.getText().trim() : "";
                            lugar = lugarElement != null ? lugarElement.getText().trim() : "";
                            fecha = fechaElement != null ? fechaElement.getText().trim() : "";
                            descripcion1 = descripElement != null ? descripElement.getText().trim() : "";
                            descripcion2 = descripElement != null ? descripElement.getAttribute("title").trim() : "";
                            cargosRelacionados = cargosRelacionadosElement != null ? cargosRelacionadosElement.getText().trim() : "";

                            String posiciontxt = String.valueOf(posicion);

                            String[] lineaOferta = new String[]{posiciontxt, tituloOferta, empresa, salario, lugar, fecha, descripcion1, descripcion2, cargosRelacionados};
                            csvWriter.writeNext(lineaOferta);

                            System.out.println("Oferta: " + posicion);

                            break;
                        } catch (StaleElementReferenceException e){
                            int numLecturas = lecturas + 1;
                            System.out.println("Elemento no encontrado. Reintentando...(" + numLecturas + "/3)");
                            lecturas++;
                            Thread.sleep(500);
                        } catch (Exception e) {
                            System.err.println("Error inesperado: " + e.getMessage());
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
                            int numIntentos = intentos + 1;
                            System.out.println("Elemento no encontrado. Reintentando...(" + numIntentos + "/3)");
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
