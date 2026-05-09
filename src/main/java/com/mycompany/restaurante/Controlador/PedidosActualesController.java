package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.Modelo.Pedido;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 *
 * @author Citlaly
 */
public class PedidosActualesController implements Initializable{
    
    @FXML private Button btnPedidosActuales;
    @FXML private Button btnHistorial;
    @FXML private Button btnCerrarSesion;
    
    @FXML private TableView<Pedido> tablaPedidos;
    @FXML private TableColumn columna1;
    @FXML private TableColumn columna2;
    
    /**
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb){
        
    }
    
    // -- Metodos para los botones --
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");

        // Mostrar y esperar respuesta
        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            cambiarPantalla(event, "/com/mycompany/restaurante/fxml/LoginPantalla.fxml", "Iniciar sesión");
        } else {
            // El usuario canceló, no se hace nada y se queda en la ventana
            alerta.close();
        }   
    }
    
    @FXML
    private void handlePedidosActuales(ActionEvent event){
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/PedidosActuales.fxml", "Pedidos Actuales");
    }
    
    @FXML
    private void handleHistorial(ActionEvent event){
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/HistorialCocina.fxml", "Historial de Cocina");
    }
    
    // Helpers
    private void cambiarPantalla(ActionEvent event, String fxmlPath, String titulo) {
        try {
            // 1. Cargar la vista desde el recurso
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            // 2. Obtener el Stage (ventana) actual
            javafx.scene.Node nodoOrigen = (javafx.scene.Node) event.getSource();
            Stage stageActual = (Stage) nodoOrigen.getScene().getWindow();

            // 3. Configurar la nueva escena y mostrarla
            Scene nuevaEscena = new Scene(root);
            stageActual.setScene(nuevaEscena);
            stageActual.setTitle(titulo + " - Saveurs Paris");
            stageActual.centerOnScreen();
            stageActual.show();

        } catch (java.io.IOException e) {
            System.err.println("Error al cargar la pantalla: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
