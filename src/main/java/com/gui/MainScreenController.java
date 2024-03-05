package com.gui;

import com.utilities.ConexionPagina;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public class MainScreenController {
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
    protected void onScrapButtonClick(){
        int numSelected = 0;
        RadioButton selectedCheckBox = null;

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
            String urlEnUso = txtUrlPagina.getText();

            txtStatus.setText("Realizando scraping...");
            Platform.runLater(() -> {
                onScrapingStarted();
            });

            RadioButton finalSelectedCheckBox = selectedCheckBox;

            new Thread(() -> {
                if (finalSelectedCheckBox == checkCompuTrabajo) {
                    System.out.println("Computrabajo");
                } else if (finalSelectedCheckBox == checkElEmpleo) {
                    System.out.println("Elempleo");
                } else if (finalSelectedCheckBox == checkLinkedIn) {
                    System.out.println("LinkedIn");
                } else if (finalSelectedCheckBox == checkTest) {
                    ConexionPagina WebScraper = new ConexionPagina();
                    WebScraper.scrape(urlEnUso);
                }

                Platform.runLater(() -> {
                    onScrapingFinished();
                });
            }).start();
        }else {
            txtStatus.setText("Seleccione solo una página.");
        }
    }

    private void onScrapingStarted() {
        txtStatus.setText("Realizando scraping...");
    }

    private void onScrapingFinished() {
        txtStatus.setText("Scraping completo.");
    }
}
