package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.Modelo.Reservacion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 *
 * @author Dana
 */

public class RegistrarReservaController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private DatePicker txtFecha;
    @FXML private TextField txtHora;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtTelefono1; // FXML fx:id para Número de Personas
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    // Guardar la referencia de la reserva original si estamos editando
    private Reservacion reservaOriginal = null;
    
    private Reservacion reservaResultado = null;

    /**
     * Se llama desde la ventana principal cuando le damos a "Editar".
     * Precarga los datos en los campos de texto.
     */
    public void cargarDatos(Reservacion reserva) {
        this.reservaOriginal = reserva;
        
        lblTitulo.setText("Editar reserva"); // Cambiamos el título
        
        txtNombre.setText(reserva.getNombreCliente());
        txtFecha.setValue(reserva.getFecha());
        txtHora.setText(reserva.getHora().substring(0, 5)); // "14:30:00" -> "14:30"
        txtTelefono.setText(reserva.getTelefono());
        txtTelefono1.setText(String.valueOf(reserva.getNumeroPersonas()));
    }

    /**
     * Devuelve la reserva al controlador principal (null si el usuario canceló)
     */
    public Reservacion getReserva() {
        return reservaResultado;
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        // 1. Validar campos vacíos
        if (txtNombre.getText().trim().isEmpty() || txtFecha.getValue() == null || 
            txtHora.getText().trim().isEmpty() || txtTelefono.getText().trim().isEmpty() || 
            txtTelefono1.getText().trim().isEmpty()) {
            
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Por favor, llena todos los datos.");
            return;
        }

        try {
            // 2. Extraer datos
            String nombre = txtNombre.getText().trim();
            LocalDate fecha = txtFecha.getValue();
            String hora = txtHora.getText().trim();
            String telefono = txtTelefono.getText().trim();
            int numPersonas = Integer.parseInt(txtTelefono1.getText().trim());
            
            // 3. Crear el objeto con la información (preservando el ID, estado y mesa si editamos)
            int id = (reservaOriginal != null) ? reservaOriginal.getId() : 0;
            int idMesa = (reservaOriginal != null) ? reservaOriginal.getIdMesa() : 1; // Asignamos mesa 1 por defecto
            String estado = (reservaOriginal != null) ? reservaOriginal.getEstado() : "Pendiente";

            reservaResultado = new Reservacion(id, nombre, telefono, fecha, hora, numPersonas, idMesa, estado);

            // 4. Cerrar
            cerrarVentana();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de formato", "El número de personas debe ser un número entero.");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        reservaResultado = null; // Devolvemos nulo indicando que se canceló
        cerrarVentana();
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
}