package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.PedidoDAO;
import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.OrdenItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controlador de CobrarPantalla.fxml — CU-54 Facturar Cuenta.
 *
 * @author Citlaly
 */
public class CobrarController implements Initializable {

    private static final Logger LOG = Logger.getLogger(CobrarController.class.getName());
    
    //  Tabla de pedido 
    @FXML private TableView<OrdenItem> tableVerPedido;
    @FXML private TableColumn<OrdenItem, String> colProducto;
    @FXML private TableColumn<OrdenItem, Integer> colCantidad;
    @FXML private TableColumn<OrdenItem, Double> colPrecio;

    //  Labels de resumen 
    @FXML private Label labelMesa;
    @FXML private Label labelIdOrden;
    @FXML private Label labelSubtotal;
    @FXML private Label labelIVA;
    @FXML private Label labelTotal;
    @FXML private Label labelCambio;

    //  Botones de mesa 
    @FXML private Button btnMesa1, btnMesa2, btnMesa3, btnMesa4;
    @FXML private Button btnMesa5, btnMesa6, btnMesa7, btnMesa8;
    @FXML private Button btnMesa9, btnMesa10, btnMesa11, btnMesa12;

    //  Botones de pago / navegación 
    @FXML private Button btnEfectivo;
    @FXML private Button btnTarjeta;
    @FXML private Button btnRegistrarPago;
    @FXML private Button btnFacturar;
    @FXML private Button btnCobrarMesa;
    @FXML private Button btnCerrarSesion;
    @FXML private Spinner<Double> spinnerMonto;

    //  Estado interno 
    private int    mesaSeleccionada = 0;
    private double subtotal = 0.0;
    private double iva = 0.0;
    private double total = 0.0;
    private String metodoPago = "EFECTIVO";   // "EFECTIVO" | "TARJETA"

    private static final double TASA_IVA = 0.16;
    
    private int idOrdenActual = 0;

    //  Estilos 
    private static final String ESTILO_MESA_LIBRE =
        "-fx-background-color: #f5efe6; -fx-border-color: #1A1E2E; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_MESA_ACTIVA =
        "-fx-background-color: #8b1a1a; -fx-border-color: #8b1a1a; -fx-background-radius: 10 10 10 10" +
        "-fx-border-radius: 10 10 10 10; -fx-text-fill: #d4c5b0;";
    private static final String ESTILO_BTN_ACTIVO =
        "-fx-background-color: #2c3b62; -fx-text-fill: #d4c5b0; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_BTN_INACTIVO =
        "-fx-background-color: #8b1a1a; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10; -fx-text-fill: #d4c5b0;";

