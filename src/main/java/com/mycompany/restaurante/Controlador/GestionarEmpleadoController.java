package com.mycompany.restaurante.Controlador;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import com.mycompany.restaurante.Modelo.Empleado;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Optional;

/**
 * @author Rubi
 */
public class GestionarEmpleadoController {

    @FXML private TextField txtBusqueda;
    @FXML private TableView<Empleado> tablaEmpleados;
    
    @FXML private TableColumn<Empleado, Integer> colId;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colPuesto;
    @FXML private TableColumn<Empleado, String> colAsistencia;
    @FXML private TableColumn<Empleado, String> colTelefono;

    // Guarda una copia de los datos para que la búsqueda pueda resetearse
    private ObservableList<Empleado> datosMaestros = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Vincular columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPuesto.setCellValueFactory(new PropertyValueFactory<>("puesto"));
        colAsistencia.setCellValueFactory(new PropertyValueFactory<>("asistencia"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        // 2. Cargar datos ficticios
        crearDatosFicticios();
    }

    private void crearDatosFicticios() {
        // Crea los personajes para el restaurante
        datosMaestros.addAll(
            new Empleado(101, "Rubi Mendoza", "Recepcionista", "Presente", "228-111-2233"),
            new Empleado(102, "Citlaly Morales", "Chef", "Presente", "228-444-5566"),
            new Empleado(103, "Dana Carmona", "Chef", "Ausente", "228-777-8899"),
            new Empleado(104, "Marco Aurelio", "Recepcionista", "Presente", "228-222-3344"),
            new Empleado(105, "Sofía Ramírez", "Mesero", "Presente", "228-555-6677")
        );

        // Muestra la información en la tabla
        tablaEmpleados.setItems(datosMaestros);
    }

    @FXML
    private void manejarEliminar() {
        Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Atención", "Por favor, selecciona un empleado de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar acción");
        confirmacion.setHeaderText("Eliminar empleado de la gestión");
        confirmacion.setContentText("¿Estás seguro de que deseas eliminar a: " + seleccionado.getNombre() + "?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Eliminamos de la lista principal y de la tabla visual
            datosMaestros.remove(seleccionado);
            tablaEmpleados.setItems(datosMaestros);

            mostrarAlerta("Éxito", "Empleado eliminado correctamente.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void manejarBusqueda() {
        String textoBusqueda = txtBusqueda.getText().toLowerCase().trim();

        if (textoBusqueda.isEmpty()) {
            // Si el buscador está vacío, restauramos la lista completa
            tablaEmpleados.setItems(datosMaestros);
            return;
        }

        // Filtramos sobre la lista de datos maestros
        ObservableList<Empleado> filtrados = datosMaestros.filtered(empleado -> 
            String.valueOf(empleado.getId()).contains(textoBusqueda) || 
            empleado.getNombre().toLowerCase().contains(textoBusqueda) ||
            empleado.getPuesto().toLowerCase().contains(textoBusqueda)
        );

        if (filtrados.isEmpty()) {
            mostrarAlerta("Búsqueda", "No se encontró ningún empleado con: " + textoBusqueda, Alert.AlertType.INFORMATION);
            tablaEmpleados.setItems(datosMaestros); // Restaurar para evitar tabla vacía
        } else {
            tablaEmpleados.setItems(filtrados);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}