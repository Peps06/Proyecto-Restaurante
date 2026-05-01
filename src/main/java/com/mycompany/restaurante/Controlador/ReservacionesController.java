package com.mycompany.restaurante.Controlador;

/**
 *
 * @author Dana
 */

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

public class ReservacionesController {

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
    @FXML private TableColumn<Reservacion, String> colEstado;

    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnEstado; 

    private ObservableList<Reservacion> masterData = FXCollections.observableArrayList();
    private FilteredList<Reservacion> filteredData;

    @FXML
    public void initialize() {
        // 1. Configurar las columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // 2. Cargar los datos desde la Base de Datos usando el método estático
        cargarTabla();
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
        cargarTabla(); // Refresca por si hubo cambios en la BD
    }

    @FXML
    private void manejarAgregar(ActionEvent event) {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Próximamente", "Ventana para registrar una nueva reserva.");
    }

    @FXML
    private void manejarEditar(ActionEvent event) {
        Reservacion seleccionada = tablaReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Por favor, selecciona una reserva de la tabla para editarla.");
            return;
        }
        
        mostrarAlerta(Alert.AlertType.INFORMATION, "Próximamente", "Aquí abriremos la ventana para editar la reserva de: " + seleccionada.getNombreCliente());
    }

    @FXML
    private void manejarEliminar(ActionEvent event) {
        Reservacion seleccionada = tablaReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Por favor, selecciona una reserva para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Cancelación");
        confirm.setContentText("¿Estás seguro de eliminar la reserva de " + seleccionada.getNombreCliente() + "?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (ReservacionDAO.eliminar(seleccionada.getId())) {
                cargarTabla();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Reserva eliminada correctamente.");
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

        seleccionada.setEstado("Completada");
        
        if (ReservacionDAO.actualizar(seleccionada)) {
            cargarTabla();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Cliente en restaurante", "El cliente " + seleccionada.getNombreCliente() + " ha sido marcado como Llegó. Ya puedes asignarle su mesa en el sistema de órdenes.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el estado en la base de datos.");
        }
    }
    
    @FXML
    private void handleReservas(ActionEvent event) {
        cargarTabla();
    }

    @FXML
    private void handleMesas(ActionEvent event) {
        System.out.println("Navegando a Gestión de Mesas...");
        // cargarPantalla("/com/mycompany/restaurante/fxml/PantallaMesas.fxml");
    }

    @FXML
    private void handleListaEsp(ActionEvent event) {
        System.out.println("Navegando a Lista de Espera...");
        // cargarPantalla("/com/mycompany/restaurante/fxml/ListaEspera.fxml");
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));
            Stage stageActual = (Stage) btnCerrarSesion.getScene().getWindow();
            Scene nuevaEscena = new Scene(root);
            stageActual.setScene(nuevaEscena);
            stageActual.setTitle("Iniciar sesión - Saveurs Paris");
            stageActual.centerOnScreen();
            stageActual.show();
        } catch (IOException e) {
            e.printStackTrace();
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