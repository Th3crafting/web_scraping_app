package com.gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class MainScreenController {
    @FXML
    private TextField txtUrlPagina;
    @FXML
    protected void onScrapButtonClick(){
        String urlEnUso = txtUrlPagina.getText();
        System.out.println(urlEnUso);
    }
}
