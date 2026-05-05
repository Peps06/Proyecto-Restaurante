package com.mycompany.dulceria;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Clase principal de la aplicación Dulcería en JavaFX.
 * Punto de entrada de la aplicación que carga la ventana inicial.
 * 
 * @author Dana, Rubi, Citlaly
 * @version 2.0 (JavaFX)
 */
public class App extends Application {

    private static Scene scene;

    /**
     * Método principal que inicia la aplicación JavaFX.
     * Este método es llamado automáticamente al iniciar la aplicación.
     * 
     * @param stage La ventana principal de la aplicación
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Cargar la ventana inicial (puedes cambiar "LoginVentana" por la ventana que quieras)
        scene = new Scene(loadFXML("LoginVentana"), 590, 410);
        
        stage.setTitle("Dulcería - Sistema de Gestión");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Cambia la escena actual por otra ventana FXML.
     * Útil para navegar entre ventanas.
     * 
     * @param LoginVentana El nombre del archivo FXML (sin extensión)
     * @throws IOException Si no se puede cargar el archivo
     */
    public static void setRoot(String LoginVentana) throws IOException {
        scene.setRoot(loadFXML(LoginVentana));
    }

    /**
     * Carga un archivo FXML y retorna su contenido como Parent.
     * 
     * @param LoginVentana El nombre del archivo FXML (sin extensión)
     * @return El contenido del FXML como Parent
     * @throws IOException Si no se puede cargar el archivo
     */
    private static Parent loadFXML(String LoginVentana) throws IOException {
        var url = App.class.getResource(
            "/com/mycompany/dulceria/fxml/" + LoginVentana + ".fxml"
        );

        if (url == null) {
            throw new IOException(
                "No se encontró: /com/mycompany/dulceria/fxml/" + LoginVentana + ".fxml"
            );
        }

        return new FXMLLoader(url).load();
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
