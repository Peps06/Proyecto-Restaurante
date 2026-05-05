package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.AsistenciaDAO;
import com.mycompany.restaurante.Modelo.Asistencia;
import com.mycompany.restaurante.Modelo.Empleado;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 *
 * @author Rubi
 */

public class AsistenciaController implements Initializable {

    // Labels dinámicos para mostrar info del empleado
    @FXML private Label lblNombreDinamico;
    @FXML private Label lblPuestoDinamico;
    @FXML private Label lblIDDinamico;
    @FXML private ImageView imgEmpleado;

    // Componentes de la tabla
    @FXML private TableView<Asistencia> tablaAsistencias;
    @FXML private TableColumn<Asistencia, String> colFecha;
    @FXML private TableColumn<Asistencia, String> colEntrada;
    @FXML private TableColumn<Asistencia, String> colSalida;
    @FXML private TableColumn<Asistencia, String> colEstado;

    // Filtro y Botones
    @FXML private ComboBox<String> cbMes;
    @FXML private Button btnRegistrarEntrada;
    @FXML private Button btnRegistrarSalida;

    private Empleado empleadoSeleccionado;
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Configurar columnas de la tabla
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEntrada.setCellValueFactory(new PropertyValueFactory<>("horaEntrada"));
        colSalida.setCellValueFactory(new PropertyValueFactory<>("horaSalida"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // 2. Llenar ComboBox de meses
        cbMes.getItems().addAll(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        
        // Seleccionar mes actual por defecto
        int mesActual = LocalDate.now().getMonthValue() - 1;
        cbMes.getSelectionModel().select(mesActual);
    }

    /**
     * Este método recibe al empleado de la pantalla principal.
     */
    public void initData(Empleado emp) {
        this.empleadoSeleccionado = emp;
        
        // Llenar labels
        lblNombreDinamico.setText(emp.getNombre());
        lblPuestoDinamico.setText(emp.getPuesto());
        lblIDDinamico.setText(String.valueOf(emp.getId()));

        // Actualizar tabla e interfaz
        actualizarInterfaz();
    }

    private void actualizarInterfaz() {
        // Cargar historial del mes seleccionado
        int mes = cbMes.getSelectionModel().getSelectedIndex() + 1;
        ObservableList<Asistencia> lista = asistenciaDAO.obtenerHistorialPorMes(empleadoSeleccionado.getId(), mes);
        tablaAsistencias.setItems(lista);

        // Validar estado de botones para hoy
        String estadoHoy = asistenciaDAO.verificarEstadoHoy(empleadoSeleccionado.getId());
        
        switch (estadoHoy) {
            case "Ausente":
                btnRegistrarEntrada.setDisable(false);
                btnRegistrarSalida.setDisable(true);
                break;
            case "Presente":
                btnRegistrarEntrada.setDisable(true);
                btnRegistrarSalida.setDisable(false);
                break;
            case "Completado":
                btnRegistrarEntrada.setDisable(true);
                btnRegistrarSalida.setDisable(true);
                break;
        }
    }

    @FXML
    private void handleRegistrarEntrada(ActionEvent event) {
        if (asistenciaDAO.registrarEntrada(empleadoSeleccionado.getId())) {
            actualizarInterfaz();
            mostrarAlerta("Éxito", "Entrada registrada correctamente.");
        }
    }

    @FXML
    private void handleRegistrarSalida(ActionEvent event) {
        if (asistenciaDAO.registrarSalida(empleadoSeleccionado.getId())) {
            actualizarInterfaz();
            mostrarAlerta("Éxito", "Salida registrada. ¡Buen trabajo!");
        }
    }

    @FXML
    private void filtrarPorMes(ActionEvent event) {
        if (empleadoSeleccionado != null) {
            actualizarInterfaz();
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        Stage stage = (Stage) lblNombreDinamico.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void handleGuardar(ActionEvent event) {
        // Mostramos un mensaje de confirmación antes de cerrar
        mostrarAlerta("Finalizado", "Registros de asistencia actualizados correctamente.");
        
        // Cerramos la ventana
        Stage stage = (Stage) lblNombreDinamico.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}