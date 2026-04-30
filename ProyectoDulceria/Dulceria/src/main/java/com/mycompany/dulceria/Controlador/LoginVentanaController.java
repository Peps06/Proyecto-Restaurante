package com.mycompany.dulceria.Controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.net.URL;

/**
 * Controlador para la ventana de selección de rol (Administrador/Empleado).
 * 
 * @author Dana, Rubi, Citlaly
 * @version 2.0 (JavaFX)
 */
public class LoginVentanaController {

    @FXML
    private Button btnAdministrador;

    @FXML
    private Button btnEmpleado;

    /**
     * Navega a la ventana de inicio de sesión de Administrador.
     */
    @FXML
    private void handleAdministrador() {
        try {
            cambiarEscena("/com/mycompany/dulceria/fxml/LoginAdmin.fxml", btnAdministrador);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navega a la ventana de inicio de sesión de Empleado.
     */
    @FXML
    private void handleEmpleado() {
        try {
            cambiarEscena("/com/mycompany/dulceria/fxml/LoginEmpleado.fxml", btnEmpleado);
        } catch (IOException e) {            
            e.printStackTrace();
        }
    }

    /**
     * Método genérico para cambiar de ventana.
     */
    private void cambiarEscena(String rutaFXML, Button botonReferencia) throws IOException {
        URL url = getClass().getResource(rutaFXML);
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();

        Stage stage = (Stage) botonReferencia.getScene().getWindow();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML
    private void handleSalir() {
        System.exit(0);
    }
}