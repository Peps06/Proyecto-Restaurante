package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.PagoDAO;
import com.mycompany.restaurante.DAO.PedidoDAO;
import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.OrdenItem;
import com.mycompany.restaurante.Modelo.Pago;

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
import javafx.scene.Node;

/**
 * Controlador principal para la gestión de cobros y monitoreo de mesas.
 * Permite visualizar el estado de ocupación, calcular cuentas con IVA
 * y procesar pagos en efectivo o tarjeta.
 *
 * @author Citlaly
 * @version 2.1 (Valida el estado de la orden)
 */
public class CobrarController implements Initializable {

    private static final Logger LOG = Logger.getLogger(CobrarController.class.getName());

    //  TABLA DE PEDIDO
    @FXML private TableView<OrdenItem>  tableVerPedido;
    @FXML private TableColumn<OrdenItem, String> colProducto;
    @FXML private TableColumn<OrdenItem, Integer> colCantidad;
    @FXML private TableColumn<OrdenItem, Double> colPrecio;

    //  LABELS DE RESUMEN
    @FXML private Label labelMesa;
    @FXML private Label labelIdOrden;
    @FXML private Label labelSubtotal;
    @FXML private Label labelIVA;
    @FXML private Label labelTotal;
    @FXML private Label labelCambio;

    //  BOTONES DE MESAS (1 AL 12)
    @FXML private Button btnMesa1,  btnMesa2,  btnMesa3,  btnMesa4;
    @FXML private Button btnMesa5,  btnMesa6,  btnMesa7,  btnMesa8;
    @FXML private Button btnMesa9,  btnMesa10, btnMesa11, btnMesa12;

    //  BOTONES DE PAGO / NAVEGACIÓN
    @FXML private Button btnEfectivo;
    @FXML private Button btnTarjeta;
    @FXML private Button btnRegistrarPago;
    @FXML private Button btnFacturar;
    @FXML private Button btnCobrarMesa;
    @FXML private Button btnCerrarSesion;
    @FXML private Spinner<Double> spinnerMonto;

    //  ESTADO INTERNO
    private int mesaSeleccionada  = 0;
    private double subtotal = 0.0;
    private double iva = 0.0;
    private double total = 0.0;
    private int idOrdenActual = 0;

    /**
     * Forma de pago actualmente seleccionada.
     * Valores posibles: {@code "EFECTIVO"} | {@code "TARJETA"}.
     */
    private String metodoPago = "EFECTIVO";

    /**
     * ID del cajero en sesión.
     * Se inyecta desde el controlador de Login con {@link #setIdEmpleadoActual(int)}.
     * Valor temporal por defecto: 3.
     */
    private int idEmpleadoActual = 3;

    private static final double TASA_IVA = 0.16;

    //  ESTILOS CSS
    private static final String ESTILO_MESA_LIBRE =
        "-fx-background-color: #627096; -fx-border-color: #627096; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_MESA_ACTIVA =
        "-fx-background-color: #8a3636; -fx-border-color: #8a3636; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_BTN_ACTIVO =
        "-fx-background-color: #2c3b62; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_BTN_INACTIVO =
        "-fx-background-color: #8b1a1a; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_MESA_NO_COBRABLE =
        "-fx-background-color: #6f5410; -fx-border-color: #6f5410; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;";


    /**
     * Define el empleado que está operando la caja.
     * @param idEmpleado ID único del usuario en sesión.
     */
    public void setIdEmpleadoActual(int idEmpleado) {
        this.idEmpleadoActual = idEmpleado;
    }

    /**
     * Inicializa la interfaz, configura el formato de celdas de la tabla
     * y carga el estado inicial de las mesas desde la base de datos.
     *
     * @param url URL del recurso FXML.
     * @param rb  ResourceBundle de internacionalización.
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
     * Se realiza en un bucle para evitar repetir código para cada botón.
     */
    private void vincularBotonesMesa() {
        Button[] mesas = obtenerArregloMesas();
        for (int i = 0; i < mesas.length; i++) {
            final int num = i + 1;
            mesas[i].setOnAction(e -> seleccionarMesa(num, mesas));
        }
    }

