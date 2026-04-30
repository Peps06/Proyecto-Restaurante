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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador de la pantalla principal de gestión de mesas.
 * Permite visualizar el mapa de mesas del restaurante, identificar su estado
 * (Libre, Ocupada, Reservada) mediante colores y navegar hacia la toma de pedidos.
 * 
 * @author Citlaly
 * @version 1.0
 */
public class MesasDisponiblesController implements Initializable {
    
    private static final Logger LOG = Logger.getLogger(MesasDisponiblesController.class.getName());
    
    // BOTONES DE MESAS (1 AL 12)
    @FXML private Button btnMesa1, btnMesa2, btnMesa3, btnMesa4;
    @FXML private Button btnMesa5, btnMesa6, btnMesa7, btnMesa8;
    @FXML private Button btnMesa9, btnMesa10, btnMesa11, btnMesa12;
    
    // BOTONES
    @FXML private Button btnDisponibilidad;
    @FXML private Button btnRealizarPedido;
    @FXML private Button btnGestionar;
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
    
    private int mesaParaPedido = 0;
    private int idEmpleadoSesion;
    
    /**
     * Inicializa la vista configurando el estilo del menú lateral y 
     * sincronizando el estado visual de las mesas con la base de datos.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Marcar "Disponibilidad" como botón activo en el sidebar
        btnDisponibilidad.setStyle(ESTILO_BTN_ACTIVO);
 
        // Registrar click en cada botón de mesa
        vincularBotonesMesa();
 
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

    /**
     * Asigna programáticamente el evento Click a cada botón del arreglo de mesas.
     */
    private void vincularBotonesMesa() {
        Button[] botones = obtenerArregloMesas();
        for (int i = 0; i < botones.length; i++) {
            final int numMesa = i + 1;
            botones[i].setOnAction(e -> handleClickMesa(numMesa));
        }
    }

    /**
     * Procesa la acción al hacer clic en una mesa.
     * Si la mesa está libre, ofrece iniciar un pedido; de lo contrario, 
     * informa sobre su estado actual.
     * 
     * @param numMesa El número de mesa seleccionada.
     */
    private void handleClickMesa(int numMesa) {
        Mesa mesa = mesasDAO.obtenerMesaPorId(numMesa);
 
        if (mesa == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                "Mesa no encontrada",
                "No se encontró la Mesa " + numMesa + " en el sistema.");
            return;
        }
 
        switch (mesa.getEstado()) {
 
            case "Libre" -> {
                //  Diálogo de confirmación para registrar pedido
                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Registrar pedido");
                confirmacion.setHeaderText("Mesa " + numMesa + "  ·  " +
                                           mesa.getCapacidad() + " personas  ·  Libre");
                confirmacion.setContentText(
                    "¿Desea registrar el pedido de la Mesa #" + numMesa + "?"
                );
 
                ButtonType btnIr      = new ButtonType("Sí, registrar pedido");
                ButtonType btnCancelar = new ButtonType("Cancelar",
                                         javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmacion.getButtonTypes().setAll(btnIr, btnCancelar);
 
                Optional<ButtonType> resultado = confirmacion.showAndWait();
 
                if (resultado.isPresent() && resultado.get() == btnIr) {
                    // Guardar la mesa seleccionada para pasarla al siguiente controlador
                    mesaParaPedido = numMesa;
                    navegarARealizarPedido(numMesa);
                }
            }
 
            case "Ocupada" -> {
                // INFORMAR QUE YA HAY UNA ORDEN ABIERTA
                mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Mesa ocupada",
                    "Mesa " + numMesa + " ya tiene una orden abierta.\n" +
                    "Para gestionar el pedido usa la opción 'Gestionar pedido'."
                );
            }
 
            case "Reservada" -> {
                // INFORMAR QUE YA ESTÁ RESERVADA
                mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Mesa reservada",
                    "Mesa " + numMesa + " está reservada.\n" +
                    "No se puede tomar un pedido directamente en esta mesa."
                );
            }
 
            default -> mostrarAlerta(Alert.AlertType.WARNING,
                "Estado desconocido",
                "La Mesa " + numMesa + " tiene un estado no reconocido: " + mesa.getEstado());
        }
    }

    // CONTROLADORES DE BOTONES
    @FXML
    private void handleDisponibilidad(ActionEvent event) {
        btnDisponibilidad.setStyle(ESTILO_BTN_ACTIVO);
        btnRealizarPedido.setStyle(ESTILO_BTN_INACTIVO);
        btnGestionar.setStyle(ESTILO_BTN_INACTIVO);
        cargarEstadoMesas();
    }

    @FXML
    private void handleRealizarPedido(ActionEvent event) {
        navegarA("/com/mycompany/restaurante/fxml/RegistrarPedidoPantalla.fxml",
                 "Realizar Pedido - Saveurs Paris", event);
    }

    @FXML
    private void handleGestionar(ActionEvent event) {
        navegarA("/com/mycompany/restaurante/fxml/GestionarPedidoPantalla.fxml",
                 "Gestionar Pedido - Saveurs Paris", event);
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        navegarA("/com/mycompany/restaurante/fxml/LoginPantalla.fxml",
                 "Iniciar sesión - Saveurs Paris", event);
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
     * Realiza la transición a la pantalla de pedidos, enviando el número de mesa seleccionada.
     */
    private void navegarARealizarPedido(int numMesa) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/mycompany/restaurante/fxml/RegistrarPedidoPantalla.fxml"
            ));
            Parent root = loader.load();

            RegistrarPedidoController ctrl = loader.getController();
            ctrl.setMesaSeleccionada(numMesa);

            Stage nuevoStage = new Stage(); 
            
            nuevoStage.setScene(new Scene(root));
            nuevoStage.setTitle("Realizar Pedido · Mesa " + numMesa + " - Saveurs Paris");

            nuevoStage.initModality(Modality.APPLICATION_MODAL);
            nuevoStage.initOwner(btnMesa1.getScene().getWindow()); 
            nuevoStage.setResizable(false);
            nuevoStage.showAndWait();

            cargarEstadoMesas();
        } catch (IOException e) {
            LOG.severe("[MesasDisponiblesController] No se pudo cargar RegistrarPedidoPantalla: "
                       + e.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error de navegación",
                "No se pudo abrir la pantalla de pedido.\n" +
                "Verifique que RegistrarPedidoPantalla.fxml exista en la ruta correcta.");
        }
    }
    
    public void setIdEmpleadoSesion(int id) {
        this.idEmpleadoSesion = id;
    }

    /**
     * Método genérico para la navegación entre escenas.
     */
    private void navegarA(String rutaFxml, String titulo, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(rutaFxml));
            Stage stageActual = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stageActual.setScene(new Scene(root));
            stageActual.setTitle(titulo);
            stageActual.show();
        } catch (IOException e) {
            LOG.severe("[MesasDisponiblesController] No se pudo cargar: " + rutaFxml);
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR,
                "Error de navegación",
                "No se pudo cargar la pantalla solicitada.");
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
