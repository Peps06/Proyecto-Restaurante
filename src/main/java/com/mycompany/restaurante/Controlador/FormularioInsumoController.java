/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Controlador;

/**
 *
 * @author mrubi
 */

import com.mycompany.restaurante.Modelo.Insumo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FormularioInsumoController {

    @FXML private TextField txtNombre, txtStock, txtUnidad;
    @FXML private ComboBox<String> cbCategoria, cbEstado;
    @FXML private Label lblTitulo;

    private Insumo insumo;
    private boolean guardadoExitoso = false;
    private com.mycompany.restaurante.DAO.InsumoDAO insumoDAO = new com.mycompany.restaurante.DAO.InsumoDAO();

    @FXML
    public void initialize() {
        cbCategoria.getItems().addAll("Abarrotes", "Lácteos", "Carnes", "Bebidas", "Vegetales");
        cbEstado.getItems().addAll("Disponible", "Por agotarse", "Agotado");
    }

    public void setInsumo(Insumo i) {
        this.insumo = i;
        if (i != null) {
            lblTitulo.setText("Editar Insumo");
            txtNombre.setText(i.getNombre());
            txtStock.setText(String.valueOf(i.getStock()));
            txtUnidad.setText(i.getUnidad());
            cbCategoria.setValue(i.getCategoria());
            cbEstado.setValue(i.getEstado());
        } else {
            lblTitulo.setText("Nuevo Insumo");
        }
    }

    @FXML
    private void guardar() {
    if (validarCampos()) {
        String nombre = txtNombre.getText();
        double stock = Double.parseDouble(txtStock.getText());
        String unidad = txtUnidad.getText();
        String cat = cbCategoria.getValue();
        String est = cbEstado.getValue();

        boolean exitoOperacion = false;

        if (insumo == null) {
            // Creamos el objeto y lo mandamos a la BD
            insumo = new Insumo(0, nombre, cat, stock, unidad, est);
            exitoOperacion = insumoDAO.insertar(insumo); // <--- se manda a la bd
        } else {
            // Actualizamos el objeto actual
            insumo.setNombre(nombre);
            insumo.setStock(stock);
            insumo.setUnidad(unidad);
            insumo.setCategoria(cat);
            insumo.setEstado(est);
            exitoOperacion = insumoDAO.editar(insumo); // <--- se actualiza la bd
        }

        if (exitoOperacion) {
            guardadoExitoso = true;
            cerrarVentana();
        } else {
            // Si el DAO regresa false (por error de conexión, etc.)
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error de base de datos");
            error.setContentText("No se pudieron guardar los cambios en el servidor.");
            error.showAndWait();
        }
    }
}

    private boolean validarCampos() {
    String mensaje = "";

    // Validar nombre
    if (txtNombre.getText().trim().isEmpty()) {
        mensaje += "- El nombre del producto es obligatorio.\n";
    }

    // Validar categoría
    if (cbCategoria.getValue() == null) {
        mensaje += "- Debe seleccionar una categoría.\n";
    }

    // Validar stock (el try-catch que ya tenías)
    try {
        if (txtStock.getText().isEmpty()) {
            mensaje += "- El stock no puede estar vacío.\n";
        } else {
            Double.parseDouble(txtStock.getText());
        }
    } catch (NumberFormatException e) {
        mensaje += "- El stock debe ser un número (ej: 10.5).\n";
    }

    // Si el mensaje sigue vacío, todo está bien
    if (mensaje.isEmpty()) {
        return true;
    } else {
        // Mostramos el error
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Datos incompletos");
        alerta.setHeaderText("Por favor corrija lo siguiente:");
        alerta.setContentText(mensaje);
        alerta.showAndWait();
        return false;
    }
}

    @FXML private void cancelar() { 
        cerrarVentana(); }
    
    private void cerrarVentana() { 
        ((Stage) txtNombre.getScene().getWindow()).close(); }
    
    public Insumo getInsumo() { 
        return insumo; }
    
    public boolean isGuardadoExitoso() { 
        return guardadoExitoso; }
}
