package com.utilities;

import com.opencsv.CSVWriter;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;

public class ElempleoScraper {
    private WebDriver driver;
    private WebDriverWait wait;
    private CSVWriter writer;
    private ProgressBar progressBar;

    public ElempleoScraper(ProgressBar progressBar) {
        this.progressBar = progressBar;
        System.setProperty("webdriver.chrome.driver", "./chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        options.addArguments("--headless");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }
    public void setProgressBar (ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void scrape(String url) {
        try {
            int ofertasProcesadas = 0;
            System.out.println(navigateToPage(url));

            System.out.println(createArchive());

            System.out.println(changePageMaxView());

            int cantOffersXPage = findOffersXPage();
            System.out.println("Cantidad de ofertas por página: " + cantOffersXPage);

            int cantTotalOffers = findTotalOffers();
            System.out.println("Cantidad total de ofertas de la página: " + cantTotalOffers);

            int pagina = 1;

            while (ofertasProcesadas <= cantTotalOffers) {
                ofertasProcesadas += startScrap(cantTotalOffers, pagina);
                if ((ofertasProcesadas < (ofertasProcesadas + 20)) && (ofertasProcesadas + 20 < cantTotalOffers)) {
                    System.out.println(changePageIndex());
                    pagina++;
                }
                System.out.println("Ofertas totales procesadas: " + ofertasProcesadas);
            }

            writer.close();
            driver.quit();
        } catch (Exception e) {
            System.out.println("Error Rama principal -> " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private String navigateToPage (String url) {
        try {
            driver.get(url);
            return "Página encontrada";
        } catch (Exception e) {
            System.out.println("Error url navegar pagina -> " + e.getMessage());
            e.printStackTrace();
            return "Error en la página";
        }
    }

    private String createArchive() throws IOException {
        Date fechaActual = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy - HH-mm");
        String fechaformateada = formato.format(fechaActual);

        String carpeta = "scrapFiles";

        File archivoCSV = new File(carpeta + File.separator + "ElempleoScrap - " + fechaformateada + ".csv");

        if (!archivoCSV.getParentFile().exists()) {
            archivoCSV.getParentFile().mkdirs();
        }

        FileWriter fileWriter = new FileWriter(archivoCSV);
        this.writer = new CSVWriter(fileWriter, ';', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        writer.writeNext(new String[]{"tituloOferta", "empresa", "salario", "lugar", "fecha", "descripcion1", "descripcion2", "cargoRelacionados"});

        return "Archivo creado correctamente";
    }

    private int startScrap(int cantTotalOfertas, int pagina) {
        try {
            List<WebElement> botonesDeAperturaInformacion = driver.findElements(By.xpath("//div[@class='result-list js-result-list js-results-container']/div[@class='result-item']/div[@class='wrapper-btn-item hidden-xs']/button[@class='btn btn-ghost-reverse btn-block js-quickview']\n"));
            boolean firstTime = true;
            int posicionAct = 1;

            for (WebElement btnAperturaInformacion : botonesDeAperturaInformacion) {
                if (pagina == 1) {
                    if (firstTime) {
                        firstTime = checkFirstTime();
                    }
                }

                WebElement iconoBtnInfo = searchBtnInfoIcon(btnAperturaInformacion);
                assert iconoBtnInfo != null;
                iconoBtnInfo.click();

                int lecturas = 0;

                while (lecturas < 3) {
                    try {
                        Thread.sleep(500);
                        String[] lineaOferta = getJobOffer();
                        writer.writeNext(lineaOferta);
                        break;
                    } catch (StaleElementReferenceException e) {
                        int numLecturas = lecturas + 1;
                        System.out.println("Elemento no encontrado. Reintentando...(" + numLecturas + "/3)");
                        lecturas++;
                        Thread.sleep(250);
                    } catch (Exception e) {
                        System.out.println("Error -> " + e.getMessage());
                    }
                }

                int intentos = 0;

                while (intentos < 3) {
                    try {
                        Thread.sleep(250);
                        WebElement contenedorPadreCerrar = searchContenedorPadre();
                        assert contenedorPadreCerrar != null;
                        WebElement btnCerrar = wait.until(ExpectedConditions.elementToBeClickable(contenedorPadreCerrar.findElement(By.cssSelector("button.close"))));
                        btnCerrar.click();
                        break;
                    } catch (StaleElementReferenceException e){
                        int numIntentos = intentos + 1;
                        System.out.println("Elemento no encontrado. Reintentando...(" + numIntentos + "/3)");
                        intentos++;
                        Thread.sleep(250);
                    }
                }
                System.out.println("Ofertas transcritas: " + posicionAct);
                updateProgressBar(posicionAct, cantTotalOfertas);
                posicionAct++;
            }
            Thread.sleep(10000);
            return posicionAct - 1;
        } catch (Exception e) {
            return -1;
        }
    }

    private String changePageIndex() {
        WebElement btnCambioPagina;
        try {
            btnCambioPagina = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@class='js-btn-next']//i[@class='fa fa-angle-right']")));
            assert btnCambioPagina != null;
            btnCambioPagina.click();
            Thread.sleep(5000);
            return "Cambio de Página exitoso";
        } catch (Exception e){
            return "Error al cambiar página || " + e.getMessage();
        }
    }

    private String[] getJobOffer() {
        WebElement tituloOfertaElement = getTitleElement();
        WebElement empresaElement = getCompanyElement();
        WebElement salarioElement = getSalaryElement();
        WebElement lugarElement = getCityElement();
        WebElement fechaElement = getDateElement();
        WebElement descripElement = getDescripElement();
        WebElement cargosElement = getPositionElement();

        String  tituloOferta = tituloOfertaElement != null ? tituloOfertaElement.getText().trim() : "";
        String empresa = empresaElement != null ? empresaElement.getText().trim() : "";
        String salario = salarioElement != null ? salarioElement.getText().trim() : "";
        String lugar = lugarElement != null ? lugarElement.getText().trim() : "";
        String fecha = fechaElement != null ? fechaElement.getText().trim() : "";
        String descripcion1 = descripElement != null ? descripElement.getText().trim() : "";
        String descripcion2 = descripElement != null ? descripElement.getAttribute("title").trim() : "";
        String cargosRelacionados = cargosElement != null ? cargosElement.getText().trim() : "";

        return new String[]{tituloOferta, empresa, salario, lugar, fecha, descripcion1, descripcion2, cargosRelacionados};
    }

    private boolean checkFirstTime() {
        WebElement btnCookies;
        WebElement iconoBtnCookies;
        try {
            btnCookies = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("buttons-politics")));
            iconoBtnCookies = wait.until(ExpectedConditions.elementToBeClickable(btnCookies.findElement(By.xpath("//a[contains(@class, 'btn btn-default submit-politics btnAcceptPolicyNavigationCO')]"))));

            iconoBtnCookies.click();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return false;
    }

    private WebElement searchBtnInfoIcon (WebElement btnAperturaInformacion) {
        WebElement icono;
        try {
            icono = wait.until(ExpectedConditions.elementToBeClickable(btnAperturaInformacion.findElement(By.xpath(".//i[contains(@class, 'fa-eye')]"))));
            return icono;
        } catch (NoSuchElementException e) {
            System.out.println("Botón desplegar oferta no encontrado || " + e.getMessage());
            return null;
        }
    }

    private String changePageMaxView () {
        WebElement changePageContainer;
        WebElement numberOfMaxView;
        try {
            changePageContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//select[contains(@id, 'ResultsByPage')]")));
            numberOfMaxView = wait.until(ExpectedConditions.elementToBeClickable(changePageContainer.findElement(By.xpath(".//option[@value='20']"))));
            numberOfMaxView.click();
            Thread.sleep(3000);
            return "Se cambio la cantidad de ofertas por página";
        } catch (Exception e) {
            System.out.println("Error en el cambio de página " + e.getMessage());
            return null;
        }
    }

    private WebElement getTitleElement () {
        WebElement element;
        try {
            Thread.sleep(250);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='modal-header ee-mod']//h3[@class='modal-title offer-title ee-mod js-quick-result-title']//a")));
            return element;
        } catch (Exception e) {
            System.out.println("Titulo de la oferta no encontrado || " + e.getMessage());
            return null;
        }
    }

    private WebElement getCompanyElement () {
        WebElement element;
        try {
            Thread.sleep(250);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='media']//span[@class='company-name']")));
            return element;
        } catch (Exception e) {
            System.out.println("Nombre de la empresa no encontrado || " + e.getMessage());
            return null;
        }
    }

    private WebElement getSalaryElement () {
        WebElement element;
        try {
            Thread.sleep(250);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='media-body media-middle']//span[@class='text-primary']")));
            return element;
        } catch (Exception e) {
            System.out.println("Salario no encontrado || " + e.getMessage());
            return null;
        }
    }

    private WebElement getCityElement () {
        WebElement element;
        try {
            Thread.sleep(250);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='data-offer-wrapper']//span[@class='info-city']")));
            return element;
        } catch (Exception e) {
            System.out.println("Ubicacion de la oferta no encontrada || " + e.getMessage());
            return null;
        }
    }

    private WebElement getDateElement () {
        WebElement element;
        try {
            Thread.sleep(250);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='data-offer-wrapper']//span[@class='pull-right info-publish-date']")));
            return element;
        } catch (Exception e) {
            System.out.println("Fecha de la oferta no encontrada || " + e.getMessage());
            return null;
        }
    }

