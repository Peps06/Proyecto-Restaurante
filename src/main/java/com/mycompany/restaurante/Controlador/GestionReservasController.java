package com.mycompany.restaurante.Controlador;

/**
 *
 * @author Dana
 */

import com.mycompany.restaurante.DAO.MesasDAO;
import com.mycompany.restaurante.DAO.ReservacionDAO;
import com.mycompany.restaurante.Modelo.Reservacion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import javafx.scene.Node;
import javafx.stage.Modality;

public class GestionReservasController {

    @FXML private Button btnReservas;
    @FXML private Button btnMesas;
    @FXML private Button btnListaEsp;
    @FXML private Button btnCerrarSesion;

    @FXML private TextField txtBusqueda;
    @FXML private Button btnBuscar;
    @FXML private Button btnMostrarTabla;

    @FXML private TableView<Reservacion> tablaReservas;
    @FXML private TableColumn<Reservacion, Integer> colId;
    @FXML private TableColumn<Reservacion, String> colNombre;
    @FXML private TableColumn<Reservacion, LocalDate> colFecha;
    @FXML private TableColumn<Reservacion, String> colHora;
    @FXML private TableColumn<Reservacion, String> colTelefono;
    @FXML private TableColumn<Reservacion, Integer> colNumPersonas;
    @FXML private TableColumn<Reservacion, String> colEstado;
    @FXML private TableColumn<Reservacion, Integer> colMesa;

    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnEstado; 
    
    private static final String ESTILO_BTN_ACTIVO =
        "-fx-background-color: #2c3b62; -fx-text-fill: #d4c5b0; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10;";
    private static final String ESTILO_BTN_INACTIVO =
        "-fx-background-color: #8b1a1a; -fx-background-radius: 10 10 10 10;" +
        "-fx-border-radius: 10 10 10 10; -fx-text-fill: #d4c5b0;";

    private ObservableList<Reservacion> masterData = FXCollections.observableArrayList();
    private FilteredList<Reservacion> filteredData;
    private final MesasDAO mesasDAO = new MesasDAO();

    @FXML
    public void initialize() {
        // 1. Configurar las columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colNumPersonas.setCellValueFactory(new PropertyValueFactory<>("numeroPersonas"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colMesa.setCellValueFactory(new PropertyValueFactory<>("idMesa"));
        
        btnReservas.setStyle(ESTILO_BTN_ACTIVO);

        // 2. Cargar los datos desde la Base de Datos usando el método estático
        cargarTabla();
    }

    @FXML
    private void handleReserva(ActionEvent event) {
        btnMesas.setStyle(ESTILO_BTN_INACTIVO);
        btnReservas.setStyle(ESTILO_BTN_ACTIVO);
        btnListaEsp.setStyle(ESTILO_BTN_INACTIVO);
    }
    
    /**
     * Carga o recarga los datos desde la BD a la tabla.
     */
    private void cargarTabla() {
        masterData = ReservacionDAO.obtenerTodos(); 
        filteredData = new FilteredList<>(masterData, p -> true);
        tablaReservas.setItems(filteredData);
    }
    
    @FXML
    private void manejarBusqueda(ActionEvent event) {
        String texto = txtBusqueda.getText().toLowerCase();
        filteredData.setPredicate(reservacion -> {
            if (texto == null || texto.isEmpty()) return true;
            
            // Permite buscar por nombre de cliente o por ID de reservación
            String idStr = String.valueOf(reservacion.getId());
            return reservacion.getNombreCliente().toLowerCase().contains(texto) || idStr.contains(texto);
        });
    }

    @FXML
    private void handleMostrarTabla(ActionEvent event) {
        // Resetea el filtro para mostrar todas las reservaciones
        filteredData.setPredicate(p -> true);
        txtBusqueda.clear();
        cargarTabla();
    }

    @FXML
    private void manejarAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/RegistrarReserva.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Saveurs Paris - Registro de Reserva");
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.setScene(new Scene(root));
            stage.showAndWait();

            RegistrarReservaController controller = loader.getController();
            Reservacion nueva = controller.getReserva();

