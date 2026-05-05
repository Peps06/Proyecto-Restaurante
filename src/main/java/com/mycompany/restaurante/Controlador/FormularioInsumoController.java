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

    @FXML private TextField txtNombre, txtStock; 
    @FXML private ComboBox<String> cbCategoria, cbUnidad;
    @FXML private Label lblTitulo;

    private Insumo insumo;
    private boolean guardadoExitoso = false;
    private com.mycompany.restaurante.DAO.InsumoDAO insumoDAO = new com.mycompany.restaurante.DAO.InsumoDAO();

    @FXML
    public void initialize() {
        cbCategoria.getItems().addAll("Abarrotes", "Lácteos", "Carnes", "Bebidas", "Vegetales");
        cbUnidad.getItems().addAll("kg", "litros", "piezas", "gramos", "ml", "botellas");
    }

    public void setInsumo(Insumo i) {
        this.insumo = i;
        if (i != null) {
            lblTitulo.setText("Editar Insumo");
            txtNombre.setText(i.getNombre());
            txtStock.setText(String.valueOf(i.getStock()));
            cbUnidad.setValue(i.getUnidad()); 
            cbCategoria.setValue(i.getCategoria());
        } else {
            lblTitulo.setText("Nuevo Insumo");
        }
    }

    @FXML
    private void guardar() {
        if (validarCampos()) {
            try {
                String nombre = txtNombre.getText();
                double stock = Double.parseDouble(txtStock.getText());
                String unidad = cbUnidad.getValue(); 
                String cat = cbCategoria.getValue();
                
                // Calculamos el estado automáticamente
                String est = definirEstado(stock); 

                boolean exitoOperacion = false;

                if (insumo == null) {
                    insumo = new Insumo(0, nombre, cat, stock, unidad, est);
                    exitoOperacion = insumoDAO.insertar(insumo); 
                } else {
                    insumo.setNombre(nombre);
                    insumo.setStock(stock);
                    insumo.setUnidad(unidad);
                    insumo.setCategoria(cat);
                    insumo.setEstado(est); 
                    exitoOperacion = insumoDAO.editar(insumo); 
                }

                if (exitoOperacion) {
                    guardadoExitoso = true;
                    cerrarVentana();
                } else {
                    mostrarAlerta("No se pudieron guardar los cambios en el servidor.");
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("La cantidad de stock debe ser un número válido.");
            }
        }
    }

    private String definirEstado(double stock) {
       if (stock <= 0) return "Agotado";
       if (stock <= 5) return "Por agotarse";
       return "Disponible";
    }

   
    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private boolean validarCampos() {
        String mensaje = "";
        if (txtNombre.getText().trim().isEmpty()) mensaje += "- El nombre es obligatorio.\n";
        if (cbCategoria.getValue() == null) mensaje += "- Seleccione una categoría.\n";
        if (cbUnidad.getValue() == null) mensaje += "- Seleccione una unidad.\n";
        
        try {
            if (txtStock.getText().isEmpty()) {
                mensaje += "- El stock no puede estar vacío.\n";
            } else {
                Double.parseDouble(txtStock.getText());
            }
        } catch (NumberFormatException e) {
            mensaje += "- El stock debe ser un número.\n";
        }

        if (mensaje.isEmpty()) return true;
        
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Datos incompletos");
        alerta.setHeaderText("Por favor corrija lo siguiente:");
        alerta.setContentText(mensaje);
        alerta.showAndWait();
        return false;
    }

    @FXML private void cancelar() { cerrarVentana(); }
    
    private void cerrarVentana() { ((Stage) txtNombre.getScene().getWindow()).close(); }
    
    public Insumo getInsumo() { return insumo; }
    
    public boolean isGuardadoExitoso() { return guardadoExitoso; }
}