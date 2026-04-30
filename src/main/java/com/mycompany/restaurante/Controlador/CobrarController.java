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
 * Controlador principal para la gestión de cobros y monitoreo de mesas.
 * Permite visualizar el estado de ocupación, calcular cuentas con IVA
 * y procesar pagos en efectivo o tarjeta.
 * 
 * @author Citlaly
 * @version 1.0
 */
public class CobrarController implements Initializable {

    private static final Logger LOG = Logger.getLogger(CobrarController.class.getName());
    
    // TABLA DE PEDIDO
    @FXML private TableView<OrdenItem> tableVerPedido;
    @FXML private TableColumn<OrdenItem, String> colProducto;
    @FXML private TableColumn<OrdenItem, Integer> colCantidad;
    @FXML private TableColumn<OrdenItem, Double> colPrecio;

    // LABELS DE RESUMEN
    @FXML private Label labelMesa;
    @FXML private Label labelIdOrden;
    @FXML private Label labelSubtotal;
    @FXML private Label labelIVA;
    @FXML private Label labelTotal;
    @FXML private Label labelCambio;

    // BOTONES DE MESAS (1 AL 12)
    @FXML private Button btnMesa1, btnMesa2, btnMesa3, btnMesa4;
    @FXML private Button btnMesa5, btnMesa6, btnMesa7, btnMesa8;
    @FXML private Button btnMesa9, btnMesa10, btnMesa11, btnMesa12;
    
    // BOTONES DE PAGO / NAVEGACIÓN
    @FXML private Button btnEfectivo;
    @FXML private Button btnTarjeta;
    @FXML private Button btnRegistrarPago;
    @FXML private Button btnFacturar;
    @FXML private Button btnCobrarMesa;
    @FXML private Button btnCerrarSesion;
    @FXML private Spinner<Double> spinnerMonto;

    // ESTADO INTERNO
    private int mesaSeleccionada = 0;
    private double subtotal = 0.0;
    private double iva = 0.0;
    private double total = 0.0;
    private String metodoPago = "EFECTIVO"; // Opciones: "EFECTIVO" | "TARJETA"
    private static final double TASA_IVA = 0.16;
    private int idOrdenActual = 0;

    // ESTILOS CSS 
    private static final String ESTILO_MESA_LIBRE =
        "-fx-background-color: #627096; -fx-border-color: #627096; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;" +
        "-fx-text-fill: #0a132b;";
    private static final String ESTILO_MESA_ACTIVA =
        "-fx-background-color: #8a3636; -fx-border-color: #8a3636; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;" +
        "-fx-text-fill: #0a132b;";
    private static final String ESTILO_BTN_ACTIVO =
        "-fx-background-color: #2c3b62; -fx-text-fill: #d4c5b0; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_BTN_INACTIVO =
        "-fx-background-color: #8b1a1a; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10; -fx-text-fill: #d4c5b0;";

