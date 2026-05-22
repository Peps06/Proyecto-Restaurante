package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.ListaEsperaDAO;
import com.mycompany.restaurante.Modelo.ClienteEspera;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author Dana
 */


public class RegistrarEsperaController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtNoPersonas;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    
    private static final String ESTILO_ERROR  =
        "-fx-background-color: #FFF0F0; -fx-border-color: #cc0000; -fx-border-width: 2; -fx-border-radius: 5 5 5 5;";

    @FXML
    public void initialize() {
        lblTitulo.setText("Añadir a lista de espera");
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        // 1. Validar que no haya campos vacíos
        if (txtNombre.getText().trim().isEmpty()){
            marcarError(txtNombre);
            mostrarAlerta(Alert.AlertType.ERROR,"Todos los campos son obligatorios",
                    "El campo 'Nombre' no puede estar vacio.");
            return;
        }
        
        if (txtNoPersonas.getText().trim().isEmpty()){
            marcarError(txtNoPersonas);
            mostrarAlerta(Alert.AlertType.ERROR,"Todos los campos son obligatorios",
                    "El campo 'Número de personas' no puede estar vacio.");
            return;
        }
        
        if (txtTelefono.getText().trim().isEmpty()){
            marcarError(txtTelefono);
            mostrarAlerta(Alert.AlertType.ERROR,"Todos los campos son obligatorios",
                    "El campo 'Teléfono' no puede estar vacio.");
            return;
        }
        
        String tel = txtTelefono.getText().trim();
        
        if (!tel.matches("\\d{10}")) {
            marcarError(txtTelefono);
            mostrarAlerta(Alert.AlertType.ERROR,"Error en teléfono", "El teléfono debe contener exactamente 10 dígitos numéricos.");
            return;
        }

        try {
            // 2. Extraer datos
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            int numPersonas = Integer.parseInt(txtNoPersonas.getText().trim());

            // 3. Verificamos si estamos en Modo Agregar o Modo Editar
            if (esperaOriginal == null) {
                
                ClienteEspera nuevoCliente = new ClienteEspera(nombre, telefono, numPersonas);
                boolean exito = ListaEsperaDAO.insertar(nuevoCliente);

                if (exito) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente agregado a la lista de espera.");
                    cerrarVentana();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar en la base de datos.");
                }
                
            } else {
                esperaResultado = new ClienteEspera(nombre, telefono, numPersonas);
                cerrarVentana();
                
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de formato", "El número de personas debe ser un valor numérico entero.");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarVentana();
    }
    
    
    private ClienteEspera esperaOriginal = null;
    private ClienteEspera esperaResultado = null;
    /**
     * Se llama desde la ventana principal cuando le damos a "Editar".
     * Precarga los datos en los campos de texto.
     */
    public void cargarDatos(ClienteEspera espera) {
        this.esperaOriginal = espera;
        
        lblTitulo.setText("Editar cliente en espera"); 
        
        txtNombre.setText(espera.getNombreCliente());
        txtTelefono.setText(espera.getTelefono());
        txtNoPersonas.setText(String.valueOf(espera.getNumeroPersonas()));
    }

    /**
     * Devuelve la reserva al controlador principal (null si el usuario canceló)
     */
    public ClienteEspera getEspera() {
        return esperaResultado;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    private void marcarError(TextField campo) {
        campo.setStyle(ESTILO_ERROR);
        campo.requestFocus();
    }
}