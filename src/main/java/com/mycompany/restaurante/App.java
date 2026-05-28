package com.mycompany.restaurante;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

/**
 * Clase principal de la aplicación Restaurante.
 * Punto de entrada de la aplicación que carga la ventana inicial.
 * 
 * @author Dana, Rubi y Citlaly
 * @version 1.0
 */
public class App extends Application {
    
    /**
     * Método principal que inicia la aplicación JavaFX.
     * Este método es llamado automáticamente al iniciar la aplicación.
     * 
     * @param stage La ventana principal de la aplicación
     * @throws java.lang.Exception
     */
    @Override
    public void start(Stage stage) throws Exception {

        URL resource = getClass().getResource("/com/mycompany/restaurante/fxml/LoginPantalla.fxml");

        Parent root = FXMLLoader.load(resource);
        stage.setScene(new Scene(root));
        stage.setTitle("Restaurante - Saveurs");
        stage.show();
    }

    /**
     * Punto de entrada principal de la aplicación.
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        launch(args);
    }
}
