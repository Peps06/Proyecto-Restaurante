package com.mycompany.restaurante.Controlador;

/**
 *
 * @author Rubi
 */

import com.mycompany.restaurante.Modelo.Empleado;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class RegistrarEmpleadoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtPassword;
    @FXML private TextField txtTelefono;
    @FXML private ComboBox<String> boxPuesto;
    
    private static final String[] EMPLEADOS = {
        "Administrador",
        "Cajero",
        "Chef",
        "Mesero",
        "Recepcionista"
    };

    private Empleado nuevoEmpleado;

    @FXML
    private void initialize() {
        boxPuesto.getItems().setAll(EMPLEADOS);
    }

    @FXML
    private void RegistrarEmpleado() {
        // 1. Validación de campos vacíos
        if (txtNombre.getText().isEmpty() || txtPassword.getText().isEmpty() || txtTelefono.getText().isEmpty() || boxPuesto.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor, llena todos los datos.");
            return;
        }

        // 2. Validación de teléfono (Solo números Y exactamente 10 dígitos)
        String tel = txtTelefono.getText().trim();
        
        if (!tel.matches("\\d{10}")) {
            mostrarAlerta("Error en teléfono", "El teléfono debe contener exactamente 10 dígitos numéricos.");
            return;
        }

        // 3. Crear el objeto si todo está bien
        nuevoEmpleado = new Empleado(
            0, // El ID se asigna en el controlador principal
            txtNombre.getText(),
            txtPassword.getText(),
            boxPuesto.getValue(),
            "Ausente", // Valor por defecto
            txtTelefono.getText()
        );

        cerrarVentana();
    }

    @FXML
    private void cancelar() {
        nuevoEmpleado = null; // Para que no se agregue nada
        cerrarVentana();
    }

    private void cerrarVentana() {
        // Obtenemos la ventana actual a través de cualquier componente y la cerramos
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    // Este método lo usará la pantalla de la tabla para obtener al empleado creado
    public Empleado getNuevoEmpleado() {
        return nuevoEmpleado;
    }

    public void cargarDatos(Empleado empleado) {
        txtNombre.setText(empleado.getNombre());
        txtPassword.setText(empleado.getPassword());
        txtTelefono.setText(empleado.getTelefono());
        boxPuesto.setValue(empleado.getPuesto());
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}