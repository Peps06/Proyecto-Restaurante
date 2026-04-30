package com.mycompany.dulceria.Controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.mycompany.dulceria.Modelo.*;

/**
 * Controlador JavaFX para la ventana de Login de Empleados.
 * Valida credenciales usando ImpUsuarioDAO.
 * 
 * @author Dana, Rubi, Citlaly
 * @version 2.0 (JavaFX)
 */
public class LoginEmpleadoController {
    
    @FXML private TextField txtId;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtTipo;
    @FXML private Button btnAceptar;
    @FXML private Button btnRegresar;
    
    private ImpUsuarioDAO usuarioDao;
    private ImpDulceDAO dao;
    private ImpVentaDAO ventaDao;
    
    @FXML
    private void initialize() {
        try {
            usuarioDao = new ImpUsuarioDAO();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar base de datos de usuarios: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        
        try {
            dao = new ImpDulceDAO();
            ventaDao = new ImpVentaDAO();
        } catch (Exception ex) {
            mostrarAlerta("Error", "Error cargando base de datos: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
        
        txtTipo.setText("Empleado");
    }
    
    @FXML
    private void iniciarSesion() {
        String id = txtId.getText().trim();
        String password = txtPassword.getText().trim();
        String tipo = txtTipo.getText().trim();
        
        if (id.isEmpty() || password.isEmpty() || tipo.isEmpty()) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos", Alert.AlertType.WARNING);
            return;
        }
        
        Usuario empleado = usuarioDao.validarEmpleado(id, password, tipo);
        
        if (empleado == null) {
            mostrarAlerta("Error de autenticación", "Datos incorrectos o el usuario no es empleado", Alert.AlertType.ERROR);
            return;
        }
        
        mostrarAlerta("Bienvenido", "Bienvenido " + empleado.getNombre(), Alert.AlertType.INFORMATION);
        
        
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/dulceria/fxml/RegistrarVentana.fxml"));
            Parent root = loader.load();
            
            RegistrarVentanaController ctrl = loader.getController();
            ctrl.setDao(dao);
            ctrl.setVentaDao(ventaDao);
            
            Stage stage = (Stage) btnAceptar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cargar la ventana de ventas." + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void regresar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/dulceria/fxml/LoginVentana.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegresar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al regresar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}