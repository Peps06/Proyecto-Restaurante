/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Controlador;

/**
 *
 * @author mrubi
 */

import com.mycompany.restaurante.Modelo.Producto;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FormularioProductoController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<String> cbTipo;
    @FXML private Button btnGuardar;

    private Producto producto;
    private boolean esEdicion = false;
    private boolean guardadoExitoso = false;

    @FXML
    public void initialize() {
        // Llenamos las opciones del ComboBox
        cbTipo.getItems().addAll("Plato fuerte", "Postre", "Entrada", "Bebida");
    }

    // --- ESTE MÉTODO ES EL "TRUCO" PARA EDITAR ---
    public void setProducto(Producto p) {
        this.producto = p;
        if (p != null) {
            esEdicion = true;
            lblTitulo.setText("Modificar producto");
            btnGuardar.setText("Actualizar producto");
            
            // Rellenamos los campos con los datos actuales
            txtNombre.setText(p.getNombre());
            txtDescripcion.setText(p.getDescripcion());
            txtPrecio.setText(String.valueOf(p.getPrecio()));
            cbTipo.setValue(p.getTipo());
        }
    }

    @FXML
    private void handleGuardar() {
        if (validarCampos()) {
            if (!esEdicion) {
                // Si es nuevo, usamos el Constructor B 
                producto = new Producto(
                    txtNombre.getText(),
                    cbTipo.getValue(),
                    Double.parseDouble(txtPrecio.getText()),
                    txtDescripcion.getText()
                );
            } else {
                // Si es edición, actualizamos el objeto existente
                producto.setNombre(txtNombre.getText());
                producto.setDescripcion(txtDescripcion.getText());
                producto.setPrecio(Double.parseDouble(txtPrecio.getText()));
                producto.setTipo(cbTipo.getValue());
            }
            guardadoExitoso = true;
            cerrarVentana();
        }
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }

    private boolean validarCampos() {
        // Validación básica: que nada esté vacío y el precio sea número
        try {
            Double.parseDouble(txtPrecio.getText());
            return !txtNombre.getText().isEmpty() && cbTipo.getValue() != null;
        } catch (NumberFormatException e) {
            return false; 
        }
    }

    public Producto getProducto() { return producto; }
    public boolean isGuardadoExitoso() { return guardadoExitoso; }
}