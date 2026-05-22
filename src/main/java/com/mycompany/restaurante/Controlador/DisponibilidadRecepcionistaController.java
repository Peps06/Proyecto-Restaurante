package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.MesasDAO;
import com.mycompany.restaurante.Modelo.Mesa;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador de la pantalla de disponibilidad de mesas.
 * Permite visualizar el mapa de mesas del restaurante, identificar su estado
 * (Libre, Ocupada, Reservada) mediante colores y navegar hacia la toma de pedidos.
 * 
 * @author Dana
 * @version 1.0
 */
public class DisponibilidadRecepcionistaController implements Initializable {
    
    private static final Logger LOG = Logger.getLogger(DisponibilidadRecepcionistaController.class.getName());
    
    // BOTONES DE MESAS (1 AL 12)
    @FXML private Button btnMesa1, btnMesa2, btnMesa3, btnMesa4;
    @FXML private Button btnMesa5, btnMesa6, btnMesa7, btnMesa8;
    @FXML private Button btnMesa9, btnMesa10, btnMesa11, btnMesa12;
    
    // BOTONES
    @FXML private Button btnMesas;
    @FXML private Button btnReservas;
    @FXML private Button btnListaEsp;
    @FXML private Button btnCerrarSesion;

   
    // ESTILOS CSS 
    private static final String ESTILO_MESA_LIBRE =
        "-fx-background-color: #627096; -fx-border-color: #627096; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;" +
        "-fx-text-fill: #0a132b;";
    private static final String ESTILO_MESA_OCUPADA =
        "-fx-background-color: #8a3636; -fx-border-color: #8a3636; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;" +
        "-fx-text-fill: #0a132b;";
    private static final String ESTILO_MESA_RESERVADA =
        "-fx-background-color: #C9A84C; -fx-border-color: #C9A84C; -fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10; -fx-border-radius: 10 10 10 10;" +
        "-fx-text-fill: #0a132b;";
    private static final String ESTILO_BTN_ACTIVO =
        "-fx-background-color: #2c3b62; -fx-text-fill: #d4c5b0; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_BTN_INACTIVO =
        "-fx-background-color: #8b1a1a; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10; -fx-text-fill: #d4c5b0;";
    
    private final MesasDAO mesasDAO = new MesasDAO();
    
    
    /**
     * Inicializa la vista configurando el estilo del menú lateral y 
     * sincronizando el estado visual de las mesas con la base de datos.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Marcar "Disponibilidad" como botón activo en el sidebar
        btnMesas.setStyle(ESTILO_BTN_ACTIVO);
        // Pintar mesas con el color de su estado actual en BD
        cargarEstadoMesas();
    }
    
    /**
     * Consulta la base de datos y aplica el estilo visual correspondiente 
     * a cada botón de mesa según su estado actual.
     */
    private void cargarEstadoMesas() {
        Button[] botones = obtenerArregloMesas();
        List<Mesa> mesas = mesasDAO.obtenerTodasLasMesas();

        // Si la BD no responde, los botones quedan con el estilo del FXML
        for (Mesa mesa : mesas) {
            int idx = mesa.getIdMesa() - 1;          // idMesa 1-12 → índice 0-11
            if (idx < 0 || idx >= botones.length) continue;

            switch (mesa.getEstado()) {
                case "Libre" -> botones[idx].setStyle(ESTILO_MESA_LIBRE);
                case "Ocupada" -> botones[idx].setStyle(ESTILO_MESA_OCUPADA);
                case "Reservada" -> botones[idx].setStyle(ESTILO_MESA_RESERVADA);
                default -> botones[idx].setStyle(ESTILO_MESA_LIBRE);
            }
        }
    }


    @FXML
    private void handleDisponibilidad(ActionEvent event) {
        btnMesas.setStyle(ESTILO_BTN_ACTIVO);
        btnReservas.setStyle(ESTILO_BTN_INACTIVO);
        btnListaEsp.setStyle(ESTILO_BTN_INACTIVO);
        cargarEstadoMesas();
    }

    
    /**
     * Agrupa los botones individuales en un arreglo para facilitar su procesamiento iterativo.
     */
    private Button[] obtenerArregloMesas() {
        return new Button[] {
            btnMesa1, btnMesa2, btnMesa3,  btnMesa4,
            btnMesa5, btnMesa6, btnMesa7,  btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };
    }

    /**
     * Método genérico para la navegación entre escenas.
     */
    private void cambiarPantalla(ActionEvent event, String fxmlPath, String titulo) {
        try {
            // 1. Cargar la vista desde el recurso
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            // 2. Obtener el Stage (ventana) actual
            javafx.scene.Node nodoOrigen = (javafx.scene.Node) event.getSource();
            Stage stageActual = (Stage) nodoOrigen.getScene().getWindow();

            // 3. Configurar la nueva escena y mostrarla
            Scene nuevaEscena = new Scene(root);
            stageActual.setScene(nuevaEscena);
            stageActual.setTitle(titulo + " - Saveurs Paris");
            stageActual.centerOnScreen();
            stageActual.show();

        } catch (java.io.IOException e) {
            System.err.println("Error al cargar la pantalla: " + fxmlPath);
            e.printStackTrace();
        }
    }
    @FXML
    private void handleMesas(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/DisponibilidadRecepcionista.fxml", "Disponibilidad Mesas");
    }

    @FXML
    private void handleReservas(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionReservas.fxml", "Gestionar Reservas");
    }

    @FXML
    private void handleListaEsp(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/ListaDeEspera.fxml", "Lista de espera");
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        // Crear la alerta de confirmación
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");

        // Mostrar y esperar respuesta
        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // Código para regresar al Login (ejemplo)
                Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // El usuario canceló, no se hace nada y se queda en la ventana
            alerta.close();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(contenido);
        a.showAndWait();
    }

}
