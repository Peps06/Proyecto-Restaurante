package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.PedidoDAO;
import com.mycompany.restaurante.Modelo.OrdenCocina;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Controlador de PedidosActuales.fxml (panel del chef).
 *
 * En lugar de usar un TableView, construye dinámicamente un GridPane
 * de 2 columnas donde cada celda es una tarjeta PedidoCelda.fxml.
 *
 * @author Citlaly
 * @version 2.0 (GridPane dinámico)
 */
public class PedidosActualesController implements Initializable {

    @FXML private GridPane gridPedidos;
    @FXML private Label cantidadPedidos; // "X Pendientes" en la cabecera

    @FXML private Button btnPedidosActuales;
    @FXML private Button btnHistorial;
    @FXML private Button btnCerrarSesion;

    // Ancho fijo de cada tarjeta
    private static final double ANCHO_TARJETA = 420.0;
    // Espacio entre tarjetas
    private static final double GAP = 20.0;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar el gap del GridPane
        gridPedidos.setHgap(GAP);
        gridPedidos.setVgap(GAP);
        gridPedidos.setPadding(new Insets(GAP));

        // Cargar las tarjetas al iniciar la pantalla
        cargarTarjetas();
    }

    /**
     * Consulta las órdenes en espera y construye las tarjetas en el GridPane.
     * Se puede llamar en cualquier momento para refrescar la vista.
     */
    private void cargarTarjetas() {
        // 1. Limpiar el grid antes de reconstruirlo
        gridPedidos.getChildren().clear();

        // 2. Obtener órdenes desde la BD
        List<OrdenCocina> ordenes = PedidoDAO.obtenerOrdenesEnEspera();

        // 3. Actualizar contador en la cabecera
        int total = ordenes.size();
        cantidadPedidos.setText(total + (total == 1 ? " Pendiente" : " Pendientes"));
        cantidadPedidos.setStyle("-fx-font-style: italic; -fx-text-fill: #8b6914;");

        if (ordenes.isEmpty()) {
            // Mensaje amigable cuando no hay pedidos
            Label sinPedidos = new Label("No hay pedidos pendientes en cocina.");
            sinPedidos.setStyle("-fx-font-size: 16px; -fx-text-fill: #627096;");
            gridPedidos.add(sinPedidos, 0, 0);
            return;
        }

        // 4. Insertar tarjetas en el grid — 4 columnas
        int col = 0;
        int row = 0;

        for (OrdenCocina orden : ordenes) {
            Node tarjeta = crearTarjeta(orden);
            if (tarjeta != null) {
                gridPedidos.add(tarjeta, col, row);

                col++;
                if (col == 4) {   // saltar a la siguiente fila al llegar a col 4
                    col = 0;
                    row++;
                }
            }
        }
    }

    /**
     * Carga PedidoCelda.fxml, inyecta los datos de la orden y configura
     * el callback de "Marcar Listo" para que refresque el grid al dispararse.
     *
     * @param orden La orden que representa esta tarjeta.
     * @return El nodo raíz de la tarjeta, o null si hubo un error de carga.
     */
    private Node crearTarjeta(OrdenCocina orden) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/mycompany/restaurante/fxml/PedidoCelda.fxml"
            ));
            Node root = loader.load();

            // Pasar datos al controlador de la tarjeta.
            PedidoCeldaController ctrl = loader.getController();
            ctrl.initData(orden, idOrden -> {
                System.out.println("[PedidosActualesController] "
                    + "Orden #" + idOrden + " lista — refrescando grid.");
                cargarTarjetas();
            });

            return root;

        } catch (IOException e) {
            System.err.println("[PedidosActualesController] "
                + "Error cargando PedidoCelda.fxml: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // BOTONES

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            cambiarPantalla(event,
                "/com/mycompany/restaurante/fxml/LoginPantalla.fxml",
                "Iniciar sesión");
        }
    }

    @FXML
    private void handlePedidosActuales(ActionEvent event) {
        cargarTarjetas();
    }

    @FXML
    private void handleHistorial(ActionEvent event) {
        cambiarPantalla(event,
            "/com/mycompany/restaurante/fxml/HistorialCocina.fxml",
            "Historial de Cocina");
    }

    // HELPER

    private void cambiarPantalla(ActionEvent event, String fxmlPath, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stageActual = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stageActual.setScene(new Scene(root));
            stageActual.setTitle(titulo + " - Saveurs Paris");
            stageActual.centerOnScreen();
            stageActual.show();
        } catch (IOException e) {
            System.err.println("[PedidosActualesController] "
                + "Error cargando pantalla: " + fxmlPath);
            e.printStackTrace();
        }
    }
}