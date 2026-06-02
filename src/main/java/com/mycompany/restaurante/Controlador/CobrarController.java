package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.MesasDAO;
import com.mycompany.restaurante.DAO.PagoDAO;
import com.mycompany.restaurante.DAO.PedidoDAO;
import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Mesa;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

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
    @FXML private TableView<OrdenItem> tableVerPedido;
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
    @FXML private Button btnMesa1, btnMesa2, btnMesa3, btnMesa4;
    @FXML private Button btnMesa5, btnMesa6, btnMesa7, btnMesa8;
    @FXML private Button btnMesa9, btnMesa10, btnMesa11, btnMesa12;

    //  BOTONES DE PAGO / NAVEGACIÓN
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

    // ESTILOS CSS
    private static final String ESTILO_MESA_LIBRE =
        "-fx-background-color: #627096;" +
        "-fx-border-color: #627096;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    
    private static final String ESTILO_MESA_ACTIVA =
        "-fx-background-color: #407a48;" +
        "-fx-border-color: #407a48;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    
    private static final String ESTILO_BTN_ACTIVO =
        "-fx-background-color: #2c3b62;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    
    private static final String ESTILO_BTN_INACTIVO =
        "-fx-background-color: #8b1a1a;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    
    private static final String ESTILO_MESA_NO_COBRABLE =
        "-fx-background-color: #8a3636;" +
        "-fx-border-color: #8a3636;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";


    /**
     * Define el empleado que está operando la caja.
     * 
     * @param idEmpleado ID único del usuario en sesión.*/
    public void setIdEmpleadoActual(int idEmpleado) {
        this.idEmpleadoActual = idEmpleado;
    }

    /**
     * Inicializa la interfaz al cargar la pantalla:
     * 1. Configura las columnas de la tabla de pedido.
     * 2. Configura el Spinner de monto con su listener de cambio.
     * 3. Sincroniza el estado {@code preparacion} de todas las órdenes
     *    abiertas para que el cajero vea información actualizada.
     * 4. Carga el estado visual del mapa de mesas.
     *
     * @param url URL del recurso FXML.
     * @param rb ResourceBundle de internacionalización.
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
                setText(empty || p == null
                    ? null
                    : "$" + String.format("%.2f", p));
            }
        });
 
        // Configurar Spinner de monto 
        SpinnerValueFactory<Double> svf =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 9999.99, 0, 1);
        spinnerMonto.setValueFactory(svf);
        spinnerMonto.setEditable(true);
        spinnerMonto.valueProperty().addListener((obs, ov, nv) -> calcularCambio());
 
        // Sincronizar preparacion de todas las órdenes abiertas 
        PedidoDAO.sincronizarTodasLasOrdenes();
 
        vincularBotonesMesa();
        cargarEstadoMesas();
    }

    /**
     * Vincula el evento ActionEvent a cada uno de los 12 botones de mesa.
     */
    private void vincularBotonesMesa() {
        Button[] mesas = obtenerArregloMesas();
        for (int i = 0; i < mesas.length; i++) {
            final int num = i + 1;
            mesas[i].setOnAction(e -> seleccionarMesa(num, mesas));
        }
    }

    /**
     * Gestiona la selección de una mesa:
     * 1. Sincroniza {@code ordenes.preparacion} de la orden de esa mesa
     *    para obtener el estado más reciente antes de evaluar si es cobrable.
     * 2. Resalta el botón correspondiente.
     * 3. Busca si tiene una orden abierta y carga su detalle.
     *
     * @param numMesa Identificador de la mesa clicada (1-12).
     * @param mesas Arreglo de botones para manejo de estilos.
     */
    private void seleccionarMesa(int numMesa, Button[] mesas) {
        mesaSeleccionada = numMesa;
 
        // Resetear colores y resaltar la mesa elegida
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
 
        // Sincronizar preparacion antes de evaluar 
        PedidoDAO.sincronizarPreparacionOrden(idOrdenActual);
 
        // Verificar estado de preparación actualizado
        String preparacion = PedidoDAO.obtenerPreparacionOrden(idOrdenActual);
 
        if (!"Preparado".equals(preparacion)) {
            // La orden todavía tiene platillos pendientes: no se puede cobrar
            botonActual.setStyle(ESTILO_MESA_NO_COBRABLE);
            limpiarInterfazOrden();
 
            // Texto descriptivo del estado actual para el aviso
            String estadoTexto = "En espera".equals(preparacion)
                ? "con platillos aún en espera en cocina"
                : "en estado: " + preparacion;
 
            mostrarAlerta(Alert.AlertType.INFORMATION,
                "Mesa " + numMesa + " no disponible para cobro",
                "El pedido de esta mesa está " + estadoTexto + ".\n\n"
                + "Solo se puede cobrar cuando todos los platillos "
                + "hayan sido preparados por cocina."
            );
            return;
        }
 
        // Orden lista → cargar y mostrar el detalle
        labelIdOrden.setText("Orden #" + idOrdenActual
                           + " · " + java.time.LocalDate.now());
        ObservableList<OrdenItem> orden =
                PedidoDAO.obtenerDetalleOrden(idOrdenActual);
        tableVerPedido.setItems(orden);
 
        // Cálculos financieros
        subtotal = orden.stream()
                        .mapToDouble(OrdenItem::getSubtotalItem)
                        .sum();
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
     * Sincroniza el color de los botones de mesa con el estado actual en la BD.
     * Las mesas con órdenes 'Abierta' y {@code preparacion = 'Preparado'} se
     * muestran en rojo (cobrables); las que aún tienen platillos en espera
     * se muestran en dorado (no cobrables); las demás en azul (libres).
     */
    private void cargarEstadoMesas() {
        Button[] botones = obtenerArregloMesas();
        MesasDAO mesasDAO = new MesasDAO();
        List<Mesa> todasLasMesas = mesasDAO.obtenerTodasLasMesas();

        // 1. Pintar todas como libres y asignar capacidad
        for (Mesa mesa : todasLasMesas) {
            int idx = mesa.getIdMesa() - 1;
            if (idx >= 0 && idx < botones.length) {
                botones[idx].setStyle(ESTILO_MESA_LIBRE);
                setTextoMesa(botones[idx], mesa.getIdMesa(), mesa.getCapacidad());
            }
        }

        // 2. Colorear mesas con órdenes abiertas según preparacion
        String sqlOrdenes =
            "SELECT idMesa, preparacion FROM ordenes WHERE estado = 'Abierta'";

        try (Connection con = ConexionDB.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sqlOrdenes)) {

            while (rs.next()) {
                int idx = rs.getInt("idMesa") - 1;
                String preparacion = rs.getString("preparacion");

                if (idx >= 0 && idx < botones.length) {
                    botones[idx].setStyle("Preparado".equals(preparacion)
                        ? ESTILO_MESA_ACTIVA
                        : ESTILO_MESA_NO_COBRABLE);
                    // El graphic con capacidad ya fue asignado arriba, no se pierde
                }
            }

            // 3. Mantener resaltado de la mesa activa seleccionada
            if (mesaSeleccionada != 0) {
                Button btn = botones[mesaSeleccionada - 1];
                btn.setStyle(btn.getStyle() + ESTILO_BTN_ACTIVO);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 
    // FORMA DE PAGO
 
    /**
     * Cambia el método de pago a Efectivo y habilita el spinner de monto.
     *
     * @param event Evento de acción del botón.
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
     *
     * @param event Evento de acción del botón.
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
        double monto = spinnerMonto.getValue() != null
                     ? spinnerMonto.getValue() : 0.0;
        double cambio = monto - total;
        labelCambio.setText(cambio >= 0
            ? "$" + String.format("%.2f", cambio)
            : "-$" + String.format("%.2f", Math.abs(cambio)));
    }
 
    // REGISTRO DE PAGO
 
    /**
     * Valida la transacción y registra el pago en el sistema.
     * Ofrece opciones para facturación inmediata tras el registro.
     *
     * @param event Evento de acción del botón.
     */
    @FXML
    private void handleRegistrarPago(ActionEvent event) {
        // Validar mesa y orden
        if (mesaSeleccionada == 0 || idOrdenActual == 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Sin cuenta activa",
                "Seleccione una mesa con una orden lista antes de "
                + "registrar el pago.");
            return;
        }
 
        // Validar monto en efectivo
        if ("EFECTIVO".equals(metodoPago)) {
            double monto = spinnerMonto.getValue() != null
                         ? spinnerMonto.getValue() : 0.0;
            if (monto == 0.0 || monto < total) {
                mostrarAlerta(Alert.AlertType.WARNING,
                    "Monto insuficiente",
                    "El monto recibido debe ser igual o mayor al total.\n"
                    + "Total: $" + String.format("%.2f", total) + "\n"
                    + "Recibido: $" + String.format("%.2f", monto));
                return;
            }
        }
 
        // Diálogo de confirmación con opción de facturar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar pago · Mesa " + mesaSeleccionada);
        confirmacion.setHeaderText("Método: " + metodoPago
                                 + "  ·  Total: $"
                                 + String.format("%.2f", total));
        confirmacion.setContentText(
            "¿Desea registrar el pago y también generar una FACTURA?");
 
        ButtonType btnSoloRegistrar = new ButtonType("Solo registrar pago");
        ButtonType btnConFactura = new ButtonType("Registrar y Facturar");
        ButtonType btnCancelar = new ButtonType(
                "Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(
                btnSoloRegistrar, btnConFactura, btnCancelar);
 
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
     * Construye el objeto {@link Pago} según la forma de pago elegida
     * y lo persiste en la base de datos.
     *
     * @return {@code true} si el pago se registró correctamente.
     */
    private boolean registrarPagoEnBD() {
        Pago pago;
 
        if ("EFECTIVO".equals(metodoPago)) {
            double montoRecibido = spinnerMonto.getValue();
            pago = Pago.efectivo(idOrdenActual, idEmpleadoActual,
                                  total, montoRecibido);
        } else {
            // TARJETA — se usa "Debito" como valor por defecto
            pago = Pago.tarjeta(idOrdenActual, idEmpleadoActual,
                                 total, "Debito");
        }
 
        int idPagoGenerado = PagoDAO.registrarPago(pago, mesaSeleccionada);
 
        if (idPagoGenerado != -1) {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                "Pago registrado",
                "Pago #" + idPagoGenerado + " registrado correctamente.\n"
                + "Mesa " + mesaSeleccionada + " queda cobrada.\n"
                + "Orden #" + idOrdenActual + " cerrada.");
 
            // Refrescar interfaz tras el cobro exitoso
            cargarEstadoMesas();
            mesaSeleccionada = 0;
            idOrdenActual = 0;
            limpiarInterfazOrden();
            labelMesa.setText("—");
            return true;
 
        } else {
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error al registrar pago",
                "Ocurrió un error al guardar el pago en la base de datos.\n"
                + "Intente nuevamente. Los datos del cobro se conservan.");
            return false;
        }
    }
 
    // FACTURACIÓN
 
    /**
     * Verifica los requisitos previos y lanza la pantalla de facturación.
     *
     * @param event Evento de acción del botón.
     */
    @FXML
    private void handleFacturar(ActionEvent event) {
        if (mesaSeleccionada == 0 || idOrdenActual == 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Sin cuenta activa",
                "Seleccione una mesa con una orden lista primero.");
            return;
        }
        abrirFacturacion();
    }
 
    /**
     * Carga la vista {@code FacturacionPantalla.fxml} de forma modal.
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
 
    // NAVEGACIÓN
 
    /**
     * Regresa a la pantalla de login cerrando la sesión actual.
     *
     * @param event Evento de acción del botón.
     */
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar salida");
        alerta.setHeaderText("Cerrar sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");
 
        Optional<ButtonType> resultado = alerta.showAndWait();
 
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource(
                    "/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));
                Stage stage = (Stage) ((Node) event.getSource())
                        .getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            alerta.close();
        }
    }
 
    @FXML private void handleCobrarMesa(ActionEvent event) { /* Pantalla actual */ }
    @FXML private void handleMesas(ActionEvent event) { /* CSS EstadoMesas */ }
    
    // HELPERS
    
    /**
    * Asigna al botón un gráfico con dos líneas:
    * número de mesa (grande) y capacidad (pequeña).
    */
   private void setTextoMesa(Button btn, int numMesa, int capacidad) {
       javafx.scene.control.Label lblNumero = new javafx.scene.control.Label(String.valueOf(numMesa));
       lblNumero.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #d4c5b0;");

       javafx.scene.control.Label lblCapacidad = new javafx.scene.control.Label(capacidad + " 👥");
       lblCapacidad.setStyle("-fx-font-size: 10px; -fx-text-fill: #d4c5b0;");

       javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(2, lblNumero, lblCapacidad);
       vbox.setAlignment(javafx.geometry.Pos.CENTER);

       btn.setText("");        // limpia el texto nativo del botón
       btn.setGraphic(vbox);
   }
   
    /**
     * Limpia los campos de texto y la tabla cuando no hay una orden activa.
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
        LOG.warning(() -> "[CobrarController] Mesa " + mesaSeleccionada
                        + " no tiene orden abierta o lista para cobrar.");
    }
 
    /**
     * Devuelve los 12 botones de mesa en orden de idMesa (1-12).
     *
     * @return Arreglo de botones indexado por número de mesa.
     */
    private Button[] obtenerArregloMesas() {
        return new Button[]{
            btnMesa1, btnMesa2, btnMesa3, btnMesa4,
            btnMesa5, btnMesa6, btnMesa7, btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };
    }
 
    /**
     * Despliega un cuadro de diálogo modal estándar para alertar al usuario.
     *
     * @param tipo Tipo de alerta ({@code INFORMATION}, {@code WARNING}, etc.).
     * @param titulo Texto del título de la ventana.
     * @param contenido Mensaje principal visible para el usuario.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo,
                                String contenido) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(contenido);
        a.showAndWait();
    }
}