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
 *  - Llenar la tabla con los ítems (cantidad + nombre del plato).
 *  - Marcar la orden como 'Preparado' al pulsar "Marcar Listo".
 *  - Mostrar las notas del mesero (ordenes.detalles) al pulsar "Ver".
 *
 * @author Citlaly
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
        // Vincular columnas con las propiedades de OrdenItem
        // "cantidad" → OrdenItem.getCantidad()
        // "producto" → OrdenItem.getProducto()
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        columnaPlato.setCellValueFactory(new PropertyValueFactory<>("producto"));

        // Centrar la columna de cantidad
        columnaCantidad.setCellFactory(col -> new TableCell<OrdenItem, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-alignment: CENTER; "
                           + "-fx-font-weight: bold; "
                           + "-fx-text-fill: #2c3b62;");
                }
            }
        });
    }

    /**
     * Inicializa la tarjeta con los datos de la orden.
     * Debe llamarse justo después de FXMLLoader.load().
     *
     * @param orden La orden en espera que representa esta tarjeta.
     * @param onListoCallback Función a ejecutar (con el idOrden) cuando el chef
     *                       pulsa "Marcar Listo". Normalmente refresca el grid.
     */
    public void initData(OrdenCocina orden, Consumer<Integer> onListoCallback) {
        this.orden = orden;
        this.onListoCallback = onListoCallback;

        // Cabecera
        idMesa.setText("Mesa " + orden.getIdMesa());
        idOrden.setText("#ord-" + String.format("%03d", orden.getIdOrden()));

        // Tabla de ítems
        tablaItems.setItems(FXCollections.observableArrayList(orden.getItems()));
        tablaItems.setPlaceholder(new Label("Sin ítems"));
    }

    // ACCIONES DE BOTONES

    /**
     * Maneja el clic en "Marcar Listo".
     * Cambia {@code ordenes.preparacion} a 'Preparado' en la BD y
     * notifica al controlador padre para que remueva esta tarjeta del grid.
     */
    @FXML
    private void handlePedidoListo() {
        // Confirmar acción con el chef
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("Mesa " + orden.getIdMesa()
                                 + "  ·  Orden #" + orden.getIdOrden());
        confirmacion.setContentText("¿Marcar este pedido como PREPARADO?\n"
                                  + "Desaparecerá de la lista de cocina.");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                boolean ok = PedidoDAO.marcarOrdenPreparada(orden.getIdOrden());
                if (ok) {
                    System.out.println("[PedidoCeldaController] Orden #"
                        + orden.getIdOrden() + " marcada como Preparada.");
                    // Avisar al grid para que quite esta tarjeta
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
     * Muestra en un diálogo las notas especiales del mesero
     * (campo {@code ordenes.detalles}) junto con el resumen de ítems.
     */
    @FXML
    private void handleDetallesPedido() {
        String detalles = orden.getDetalles();
        String contenido;

        // Si hay notas las mostramos; si no, informamos que no hay
        if (detalles.isBlank()) {
            contenido = "Sin notas adicionales del mesero.";
        } else {
            contenido = "Notas del mesero:\n\n" + detalles;
        }

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Detalles del Pedido");
        info.setHeaderText("Mesa " + orden.getIdMesa()
                         + "  ·  Orden #" + orden.getIdOrden());
        info.setContentText(contenido);
        info.showAndWait();
    }
}