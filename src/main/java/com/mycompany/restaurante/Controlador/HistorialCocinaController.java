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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Controlador de HistorialCocina.fxml.
 *
 * Muestra las órdenes ya completadas (preparacion = Preparado o Entregado)
 * como tarjetas visuales (cards) con chips por platillo,
 * filtradas por fecha y turno (Matutino / Vespertino).
 *
 * Al hacer clic en una card, el panel derecho muestra el detalle
 * completo de esa orden (tabla de ítems + notas del mesero).
 *
 * @author Citlaly
 * @version 2.0 (diseño card)
 */
public class HistorialCocinaController implements Initializable {

    // Sidebar
    @FXML private Button btnPedidosActuales;
    @FXML private Button btnHistorial;
    @FXML private Button btnCerrarSesion;

    // Filtros
    @FXML private DatePicker dpFecha;
    @FXML private ToggleGroup tgTurno;
    @FXML private RadioButton rbMatutino;
    @FXML private RadioButton rbVespertino;
    @FXML private Button btnBuscar;

    // Resumen (barra azul)
    @FXML private Label lblTotalOrdenes; // header "X Completados"
    @FXML private Label lblOrdenesResumen; // barra azul
    @FXML private Label lblMontoTotal;
    @FXML private Label lblFechaResumen;
    @FXML private Label lblTurnoResumen;

    // Cards
    @FXML private VBox vboxOrdenes; // contenedor scrolleable de cards

    // Panel detalle
    @FXML private TableView<OrdenItem> tablaDetalle;
    @FXML private TableColumn<OrdenItem, Integer> colCantidad;
    @FXML private TableColumn<OrdenItem, String>  colProducto;
    @FXML private TableColumn<OrdenItem, String>  colPrecioUnit;
    @FXML private TableColumn<OrdenItem, String>  colSubtotal;
    @FXML private Label lblNotasOrden;
    @FXML private Label lblDetalleOrden;
    @FXML private Label lblDetalleMesa;

    // Estilos internos
    private static final String CARD_NORMAL =
        "-fx-background-color: #2c3b62; " +
        "-fx-background-radius: 10; " +
        "-fx-cursor: hand;";
    private static final String CARD_SELECCIONADA =
        "-fx-background-color: #1a1e2e; " +
        "-fx-background-radius: 10; " +
        "-fx-border-color: #c9a84c; " +
        "-fx-border-width: 2; " +
        "-fx-border-radius: 10; " +
        "-fx-cursor: hand;";
    private static final String CHIP_STYLE =
        "-fx-background-color: #f5efe6; " +
        "-fx-background-radius: 20; " +
        "-fx-text-fill: #2c3b62; " +
        "-fx-font-size: 11px;";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Card actualmente seleccionada
    private VBox cardSeleccionada = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpFecha.setValue(LocalDate.now());
        rbMatutino.setSelected(true);

        // Columnas del panel detalle
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colPrecioUnit.setCellValueFactory(data ->
            new SimpleStringProperty("$" + String.format("%.2f", data.getValue().getPrecio())));
        colSubtotal.setCellValueFactory(data ->
            new SimpleStringProperty("$" + String.format("%.2f", data.getValue().getSubtotalItem())));

