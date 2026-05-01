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
 * Controlador para la interfaz de facturación.
 * Gestiona la captura de datos fiscales, validación de formatos (RFC, CP, Correo)
 * y la persistencia de la factura a través de {@link FacturaDAO}.
 * 
 * @author Citlaly
 * @version 2.0 (Integración con Base de Datos)
 */
public class FacturacionController implements Initializable {

    // COMPONENTES FXML 
    @FXML private TextField fieldNombreRazonSocial;
    @FXML private TextField fieldRFC;
    @FXML private TextField fieldCodigoPostal;
    @FXML private TextField fieldCorreo;
    @FXML private ComboBox<String> comboRegimenFiscal;
    @FXML private ComboBox<String> comboUsoCfdi;
    @FXML private Button btnFacturaPago;
    @FXML private Button btnCancelarFactura;

    // ESTADO INTERNO Y DATOS DE LA ORDEN
    private double totalFactura = 0.0;
    private int numMesa = 0;
    private int idOrden = 0;
    private double subtotal = 0.0;
    private double iva = 0.0;

    // CATÁLOGOS SAT (Constantes para cumplimiento fiscal)
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

    // ESTILOS CSS EN LÍNEA PARA VALIDACIÓN VISUAL
    private static final String ESTILO_NORMAL =
        "-fx-background-color: #F7F4ED; -fx-border-color: #1A1E2E; -fx-border-radius: 5;";
    private static final String ESTILO_ERROR  =
        "-fx-background-color: #FFF0F0; -fx-border-color: #cc0000; -fx-border-width: 2; -fx-border-radius: 5;";
    private static final String ESTILO_COMBO_ERROR =
        "-fx-border-color: #cc0000; -fx-border-width: 2;";

    /**
     * Inicializa la pantalla configurando los catálogos de los ComboBoxes
     * y añadiendo listeners para limpiar el estilo de error al escribir.
     * 
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboRegimenFiscal.setItems(FXCollections.observableArrayList(REGIMENES));
        comboUsoCfdi.setItems(FXCollections.observableArrayList(USOS_CFDI));

        // Limpiar estilos de error automáticamente cuando el usuario comienza a corregir
        configurarLimpiezaDeErrores();
    }

    // Setters para los datos desde CobrarMesaControlador
    public void setIdOrden(int id) {
        this.idOrden = id;
    }
    public void setSubtotal(double s) {
        this.subtotal = s;
    }
    public void setIva(double i) {
        this.iva = i; 
    }
    public void setTotalFactura(double total) { 
        this.totalFactura = total; 
    }
    public void setNumMesa(int numMesa) { 
        this.numMesa = numMesa; 
    }

    /**
     * Procesa la generación de la factura.
     * Valida campos, crea los objetos de modelo e intenta guardarlos en la base de datos.
     * 
     * @param event Evento de clic en el botón.
     */
    @FXML
    private void handleFacturaPago(ActionEvent event) {
        if (!validarCampos()) return;

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
            
            if (guardado) {
                mostrarAlertaExito(factura, datos);
                cerrarVentana();
            } else {
                throw new Exception("La base de datos rechazó el registro de la factura.");
            }

        } catch (Exception e) {
            mostrarAlertaError(e.getMessage());
        }
    }

    @FXML
    private void handleCancelarFactura(ActionEvent event) {
        cerrarVentana();
    }

    // VALIDACIÓN

    /**
     * Coordina la validación de presencia de datos y formatos específicos.
     * @return true si el formulario es válido.
     */
    private boolean validarCampos() {
        if (!validarPresencia(fieldNombreRazonSocial, "Nombre o Razón Social")) return false;
        if (!validarPresencia(fieldRFC, "RFC")) return false;
        if (!validarPresencia(fieldCodigoPostal, "Código Postal")) return false;
        if (!validarPresencia(fieldCorreo, "Correo Electrónico")) return false;

        // Validaciones de formato delegadas a la clase de modelo DatosFacturacion
        if (!validarFormato(fieldRFC, DatosFacturacion.rfcValido(fieldRFC.getText().trim()), 
            "RFC inválido", "Verifique el formato (ej. XAXX010101XXX).")) return false;

        if (!validarFormato(fieldCodigoPostal, DatosFacturacion.cpValido(fieldCodigoPostal.getText().trim()), 
            "Código Postal inválido", "El Código Postal debe tener 5 dígitos.")) return false;

        if (!validarFormato(fieldCorreo, DatosFacturacion.correoValido(fieldCorreo.getText().trim()), 
            "Correo electrónico no válido", "Verifique la estructura del correo (debe incluir @ y dominio).")) return false;

        if (!validarCombo(comboRegimenFiscal, "Régimen Fiscal")) return false;
        if (!validarCombo(comboUsoCfdi, "Uso de CFDI")) return false;

        return true;
    }

    // HELPERS

    private void configurarLimpiezaDeErrores() {
        fieldNombreRazonSocial.textProperty().addListener((o, a, b) -> fieldNombreRazonSocial.setStyle(ESTILO_NORMAL));
        fieldRFC.textProperty().addListener((o, a, b) -> fieldRFC.setStyle(ESTILO_NORMAL));
        fieldCodigoPostal.textProperty().addListener((o, a, b) -> fieldCodigoPostal.setStyle(ESTILO_NORMAL));
        fieldCorreo.textProperty().addListener((o, a, b) -> fieldCorreo.setStyle(ESTILO_NORMAL));
    }

    private boolean validarPresencia(TextField campo, String nombre) {
        if (campo.getText().trim().isEmpty()) {
            marcarError(campo);
            mostrarAlertaCampo("Campo Obligatorio", "El campo '" + nombre + "' no puede estar vacío.");
            return false;
        }
        return true;
    }

    private boolean validarFormato(TextField campo, boolean esValido, String titulo, String mensaje) {
        if (!esValido) {
            marcarError(campo);
            mostrarAlertaCampo(titulo, mensaje);
            return false;
        }
        return true;
    }

    private boolean validarCombo(ComboBox<String> combo, String nombre) {
        if (combo.getValue() == null) {
            combo.setStyle(ESTILO_COMBO_ERROR);
            mostrarAlertaCampo("Selección Requerida", "Debe seleccionar un '" + nombre + "'.");
            return false;
        }
        combo.setStyle("");
        return true;
    }

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

    private void mostrarAlertaExito(Factura factura, DatosFacturacion datos) {
        Alert exito = new Alert(Alert.AlertType.INFORMATION);
        exito.setTitle("Facturación Exitosa");
        exito.setHeaderText("Factura generada con folio: " + factura.getFolio());
        exito.setContentText(String.format(
            "Receptor: %s\nRFC: %s\nTotal: $%.2f\n\nEl registro se ha guardado en la base de datos.",
            datos.getNombreRazonSocial(), datos.getRfc(), totalFactura
        ));
        exito.showAndWait();
    }

    private void mostrarAlertaError(String detalle) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Error de Facturación");
        error.setHeaderText("No se pudo completar la operación");
        error.setContentText("Detalle técnico: " + detalle);
        error.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelarFactura.getScene().getWindow();
        stage.close();
    }
}