    private WebElement getDescripElement () {
        WebElement element;
        try {
            Thread.sleep(250);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[@class='js-description' and @title]")));
            return element;
        } catch (Exception e) {
            System.out.println("Descripcion de la oferta no encontrada || " + e.getMessage());
            return null;
        }
    }

    private WebElement getPositionElement () {
        WebElement element;
        try {
            Thread.sleep(250);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='ee-modal-equivalent-position']//span")));
            return element;
        } catch (Exception e) {
            System.out.println("Cargos relacionados no encontrados || " + e.getMessage());
            return null;
        }
    }

    private WebElement searchContenedorPadre () {
        WebElement element;
        try {
            Thread.sleep(1000);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".modal-header.ee-mod")));
            return element;
        } catch (Exception e) {
            System.out.println("Contenedor Padre del botón cerrar no encontrado || " + e.getMessage());
            return null;
        }
    }

    private int findOffersXPage () {
        WebElement cantMaxOfertasPag;
        try {
            cantMaxOfertasPag = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[@class='results-data js-results-data']//strong[@class='js-end-index']")));
            String maxPosicionTxt = cantMaxOfertasPag != null ? cantMaxOfertasPag.getText().trim() : "";
            return Integer.parseInt(maxPosicionTxt);
        } catch (Exception e) {
            System.out.println("Cantidad de ofertas por página no encontrada || " + e.getMessage());
            return -1;
        }
    }

    private int findTotalOffers (){
        WebElement cantTotalOfertas;
        try {
            cantTotalOfertas = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[@class='results-data js-results-data']//strong[@class='js-total-results']")));
            String maxOfertasTxt = cantTotalOfertas != null ? cantTotalOfertas.getText().trim() : "";
            return Integer.parseInt(maxOfertasTxt);
        } catch (Exception e) {
            System.out.println("Cantidad total de ofertas de la página no encontrada || " + e.getMessage());
            return -1;
        }
    }

    private void updateProgressBar (int currentPos, int maxPos) {
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
}