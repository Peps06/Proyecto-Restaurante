/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.restaurante.Controlador;

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
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador de CobrarMesaPantalla.fxml.
 * 
 * @author Citlaly
 *
 * Botones manejados:
 *   - btnMesa1 … btnMesa12 → selecciona la mesa y carga su orden de prueba
 *   - btnEfectivo           → marca método de pago Efectivo y calcula cambio
 *   - btnTarjeta            → marca método de pago Tarjeta (oculta cambio)
 *   - btnRegistrarPago      → muestra confirmación y opción de facturar
 *   - btnFacturar           → abre FacturacionPantalla.fxml como ventana emergente
 *   - btnCobrarMesa         → (sidebar) navega a la vista de cobro (ya está aquí)
 *   - btnCerrarSesion       → cierra sesión / cierra la ventana
 */
public class CobrarMesaController implements Initializable {

    // Tabla de pedido
    @FXML private TableView<OrdenItem>     tableVerPedido;
    @FXML private TableColumn<OrdenItem, String>  colProducto;
    @FXML private TableColumn<OrdenItem, Integer> colCantidad;
    @FXML private TableColumn<OrdenItem, Double>  colPrecio;

    // Labels de resumen
    @FXML private Label labelMesa;
    @FXML private Label labelIdOrden;
    @FXML private Label labelSubtotal;
    @FXML private Label labelIVA;
    @FXML private Label labelTotal;
    @FXML private Label labelCambio;

    // Botones de mesa
    @FXML private Button btnMesa1;
    @FXML private Button btnMesa2;
    @FXML private Button btnMesa3;
    @FXML private Button btnMesa4;
    @FXML private Button btnMesa5;
    @FXML private Button btnMesa6;
    @FXML private Button btnMesa7;
    @FXML private Button btnMesa8;
    @FXML private Button btnMesa9;
    @FXML private Button btnMesa10;
    @FXML private Button btnMesa11;
    @FXML private Button btnMesa12;

    // Botones de pago
    @FXML private Button  btnEfectivo;
    @FXML private Button  btnTarjeta;
    @FXML private Button  btnRegistrarPago;
    @FXML private Button  btnFacturar;
    @FXML private Spinner<Double> spinnerMonto;

    // Botones de navegación 
    @FXML private Button btnCobrarMesa;
    @FXML private Button btnCerrarSesion;

    // Estado interno 
    private int     mesaSeleccionada = 0;
    private double  subtotal         = 0.0;
    private double  iva              = 0.0;
    private double  total            = 0.0;
    private String  metodoPago       = "EFECTIVO";  // "EFECTIVO" | "TARJETA"

    private static final double TASA_IVA = 0.16;

    // Datos de prueba (sin BD) 
    private final ObservableList<OrdenItem> ordenPrueba =
        FXCollections.observableArrayList(
            new OrdenItem("Soupe à l'oignon",   1, 120.00),
            new OrdenItem("Coq au Vin",         2, 280.00),
            new OrdenItem("Crème Brûlée",       2,  95.00),
            new OrdenItem("Eau minérale 500ml", 3,  40.00)
        );

