package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.ListaEsperaDAO;
import com.mycompany.restaurante.Modelo.ClienteEspera;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador principal para la gestión de la lista de espera dinámica en Saveurs Paris.
 * Se encarga de mostrar los flujos de clientes en espera dentro de un TableView, 
 * formatear los tiempos de llegada e interactuar con subventanas modales para registrar, 
 * modificar o realizar la asignación de mesas físicas.
 * * @author Dana
 * @version 1.0
 */
public class ListaEsperaController {

    // BOTONES DE NAVEGACIÓN Y MENÚ LATERAL
    @FXML private Button btnMesas;
    @FXML private Button btnReservas;
    @FXML private Button btnListaEsp;
    @FXML private Button btnCerrarSesion;
    
    // BOTONES DE ACCIONES OPERATIVAS
    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnCancelar;
    @FXML private Button btnAsignar;

    // TABLA DE LISTA DE ESPERA Y SUS COLUMNAS 
    @FXML private TableView<ClienteEspera> tablaListaEspera;
    @FXML private TableColumn<ClienteEspera, Integer> colId;
    @FXML private TableColumn<ClienteEspera, String> colNombre;
    @FXML private TableColumn<ClienteEspera, String> colTelefono;
    @FXML private TableColumn<ClienteEspera, Integer> colNumPersonas;
    @FXML private TableColumn<ClienteEspera, String> colLlegada; 
    @FXML private TableColumn<ClienteEspera, String> colEstado;

    /**
     * Inicializa el controlador enlazando los campos del modelo con el TableView
     * y aplicando una fábrica de celdas personalizada para formatear la hora de llegada de forma elegante.
     */
    @FXML
    public void initialize() {
        // 1. Configurar cómo se llenan las columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("idEspera"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colNumPersonas.setCellValueFactory(new PropertyValueFactory<>("numeroPersonas"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Configuración especial para que la hora se vea elegante (Ej: 14:30 PM)
        colLlegada.setCellValueFactory(cellData -> {
            LocalDateTime fechaHora = cellData.getValue().getHoraLlegada();
            String horaFormateada = fechaHora != null ? fechaHora.format(DateTimeFormatter.ofPattern("hh:mm a")) : "";
            return new SimpleStringProperty(horaFormateada);
        });

        // 2. Cargar los datos desde la BD
        cargarDatosTabla();
    }

    /**
     * Consulta la base de datos a través de la capa DAO para actualizar el contenedor de elementos de la tabla.
     */
    private void cargarDatosTabla() {
        ObservableList<ClienteEspera> lista = ListaEsperaDAO.obtenerEsperando();
        tablaListaEspera.setItems(lista);
    }

    /**
     * Inicializa y despliega la ventana modal encargada del registro de nuevos clientes en la lista de espera.
     * @param event Evento de acción originado por el botón agregar.
     */
    @FXML
    private void manejarAgregar(ActionEvent event) {
        try {
            // 1. Cargar el diseño de la ventanita
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/RegistrarEspera.fxml"));
            Parent root = loader.load();

            // 2. Configurar la nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setTitle("Añadir a lista de espera");
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.setScene(new Scene(root));
            
            // 3. Mostrar la ventana y pausar la ejecución de este código hasta que se cierre
            stage.showAndWait();

            // 4. Una vez que la ventanita se cierra recargamos la tabla para mostrar la base de datos actualizada.
            cargarDatosTabla();

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Sistema", "No se pudo cargar la ventana de registro: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    /**
     * Recupera el cliente seleccionado del TableView e inyecta su información en el formulario modal para su edición.
     * @param event Evento de acción originado por el botón de edición.
     */
    @FXML
    private void manejarEditar(ActionEvent event) {
        ClienteEspera seleccionada = tablaListaEspera.getSelectionModel().getSelectedItem();
        
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Por favor, selecciona una reserva de la tabla para editarla.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/RegistrarEspera.fxml"));
            Parent root = loader.load();

            // Pasar los datos antes de abrir la ventana
            RegistrarEsperaController controller = loader.getController();
            controller.cargarDatos(seleccionada);

            Stage stage = new Stage();
            stage.setTitle("Editar Reserva - Saveurs Paris");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recuperar la reserva editada
            ClienteEspera editada = controller.getEspera();

            if (editada != null) {
                // Actualizar los datos en la tabla 
                seleccionada.setNombreCliente(editada.getNombreCliente());
                seleccionada.setTelefono(editada.getTelefono());
                seleccionada.setNumeroPersonas(editada.getNumeroPersonas());

                ListaEsperaDAO.actualizar(seleccionada);
                
                tablaListaEspera.refresh();
                mostrarAlerta(Alert.AlertType.WARNING,"Éxito", "Datos actualizados correctamente.");
            }
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana de edición.");
            e.printStackTrace();
        }
    }

    /**
     * Evento de control temporal para la gestión del botón de cancelación de esperas.
     * @param event Evento de acción gatillado por el botón.
     */
    @FXML
    private void manejarCancelar(ActionEvent event) {
        System.out.println("Botón Cancelar presionado");
    }

    /**
     * Extrae el comensal seleccionado de la lista y despliega el formulario modal para asignarle una mesa libre.
     * @param event Evento de acción del botón de asignación.
     */
    @FXML
    private void manejarAsignar(ActionEvent event) {
        ClienteEspera seleccionado = tablaListaEspera.getSelectionModel().getSelectedItem();
        
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona a un cliente de la lista.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/AsignarMesa.fxml"));
            Parent root = loader.load();

            AsignarMesaController controller = loader.getController();
            controller.setDatosDesdeEspera(seleccionado);

            Stage stage = new Stage();
            stage.setTitle("Asignar Mesa");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarDatosTabla();

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana.");
            e.printStackTrace();
        }
    }

    /**
     * Redirige al operador del sistema hacia la pantalla de control de disponibilidad de mesas.
     * @param event Evento de procedencia del menú.
     */
    @FXML
    private void handleMesas(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/DisponibilidadRecepcionista.fxml", "Mesas - Recepción");
    }

    /**
     * Redirige al operador del sistema hacia la pantalla de administración de reservaciones generales.
     * @param event Evento de procedencia del menú.
     */
    @FXML
    private void handleReservas(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionReservas.fxml", "Reservas - Recepción");
    }

    /**
     * Fuerza la recarga síncrona de los elementos comensales vigentes en el TableView.
     * @param event Evento de acción lanzado por el botón de refrescado de lista.
     */
    @FXML
    private void handleListaEsp(ActionEvent event) {
        cargarDatosTabla();
    }
    
    /**
     * Despliega un cuadro de diálogo modal e informativo estandarizado en pantalla.
     * * @param tipo El tipo de categorización o icono de la alerta ({@code ERROR}, {@code WARNING}, etc.).
     * @param titulo Texto para el título del marco.
     * @param mensaje Cuerpo descriptivo explícito del aviso.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Valida mediante un diálogo de confirmación el cierre de la sesión de recepción, regresando al Login.
     * @param event Evento de acción del botón de salida.
     */
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        // Crear la alerta de confirmación
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");

        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // Código para regresar al Login 
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

    /**
     * Centraliza las transiciones y cambios de pantalla del sistema sustituyendo la escena del Stage principal.
     * @param event Evento origen de la navegación.
     * @param rutaFxml Ubicación del recurso `.fxml` al que se desea navegar.
     * @param titulo Encabezado descriptivo para la barra de la ventana.
     */
    private void cambiarPantalla(ActionEvent event, String rutaFxml, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(rutaFxml));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al cargar la pantalla: " + rutaFxml);
            alert.showAndWait();
        }
    }
}