package com.mycompany.dulceria.Controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.mycompany.dulceria.Modelo.*;
import java.util.Optional;

/**
 * Controlador JavaFX para la ventana de Gestión de Usuarios.
 * Incluye operaciones CRUD completas (Crear, Buscar, Actualizar, Eliminar).
 * 
 * @author Dana, Rubi, Citlaly
 * @version 2.0 (JavaFX)
 */
public class CrearUsuarioController {
    
    @FXML private TextField txtBuscarId;
    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbTipo;
    
    @FXML private Button btnBuscar;
    @FXML private Button btnCrear;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnRegresar;
    
    private ImpUsuarioDAO dao;
    
    @FXML
    private void initialize() {
        try {
            dao = new ImpUsuarioDAO();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al inicializar base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        
        if (cmbTipo != null && cmbTipo.getItems() != null && !cmbTipo.getItems().isEmpty()) {
            cmbTipo.getSelectionModel().selectFirst();
        }
    }
    
    @FXML
    private void buscarUsuario() {
        String id = txtBuscarId.getText().trim();
        
        if (id.isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor ingresa un ID", Alert.AlertType.WARNING);
            return;
        }
        
        Usuario u = dao.obtenerUsuario(id);
        
        if (u == null) {
            mostrarAlerta("No encontrado", "Usuario no encontrado", Alert.AlertType.WARNING);
            limpiarCampos();
            return;
        }
        
        txtId.setText(u.getId());
        txtNombre.setText(u.getNombre());
        txtPassword.setText(u.getPassword());
        cmbTipo.setValue(u.getTipo());
    }
    
    @FXML
    private void crearUsuario() {
        String id = txtId.getText().trim();
        String nombre = txtNombre.getText().trim();
        String password = txtPassword.getText().trim();
        String tipo = cmbTipo.getValue();
        
        if (id.isEmpty() || nombre.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos", Alert.AlertType.WARNING);
            return;
        }
        
        if (dao.obtenerUsuario(id) != null) {
            mostrarAlerta("ID duplicado", "Ya existe un usuario con ese ID", Alert.AlertType.WARNING);
            return;
        }
        
        Usuario nuevo = new Usuario(id, nombre, password, tipo);
        
        try {
            dao.agregarUsuario(nuevo);
            mostrarAlerta("Éxito", "Usuario creado correctamente", Alert.AlertType.INFORMATION);
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al crear usuario: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void actualizarUsuario() {
        String id = txtId.getText().trim();
        
        if (dao.obtenerUsuario(id) == null) {
            mostrarAlerta("Usuario no encontrado", "Busca un usuario primero antes de actualizar", Alert.AlertType.WARNING);
            return;
        }
        
        String nombre = txtNombre.getText().trim();
        String password = txtPassword.getText().trim();
        String tipo = cmbTipo.getValue();
        
        if (nombre.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Campos incompletos", "Por favor completa todos los campos", Alert.AlertType.WARNING);
            return;
        }
        
        Usuario actualizado = new Usuario(id, nombre, password, tipo);
        
        try {
            dao.actualizarUsuario(actualizado);
            mostrarAlerta("Éxito", "Usuario actualizado correctamente", Alert.AlertType.INFORMATION);
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al actualizar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void eliminarUsuario() {
        String id = txtId.getText().trim();
        
        if (id.isEmpty()) {
            mostrarAlerta("Advertencia", "Busca un usuario antes de eliminar", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar eliminación");
        confirmAlert.setHeaderText("¿Seguro que deseas eliminar este usuario?");
        confirmAlert.setContentText("Usuario: " + txtNombre.getText() + " (ID: " + id + ")");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dao.eliminarUsuario(id);
                mostrarAlerta("Éxito", "Usuario eliminado correctamente", Alert.AlertType.INFORMATION);
                limpiarCampos();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void limpiarCampos() {
        txtBuscarId.clear();
        txtId.clear();
        txtNombre.clear();
        txtPassword.clear();
        if (cmbTipo != null && !cmbTipo.getItems().isEmpty()) {
            cmbTipo.getSelectionModel().selectFirst();
        }
        txtBuscarId.requestFocus();
    }
    
    @FXML
    private void regresar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/dulceria/fxml/MenuAdmin.fxml"));
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
