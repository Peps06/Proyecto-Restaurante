package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.EmpleadoDAO;
import com.mycompany.restaurante.Modelo.Empleado;

import java.io.IOException;
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

/**
 * Controlador principal para la gestión de acceso al sistema Saveurs Paris.
 * Se encarga de validar las credenciales de los usuarios y redirigirlos
 * a sus respectivos paneles de trabajo según su rol asignado.
 * 
 * @author Dana
 * @version 2.0
 */
public class LoginController {

    // CONTROLES DE LA INTERFAZ FXML
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContraseña;
    @FXML private Button btnIniciarSesion;

    /**
     * Valida las credenciales a través de la capa de acceso a datos de Empleados.
     * * @param usr Nombre de usuario ingresado.
     * @param pass Contraseña ingresada.
     * @return El rol obtenido del empleado, o {@code null} si es inválido.
     */
    private String autenticarUsuario(String usr, String pass) {
        return EmpleadoDAO.autenticar(usr, pass);
    }
    
    /**
     * Gestiona la acción del botón de inicio de sesión: valida campos vacíos,
     * evalúa el rol de las credenciales y redirige a la vista correspondiente.
     * * @param event Evento de acción del botón.
     */
    @FXML
    private void handleIniciarSesion(ActionEvent event) {
        String usuario  = txtUsuario.getText();
        String password = txtContraseña.getText();

        if (usuario == null || usuario.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Campos vacíos",
                "Por favor, ingresa tu usuario y contraseña.");
            return;
        }

        // Usamos el método nuevo que devuelve el objeto completo con ID
        Empleado empleado = EmpleadoDAO.autenticarConId(usuario, password);

        if (empleado == null) {
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error de autenticación",
                "Usuario o contraseña incorrectos.");
            return;
        }

        switch (empleado.getPuesto()) {
            case "Mesero"         -> cargarPantallaMesero(empleado.getId());
            case "Administrador"  -> cargarPantalla(
                "/com/mycompany/restaurante/fxml/GestionarEmpleado.fxml",
                "Panel de Administración");
            case "Chef"           -> cargarPantalla(
                "/com/mycompany/restaurante/fxml/PedidosActuales.fxml",
                "Panel de Cocina");
            case "Cajero"         -> cargarPantallaCajero(empleado.getId());
            case "Recepcionista"  -> cargarPantalla(
                "/com/mycompany/restaurante/fxml/DisponibilidadRecepcionista.fxml",
                "Panel de Recepción");
            default -> mostrarAlerta(Alert.AlertType.ERROR,
                "Error de Rol",
                "El rol asignado no tiene una vista creada aún.");
        }
    }
    
    /**
     * Redirige al menú de visualización general del restaurante.
     * * @param event Evento de acción del botón de menú.
     */
    @FXML
    private void handleMenu(ActionEvent event) {
        cargarPantalla("/com/mycompany/restaurante/fxml/Menu.fxml", "Panel de Mesero");
    }
    
    /**
     * Carga la pantalla del mesero e inyecta su ID en el controlador.
     */
    private void cargarPantallaMesero(int idEmpleado) {
        try {
            java.net.URL url = getClass().getResource(
                "/com/mycompany/restaurante/fxml/MesasDisponiblesPantalla.fxml");

            if (url == null) {
                mostrarAlerta(Alert.AlertType.ERROR,
                    "Archivo no encontrado",
                    "No se encontró MesasDisponiblesPantalla.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Inyectar el ID real del mesero
            MesasDisponiblesController ctrl = loader.getController();
            ctrl.setIdEmpleadoSesion(idEmpleado);

            Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel de Mesero - Saveurs Paris");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error de Carga",
                "Ocurrió un problema al cargar la pantalla del mesero.");
        }
    }

    /**
     * Carga la pantalla del cajero e inyecta su ID en el controlador.
     */
    private void cargarPantallaCajero(int idEmpleado) {
        try {
            java.net.URL url = getClass().getResource(
                "/com/mycompany/restaurante/fxml/CobrarPantalla.fxml");

            if (url == null) {
                mostrarAlerta(Alert.AlertType.ERROR,
                    "Archivo no encontrado",
                    "No se encontró CobrarPantalla.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Inyectar el ID real del cajero
            CobrarController ctrl = loader.getController();
            ctrl.setIdEmpleadoActual(idEmpleado);

            Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel de Caja - Saveurs Paris");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error de Carga",
                "Ocurrió un problema al cargar la pantalla del cajero.");
        }
    }

    /**
     * Carga y despliega un nuevo archivo FXML sobre el Stage actual del sistema.
     * Realiza una validación previa para mitigar rupturas visuales por rutas erróneas.
     * 
     * @param rutaFxml Ubicación del archivo de la vista dentro del proyecto.
     * @param titulo Texto que se desplegará en la barra superior de la ventana.
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

    /**
     * Despliega un cuadro de diálogo emergente estándar para interactuar con el usuario.
     * * @param tipo Categoría o icono del aviso ({@code ERROR}, {@code WARNING}, {@code INFORMATION}).
     * @param titulo Texto que aparecerá en la cabecera de la alerta.
     * @param mensaje Contenido textual explícito del aviso.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}