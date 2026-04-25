package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.FacturaDAO;
import com.mycompany.restaurante.Modelo.DatosFacturacion;
import com.mycompany.restaurante.Modelo.Factura;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de FacturacionPantalla.fxml 
 * 
 * @author Citlaly
 * @version 2.0 (con BD)
 */
public class FacturacionController implements Initializable {

    //  Campos del formulario 
    @FXML private TextField fieldNombreRazonSocial;
    @FXML private TextField fieldRFC;
    @FXML private TextField fieldCodigoPostal;
    @FXML private TextField fieldCorreo;
    @FXML private ComboBox<String> comboRegimenFiscal;
    @FXML private ComboBox<String> comboUsoCfdi;

    //  Botones 
    @FXML private Button btnFacturaPago;
    @FXML private Button btnCancelarFactura;

    //  Datos recibidos desde CobrarMesaControlador 
    private double totalFactura = 0.0;
    private int numMesa = 0;

    //  Catálogos SAT (simplificados para prototipo sin BD) 
    private static final String[] REGIMENES = {
        "601 - General de Ley Personas Morales",
        "603 - Personas Morales con Fines no Lucrativos",
        "605 - Sueldos y Salarios e Ingresos Asimilados a Salarios",
        "606 - Arrendamiento",
        "612 - Personas Físicas con Actividades Empresariales y Profesionales",
        "616 - Sin obligaciones fiscales",
        "621 - Incorporación Fiscal",
        "625 - Reg. Actividades Empresariales con ingresos a través de Plataformas",
        "626 - Régimen Simplificado de Confianza (RESICO)"
    };

    private static final String[] USOS_CFDI = {
        "G01 - Adquisición de mercancias",
        "G03 - Gastos en general",
        "D01 - Honorarios médicos, dentales y gastos hospitalarios",
        "D10 - Pagos por servicios educativos (colegiaturas)",
        "S01 - Sin efectos fiscales"
    };

    // Estilos de campo
    private static final String ESTILO_NORMAL =
        "-fx-background-color: #F7F4ED; -fx-border-color: #1A1E2E; -fx-border-radius: 5 5 5 5;";
    private static final String ESTILO_ERROR  =
        "-fx-background-color: #FFF0F0; -fx-border-color: #cc0000; -fx-border-width: 2; -fx-border-radius: 5 5 5 5;";
    private static final String ESTILO_COMBO_ERROR =
        "-fx-border-color: #cc0000; -fx-border-width: 2;";
    
    // campos y 
    private int idOrden  = 0;
    private double subtotal = 0.0;
    private double iva = 0.0;
    
    //setters
    public void setIdOrden(int id) {
        this.idOrden  = id;
    }
    public void setSubtotal(double s) {
        this.subtotal = s;
    }
    public void setIva(double i) {
        this.iva = i; 
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboRegimenFiscal.setItems(FXCollections.observableArrayList(REGIMENES));
        comboUsoCfdi.setItems(FXCollections.observableArrayList(USOS_CFDI));

        fieldNombreRazonSocial.textProperty().addListener((o, a, b) -> fieldNombreRazonSocial.setStyle(ESTILO_NORMAL));
        fieldRFC.textProperty().addListener((o, a, b) -> fieldRFC.setStyle(ESTILO_NORMAL));
        fieldCodigoPostal.textProperty().addListener((o, a, b) -> fieldCodigoPostal.setStyle(ESTILO_NORMAL));
        fieldCorreo.textProperty().addListener((o, a, b) -> fieldCorreo.setStyle(ESTILO_NORMAL));
    }

    //  Setters llamados desde CobrarMesaControlador 
    public void setTotalFactura(double total) { this.totalFactura = total; }
    public void setNumMesa(int numMesa) { this.numMesa = numMesa; }

