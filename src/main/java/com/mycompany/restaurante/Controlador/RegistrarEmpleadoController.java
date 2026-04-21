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

    private static final String ESTILO_NORMAL =
        "-fx-background-color: #F7F4ED; -fx-border-color: #1A1E2E; -fx-border-radius: 5 5 5 5;";
    private static final String ESTILO_ERROR  =
        "-fx-background-color: #FFF0F0; -fx-border-color: #cc0000; -fx-border-width: 2; -fx-border-radius: 5 5 5 5;";
    private static final String ESTILO_COMBO_ERROR =
        "-fx-background-color: #FFF0F0; -fx-border-color: #cc0000; -fx-border-width: 2; -fx-border-radius: 5 5 5 5;";
    
    @FXML
    private void initialize() {
//        boxPuesto.getItems().setAll(EMPLEADOS);
        boxPuesto.setItems(FXCollections.observableArrayList(EMPLEADOS));
        
        txtNombre.textProperty().addListener((o, a, b) -> txtNombre.setStyle(ESTILO_NORMAL));
        txtPassword.textProperty().addListener((o, a, b) -> txtPassword.setStyle(ESTILO_NORMAL));
        txtTelefono.textProperty().addListener((o, a, b) -> txtTelefono.setStyle(ESTILO_NORMAL));
    }

    @FXML
    private void RegistrarEmpleado() {
        // 1. Validación de campos vacíos
//        if (txtNombre.getText().isEmpty() || txtPassword.getText().isEmpty() || txtTelefono.getText().isEmpty() || boxPuesto.getValue() == null) {
//            mostrarAlerta("Campos incompletos", "Por favor, llena todos los datos.");
//            return;
//        }

        // Validación del txt
        if (txtNombre.getText().trim().isEmpty()){
            marcarError(txtNombre);
            mostrarAlerta("Todos los campos son obligatorios",
                    "El campo 'Nombre' no puede estar vacio.");
            return;
        }
        
        if (txtPassword.getText().trim().isEmpty()){
            marcarError(txtPassword);
            mostrarAlerta("Todos los campos son obligatorios",
                    "El campo 'Contraseña' no puede estar vacio.");
            return;
        }
        
        if (txtTelefono.getText().trim().isEmpty()){
            marcarError(txtTelefono);
            mostrarAlerta("Todos los campos son obligatorios",
                    "El campo 'Teléfono' no puede estar vacio.");
            return;
        }
        
        String tel = txtTelefono.getText().trim();
        
        if (!tel.matches("\\d{10}")) {
            marcarError(txtTelefono);
            mostrarAlerta("Error en teléfono", "El teléfono debe contener exactamente 10 dígitos numéricos.");
            return;
        }
        
        if (boxPuesto.getValue() == null) {
            boxPuesto.setStyle(ESTILO_COMBO_ERROR);
            mostrarAlerta("Todos los campos son obligatorios",
                    "Selecciona un 'Puesto'.");
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
    
    private void marcarError(TextField campo) {
        campo.setStyle(ESTILO_ERROR);
        campo.requestFocus();
    }
}