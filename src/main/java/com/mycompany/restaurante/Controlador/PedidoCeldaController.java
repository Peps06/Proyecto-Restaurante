package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.PedidoDAO;
import com.mycompany.restaurante.Modelo.OrdenCocina;
import com.mycompany.restaurante.Modelo.OrdenItem;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controlador de la tarjeta individual {@code PedidoCelda.fxml}.
 *
 * Recibe un {@link OrdenCocina} mediante {@link #initData(OrdenCocina, Consumer)}
 * y se encarga de:
 *  - Mostrar "Mesa X" y "#ord-XXX" en la cabecera.
 *  - Llenar la tabla con los ítems pendientes (cantidad + nombre del plato).
 *  - Marcar como 'Preparado' en {@code detalle_orden} solo los platillos
 *    actualmente en estado 'En espera' al pulsar "Marcar Listo".
 *  - Mostrar las notas del mesero ({@code ordenes.detalles}) al pulsar "Ver".
 *
 * A partir de la versión 2.1, el botón "Marcar Listo" actualiza el campo
 * {@code preparacion} de cada fila en {@code detalle_orden} en lugar de
 * la cabecera de la orden, permitiendo que nuevos platillos añadidos después
 * vuelvan a aparecer como una nueva tanda sin perder la orden original.
 *
 * @author Citlaly
 * @version 2.1 (preparacion por detalle_orden)
 */
public class PedidoCeldaController implements Initializable {

    // FXML
    @FXML private Label idMesa;
    @FXML private Label idOrden;
    
    @FXML private TableView<OrdenItem> tablaItems;
    @FXML private TableColumn<OrdenItem, Integer> columnaCantidad;
    @FXML private TableColumn<OrdenItem, String> columnaPlato;
    @FXML private Button btnListo;
    @FXML private Button btnVerDetalles;

    // Estado interno
    private OrdenCocina orden;
    private Consumer<Integer> onListoCallback;

        @Override
    public void initialize(URL url, ResourceBundle rb) {
        tablaItems.setStyle("-fx-background-color: transparent;");
 
        // Vincular columnas con las propiedades de OrdenItem
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        columnaPlato.setCellValueFactory(new PropertyValueFactory<>("producto"));
 
        // --- Estilo personalizado para la columna de CANTIDAD ---
        columnaCantidad.setCellFactory(col -> new TableCell<OrdenItem, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                    setStyle(
                        "-fx-alignment: CENTER;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #2c3b62;"
                        + "-fx-background-color: transparent;"
                        + "-fx-border-color: transparent transparent #E8DB6C transparent;"
                        + "-fx-border-width: 0 0 1 0;"
                    );
                }
            }
        });
 
        // --- Estilo personalizado para la columna de PLATO ---
        columnaPlato.setCellFactory(col -> new TableCell<OrdenItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setStyle(
                        "-fx-background-color: transparent;"
                        + "-fx-border-color: transparent transparent #E8DB6C transparent;"
                        + "-fx-border-width: 0 0 1 0;"
                        + "-fx-text-fill: #2c3b62;"
                    );
                }
            }
        });
 
        // Fondo transparente en las filas para ver la imagen de nota detrás
        tablaItems.setRowFactory(tv -> {
            TableRow<OrdenItem> row = new TableRow<>();
            row.setStyle("-fx-background-color: transparent;");
            return row;
        });
 
        // Encabezado de columnas transparente con texto del color del tema
        tablaItems.widthProperty().addListener((obs, oldVal, newVal) -> {
            tablaItems.lookupAll(".column-header").forEach(node ->
                node.setStyle("-fx-background-color: transparent;"));
            tablaItems.lookupAll(".column-header-background").forEach(node ->
                node.setStyle("-fx-background-color: transparent;"));
            tablaItems.lookupAll(".column-header .label").forEach(node ->
                node.setStyle(
                    "-fx-text-fill: #463A2B;"
                    + "-fx-font-weight: bold;"));
        });
    }

    /**
     * Inicializa la tarjeta con los datos de la orden y su callback de notificación.
     * Debe llamarse justo después de {@code FXMLLoader.load()} desde
     * {@link PedidosActualesController}.
     *
     * Solo se muestran los platillos que el DAO ya filtró con
     * {@code detalle_orden.preparacion = 'En espera'}, es decir, la tanda
     * pendiente actual de esta orden.
     *
     * @param orden La orden con sus platillos pendientes a mostrar.
     * @param onListoCallback Función que recibe el idOrden cuando el chef
     *                        pulsa "Marcar Listo". Normalmente refresca el grid.
     */
    public void initData(OrdenCocina orden, Consumer<Integer> onListoCallback) {
        this.orden = orden;
        this.onListoCallback = onListoCallback;
 
        // Cabecera: mesa y número de orden
        idMesa.setText("Mesa " + orden.getIdMesa());
        idOrden.setText("#ord-" + String.format("%03d", orden.getIdOrden()));
 
        // Tabla: solo los platillos en espera que trae el DAO
        tablaItems.setItems(FXCollections.observableArrayList(orden.getItems()));
        tablaItems.setPlaceholder(new Label("Sin ítems pendientes"));
    }

    // ACCIONES DE BOTONES

    /**
     * Maneja el clic en "Marcar Listo".
     *
     * Llama a {@link PedidoDAO#marcarOrdenPreparada(int)}, que cambia a
     * 'Preparado' todos los registros de {@code detalle_orden} cuyo estado
     * sea actualmente 'En espera' para esta orden.
     *
     * Los platillos que el mesero añada después conservarán el estado
     * 'En espera' y volverán a aparecer como una nueva tanda, sin necesidad
     * de crear una orden nueva.
     *
     * Una vez ejecutado, notifica al controlador padre mediante el callback
     * para que retire esta tarjeta del grid.
     */
    @FXML
    private void handlePedidoListo() {
        // Confirmación con el chef antes de ejecutar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("Mesa " + orden.getIdMesa()
                                 + "  ·  Orden #" + orden.getIdOrden());
        confirmacion.setContentText(
            "¿Marcar los platillos pendientes como PREPARADOS?\n"
            + "Esta tanda desaparecerá de la lista de cocina.");
 
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
 
                // Marca como 'Preparado' solo los detalles en 'En espera'
                boolean ok = PedidoDAO.marcarOrdenPreparada(orden.getIdOrden());
 
                if (ok) {
                    System.out.println("[PedidoCeldaController] Platillos de la Orden #"
                        + orden.getIdOrden() + " marcados como Preparados.");
 
                    // Avisar al grid para que retire esta tarjeta
                    if (onListoCallback != null) {
                        onListoCallback.accept(orden.getIdOrden());
                    }
                } else {
                    new Alert(Alert.AlertType.ERROR,
                        "No se pudo actualizar el estado en la base de datos. "
                        + "Intenta nuevamente.").showAndWait();
                }
            }
        });
    }
 
    /**
     * Maneja el clic en "Ver".
     * Muestra en un diálogo las notas especiales escritas por el mesero
     * (campo {@code ordenes.detalles}) junto con el identificador de la orden.
     * Si no hay notas, lo indica explícitamente.
     */
    @FXML
    private void handleDetallesPedido() {
        String detalles = orden.getDetalles();
 
        // Si el campo está en blanco, mostramos un mensaje informativo
        String contenido = detalles.isBlank()
            ? "Sin notas adicionales del mesero."
            : "Notas del mesero:\n\n" + detalles;
 
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Detalles del pedido");
        info.setHeaderText("Mesa " + orden.getIdMesa()
                         + "  ·  Orden #" + orden.getIdOrden());
        info.setContentText(contenido);
        info.showAndWait();
    }
}
