package com.mycompany.dulceria.Controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException; 
import java.net.URL;
import javafx.scene.control.PasswordField;

/**
 * Controlador para la gestión del inicio de sesión
 * 
 * @author Dana, Rubi, Citlaly
 * @version 2.0 (JavaFX)
 */
public class LoginAdminController {

    @FXML 
    private TextField txtNombre;

    @FXML 
    private PasswordField txtContraseña;

    @FXML 
    private Button btnAceptar;
    
    @FXML
    private Button btnRegresar;

    @FXML
    private void initialize() {
         
        txtNombre.setText("admin");
    }
    
    /**
     * Método para confirmar la el inicio de sesión de admin
     */
    @FXML
    private void handleAceptar() {
        String nombre = txtNombre.getText();
        String contra = txtContraseña.getText();
        
        String usuarioCorrecto = "admin";
        String passCorrecta = "1234";
        
        if (nombre.equals(usuarioCorrecto) && contra.equals(passCorrecta)) {
                 
            try {
                switchToMenuAdmin();
            } catch (IOException e) {
                mostrarAlerta("Error al cargar MenuAdmin: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }

        } else {
            mostrarAlerta("Usuario o contraseña incorrectos", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleRegresar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/dulceria/fxml/LoginVentana.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegresar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            mostrarAlerta("Error al regresar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Carga el archivo FXML de la siguiente ventana y cierra la actual.
     */
    private void switchToMenuAdmin() throws IOException {
        URL url = getClass().getResource("/com/mycompany/dulceria/fxml/MenuAdmin.fxml");

        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();

        Stage stage = (Stage) btnAceptar.getScene().getWindow();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Método auxiliar para mostrar alertas.
     */
    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Sistema Dulcería");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}