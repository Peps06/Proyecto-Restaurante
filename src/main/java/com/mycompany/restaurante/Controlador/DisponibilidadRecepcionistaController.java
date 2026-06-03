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
 * Controlador de la pantalla de disponibilidad de mesas para la recepción.
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
    
    // BOTONES DE NAVEGACIÓN Y MENÚ LATERAL
    @FXML private Button btnMesas;
    @FXML private Button btnReservas;
    @FXML private Button btnListaEsp;
    @FXML private Button btnCerrarSesion;

   
    // ESTILOS CSS PARA ESTADOS VISUALES
    private static final String ESTILO_MESA_LIBRE =
        "-fx-background-color: #627096;" +
        "-fx-border-color: #627096;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_MESA_OCUPADA =
        "-fx-background-color: #8a3636;" +
        "-fx-border-color: #8a3636;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_MESA_RESERVADA =
        "-fx-background-color: #C9A84C;" +
        "-fx-border-color: #C9A84C;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_BTN_ACTIVO =
        "-fx-background-color: #2c3b62;" +
        "-fx-text-fill: #d4c5b0;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_BTN_INACTIVO =
        "-fx-background-color: #8b1a1a;" +
        "-fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;" +
        "-fx-text-fill: #d4c5b0;";
    
    private final MesasDAO mesasDAO = new MesasDAO();
    
    
    /**
     * Inicializa la vista configurando el estilo del menú lateral y 
     * sincronizando el estado visual de las mesas con la base de datos.
     * @param url Ubicación del recurso FXML utilizado.
     * @param rb Contenedor de recursos para internacionalización.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnMesas.setStyle(ESTILO_BTN_ACTIVO);
        cargarEstadoMesas();
    }
    
    /**
     * Consulta la base de datos y aplica el estilo visual correspondiente 
     * a cada botón de mesa según su estado actual.
     */
    private void cargarEstadoMesas() {
        Button[] botones = obtenerArregloMesas();
        List<Mesa> mesas = mesasDAO.obtenerTodasLasMesas();

        for (Mesa mesa : mesas) {
            int idx = mesa.getIdMesa() - 1;    
            if (idx < 0 || idx >= botones.length) continue;
            setTextoMesa(botones[idx], mesa.getIdMesa(), mesa.getCapacidad());

            switch (mesa.getEstado()) {
                case "Libre" -> botones[idx].setStyle(ESTILO_MESA_LIBRE);
                case "Ocupada" -> botones[idx].setStyle(ESTILO_MESA_OCUPADA);
                case "Reservada" -> botones[idx].setStyle(ESTILO_MESA_RESERVADA);
                default -> botones[idx].setStyle(ESTILO_MESA_LIBRE);
            }
        }
    }

    /**
     * Alterna y refresca los estilos visuales del menú y la cuadrícula al presionar el botón de disponibilidad.
     * @param event Evento de acción del botón.
     */
    @FXML
    private void handleDisponibilidad(ActionEvent event) {
        btnMesas.setStyle(ESTILO_BTN_ACTIVO);
        btnReservas.setStyle(ESTILO_BTN_INACTIVO);
        btnListaEsp.setStyle(ESTILO_BTN_INACTIVO);
        cargarEstadoMesas();
    }

    
    /**
     * Agrupa los botones individuales en un arreglo para facilitar su procesamiento iterativo.
     * @return Arreglo indexado con las instancias de los botones de las mesas.
     */
    private Button[] obtenerArregloMesas() {
        return new Button[] {
            btnMesa1, btnMesa2, btnMesa3,  btnMesa4,
            btnMesa5, btnMesa6, btnMesa7,  btnMesa8,
            btnMesa9, btnMesa10, btnMesa11, btnMesa12
        };
    }
    
    /**
     * Detecta la mesa seleccionada en el mapa; abre el modal de asignación si está libre o notifica su ocupación.
     * @param event Evento de acción disparado por el botón de la mesa cliqueada.
     */
    @FXML
    private void manejarClicMesa(ActionEvent event) {
        Button btnClickeado = (Button) event.getSource();

        // Obtener número de mesa desde el fx:id
        String idBoton = btnClickeado.getId();
        int numMesa = Integer.parseInt(idBoton.replace("btnMesa", ""));

        // Consultar estado real en BD (más confiable que leer el estilo CSS)
        Mesa mesa = mesasDAO.obtenerMesaPorId(numMesa);

        if (mesa == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", 
                "No se encontró la mesa " + numMesa);
            return;
        }

        if ("Libre".equals(mesa.getEstado())) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/mycompany/restaurante/fxml/AsignarMesa.fxml"));
                Parent root = loader.load();

                AsignarMesaController controller = loader.getController();
                controller.setDatosDesdeMapa(numMesa);

                Stage stage = new Stage();
                stage.setTitle("Asignar Mesa " + numMesa);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();

                cargarEstadoMesas();

            } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error",
                        "No se pudo abrir la ventana de asignación.");
                e.printStackTrace();
            }
        } else {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Mesa no disponible",
                    "La mesa " + numMesa + " ya está " + mesa.getEstado().toLowerCase() + ".");
        }
    }

    /**
     * Método genérico para realizar la transición y navegación fluida entre escenas del sistema.
     * @param event Evento origen de la navegación.
     * @param fxmlPath Ruta relativa del archivo de interfaz FXML de destino.
     * @param titulo Texto para la barra superior de la nueva ventana.
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

    /**
     * Redirige al operador hacia la pantalla de disponibilidad de mesas.
     * @param event Evento de disparo vinculado al menú de mesas.
     */
    @FXML
    private void handleMesas(ActionEvent event) {
        cambiarPantalla(event,
                "/com/mycompany/restaurante/fxml/DisponibilidadRecepcionista.fxml",
                "Disponibilidad Mesas");
    }

    /**
     * Redirige al operador hacia la pantalla de administración y control de reservaciones.
     * @param event Evento de disparo vinculado al menú de reservas.
     */
    @FXML
    private void handleReservas(ActionEvent event) {
        cambiarPantalla(event,
                "/com/mycompany/restaurante/fxml/GestionReservas.fxml",
                "Gestionar Reservas");
    }

    /**
     * Redirige al operador hacia la interfaz de control de la lista de espera dinámica.
     * @param event Evento de disparo vinculado al menú de lista de espera.
     */
    @FXML
    private void handleListaEsp(ActionEvent event) {
        cambiarPantalla(event,
                "/com/mycompany/restaurante/fxml/ListaDeEspera.fxml",
                "Lista de espera");
    }
    
    /**
    * Asigna al botón un gráfico con dos líneas:
    * número de mesa (grande) y capacidad (pequeña).
    */
   private void setTextoMesa(Button btn, int numMesa, int capacidad) {
       javafx.scene.control.Label lblNumero =
               new javafx.scene.control.Label(String.valueOf(numMesa));
       lblNumero.setStyle("-fx-font-size: 18px;" + 
                          "-fx-font-weight: bold;" + 
                          "-fx-text-fill: #d4c5b0;");

       javafx.scene.control.Label lblCapacidad =
               new javafx.scene.control.Label(capacidad + " 👥");
       lblCapacidad.setStyle("-fx-font-size: 10px;" +
                             "-fx-text-fill: #d4c5b0;");

       javafx.scene.layout.VBox vbox =
               new javafx.scene.layout.VBox(2, lblNumero, lblCapacidad);
       vbox.setAlignment(javafx.geometry.Pos.CENTER);

       btn.setText(""); // limpia el texto nativo del botón
       btn.setGraphic(vbox);
   }

    /**
     * Valida mediante una ventana de confirmación el cierre de sesión, redirigiendo al Login en caso afirmativo.
     * @param event Evento de disparo vinculado al botón de salida.
     */
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
                Parent root = FXMLLoader.load(getClass().getResource(
                        "/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));
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

    /**
     * Crea y despliega una ventana de aviso o alerta modal en pantalla de forma genérica.
     * @param tipo El tipo de categorización o icono del aviso ({@code ERROR}, {@code WARNING}, etc.).
     * @param titulo Texto a colocar en el encabezado de la barra del recuadro.
     * @param contenido Mensaje o cuerpo explícito que detalla la alerta.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(contenido);
        a.showAndWait();
    }
}