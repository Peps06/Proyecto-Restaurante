package com.mycompany.restaurante;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        //Pruebas de uso
        URL resource = getClass().getResource("/com/mycompany/restaurante/fxml/VentanaRegistro.fxml");
//        URL resource = getClass().getResource("/com/mycompany/restaurante/fxml/CobrarPantalla.fxml");

        if (resource == null) {
            throw new RuntimeException("No se encuentra el FXML. Revisa que esté en src/main/resources/com/mycompany/restaurante/");
        }
        Parent root = FXMLLoader.load(resource);
        stage.setScene(new Scene(root));
        stage.setTitle("Restaurante - Pedido Mesero");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}