    /**
     * Gestiona la selección de una mesa: resalta el botón correspondiente,
     * busca si tiene una orden abierta en la BD y carga su detalle.
     *
     * @param numMesa Identificador de la mesa clicada (1-12).
     * @param mesas Arreglo de botones para manejo de estilos.
     */
    private void seleccionarMesa(int numMesa, Button[] mesas) {
        mesaSeleccionada = numMesa;

        // Resetear colores base y resaltar la mesa elegida
        cargarEstadoMesas();
        Button botonActual = mesas[numMesa - 1];
        botonActual.setStyle(botonActual.getStyle() + ESTILO_BTN_ACTIVO);
 
        // Buscar orden abierta en la BD
        idOrdenActual = PedidoDAO.obtenerOrdenAbiertaPorMesa(numMesa);
        labelMesa.setText("Mesa " + numMesa);
 
        // Sin orden abierta → limpiar y salir
        if (idOrdenActual == 0) {
            limpiarInterfazOrden();
            return;
        }
 
        // Verificar estado de preparación
        String preparacion = PedidoDAO.obtenerPreparacionOrden(idOrdenActual);
 
        if (!"Entregado".equals(preparacion)) {
            // La orden todavía no fue entregada: no se puede cobrar
            botonActual.setStyle(ESTILO_MESA_NO_COBRABLE);
            limpiarInterfazOrden();
 
            // Texto descriptivo del estado actual para el aviso
            String estadoTexto = switch (preparacion) {
                case "En espera" -> "en espera de preparación";
                case "Preparado" -> "preparada pero aún no entregada";
                default -> "en estado: " + preparacion;
            };
 
            mostrarAlerta(Alert.AlertType.INFORMATION,
                "Mesa " + numMesa + " no disponible para cobro",
                "El pedido de esta mesa está " + estadoTexto + ".\n\n" +
                "Solo se puede cobrar cuando el mesero haya marcado el pedido como 'Entregado'."
            );
            return;
        }
 
        // Orden entregada → cargar y mostrar el detalle
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
        idOrdenActual = 0;
        LOG.warning("[CobrarController] Mesa " + mesaSeleccionada + " no tiene orden abierta.");
    }

