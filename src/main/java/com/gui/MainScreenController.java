package com.gui;

import com.utilities.ComputrabajoScraper;
import com.utilities.ConexionPagina;
import com.utilities.ElempleoScraper;
import com.utilities.LinkedInScraper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.jetbrains.annotations.Nullable;

public class MainScreenController {
    private final ElempleoScraper webScraper;

    @FXML
    private Button buttonScrap;

    @FXML
    private Button buttonCancel;

    @FXML
    private TextField txtUrlPagina;

    @FXML
    private Label txtStatus;

    @FXML
    private RadioButton checkCompuTrabajo;

    @FXML
    private RadioButton checkElEmpleo;

    @FXML
    private RadioButton checkLinkedIn;

    @FXML
    private RadioButton checkTest;

    @FXML
    private ProgressBar progressBar;

    private ElempleoScraper elempleoScraper;
    private ComputrabajoScraper computrabajoScraper;
    private LinkedInScraper linkedInScraper;
    private ConexionPagina conexionPagina;


    public MainScreenController() {
        webScraper = new ElempleoScraper(progressBar);
    }

    @FXML
    protected void onScrapButtonClick(){
        RadioButton selectedCheckBox;
        if ((selectedCheckBox = checkSelectedCount()) != null) {
            String urlEnUso = txtUrlPagina.getText();

            txtStatus.setText("Realizando scraping...");

            Platform.runLater(this::onScrapingStarted);

            new Thread(() -> {
                if (selectedCheckBox == checkCompuTrabajo) {
                    System.out.println("Computrabajo");
                } else if (selectedCheckBox == checkElEmpleo) {
                    elempleoScraper = new ElempleoScraper(progressBar);
                    elempleoScraper.scrape(urlEnUso);
                } else if (selectedCheckBox == checkLinkedIn) {
                    linkedInScraper = new LinkedInScraper(progressBar);
                    linkedInScraper.scrape(urlEnUso);
                } else if (selectedCheckBox == checkTest) {
                    conexionPagina = new ConexionPagina();
                    conexionPagina.scrape(urlEnUso);
                }

                Platform.runLater(this::onScrapingFinished);
            }).start();
        }
    }

    @FXML
    protected void onCancelButtonClick () {
        RadioButton selectedCheckBox;
        if ((selectedCheckBox = checkSelectedCount()) != null) {
            txtStatus.setText("Scraping cancelado.");

            new Thread(() -> {
                if (selectedCheckBox == checkCompuTrabajo) {
                    System.out.println("Computrabajo");
                } else if (selectedCheckBox == checkElEmpleo) {
                    elempleoScraper.cancelScraper();
                } else if (selectedCheckBox == checkLinkedIn) {
                    linkedInScraper.cancelScraper();
                } else if (selectedCheckBox == checkTest) {
                    System.out.println("Cancelado.");
                }

                Platform.runLater(this::onScrapingFinished);
            }).start();
        }
    }

    private void onScrapingStarted() {
        txtStatus.setText("Realizando scraping...");
        Platform.runLater(() -> {
            webScraper.setProgressBar(progressBar);
            buttonCancel.setDisable(false);
            txtUrlPagina.setDisable(true);
            checkTest.setDisable(true);
            checkLinkedIn.setDisable(true);
            checkElEmpleo.setDisable(true);
            checkCompuTrabajo.setDisable(true);
            buttonScrap.setDisable(true);
        });
    }

    private void onScrapingFinished() {
        txtStatus.setText("Scraping completo.");
        Platform.runLater(() -> {
            progressBar.setProgress(1.0);
            buttonCancel.setDisable(true);
            txtUrlPagina.setDisable(false);
            checkTest.setDisable(false);
            checkLinkedIn.setDisable(false);
            checkElEmpleo.setDisable(false);
            checkCompuTrabajo.setDisable(false);
            buttonScrap.setDisable(false);
        });
    }

    @Nullable
    private RadioButton checkSelectedCount () {
        RadioButton selectedCheckBox = null;
        int numSelected = 0;

        if (checkCompuTrabajo.isSelected()){
            numSelected++;
            selectedCheckBox = checkCompuTrabajo;
        }
        if (checkElEmpleo.isSelected()){
            numSelected++;
            selectedCheckBox = checkElEmpleo;
        }
        if (checkLinkedIn.isSelected()){
            numSelected++;
            selectedCheckBox = checkLinkedIn;
        }
        if (checkTest.isSelected()){
            numSelected++;
            selectedCheckBox = checkTest;
        }

        if (numSelected == 1) {
            return selectedCheckBox;
        }else {
            txtStatus.setText("Seleccione solo una página.");
            return null;
        }
    }
}
