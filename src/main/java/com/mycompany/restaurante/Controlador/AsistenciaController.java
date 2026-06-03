package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.AsistenciaDAO;
import com.mycompany.restaurante.Modelo.Asistencia;
import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Empleado;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime; 
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Esta clase es el controlador para la pantalla de Asistencia de "Saveurs Paris".
 * Su función principal es registrar las entradas y salidas diarias del personal 
 * y permitirles consultar su historial de asistencia mes por mes.
 * @author Rubi
 * @version 2.6
 */
public class AsistenciaController implements Initializable {

    //  Información dinámica del empleado (Se eliminaron los labels viejos)
    @FXML private Label lblNombreDinamico; // Muestra el nombre del empleado seleccionado
    @FXML private Label lblTurnoDetectado; // Muestra el turno en tiempo real antes de picar entrada

    // Tabla de Historial
    @FXML private TableView<Asistencia> tablaAsistencias;
    @FXML private TableColumn<Asistencia, String> colFecha;
    @FXML private TableColumn<Asistencia, String> colEntrada;
    @FXML private TableColumn<Asistencia, String> colSalida;
    @FXML private TableColumn<Asistencia, String> colEstado;
    @FXML private TableColumn<Asistencia, String> colTurno;

    //  Controles de la interfaz 
    @FXML private ComboBox<String> cbMes;        // Menú para filtrar el historial por mes
    @FXML private Button btnRegistrarEntrada;    // Botón para marcar el inicio de turno
    @FXML private Button btnRegistrarSalida;     // Botón para marcar el fin de turno
    @FXML private Button btnCancelar;            // Botón para salir sin cambios
    @FXML private Button btnGuardar;             // Botón para guardar

    /**
     * Objeto para interactuar con la base de datos en lo relacionado a faltas y asistencias.
     */
    private AsistenciaDAO asistenciaDAO; 
    
    /**
     * El empleado al que le estamos consultando o registrando la asistencia.
     */
    private Empleado empleadoSeleccionado;

    /**
     * Prepara la pantalla al cargar.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Inicializamos de forma correcta el DAO pasándole tu ConexionDB
        try {
            this.asistenciaDAO = new AsistenciaDAO(ConexionDB.getConexion());
        } catch (SQLException e) {
            System.err.println("Error al enlazar la conexión en AsistenciaController:");
            e.printStackTrace();
            mostrarAlerta("Error de Conexión", "No se pudo establecer el enlace con MySQL.");
        }

        // 2. Vinculamos las columnas de la tabla con los datos del objeto Asistencia
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEntrada.setCellValueFactory(new PropertyValueFactory<>("horaEntrada"));
        colSalida.setCellValueFactory(new PropertyValueFactory<>("horaSalida"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colTurno.setCellValueFactory(new PropertyValueFactory<>("turno"));

        // Aplicamos el formato de colores (verde para presente, gris para completado)
        configurarColoresEstado();

        // 3. Llenamos el ComboBox con los nombres de los meses
        cbMes.getItems().addAll(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        
        // Seleccionamos el mes actual para que el usuario no tenga que buscarlo
        int mesActual = LocalDate.now().getMonthValue() - 1;
        cbMes.getSelectionModel().select(mesActual);

        // 4. Calcular y mostrar el turno actual del sistema de forma informativa
        mostrarTurnoEnPantalla();
    }

    /**
     * Evalúa la hora del sistema para avisar visualmente qué turno capturará la base de datos
     */
    private void mostrarTurnoEnPantalla() {
        if (lblTurnoDetectado != null) {
            LocalTime horaAhora = LocalTime.now();
            if (horaAhora.isBefore(LocalTime.of(14, 0, 0))) {
                lblTurnoDetectado.setText("Matutino (06-14h)");
                lblTurnoDetectado.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");
            } else {
                lblTurnoDetectado.setText("Vespertino (15-22h)");
                lblTurnoDetectado.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");
            }
        }
    }

    /**
     * Le da un toque visual a la tabla
     */
    private void configurarColoresEstado() {
        colEstado.setCellFactory(column -> {
            return new TableCell<Asistencia, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.equalsIgnoreCase("Presente")) {
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); 
                        } else if (item.equalsIgnoreCase("Completado")) {
                            setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: normal;"); 
                        } else {
                            setStyle("-fx-text-fill: #e74c3c;"); 
                        }
                    }
                }
            };
        });
    }

    /**
     * Recibe la información del empleado desde la pantalla de Gestión
     */
    public void initData(Empleado emp) {
        this.empleadoSeleccionado = emp;
        lblNombreDinamico.setText(emp.getNombre());
        actualizarInterfaz();
    }

    /**
     * Consulta el historial en la base de datos y refresca los elementos gráficos
     */
    private void actualizarInterfaz() {
        if (empleadoSeleccionado == null || asistenciaDAO == null) return;

        int mes = cbMes.getSelectionModel().getSelectedIndex() + 1;
        ObservableList<Asistencia> lista = asistenciaDAO.obtenerHistorialPorMes(empleadoSeleccionado.getId(), mes);
        tablaAsistencias.setItems(lista);
        tablaAsistencias.refresh();

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
            default:
                btnRegistrarEntrada.setDisable(false);
                btnRegistrarSalida.setDisable(true);
                break;
        }
    }

    /**
     * Guarda la hora actual como la entrada del empleado en la base de datos.
     */
    @FXML
    private void handleRegistrarEntrada(ActionEvent event) {
        if (empleadoSeleccionado == null) return;
        
        if (asistenciaDAO.registrarEntrada(empleadoSeleccionado.getId())) {
            actualizarInterfaz();
            mostrarAlerta("Éxito", "Entrada registrada correctamente para la fecha de hoy.");
        } else {
            mostrarAlerta("Error", "No se pudo procesar el registro de entrada.");
        }
    }

    /**
     * Busca la asistencia activa del empleado hoy y le pone hora de salida.
     */
    @FXML
    private void handleRegistrarSalida(ActionEvent event) {
        if (empleadoSeleccionado == null) return;

        if (asistenciaDAO.registrarSalida(empleadoSeleccionado.getId())) {
            actualizarInterfaz();
            mostrarAlerta("Éxito", "Salida registrada. ¡Hasta mañana!");
        } else {
            mostrarAlerta("Error", "No se pudo registrar la salida. Verifica la conexión.");
        }
    }

    /**
     * Actualiza la tabla cuando el usuario elige un mes diferente en el ComboBox.
     */
    @FXML
    private void filtrarPorMes(ActionEvent event) {
        actualizarInterfaz();
    }

    /**
     * Cierra la ventana sin aplicar cambios adicionales.
     */
    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarVentana();
    }

    /**
     * Finaliza la sesión de asistencia y guarda los cambios generales.
     */
    @FXML
    private void handleGuardar(ActionEvent event) {
        mostrarAlerta("Información", "Los cambios han sido guardados en el sistema.");
        cerrarVentana();
    }

    /**
     * Método para cerrar el Stage (ventana) actual.
     */
    private void cerrarVentana() {
        Stage stage = (Stage) lblNombreDinamico.getScene().getWindow();
        stage.close();
    }

    /**
     * Muestra ventanitas de aviso rápidas para informar al usuario.
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}