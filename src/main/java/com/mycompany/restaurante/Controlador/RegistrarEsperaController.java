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

    @FXML
    public void initialize() {
        lblTitulo.setText("Añadir a lista de espera");
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        // 1. Validar que no haya campos vacíos
        if (txtNombre.getText().trim().isEmpty() || 
            txtTelefono.getText().trim().isEmpty() || 
            txtNoPersonas.getText().trim().isEmpty()) {
            
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Por favor, llena todos los datos del cliente.");
            return;
        }

        try {
            // 2. Extraer datos
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            int numPersonas = Integer.parseInt(txtNoPersonas.getText().trim());

            // 3. Crear el objeto temporal
            ClienteEspera nuevoCliente = new ClienteEspera(nombre, telefono, numPersonas);

            // 4. Guardar en la Base de Datos
            boolean exito = ListaEsperaDAO.insertar(nuevoCliente);

            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente agregado a la lista de espera.");
                cerrarVentana();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar en la base de datos.");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de formato", "El número de personas debe ser un valor numérico entero.");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
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