        cargarHistorial();
    }

    // Acción del botón Buscar
    @FXML
    private void handleBuscar(ActionEvent event) {
        cargarHistorial();
    }

    // Lógica principal

    /**
     * Consulta el DAO y reconstruye las cards + resumen.
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

        List<OrdenCocina> ordenes = HistorialCocinaDAO.obtenerHistorial(fecha, turno);

        // Actualizar resumen
        double[] resumen = HistorialCocinaDAO.obtenerResumen(fecha, turno);
        int totalOrdenes = (int) resumen[0];

        lblTotalOrdenes.setText(totalOrdenes + " Completado" + (totalOrdenes != 1 ? "s" : ""));
        lblOrdenesResumen.setText(String.valueOf(totalOrdenes));
        lblMontoTotal.setText("$" + String.format("%.2f", resumen[1]));
        lblFechaResumen.setText(fecha.format(FMT));
        lblTurnoResumen.setText(turno);

        // Limpiar panel detalle
        tablaDetalle.getItems().clear();
        lblNotasOrden.setText("Selecciona una orden para ver los detalles.");
        lblDetalleOrden.setText("#ord-—");
        lblDetalleMesa.setText("Mesa —");
        cardSeleccionada = null;

        // Reconstruir cards
        vboxOrdenes.getChildren().clear();

        if (ordenes.isEmpty()) {
            Label sinOrdenes = new Label("No hay órdenes completadas en este turno.");
            sinOrdenes.setStyle("-fx-text-fill: #627096; -fx-font-size: 14px;");
            sinOrdenes.setPadding(new Insets(20));
            vboxOrdenes.getChildren().add(sinOrdenes);
            return;
        }

        for (OrdenCocina orden : ordenes) {
            VBox card = crearCard(orden);
            vboxOrdenes.getChildren().add(card);
        }
    }

    /**
     * Construye la card visual de una orden completada.
     * Replica el estilo del screenshot: encabezado navy con mesa + id,
     * chips blancos con "X x Platillo" para cada ítem.
     */
    private VBox crearCard(OrdenCocina orden) {
        VBox card = new VBox(0);
        card.setStyle(CARD_NORMAL);
        card.setPrefWidth(340.0);
        card.setMaxWidth(Double.MAX_VALUE);

        // Cabecera de la card
        HBox header = new HBox(12.0);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 14, 10, 14));
        header.setStyle("-fx-background-color: #2c3b62; -fx-background-radius: 10 10 0 0;");

        Label lblMesa = new Label("Mesa " + orden.getIdMesa());
        lblMesa.setStyle("-fx-text-fill: #f5efe6; -fx-font-weight: bold; -fx-font-size: 15px;");
        lblMesa.setFont(Font.font("Candara", 15));

        Label lblId = new Label("#ord-" + String.format("%03d", orden.getIdOrden()));
        lblId.setStyle("-fx-text-fill: #c9a84c; -fx-font-style: italic; -fx-font-size: 12px;");

        // Badge de estado (Preparado / Entregado)
        Label lblEstado = new Label(orden.getPreparacion());
        String badgeColor = "Preparado".equals(orden.getPreparacion()) ? "#8b6914" : "#2a6140";
        lblEstado.setStyle(
            "-fx-background-color: " + badgeColor + "; " +
            "-fx-text-fill: #f5efe6; " +
            "-fx-background-radius: 10; " +
            "-fx-font-size: 10px; " +
            "-fx-padding: 2 8 2 8;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(lblMesa, lblId, spacer, lblEstado);

        // Chips de platillos
        FlowPane chips = new FlowPane();
        chips.setHgap(6);
        chips.setVgap(6);
        chips.setPadding(new Insets(10, 14, 12, 14));
        chips.setStyle("-fx-background-color: #3a4d7a; -fx-background-radius: 0 0 10 10;");

        for (OrdenItem item : orden.getItems()) {
            Label chip = new Label(item.getCantidad() + " x " + item.getProducto());
            chip.setStyle(CHIP_STYLE);
            chip.setPadding(new Insets(4, 10, 4, 10));
            chips.getChildren().add(chip);
        }

        card.getChildren().addAll(header, chips);

        // Click: seleccionar y mostrar detalle
        card.setOnMouseClicked(e -> seleccionarCard(card, orden));

        return card;
    }

    /**
     * Resalta la card clicada y carga el detalle en el panel derecho.
     */
    private void seleccionarCard(VBox card, OrdenCocina orden) {
        // Resetear card anterior
        if (cardSeleccionada != null) {
            cardSeleccionada.setStyle(CARD_NORMAL);
            // El header y chips también necesitan volver al color original
            if (!cardSeleccionada.getChildren().isEmpty()) {
                cardSeleccionada.getChildren().get(0).setStyle(
                    "-fx-background-color: #2c3b62; -fx-background-radius: 10 10 0 0;");
                if (cardSeleccionada.getChildren().size() > 1) {
                    cardSeleccionada.getChildren().get(1).setStyle(
                        "-fx-background-color: #3a4d7a; -fx-background-radius: 0 0 10 10;");
                }
            }
        }

        // Estilo de selección
        card.setStyle(CARD_SELECCIONADA);
        if (!card.getChildren().isEmpty()) {
            card.getChildren().get(0).setStyle(
                "-fx-background-color: #1a1e2e; " +
                "-fx-background-radius: 10 10 0 0; " +
                "-fx-border-color: #c9a84c; " +
                "-fx-border-width: 0 2 0 2;");
            if (card.getChildren().size() > 1) {
                card.getChildren().get(1).setStyle(
                    "-fx-background-color: #253055; " +
                    "-fx-background-radius: 0 0 10 10; " +
                    "-fx-border-color: #c9a84c; " +
                    "-fx-border-width: 0 2 2 2;");
            }
        }
        cardSeleccionada = card;

        // Panel detalle
        mostrarDetalle(orden);
    }

    /**
     * Llena el panel derecho con los datos de la orden seleccionada.
     */
    private void mostrarDetalle(OrdenCocina orden) {
        lblDetalleOrden.setText("#ord-" + String.format("%03d", orden.getIdOrden()));
        lblDetalleMesa.setText("Mesa " + orden.getIdMesa());

        tablaDetalle.setItems(FXCollections.observableArrayList(orden.getItems()));

        String notas = orden.getDetalles();
        lblNotasOrden.setText(notas.isBlank() ? "Sin notas especiales." : notas);
    }

    // Navegación sidebar

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

    // Helpers

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