    // Estilo de mesa seleccionada 
    private static final String ESTILO_MESA_LIBRE     =
        "-fx-background-color: #f5efe6; -fx-border-color: #1A1E2E; " +
        "-fx-border-radius: 10 10 10 10; -fx-font-family: mont;";
    private static final String ESTILO_MESA_SELECCION =
        "-fx-background-color: #8b1a1a; -fx-border-color: #8b1a1a; " +
        "-fx-border-radius: 10 10 10 10; -fx-font-family: mont; -fx-text-fill: #d4c5b0;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Configurar columnas de la tabla
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        // Formato de precio en la columna
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                setText(empty || precio == null ? null : "$" + String.format("%.2f", precio));
            }
        });

        // Configurar spinner de monto recibido (rango 0 – 9999.99)
        SpinnerValueFactory<Double> svf =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 9999.99, 0, 1);
        spinnerMonto.setValueFactory(svf);
        spinnerMonto.setEditable(true);

        // Recalcular cambio cuando cambia el monto
        spinnerMonto.valueProperty().addListener((obs, ov, nv) -> calcularCambio());

        // Vincular botones de mesa
        vincularBotonesMesa();
    }

    //  btnMesa1 … btnMesa12
    private void vincularBotonesMesa() {
        Button[] mesas = {
            btnMesa1, btnMesa2, btnMesa3, btnMesa4,
            btnMesa5, btnMesa6, btnMesa7, btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };
        for (int i = 0; i < mesas.length; i++) {
            final int numMesa = i + 1;
            mesas[i].setOnAction(e -> seleccionarMesa(numMesa, mesas));
        }
    }

    private void seleccionarMesa(int numMesa, Button[] mesas) {
        // Resetear estilos de todas las mesas
        for (Button b : mesas) b.setStyle(ESTILO_MESA_LIBRE);

        // Marcar la seleccionada
        mesas[numMesa - 1].setStyle(ESTILO_MESA_SELECCION);
        mesaSeleccionada = numMesa;

        // Actualizar labels de cabecera
        labelMesa.setText("Mesa " + numMesa);
        labelIdOrden.setText("Orden #00" + numMesa + " · " + java.time.LocalDate.now());

        // Cargar datos de prueba en la tabla
        tableVerPedido.setItems(ordenPrueba);

        // Calcular totales
        subtotal = ordenPrueba.stream()
                              .mapToDouble(OrdenItem::getSubtotalItem)
                              .sum();
        iva   = subtotal * TASA_IVA;
        total = subtotal + iva;

        labelSubtotal.setText("$" + String.format("%.2f", subtotal));
        labelIVA.setText("$"      + String.format("%.2f", iva));
        labelTotal.setText("$"    + String.format("%.2f", total));

        // Resetear spinner y cambio
        spinnerMonto.getValueFactory().setValue(0.0);
        labelCambio.setText("$0.00");
    }

    //  btnEfectivo
    @FXML
    private void handleEfectivo(ActionEvent event) {
        metodoPago = "EFECTIVO";
        btnEfectivo.setStyle(
            "-fx-background-color: #2c3b62; -fx-text-fill: #d4c5b0; " +
            "-fx-font-weight: bold; -fx-background-radius: 10 10 10 10;");
        btnTarjeta.setStyle(
            "-fx-font-family: mont; -fx-background-color: #8b1a1a; " +
            "-fx-font-style: bold; -fx-background-radius: 10 10 10 10; -fx-text-fill: #d4c5b0;");
        spinnerMonto.setDisable(false);
        calcularCambio();
    }

    //  btnTarjeta
    @FXML
    private void handleTarjeta(ActionEvent event) {
        metodoPago = "TARJETA";
        btnTarjeta.setStyle(
            "-fx-background-color: #2c3b62; -fx-text-fill: #d4c5b0; " +
            "-fx-font-weight: bold; -fx-background-radius: 10 10 10 10;");
        btnEfectivo.setStyle(
            "-fx-font-family: mont; -fx-background-color: #8b1a1a; " +
            "-fx-font-style: bold; -fx-background-radius: 10 10 10 10; -fx-text-fill: #d4c5b0;");
        // Con tarjeta no se necesita calcular cambio
        spinnerMonto.setDisable(true);
        labelCambio.setText("N/A");
    }

    //  Calcular cambio (solo efectivo) 
    private void calcularCambio() {
        if (!"EFECTIVO".equals(metodoPago)) return;
        double montoRecibido = spinnerMonto.getValue() != null ? spinnerMonto.getValue() : 0.0;
        double cambio = montoRecibido - total;
        labelCambio.setText(cambio >= 0
            ? "$" + String.format("%.2f", cambio)
            : "-$" + String.format("%.2f", Math.abs(cambio)));
    }

    //  btnRegistrarPago
    //  Muestra ventana de confirmación y opción de generar factura
    @FXML
    private void handleRegistrarPago(ActionEvent event) {

        // Verificar que haya una mesa seleccionada
        if (mesaSeleccionada == 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Sin mesa seleccionada",
                "Seleccione una mesa antes de registrar el pago.");
            return;
        }

        // Verificar monto suficiente en efectivo
        if ("EFECTIVO".equals(metodoPago)) {
            double montoRecibido = spinnerMonto.getValue() != null ? spinnerMonto.getValue() : 0.0;
            if (montoRecibido < total) {
                mostrarAlerta(Alert.AlertType.WARNING,
                    "Monto insuficiente",
                    "El monto recibido ($" + String.format("%.2f", montoRecibido) +
                    ") es menor al total ($" + String.format("%.2f", total) + ").");
                return;
            }
        }

        // Ventana de confirmación con botones personalizados 
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar pago");
        confirmacion.setHeaderText("Mesa " + mesaSeleccionada + " · Pago con " + metodoPago);
        confirmacion.setContentText(
            "Total: $" + String.format("%.2f", total) + "\n\n" +
            "¿Desea también generar una FACTURA para este pago?"
        );

        ButtonType btnSoloRegistrar  = new ButtonType("Registrar pago");
        ButtonType btnGenerarFactura = new ButtonType("Registrar y Facturar");
        ButtonType btnCancelar       = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmacion.getButtonTypes().setAll(btnSoloRegistrar, btnGenerarFactura, btnCancelar);

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent()) {
            if (resultado.get() == btnSoloRegistrar) {
                registrarPagoSimple();
            } else if (resultado.get() == btnGenerarFactura) {
                registrarPagoSimple();
                abrirFacturacion();
            }
            // Si cancela, no hace nada
        }
    }

    /** Simula el registro del pago (sin BD). */
    private void registrarPagoSimple() {
        mostrarAlerta(Alert.AlertType.INFORMATION,
            "Pago registrado",
            "El pago de la Mesa " + mesaSeleccionada +
            " ha sido registrado correctamente.\nTotal: $" + String.format("%.2f", total));
    }

    //  btnFacturar → abre FacturacionPantalla.fxml
    @FXML
    private void handleFacturar(ActionEvent event) {
        if (mesaSeleccionada == 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Sin mesa seleccionada",
                "Seleccione una mesa antes de generar la factura.");
            return;
        }
        abrirFacturacion();
    }

    /** Abre FacturacionPantalla.fxml como ventana modal. */
    private void abrirFacturacion() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/restaurante/fxml/FacturacionPantalla.fxml")
            );
            Parent root = loader.load();

            // Pasar el total al controlador de facturación
            FacturacionController facturacionCtrl = loader.getController();
            facturacionCtrl.setTotalFactura(total);

            Stage stage = new Stage();
            stage.setTitle("Datos de Facturación · Mesa " + mesaSeleccionada);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);   // bloquea la ventana padre
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error al abrir facturación",
                "No se pudo cargar FacturacionPantalla.fxml:\n" + e.getMessage());
        }
    }

    //  btnCerrarSesion
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar sesión");
        confirmacion.setHeaderText("¿Desea cerrar la sesión?");
        confirmacion.setContentText("Se cerrará la pantalla actual.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
            stage.close();
        }
    }

    //  btnCobrarMesa (sidebar) — ya estamos en esta pantalla
    @FXML
    private void handleCobrarMesa(ActionEvent event) {
        // Ya estamos en la pantalla de cobro; no se hace nada extra.
    }

    // Utilidad
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
