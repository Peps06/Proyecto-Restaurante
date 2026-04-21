/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Controlador;

/**
 *
 * @author mrubi
 */

import com.mycompany.restaurante.Modelo.Empleado;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class RegistrarEmpleadoController {

    // Estos nombres deben ser los mismos que pongas en el fx:id de Scene Builder
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtPuesto;

    private Empleado nuevoEmpleado;

    @FXML
    private void guardar() {
        // 1. Validación de campos vacíos
        if (txtNombre.getText().isEmpty() || txtTelefono.getText().isEmpty() || txtPuesto.getText().isEmpty()) {
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
            txtPuesto.getText(),
            "Presente", // Valor por defecto
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
    txtTelefono.setText(empleado.getTelefono());
    txtPuesto.setText(empleado.getPuesto());
}

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
