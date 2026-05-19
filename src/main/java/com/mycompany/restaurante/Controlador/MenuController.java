package com.mycompany.restaurante.Controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

/**
 * 
 * @author Dana
 */
public class MenuController {

    @FXML
    private void handleRegresarLogin(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));
            
            Node nodoOrigen = (Node) event.getSource();
            Stage stageActual = (Stage) nodoOrigen.getScene().getWindow();
            
            Scene nuevaEscena = new Scene(root);
            stageActual.setScene(nuevaEscena);
            stageActual.setTitle("Iniciar sesión - Saveurs Paris");
            stageActual.centerOnScreen();
            stageActual.show();
            
        } catch (IOException e) {
            System.err.println("Error al regresar a la pantalla de Login");
            e.printStackTrace();
        }
    }
}