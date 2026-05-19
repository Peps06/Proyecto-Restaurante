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
 * Esta clase es el controlador para la pantalla de Asistencia de "Saveurs Paris".
 * Su función principal es registrar las entradas y salidas diarias del personal 
 * y permitirles consultar su historial de asistencia mes por mes.
 * @author Rubi
 * @version 2.0
 */
public class AsistenciaController implements Initializable {

    //  Información dinámica del empleado 
    @FXML private Label lblNombreDinamico; // Muestra el nombre del empleado seleccionado
    @FXML private Label lblPuestoDinamico; // Muestra su cargo (ej. Chef, Mesero)
    @FXML private Label lblIDDinamico;     // Muestra su ID único de nómina
    @FXML private ImageView imgEmpleado;   // Espacio para la foto del trabajador

    // Tabla de Historial
    @FXML private TableView<Asistencia> tablaAsistencias;
    @FXML private TableColumn<Asistencia, String> colFecha;
    @FXML private TableColumn<Asistencia, String> colEntrada;
    @FXML private TableColumn<Asistencia, String> colSalida;
    @FXML private TableColumn<Asistencia, String> colEstado;

    //  Controles de la interfaz 
    @FXML private ComboBox<String> cbMes;        // Menú para filtrar el historial por mes
    @FXML private Button btnRegistrarEntrada;    // Botón para marcar el inicio de turno
    @FXML private Button btnRegistrarSalida;     // Botón para marcar el fin de turno

    /**
     * Objeto para interactuar con la base de datos en lo relacionado a faltas y asistencias.
     */
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
    
    /**
     * El empleado al que le estamos consultando o registrando la asistencia.
     */
    private Empleado empleadoSeleccionado;

    /**
     * Prepara la pantalla al cargar.
     * Configura la tabla para mostrar los registros y llena el menú de meses,
     * seleccionando automáticamente el mes en el que nos encontramos hoy.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Vinculamos las columnas de la tabla con los datos del objeto Asistencia
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEntrada.setCellValueFactory(new PropertyValueFactory<>("horaEntrada"));
        colSalida.setCellValueFactory(new PropertyValueFactory<>("horaSalida"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Aplicamos el formato de colores (verde para presente, gris para completado)
        configurarColoresEstado();

        // 2. Llenamos el ComboBox con los nombres de los meses
        cbMes.getItems().addAll(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        
        // Seleccionamos el mes actual para que el usuario no tenga que buscarlo
        int mesActual = LocalDate.now().getMonthValue() - 1;
        cbMes.getSelectionModel().select(mesActual);
    }

    /**
     * Le da un toque visual a la tabla:
     * Verde fuerte: Cuando el empleado está actualmente en el restaurante (Presente).
     * Gris: Cuando ya cumplió su jornada y marcó salida (Completado).
     * Rojo: Para faltas o errores de registro (Ausente)
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
                        if (item.equals("Presente")) {
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); 
                        } else if (item.equals("Completado")) {
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
     * y llena los datos en la parte superior de la ventana.
     * @param emp El empleado que seleccionamos previamente.
     */
    public void initData(Empleado emp) {
        this.empleadoSeleccionado = emp;
        lblNombreDinamico.setText(emp.getNombre());
        lblPuestoDinamico.setText(emp.getPuesto());
        lblIDDinamico.setText(String.valueOf(emp.getId()));
        actualizarInterfaz();
    }

    /**
     * Consulta el historial en la base de datos y revisa qué ha hecho el empleado HOY
     * para saber qué botones dejarle picar.
     */
    private void actualizarInterfaz() {
        if (empleadoSeleccionado == null) return;

        // Traemos el historial del mes seleccionado
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
        }
    }

    /**
     * Guarda la hora actual como la entrada del empleado en la base de datos.
     */
    @FXML
    private void handleRegistrarEntrada(ActionEvent event) {
        if (asistenciaDAO.registrarEntrada(empleadoSeleccionado.getId())) {
            actualizarInterfaz();
            mostrarAlerta("Éxito", "Entrada registrada: " + LocalDate.now());
        }
    }

    /**
     * Busca la asistencia activa del empleado hoy y le pone hora de salida.
     */
    @FXML
    private void handleRegistrarSalida(ActionEvent event) {
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