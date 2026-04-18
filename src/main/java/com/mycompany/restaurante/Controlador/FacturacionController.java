/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.Modelo.DatosFacturacion;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de Facturación (FacturacionPantalla.fxml).
 *
 * @author Citlaly
 * 
 * Botones manejados:
 *   - btnFacturaPago  → valida campos y registra la factura
 *   - btnCancelarFactura → cierra la ventana sin guardar
 */
public class FacturacionController implements Initializable {

    // Campos del formulario 
    @FXML private TextField fieldNombreRazonSocial;
    @FXML private TextField fieldRFC;
    @FXML private TextField fieldCodigoPostal;
    @FXML private TextField fieldCorreo;
    @FXML private ComboBox<String> comboRegimenFiscal;
    @FXML private ComboBox<String> comboUsoCfdi;

    // Botones
    @FXML private Button btnFacturaPago;
    @FXML private Button btnCancelarFactura;

    /** Se setea desde CobrarMesaControlador para saber el total a facturar. */
    private double totalFactura = 0.0;

    // Listas de catálogos SAT (simplificadas para prototipo)
    private static final String[] REGIMENES = {
        "601 - General de Ley Personas Morales",
        "603 - Personas Morales con Fines no Lucrativos",
        "605 - Sueldos y Salarios e Ingresos Asimilados a Salarios",
        "606 - Arrendamiento",
        "612 - Personas Físicas con Actividades Empresariales y Profesionales",
        "616 - Sin obligaciones fiscales",
        "621 - Incorporación Fiscal",
        "625 - Régimen de las Actividades Empresariales con ingresos a través de Plataformas Tecnológicas",
        "626 - Régimen Simplificado de Confianza (RESICO)"
    };

    private static final String[] USOS_CFDI = {
        "G01 - Adquisición de mercancias",
        "G03 - Gastos en general",
        "D01 - Honorarios médicos, dentales y gastos hospitalarios",
        "D10 - Pagos por servicios educativos (colegiaturas)",
        "S01 - Sin efectos fiscales"
    };

    // Inicialización
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboRegimenFiscal.setItems(FXCollections.observableArrayList(REGIMENES));
        comboUsoCfdi.setItems(FXCollections.observableArrayList(USOS_CFDI));
    }

    // ── Setter para recibir el total desde la pantalla de cobro ───────────
    public void setTotalFactura(double total) {
        this.totalFactura = total;
    }

    //  btnFacturaPago → "Facturar y Registrar pago"
    @FXML
    private void handleFacturaPago(ActionEvent event) {

        // 1. Validar que no haya campos vacíos
        if (!validarCampos()) return;

        // 2. Construir objeto con los datos capturados
        DatosFacturacion datos = new DatosFacturacion(
            fieldNombreRazonSocial.getText().trim(),
            fieldRFC.getText().trim().toUpperCase(),
            fieldCodigoPostal.getText().trim(),
            fieldCorreo.getText().trim(),
            comboRegimenFiscal.getValue(),
            comboUsoCfdi.getValue()
        );

        // 3. Confirmar al usuario (en prototipo sin BD solo mostramos el resumen)
        Alert confirmacion = new Alert(Alert.AlertType.INFORMATION);
        confirmacion.setTitle("Factura Generada");
        confirmacion.setHeaderText("✔  Factura registrada correctamente");
        confirmacion.setContentText(
            "Razón social : " + datos.getNombreRazonSocial() + "\n" +
            "RFC          : " + datos.getRfc() + "\n" +
            "CP           : " + datos.getCodigoPostal() + "\n" +
            "Correo       : " + datos.getCorreo() + "\n" +
            "Régimen      : " + datos.getRegimenFiscal() + "\n" +
            "Uso CFDI     : " + datos.getUsoCfdi() + "\n\n" +
            "Total facturado: $" + String.format("%.2f", totalFactura)
        );
        confirmacion.showAndWait();

        // 4. Cerrar la ventana de facturación
        cerrarVentana();
    }

    //  btnCancelarFactura → cierra sin guardar
    @FXML
    private void handleCancelarFactura(ActionEvent event) {
        cerrarVentana();
    }

    // Helpers

    /**
     * Valida que todos los campos estén llenos.
     * Resalta en rojo el borde del campo vacío y muestra una alerta.
     *
     * @return true si todo está correcto, false si falta algo.
     */
    private boolean validarCampos() {
        boolean valido = true;
        String estiloCampoError   = "-fx-border-color: #cc0000; -fx-border-width: 2;";
        String estiloCampoNormal  = "-fx-background-color: #F7F4ED; -fx-border-color: #1A1E2E; -fx-border-radius: 5 5 5 5;";

        // Resetear estilos
        fieldNombreRazonSocial.setStyle(estiloCampoNormal);
        fieldRFC.setStyle(estiloCampoNormal);
        fieldCodigoPostal.setStyle(estiloCampoNormal);
        fieldCorreo.setStyle(estiloCampoNormal);

        if (fieldNombreRazonSocial.getText().trim().isEmpty()) {
            fieldNombreRazonSocial.setStyle(estiloCampoError);
            valido = false;
        }
        if (fieldRFC.getText().trim().isEmpty()) {
            fieldRFC.setStyle(estiloCampoError);
            valido = false;
        }
        if (fieldCodigoPostal.getText().trim().isEmpty()) {
            fieldCodigoPostal.setStyle(estiloCampoError);
            valido = false;
        }
        if (fieldCorreo.getText().trim().isEmpty()) {
            fieldCorreo.setStyle(estiloCampoError);
            valido = false;
        }
        if (comboRegimenFiscal.getValue() == null) {
            comboRegimenFiscal.setStyle("-fx-border-color: #cc0000; -fx-border-width: 2;");
            valido = false;
        } else {
            comboRegimenFiscal.setStyle("");
        }
        if (comboUsoCfdi.getValue() == null) {
            comboUsoCfdi.setStyle("-fx-border-color: #cc0000; -fx-border-width: 2;");
            valido = false;
        } else {
            comboUsoCfdi.setStyle("");
        }

        if (!valido) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Campos incompletos");
            alerta.setHeaderText("Por favor complete todos los campos.");
            alerta.setContentText("Los campos marcados en rojo son obligatorios.");
            alerta.showAndWait();
        }

        return valido;
    }

    /** Cierra la ventana emergente de facturación. */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelarFactura.getScene().getWindow();
        stage.close();
    }
}