    /**
     * Sincroniza el color de los botones de mesa con el estado actual en la BD.
     * Las mesas con órdenes {@code 'Abierta'} se muestran en rojo; las demás en azul.
     */
    private void cargarEstadoMesas() {
        Button[] mesas = obtenerArregloMesas();
 
        // 1. Pintar todas las mesas como libres
        try (Connection con = ConexionDB.getConexion();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery("SELECT idMesa FROM mesas")) {
            while (rs.next()) {
                int idx = rs.getInt("idMesa") - 1;
                if (idx >= 0 && idx < mesas.length) {
                    mesas[idx].setStyle(ESTILO_MESA_LIBRE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
 
        // 2. Colorear mesas con órdenes abiertas según su estado de preparación
        String sqlOrdenes =
            "SELECT idMesa, preparacion FROM ordenes WHERE estado = 'Abierta'";
 
        try (Connection con = ConexionDB.getConexion();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sqlOrdenes)) {
 
            while (rs.next()) {
                int    idx         = rs.getInt("idMesa") - 1;
                String preparacion = rs.getString("preparacion");
 
                if (idx >= 0 && idx < mesas.length) {
                    // Entregado → color normal de ocupada (cobrable)
                    // Cualquier otro estado → color no-cobrable
                    if ("Entregado".equals(preparacion)) {
                        mesas[idx].setStyle(ESTILO_MESA_ACTIVA);
                    } else {
                        mesas[idx].setStyle(ESTILO_MESA_NO_COBRABLE);
                    }
                }
            }
 
            // 3. Mantener el resaltado de selección sobre la mesa actualmente activa
            if (mesaSeleccionada != 0) {
                Button btn = mesas[mesaSeleccionada - 1];
                btn.setStyle(btn.getStyle() + ESTILO_BTN_ACTIVO);
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Devuelve los 12 botones de mesa en orden de idMesa (1-12).
     */
    private Button[] obtenerArregloMesas() {
        return new Button[] {
            btnMesa1, btnMesa2, btnMesa3,  btnMesa4,
            btnMesa5, btnMesa6, btnMesa7,  btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };
    }

    /**
     * Cambia el método de pago a Efectivo y habilita el spinner de monto.
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
     * Solo aplica cuando la forma de pago es efectivo.
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
        // Validar mesa y orden
        if (mesaSeleccionada == 0 || idOrdenActual == 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Sin cuenta activa",
                "Seleccione una mesa con una orden abierta antes de registrar el pago.");
            return;
        }

        // Validar monto en efectivo
        if ("EFECTIVO".equals(metodoPago)) {
            double monto = spinnerMonto.getValue() != null ? spinnerMonto.getValue() : 0.0;
            if (monto == 0.0 || monto < total) {
                mostrarAlerta(Alert.AlertType.WARNING,
                    "Monto insuficiente",
                    "El monto recibido debe ser igual o mayor al total.\n" +
                    "Total: $" + String.format("%.2f", total) + "\n" +
                    "Recibido: $" + String.format("%.2f", monto));
                return;
            }
        }

        // Diálogo de confirmación con opción de facturar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar pago · Mesa " + mesaSeleccionada);
        confirmacion.setHeaderText("Método: " + metodoPago +
                                   "  ·  Total: $" + String.format("%.2f", total));
        confirmacion.setContentText("¿Desea registrar el pago y también generar una FACTURA?");

        ButtonType btnSoloRegistrar = new ButtonType("Solo registrar pago");
        ButtonType btnConFactura = new ButtonType("Registrar y Facturar");
        ButtonType btnCancelar = new ButtonType("Cancelar",
                                 ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(btnSoloRegistrar, btnConFactura, btnCancelar);

        Optional<ButtonType> res = confirmacion.showAndWait();
        if (res.isPresent()) {
            if (res.get() == btnSoloRegistrar) {
                registrarPagoEnBD();
            } else if (res.get() == btnConFactura) {
                if (registrarPagoEnBD()) {
                    abrirFacturacion();
                }
            }
        }
    }

    /**
     * Crea a "Pago" segun la forma de pago elegida
     *
     * @return {@code true} si el pago se registró correctamente; {@code false} si falló.
     */
    private boolean registrarPagoEnBD() {
        Pago pago;

        if ("EFECTIVO".equals(metodoPago)) {
            double montoRecibido = spinnerMonto.getValue();
            pago = Pago.efectivo(idOrdenActual, idEmpleadoActual, total, montoRecibido);

        } else {
            // TARJETA — se usa "Debito" como valor por defecto.
            pago = Pago.tarjeta(idOrdenActual, idEmpleadoActual, total, "Debito");
        }

        int idPagoGenerado = PagoDAO.registrarPago(pago, mesaSeleccionada);

        if (idPagoGenerado != -1) {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                "Pago registrado",
                "Pago #" + idPagoGenerado + " registrado correctamente.\n" +
                "Mesa " + mesaSeleccionada + " queda libre.\n" +
                "Orden #" + idOrdenActual + " cerrada.");

            // Refrescar interfaz tras el cobro exitoso
            cargarEstadoMesas();
            mesaSeleccionada = 0;
            idOrdenActual = 0;
            limpiarInterfazOrden();
            labelMesa.setText("—");
            return true;

        } else {
            // conservar datos para reintento
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error al registrar pago",
                "Ocurrió un error al guardar el pago en la base de datos.\n" +
                "Intente nuevamente. Los datos del cobro se conservan.");
            return false;
        }
    }

    /**
     * Verifica los requisitos previos y lanza la pantalla de facturación.
     */
    @FXML
    private void handleFacturar(ActionEvent event) {
        if (mesaSeleccionada == 0 || idOrdenActual == 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Sin cuenta activa",
                "Seleccione una mesa con una orden abierta primero.");
            return;
        }
        abrirFacturacion();
    }

    /**
     * Carga la vista FacturacionPantalla.fxml de forma modal.
     */
    private void abrirFacturacion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/mycompany/restaurante/fxml/FacturacionPantalla.fxml"
            ));
            Parent root = loader.load();

            FacturacionController ctrl = loader.getController();
            ctrl.setTotalFactura(total);
            ctrl.setIdOrden(idOrdenActual);
            ctrl.setSubtotal(subtotal);
            ctrl.setIva(iva);
            ctrl.setNumMesa(mesaSeleccionada);

            Stage stage = new Stage();
            stage.setTitle("Datos de Facturación · Mesa " + mesaSeleccionada);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Refrescar mesas por si la facturación también cerró la orden
            cargarEstadoMesas();

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error de navegación",
                "No se pudo cargar la pantalla de facturación.");
        }
    }

    /**
     * Regresa a la pantalla de login cerrando la sesión actual.
     */
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        // Crear la alerta de confirmación
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");

        // Mostrar y esperar respuesta
        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // Código para regresar al Login (ejemplo)
                Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // El usuario canceló, no se hace nada y se queda en la ventana
            alerta.close();
        }
    }

    @FXML private void handleCobrarMesa(ActionEvent event) { /* Pantalla actual */ }

    @FXML private void handleMesas(ActionEvent event) { /*CSS EstadoMesas */ }

    /**
     * Muestra un diálogo emergente estándar de alerta.
     *
     * @param tipo      Tipo de alerta ({@code INFORMATION}, {@code WARNING}, {@code ERROR}).
     * @param titulo    Texto del título de la ventana.
     * @param contenido Mensaje principal visible para el usuario.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(contenido);
        a.showAndWait();
    }
}