    //  Datos de prueba por mesa
    private final ObservableList<OrdenItem> ordenMesaPrueba =
        FXCollections.observableArrayList(
            new OrdenItem("Soupe à l'oignon", 1, 120.00),
            new OrdenItem("Coq au Vin", 2, 280.00),
            new OrdenItem("Crème Brûlée", 2, 95.00),
            new OrdenItem("Eau minérale 500ml", 3, 40.00)
        );

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Configurar columnas
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : "$" + String.format("%.2f", p));
            }
        });

        // Spinner: rango $0 – $9 999.99
        SpinnerValueFactory<Double> svf =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 9999.99, 0, 1);
        spinnerMonto.setValueFactory(svf);
        spinnerMonto.setEditable(true);

        // Recalcular cambio en tiempo real
        spinnerMonto.valueProperty().addListener((obs, ov, nv) -> calcularCambio());

        vincularBotonesMesa();
        cargarEstadoMesas();
    }

    //   El cajero selecciona una mesa
    private void vincularBotonesMesa() {
        Button[] mesas = {
            btnMesa1, btnMesa2, btnMesa3, btnMesa4,
            btnMesa5, btnMesa6, btnMesa7, btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };
        for (int i = 0; i < mesas.length; i++) {
            final int num = i + 1;
            mesas[i].setOnAction(e -> seleccionarMesa(num, mesas));
        }
    }

    /**
     * Selecciona mesa y muestra el detalle de la cuenta.
     */
    private void seleccionarMesa(int numMesa, Button[] mesas) {
        cargarEstadoMesas();

        mesaSeleccionada = numMesa;
        idOrdenActual = PedidoDAO.obtenerOrdenAbiertaPorMesa(numMesa);

        labelMesa.setText("Mesa " + numMesa);

        if (idOrdenActual == 0) {
            tableVerPedido.getItems().clear();
            labelIdOrden.setText("Sin orden activa");
            labelSubtotal.setText("$0.00");
            labelIVA.setText("$0.00");
            labelTotal.setText("$0.00");
            labelCambio.setText("$0.00");
            subtotal = iva = total = 0.0;
            LOG.warning("CobrarController: mesa " + numMesa + " no tiene orden abierta.");
            return;
        }

        labelIdOrden.setText("Orden #" + idOrdenActual + " · " + java.time.LocalDate.now());

        ObservableList<OrdenItem> orden = PedidoDAO.obtenerDetalleOrden(idOrdenActual);
        tableVerPedido.setItems(orden);

        subtotal = orden.stream()
                        .mapToDouble(OrdenItem::getSubtotalItem)
                        .sum();
        iva   = subtotal * TASA_IVA;
        total = subtotal + iva;

        labelSubtotal.setText("$" + String.format("%.2f", subtotal));
        labelIVA.setText("$" + String.format("%.2f", iva));
        labelTotal.setText("$" + String.format("%.2f", total));

        spinnerMonto.getValueFactory().setValue(0.0);
        spinnerMonto.setDisable(false);
        metodoPago = "EFECTIVO";
        btnEfectivo.setStyle(ESTILO_BTN_ACTIVO);
        btnTarjeta.setStyle(ESTILO_BTN_INACTIVO);
        labelCambio.setText("$0.00");
    }
    
    private void cargarEstadoMesas() {
        Button[] mesas = {
            btnMesa1, btnMesa2, btnMesa3, btnMesa4,
            btnMesa5, btnMesa6, btnMesa7, btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };

        String sql = "SELECT idMesa, estado FROM mesas";

        try (Connection con = ConexionDB.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("idMesa");
                String estado = rs.getString("estado");

                if ("Ocupada".equals(estado)) {
                    mesas[id - 1].setStyle(ESTILO_MESA_ACTIVA);
                } else {
                    mesas[id - 1].setStyle(ESTILO_MESA_LIBRE);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  btnEfectivo
    @FXML
    private void handleEfectivo(ActionEvent event) {
        metodoPago = "EFECTIVO";
        btnEfectivo.setStyle(ESTILO_BTN_ACTIVO);
        btnTarjeta.setStyle(ESTILO_BTN_INACTIVO);
        spinnerMonto.setDisable(false);
        calcularCambio();
    }

    //  btnTarjeta
    @FXML
    private void handleTarjeta(ActionEvent event) {
        metodoPago = "TARJETA";
        btnTarjeta.setStyle(ESTILO_BTN_ACTIVO);
        btnEfectivo.setStyle(ESTILO_BTN_INACTIVO);
        spinnerMonto.setDisable(true);
        labelCambio.setText("N/A");
    }

    //  Cambio en tiempo real 
    private void calcularCambio() {
        if (!"EFECTIVO".equals(metodoPago)) return;
        double monto  = spinnerMonto.getValue() != null ? spinnerMonto.getValue() : 0.0;
        double cambio = monto - total;
        labelCambio.setText(cambio >= 0
            ? "$" + String.format("%.2f", cambio)
            : "-$" + String.format("%.2f", Math.abs(cambio)));
    }

    //  Muestra confirmación; desde aquí también se puede abrir facturación.
    //  verifica monto suficiente antes de proceder.
    @FXML
    private void handleRegistrarPago(ActionEvent event) {

        if (mesaSeleccionada == 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Sin mesa seleccionada",
                "Seleccione una mesa antes de registrar el pago.");
            return;
        }

        // Monto en efectivo insuficiente o no ingresado
        if ("EFECTIVO".equals(metodoPago)) {
            double monto = spinnerMonto.getValue() != null ? spinnerMonto.getValue() : 0.0;
            if (monto < total || monto == 0.0) {
                mostrarAlerta(Alert.AlertType.WARNING,
                    "Monto insuficiente",
                    "El monto recibido debe ser igual o mayor al total.\n" +
                    "Total: $" + String.format("%.2f", total) + "\n" +
                    "Recibido: $" + String.format("%.2f", monto));
                return;
            }
        }

        // Confirmación con opción de facturar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar pago · Mesa " + mesaSeleccionada);
        confirmacion.setHeaderText("Método de pago: " + metodoPago);
        confirmacion.setContentText(
            "Total: $" + String.format("%.2f", total) + "\n\n" +
            "¿Desea también generar una FACTURA para este pago?"
        );

        ButtonType btnSoloRegistrar = new ButtonType("Solo registrar pago");
        ButtonType btnConFactura = new ButtonType("Registrar y Facturar");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(btnSoloRegistrar, btnConFactura, btnCancelar);

        Optional<ButtonType> res = confirmacion.showAndWait();
        if (res.isPresent()) {
            if (res.get() == btnSoloRegistrar) {
                registrarPagoSimple();
            } else if (res.get() == btnConFactura) {
                registrarPagoSimple();
                abrirFacturacion();
            }
        }
    }

    private void registrarPagoSimple() {
        mostrarAlerta(Alert.AlertType.INFORMATION,
            "Pago registrado",
            "El pago de Mesa " + mesaSeleccionada +
            " ha sido registrado.\nTotal: $" + String.format("%.2f", total));
    }

    // 
    //  btnFacturar → abre FacturacionPantalla.fxml
    // 
    @FXML
    private void handleFacturar(ActionEvent event) {

        // debe haber una mesa seleccionada con cuenta cargada
        if (mesaSeleccionada == 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Sin mesa seleccionada",
                "Seleccione una mesa antes de generar la factura.");
            return;
        }

        // Si el método es efectivo, verificar monto suficiente
        if ("EFECTIVO".equals(metodoPago)) {
            double monto = spinnerMonto.getValue() != null ? spinnerMonto.getValue() : 0.0;
            if (monto < total || monto == 0.0) {
                mostrarAlerta(Alert.AlertType.WARNING,
                    "Monto insuficiente",
                    "El monto recibido debe ser igual o mayor al total.\n" +
                    "Total: $" + String.format("%.2f", total) + "\n" +
                    "Recibido: $" + String.format("%.2f", monto));
                return;
            }
        }

        abrirFacturacion();
    }

    /**
     * Pasa el total y número de mesa al controlador de facturación.
     */
    private void abrirFacturacion() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/mycompany/restaurante/fxml/FacturacionPantalla.fxml"
                )
            );
            Parent root = loader.load();

            // Pasar contexto al controlador de facturación
            FacturacionController facturacionCtrl = loader.getController();
            facturacionCtrl.setTotalFactura(total);
            facturacionCtrl.setIdOrden(idOrdenActual);
            facturacionCtrl.setSubtotal(subtotal);
            facturacionCtrl.setIva(iva);
            facturacionCtrl.setNumMesa(mesaSeleccionada);

            Stage stage = new Stage();
            stage.setTitle("Datos de Facturación · Mesa " + mesaSeleccionada);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);  // bloquea ventana padre
            stage.setResizable(false);
            stage.showAndWait();
            // ventana de facturación cerrada, pantalla de cobro visible

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error al abrir facturación",
                "No se pudo cargar FacturacionPantalla.fxml:\n" + e.getMessage());
        }
    }

    // 
    //  btnCerrarSesion
    // 
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        try {
            // 1. Cargar directamente la pantalla de Login
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));

            // 2. Obtener la ventana actual usando el evento del clic
            javafx.scene.Node nodoOrigen = (javafx.scene.Node) event.getSource();
            Stage stageActual = (Stage) nodoOrigen.getScene().getWindow();

            // 3. Cambiar la escena
            Scene nuevaEscena = new Scene(root);
            stageActual.setScene(nuevaEscena);
            stageActual.setTitle("Iniciar sesión - Saveurs Paris");
            stageActual.centerOnScreen();
            stageActual.show();

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // 
    //  btnCobrarMesa (sidebar) — ya estamos en esta pantalla
    // 
    @FXML
    private void handleCobrarMesa(ActionEvent event) {
        // Ya estamos en la pantalla de cobro; no se requiere acción.
    }

    //  Utilidad 
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(contenido);
        a.showAndWait();
    }
}