    //  btnFacturaPago — "Facturar y Registrar pago"
    @FXML
    private void handleFacturaPago(ActionEvent event) {

        // Validar campos
        if (!validarCampos()) return;

        // Generar factura con folio único
        try {
            DatosFacturacion datos = new DatosFacturacion(
                fieldNombreRazonSocial.getText().trim(),
                fieldRFC.getText().trim().toUpperCase(),
                fieldCodigoPostal.getText().trim(),
                fieldCorreo.getText().trim(),
                comboRegimenFiscal.getValue(),
                comboUsoCfdi.getValue()
            );

            Factura factura = new Factura(numMesa, totalFactura, datos);
            
            boolean guardado = FacturaDAO.insertar(factura, idOrden, subtotal, iva);
            if (!guardado) {
                // Opcional: mostrar advertencia, la factura se generó pero no se guardó en BD
                System.err.println("Advertencia: factura generada pero no exsistente en BD.");
            }

            // mostrar mensaje de éxito con folio generado
            Alert exito = new Alert(Alert.AlertType.INFORMATION);
            exito.setTitle("Factura generada");
            exito.setHeaderText("Factura generada correctamente");
            exito.setContentText(
                "Folio: " + factura.getFolio() + "\n\n" +
                "Mesa       : " + numMesa + "\n" +
                "Razón social: " + datos.getNombreRazonSocial() + "\n" +
                "RFC        : " + datos.getRfc() + "\n" +
                "CP         : " + datos.getCodigoPostal() + "\n" +
                "Correo     : " + datos.getCorreo() + "\n" +
                "Régimen    : " + datos.getRegimenFiscal() + "\n" +
                "Uso CFDI   : " + datos.getUsoCfdi() + "\n\n" +
                "Total facturado: $" + String.format("%.2f", totalFactura)
            );
            exito.showAndWait();

            cerrarVentana();

        } catch (Exception e) {
            // Error al generar la factura (conexión PAC / BD / falla interna)
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error de facturación");
            error.setHeaderText("Error al generar la factura");
            error.setContentText(
                "Error al generar la factura. Intente nuevamente.\n\n" +
                "Detalle técnico: " + e.getMessage()
            );
            error.showAndWait();
            // los datos del formulario se conservan; NO se cierra la ventana.
        }
    }

    //  btnCancelarFactura
    @FXML
    private void handleCancelarFactura(ActionEvent event) {
        cerrarVentana();
    }

    //  Validación de campos

    /**
     * Valida cada campo del formulario en orden.
     *  
     * Ante el primer error muestra la alerta correspondiente y marca el campo en rojo.
     * Regresa true solo si todos los campos son correctos.
     */
    private boolean validarCampos() {

        // Campos vacíos (nombre / razón social)
        if (fieldNombreRazonSocial.getText().trim().isEmpty()) {
            marcarError(fieldNombreRazonSocial);
            mostrarAlertaCampo("Todos los campos son obligatorios",
                "El campo 'Nombre o Razón Social' no puede estar vacío.");
            return false;
        }

        // RFC inválido
        String rfcTexto = fieldRFC.getText().trim().toUpperCase();
        if (rfcTexto.isEmpty()) {
            marcarError(fieldRFC);
            mostrarAlertaCampo("Todos los campos son obligatorios",
                "El campo 'RFC' no puede estar vacío.");
            return false;
        }
        if (!DatosFacturacion.rfcValido(rfcTexto)) {
            marcarError(fieldRFC);
            mostrarAlertaCampo("RFC inválido",
                "RFC inválido. Verifique el formato (ej. XAXX010101XXX).");
            return false;
        }

        // Código postal inválido
        String cpTexto = fieldCodigoPostal.getText().trim();
        if (cpTexto.isEmpty()) {
            marcarError(fieldCodigoPostal);
            mostrarAlertaCampo("Todos los campos son obligatorios",
                "El campo 'Código Postal' no puede estar vacío.");
            return false;
        }
        if (!DatosFacturacion.cpValido(cpTexto)) {
            marcarError(fieldCodigoPostal);
            mostrarAlertaCampo("Código Postal inválido",
                "Código Postal debe tener 5 dígitos.");
            return false;
        }

        // Correo electrónico inválido
        String correoTexto = fieldCorreo.getText().trim();
        if (correoTexto.isEmpty()) {
            marcarError(fieldCorreo);
            mostrarAlertaCampo("Todos los campos son obligatorios",
                "El campo 'Correo Electrónico' no puede estar vacío.");
            return false;
        }
        if (!DatosFacturacion.correoValido(correoTexto)) {
            marcarError(fieldCorreo);
            mostrarAlertaCampo("Correo electrónico no válido",
                "Correo electrónico no válido.");
            return false;
        }

        // ComboBox régimen fiscal vacío
        if (comboRegimenFiscal.getValue() == null) {
            comboRegimenFiscal.setStyle(ESTILO_COMBO_ERROR);
            mostrarAlertaCampo("Todos los campos son obligatorios",
                "Seleccione un 'Régimen Fiscal'.");
            return false;
        } else {
            comboRegimenFiscal.setStyle("");
        }

        // ComboBox uso CFDI vacío
        if (comboUsoCfdi.getValue() == null) {
            comboUsoCfdi.setStyle(ESTILO_COMBO_ERROR);
            mostrarAlertaCampo("Todos los campos son obligatorios",
                "Seleccione un 'Uso de CFDI'.");
            return false;
        } else {
            comboUsoCfdi.setStyle("");
        }

        return true;
    }

    //  Helpers 

    private void marcarError(TextField campo) {
        campo.setStyle(ESTILO_ERROR);
        campo.requestFocus();
    }

    private void mostrarAlertaCampo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelarFactura.getScene().getWindow();
        stage.close();
    }
}

