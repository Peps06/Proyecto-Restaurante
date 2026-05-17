package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.HistorialCocinaDAO;
import com.mycompany.restaurante.Modelo.OrdenCocina;
import com.mycompany.restaurante.Modelo.OrdenItem;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controlador de HistorialCocina.fxml.
 *
 * Muestra las órdenes ya completadas (preparacion = Preparada o Entregada)
 * filtradas por fecha y turno (Matutino / Vespertino).
 *
 * Flujo:
 *   1. El chef selecciona fecha y turno.
 *   2. Se carga la tabla maestra de órdenes (una fila por orden).
 *   3. Al seleccionar una orden, la tabla de detalle muestra sus ítems.
 *   4. El resumen (total órdenes y monto) se actualiza en los labels.
 *
 * @author Citlaly
 * @version 1.0
 */
public class HistorialCocinaController implements Initializable {

    // Sidebar 
    @FXML private Button btnPedidosActuales;
    @FXML private Button btnHistorial;
    @FXML private Button btnCerrarSesion;

    // Filtros 
    @FXML private DatePicker dpFecha;
    @FXML private ToggleGroup tgTurno; // RadioButtons en el FXML
    @FXML private RadioButton rbMatutino;
    @FXML private RadioButton rbVespertino;
    @FXML private Button btnBuscar;

    // Resumen
    @FXML private Label lblTotalOrdenes;
    @FXML private Label lblMontoTotal;
    @FXML private Label lblFechaResumen;
    @FXML private Label lblTurnoResumen;

    //  Tabla maestra: lista de órdenes
    @FXML private TableView<OrdenCocina> tablaOrdenes;
    @FXML private TableColumn<OrdenCocina, String> colMesa;
    @FXML private TableColumn<OrdenCocina, String> colIdOrden;
    @FXML private TableColumn<OrdenCocina, String> colEstado;
    @FXML private TableColumn<OrdenCocina, String> colItems;   // resumen "3 productos"

    //  Tabla detalle: ítems de la orden seleccionada 
    @FXML private TableView<OrdenItem> tablaDetalle;
    @FXML private TableColumn<OrdenItem, Integer> colCantidad;
    @FXML private TableColumn<OrdenItem, String> colProducto;
    @FXML private TableColumn<OrdenItem, String> colPrecioUnit;
    @FXML private TableColumn<OrdenItem, String> colSubtotal;

    @FXML private Label lblNotasOrden;   // muestra ordenes.detalles de la orden seleccionada

    // Formato 
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Fecha por defecto = hoy
        dpFecha.setValue(LocalDate.now());

        // 2. Turno por defecto = Matutino
        rbMatutino.setSelected(true);

        // 3. Columnas de la tabla maestra
        colMesa.setCellValueFactory(
                data -> new SimpleStringProperty("Mesa " + data.getValue().getIdMesa()));

        colIdOrden.setCellValueFactory(
                data -> new SimpleStringProperty(
                        "#ord-" + String.format("%03d", data.getValue().getIdOrden())));

        colEstado.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getPreparacion()));

        // Columna "ítems": muestra cuántos productos distintos tiene la orden
        colItems.setCellValueFactory(data -> {
            int total = data.getValue().getItems().stream()
                    .mapToInt(OrdenItem::getCantidad).sum();
            return new SimpleStringProperty(total + " uds.");
        });

        // Color por estado en la columna Estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = "Preparado".equals(item)
                            ? "-fx-text-fill: #8b6914; -fx-font-weight: bold;"
                            : "-fx-text-fill: #155724; -fx-font-weight: bold;";
                    setStyle(color);
                }
            }
        });

        // 4. Columnas de la tabla detalle
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));

        colPrecioUnit.setCellValueFactory(
                data -> new SimpleStringProperty(
                        "$" + String.format("%.2f", data.getValue().getPrecio())));

        colSubtotal.setCellValueFactory(
                data -> new SimpleStringProperty(
                        "$" + String.format("%.2f", data.getValue().getSubtotalItem())));

        // 5. Listener: cuando se selecciona una orden, cargar su detalle
        tablaOrdenes.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionada) -> mostrarDetalle(seleccionada));

        // 6. Cargar historial inicial (hoy, turno matutino)
        cargarHistorial();
    }

    // Acción del botón Buscar

    @FXML
    private void handleBuscar(ActionEvent event) {
        cargarHistorial();
    }

    //  Lógica principal 

    /**
     * Consulta el DAO con la fecha y turno seleccionados y actualiza
     * la tabla maestra y el resumen.
     */
    private void cargarHistorial() {
        LocalDate fecha = dpFecha.getValue();
        if (fecha == null) {
            mostrarAlerta("Fecha requerida", "Por favor selecciona una fecha.");
            return;
        }

        String turno = rbVespertino.isSelected()
                ? HistorialCocinaDAO.TURNO_VESPERTINO
                : HistorialCocinaDAO.TURNO_MATUTINO;

        // Ordenes
        List<OrdenCocina> ordenes = HistorialCocinaDAO.obtenerHistorial(fecha, turno);
        ObservableList<OrdenCocina> lista = FXCollections.observableArrayList(ordenes);
        tablaOrdenes.setItems(lista);
        tablaOrdenes.setPlaceholder(new Label("No hay órdenes completadas en este turno."));

        // Limpiar detalle al recargar
        tablaDetalle.getItems().clear();
        lblNotasOrden.setText("—");

        // Resumen
        double[] resumen = HistorialCocinaDAO.obtenerResumen(fecha, turno);
        lblTotalOrdenes.setText(String.valueOf((int) resumen[0]));
        lblMontoTotal.setText("$" + String.format("%.2f", resumen[1]));
        lblFechaResumen.setText(fecha.format(FMT_FECHA));
        lblTurnoResumen.setText(turno);
    }

    /**
     * Llena la tabla de detalle con los ítems de la orden seleccionada.
     */
    private void mostrarDetalle(OrdenCocina orden) {
        if (orden == null) {
            tablaDetalle.getItems().clear();
            lblNotasOrden.setText("—");
            return;
        }

        tablaDetalle.setItems(FXCollections.observableArrayList(orden.getItems()));

        String notas = orden.getDetalles();
        lblNotasOrden.setText(notas.isBlank() ? "Sin notas especiales." : notas);
    }

    //  Navegación sidebar ─

    @FXML
    private void handlePedidosActuales(ActionEvent event) {
        cambiarPantalla(event,
                "/com/mycompany/restaurante/fxml/PedidosActuales.fxml",
                "Pedidos en Cocina");
    }

    @FXML
    private void handleHistorial(ActionEvent event) {
        cargarHistorial();
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");

        alerta.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                cambiarPantalla(event,
                        "/com/mycompany/restaurante/fxml/LoginPantalla.fxml",
                        "Iniciar sesión");
            }
        });
    }

    //  Helpers 

    private void cambiarPantalla(ActionEvent event, String fxmlPath, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo + " - Saveurs Paris");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("[HistorialCocinaController] Error cargando: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}