            if (nueva != null) {
                int idGenerado = ReservacionDAO.insertar(nueva);
                
                if (idGenerado == -1) {
                    mostrarAlerta(Alert.AlertType.WARNING,"Error", "No se pudo guardar en la base de datos.");
                    return;
                }
                nueva.setId(idGenerado);
                mesasDAO.actualizarEstadoMesa(nueva.getIdMesa(), "Reservada");

                masterData.add(nueva);
                tablaReservas.refresh();
                mostrarAlerta(Alert.AlertType.WARNING,"Éxito", "Reserva de " + nueva.getNombreCliente() + " registrada correctamente.");
            }

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.WARNING,"Error de Sistema", "No se pudo cargar la ventana de registro: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    @FXML
    private void manejarEditar(ActionEvent event) {
        Reservacion seleccionada = tablaReservas.getSelectionModel().getSelectedItem();
        
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Por favor, selecciona una reserva de la tabla para editarla.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/RegistrarReserva.fxml"));
            Parent root = loader.load();

            // Pasar los datos antes de abrir la ventana
            RegistrarReservaController controller = loader.getController();
            controller.cargarDatos(seleccionada);

            Stage stage = new Stage();
            stage.setTitle("Editar Reserva - Saveurs Paris");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recuperar la reserva editada
            Reservacion editada = controller.getReserva();

            if (editada != null) {
                // Actualizar los datos en la tabla 
                int idMesaAntigua = seleccionada.getIdMesa();
                seleccionada.setNombreCliente(editada.getNombreCliente());
                seleccionada.setTelefono(editada.getTelefono());
                seleccionada.setFecha(editada.getFecha());
                seleccionada.setHora(editada.getHora());
                seleccionada.setNumeroPersonas(editada.getNumeroPersonas());
                seleccionada.setIdMesa(editada.getIdMesa());

                if (idMesaAntigua != editada.getIdMesa()) {
                    mesasDAO.actualizarEstadoMesa(idMesaAntigua, "Libre");
                }
                mesasDAO.actualizarEstadoMesa(editada.getIdMesa(), "Reservada");
                
                ReservacionDAO.actualizar(seleccionada);
                
                tablaReservas.refresh();
                mostrarAlerta(Alert.AlertType.WARNING,"Éxito", "Datos actualizados correctamente.");
            }
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana de edición.");
            e.printStackTrace();
        }
    }

    @FXML
    private void manejarCancelar(ActionEvent event) {
        Reservacion seleccionada = tablaReservas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Por favor, selecciona una reserva para cancelar.");
            return;
        }

        String estadoActual = seleccionada.getEstado();
        if (estadoActual.equalsIgnoreCase("Cancelada") || estadoActual.equalsIgnoreCase("Completada")) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acción no permitida", 
                "No puedes cancelar esta reserva porque actualmente se encuentra: " + estadoActual + ".");
            return; // Cortamos la ejecución aquí para que no abra la ventana de confirmación
        }

        // 2. Configurar la alerta de confirmación
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Cancelación");
        confirm.setHeaderText("¿Deseas cancelar esta reservación?");
        confirm.setContentText("La reserva de " + seleccionada.getNombreCliente() + " cambiará su estado a 'Cancelada'.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (ReservacionDAO.cancelarReserva(seleccionada.getId())) {
                
                mesasDAO.actualizarEstadoMesa(seleccionada.getIdMesa(), "Libre");

                cargarTabla(); 
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "La reserva ha sido cancelada correctamente y la mesa está libre.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el estado de la reserva en la base de datos.");
            }
        }
    }

    @FXML
    private void manejarLlegada(ActionEvent event) {
        Reservacion seleccionada = tablaReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Selecciona una reserva para marcar su llegada.");
            return;
        }

        if (seleccionada.getEstado().equalsIgnoreCase("Cancelada")) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acción no permitida", "No puedes marcar llegada en una reserva cancelada.");
            return;
        }

        seleccionada.setEstado("Completada");
        
        if (ReservacionDAO.actualizar(seleccionada)) {
            mesasDAO.actualizarEstadoMesa(seleccionada.getIdMesa(), "Ocupada");

            cargarTabla();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Cliente en restaurante", "El cliente " + seleccionada.getNombreCliente() + " ha sido marcado como Llegó. Su mesa ahora aparece como Ocupada.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el estado en la base de datos.");
        }
    }
    
    private void cambiarPantalla(ActionEvent event, String fxmlPath, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            javafx.scene.Node nodoOrigen = (javafx.scene.Node) event.getSource();
            Stage stageActual = (Stage) nodoOrigen.getScene().getWindow();
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
    private void handleReservas(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionReservas.fxml", "Gestionar Reservas");
    }

    @FXML
    private void handleMesas(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/DisponibilidadRecepcionista.fxml", "Disponibilidad Mesas");
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

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}