    /**
     * Inicializa la interfaz, configura el formato de celdas de la tabla 
     * y carga el estado inicial de las mesas desde la base de datos.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar columnas de la tabla
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        // Formato de moneda para la columna de precio
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : "$" + String.format("%.2f", p));
            }
        });

        // Configuración del Spinner para montos de pago
        SpinnerValueFactory<Double> svf =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 9999.99, 0, 1);
        spinnerMonto.setValueFactory(svf);
        spinnerMonto.setEditable(true);

        // Listener para recalcular cambio mientras el usuario escribe
        spinnerMonto.valueProperty().addListener((obs, ov, nv) -> calcularCambio());
        
        vincularBotonesMesa();
        cargarEstadoMesas();
    }

    /**
     * Vincula el evento ActionEvent a cada uno de los 12 botones de mesa.
     */
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
     * Gestiona la selección de una mesa, resalta el botón correspondiente y 
     * carga la orden activa desde el DAO.
     * 
     * @param numMesa Identificador de la mesa clicada.
     * @param mesas Arreglo de botones de mesa para manejo de estilos.
     */
    private void seleccionarMesa(int numMesa, Button[] mesas) {
        mesaSeleccionada = numMesa;

        // Resetear colores base desde la BD
        cargarEstadoMesas();

        // Aplicar resaltado a la mesa seleccionada
        Button botonActual = mesas[numMesa - 1];
        String estiloBase = botonActual.getStyle();
        botonActual.setStyle(estiloBase + ESTILO_BTN_ACTIVO);

        // Buscar orden abierta en la BD
        idOrdenActual = PedidoDAO.obtenerOrdenAbiertaPorMesa(numMesa);
        labelMesa.setText("Mesa " + numMesa);

        if (idOrdenActual == 0) {
            limpiarInterfazOrden();
            return;
        }

        // Cargar detalle de la orden si existe
        labelIdOrden.setText("Orden #" + idOrdenActual + " · " + java.time.LocalDate.now());
        ObservableList<OrdenItem> orden = PedidoDAO.obtenerDetalleOrden(idOrdenActual);
        tableVerPedido.setItems(orden);

        // Cálculos financieros
        subtotal = orden.stream().mapToDouble(OrdenItem::getSubtotalItem).sum();
        iva = subtotal * TASA_IVA;
        total = subtotal + iva;

        // Actualizar visualización
        labelSubtotal.setText("$" + String.format("%.2f", subtotal));
        labelIVA.setText("$" + String.format("%.2f", iva));
        labelTotal.setText("$" + String.format("%.2f", total));

        // Resetear controles de pago
        spinnerMonto.getValueFactory().setValue(0.0);
        spinnerMonto.setDisable(false);
        metodoPago = "EFECTIVO";
        btnEfectivo.setStyle(ESTILO_BTN_ACTIVO);
        btnTarjeta.setStyle(ESTILO_BTN_INACTIVO);
        labelCambio.setText("$0.00");
    }
    
    /**
     * Limpia los campos de texto cuando no hay una orden activa.
     */
    private void limpiarInterfazOrden() {
        tableVerPedido.getItems().clear();
        labelIdOrden.setText("Sin orden activa");
        labelSubtotal.setText("$0.00");
        labelIVA.setText("$0.00");
        labelTotal.setText("$0.00");
        labelCambio.setText("$0.00");
        subtotal = iva = total = 0.0;
        LOG.warning("CobrarController: mesa " + mesaSeleccionada + " no tiene orden abierta.");
    }

