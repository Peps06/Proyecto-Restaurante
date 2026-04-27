package com.mycompany.restaurante.Controlador;

/**
 * 
 * @author Dana
 */
import com.mycompany.restaurante.DAO.EmpleadoDAO;
import com.mycompany.restaurante.Modelo.Empleado;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsuario;

    @FXML private PasswordField txtContraseña;

    @FXML private Button btnIniciarSesion;

    // Lista para almacenar los empleados (por si decides usarla más adelante)
    private ObservableList<Empleado> datosMaestros = FXCollections.observableArrayList();

    private String autenticarUsuario(String usr, String pass) {
        return EmpleadoDAO.autenticar(usr, pass);
    }
    
    @FXML
    private void handleIniciarSesion(ActionEvent event) {
        // Obtener lo que el usuario escribió
        String usuario = txtUsuario.getText();
        String password = txtContraseña.getText();

        // Validar que no estén vacíos
        if (usuario == null || usuario.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Por favor, ingresa tu usuario y contraseña.");
            return;
        }

        // Autenticar y obtener el rol
        String rolEmpleado = autenticarUsuario(usuario, password);

        // Redirigir según el rol
        if (rolEmpleado != null) {
            switch (rolEmpleado) {
                case "Mesero":
                    cargarPantalla("/com/mycompany/restaurante/fxml/RegistrarPedidoPantalla.fxml", "Panel de Mesero");
                    break;
                case "Administrador":
                    cargarPantalla("/com/mycompany/restaurante/fxml/GestionarEmpleado.fxml", "Panel de Administración");
                    break;
                case "Chef":
                    cargarPantalla("/fxml/PantallaCocina.fxml", "Panel de Cocina");
                    break;
                case "Cajero":
                    cargarPantalla("/com/mycompany/restaurante/fxml/CobrarPantalla.fxml", "Panel de Caja");
                    break;
                case "Recepcionista":
                    cargarPantalla("/fxml/PantallaRecepcion.fxml", "Panel de Recepción");
                    break;
                default:
                    mostrarAlerta(Alert.AlertType.ERROR, "Error de Rol", "El rol asignado no tiene una vista creada aún.");
                    break;
            }
        } else {
            // Credenciales incorrectas
            mostrarAlerta(Alert.AlertType.ERROR, "Error de autenticación", "Usuario o contraseña incorrectos.");
        }
    }

    /**
     * Método genérico para cambiar de pantalla
     */
    private void cargarPantalla(String rutaFxml, String titulo) {
        try {
            // Buscar la ruta del archivo primero
            java.net.URL url = getClass().getResource(rutaFxml);
            
            // Si el archivo no existe o la ruta está mal, mostramos alerta y evitamos el choque
            if (url == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Archivo no encontrado", 
                        "Aún no has creado o está mal escrita la ruta de la pantalla:\n" + rutaFxml);
                return; // Detenemos el código aquí
            }

            // Si sí existe, cargamos la nueva vista normalmente
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            

            // Obtener el Stage actual desde el botón
            Stage stageActual = (Stage) btnIniciarSesion.getScene().getWindow();

            // Configurar la nueva escena
            Scene nuevaEscena = new Scene(root);
            stageActual.setScene(nuevaEscena);
            stageActual.setTitle(titulo);
            stageActual.centerOnScreen();
            stageActual.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "Ocurrió un problema al cargar la pantalla.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}