    /**
     * Sincroniza el color de los botones de mesa con el estado actual en la BD.
     * Mesas con órdenes "Abiertas" se muestran en color de alerta (rojo/activo).
     */
    private void cargarEstadoMesas() {
        Button[] mesas = {
            btnMesa1, btnMesa2, btnMesa3, btnMesa4,
            btnMesa5, btnMesa6, btnMesa7, btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };

        // Pintar todas como libres inicialmente
        String sql2 = "SELECT idMesa FROM mesas";
        try (Connection con = ConexionDB.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql2)) {
            while (rs.next()) {
                int id = rs.getInt("idMesa");
                mesas[id - 1].setStyle(ESTILO_MESA_LIBRE);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        // Pintar mesas ocupadas
        String sql = "SELECT idMesa, estado FROM ordenes";
        try (Connection con = ConexionDB.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("idMesa");
                String estado = rs.getString("estado");
                if ("Abierta".equals(estado)) {
                    mesas[id - 1].setStyle(ESTILO_MESA_ACTIVA);
                }
            }

            // Reaplicar resaltado si hay selección previa
            if (mesaSeleccionada != 0) {
                Button btnSeleccionada = mesas[mesaSeleccionada - 1];
                btnSeleccionada.setStyle(btnSeleccionada.getStyle() + ESTILO_BTN_ACTIVO);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    @FXML private void handleMesas(ActionEvent event){ /* si se aplica css*/ }

    /**
     * Cambia el método de pago a Efectivo y habilita el ingreso de monto.
     */
    @FXML
    private void handleEfectivo(ActionEvent event) {
        metodoPago = "EFECTIVO";
        btnEfectivo.setStyle(ESTILO_BTN_ACTIVO);
        btnTarjeta.setStyle(ESTILO_BTN_INACTIVO);
        spinnerMonto.setDisable(false);
        calcularCambio();
    }

    /** 
     * Cambia el método de pago a Tarjeta y deshabilita el cálculo de cambio. 
     */
    @FXML
    private void handleTarjeta(ActionEvent event) {
        metodoPago = "TARJETA";
        btnTarjeta.setStyle(ESTILO_BTN_ACTIVO);
        btnEfectivo.setStyle(ESTILO_BTN_INACTIVO);
        spinnerMonto.setDisable(true);
        labelCambio.setText("N/A");
    }

    /** 
     * Calcula la diferencia entre el monto recibido y el total. 
     */
    private void calcularCambio() {
        if (!"EFECTIVO".equals(metodoPago)) return;
        double monto  = spinnerMonto.getValue() != null ? spinnerMonto.getValue() : 0.0;
        double cambio = monto - total;
        labelCambio.setText(cambio >= 0
            ? "$" + String.format("%.2f", cambio)
            : "-$" + String.format("%.2f", Math.abs(cambio)));
    }

    /**
     * Valida la transacción y registra el pago en el sistema.
     * Ofrece opciones para facturación inmediata tras el registro.
     */
    @FXML
    private void handleRegistrarPago(ActionEvent event) {
        if (mesaSeleccionada == 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin mesa seleccionada",
                    "Seleccione una mesa antes de registrar el pago.");
            return;
        }

        if ("EFECTIVO".equals(metodoPago)) {
            double monto = spinnerMonto.getValue() != null ? spinnerMonto.getValue() : 0.0;
            if (monto < total || monto == 0.0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Monto insuficiente",
                        "Monto insuficiente para el total de $" + String.format("%.2f", total));
                return;
            }
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar pago · Mesa " + mesaSeleccionada);
        confirmacion.setHeaderText("Método de pago: " + metodoPago);
        confirmacion.setContentText("Total: $" + String.format("%.2f", total) + "\n\n¿Desea facturar?");

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
        
        mostrarAlerta(Alert.AlertType.INFORMATION, "Pago registrado",
                "El pago de Mesa " + mesaSeleccionada + " ha sido registrado.");
    }

    /** 
     * Verifica requisitos previos y lanza la ventana de facturación. 
     */
    @FXML
    private void handleFacturar(ActionEvent event) {
        if (mesaSeleccionada == 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin mesa seleccionada",
                    "Seleccione una mesa primero.");
            return;
        }
        abrirFacturacion();
    }

    /** 
     * Carga la vista FacturacionPantalla.fxml de forma modal. 
     */
    private void abrirFacturacion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FacturacionPantalla.fxml"));
            Parent root = loader.load();

            FacturacionController facturacionCtrl = loader.getController();
            facturacionCtrl.setTotalFactura(total);
            facturacionCtrl.setIdOrden(idOrdenActual);
            facturacionCtrl.setSubtotal(subtotal);
            facturacionCtrl.setIva(iva);
            facturacionCtrl.setNumMesa(mesaSeleccionada);

            Stage stage = new Stage();
            stage.setTitle("Datos de Facturación · Mesa " + mesaSeleccionada);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar la pantalla de facturación.");
        }
    }

    /** 
     * Regresa a la pantalla de login cerrando la sesión actual. 
     */
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));
            Stage stageActual = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stageActual.setScene(new Scene(root));
            stageActual.setTitle("Iniciar sesión - Saveurs Paris");
            stageActual.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleCobrarMesa(ActionEvent event) { /* Pantalla actual */ }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(contenido);
        a.showAndWait();
    }
}