package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.EmpleadoDAO;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

/**
 * Esta clase es el "cerebro" de la pantalla de Gestión de Empleados. 
 * Aquí controlo todo lo que pasa en la tabla: desde ver quién vino a trabajar hoy, 
 * hasta agregar, editar o despedir (eliminar) a alguien del equipo.
 * * @author Rubi
 * @version 2.0
 */
public class GestionarEmpleadoController {

    // Componentes de la interfaz (FXML)
    @FXML private TextField txtBusqueda; // Cuadro para buscar empleados por nombre o ID
    @FXML private TableView<Empleado> tablaEmpleados; // La tabla principal donde se ve la lista
    
    // Columnas de la tabla
    @FXML private TableColumn<Empleado, Integer> colId;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colPuesto;
    @FXML private TableColumn<Empleado, String> colAsistencia;
    @FXML private TableColumn<Empleado, String> colTelefono;
    
    // Botones del menú lateral
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnEmpleados;
    @FXML private Button btnMenu;
    @FXML private Button btnAlmacen;
    @FXML private Button btnReportes;
    @FXML private Button btnHistorial;

    /**
     * Lista maestra que guarda a todos los empleados que traemos de la base de datos.
     */
    private ObservableList<Empleado> datosMaestros = FXCollections.observableArrayList();

    /**
     * Este método se ejecuta solito cuando se abre la pantalla.
     * Aquí configuro qué dato va en qué columna
     */
    @FXML
    public void initialize() {
        refrescarTabla();
    
        // 1. Vinculamos cada columna con los datos que tiene el objeto Empleado
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPuesto.setCellValueFactory(new PropertyValueFactory<>("puesto"));
        colAsistencia.setCellValueFactory(new PropertyValueFactory<>("asistencia"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        // 2. Colores para la asistencia
        colAsistencia.setCellFactory(column -> {
            return new TableCell<Empleado, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle(""); // Limpiamos estilos para que no se hereden al hacer scroll

                        // Verde si está presente, Gris si ya terminó, Rojo si no vino
                        if (item.equalsIgnoreCase("Presente")) {
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); 
                        } 
                        else if (item.equalsIgnoreCase("Completado")) {
                            setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: normal;"); 
                        } 
                        else if (item.equalsIgnoreCase("Ausente")) {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); 
                        }
                    }
                }
            };
        });
    }

    /**
     * Este método limpia la tabla y vuelve a pedirle toda la información 
     * actualizada a la base de datos. Útil después de agregar o editar.
     */
    private void refrescarTabla() {
        datosMaestros.clear();
        datosMaestros = EmpleadoDAO.obtenerTodos();
        tablaEmpleados.setItems(datosMaestros);
    }

    /**
     * Borra al empleado seleccionado de la tabla y de la base de datos.
     * Siempre pide una confirmación antes para no borrar a nadie por error.
     */
    @FXML
    private void manejarEliminar() {
        Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Atención", "Por favor, selecciona un empleado de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar acción");
        confirmacion.setHeaderText("Eliminar empleado");
        confirmacion.setContentText("¿Estás seguro de que deseas eliminar a: " + seleccionado.getNombre() + "?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            EmpleadoDAO.eliminar(seleccionado.getId());
            datosMaestros.remove(seleccionado);
            tablaEmpleados.setItems(datosMaestros);
            mostrarAlerta("Éxito", "Empleado eliminado correctamente.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Filtra la tabla según lo que el usuario escriba en el cuadro de búsqueda.
     * Se puede buscar por nombre, puesto o ID.
     */
    @FXML
    private void manejarBusqueda() {
        String textoBusqueda = txtBusqueda.getText().toLowerCase().trim();

        ObservableList<Empleado> filtrados = datosMaestros.filtered(empleado -> 
            String.valueOf(empleado.getId()).contains(textoBusqueda) || 
            empleado.getNombre().toLowerCase().contains(textoBusqueda) ||
            empleado.getPuesto().toLowerCase().contains(textoBusqueda)
        );

        if (filtrados.isEmpty()) {
            mostrarAlerta("Búsqueda", "No se encontró ningún empleado.", Alert.AlertType.INFORMATION);
            tablaEmpleados.setItems(datosMaestros);
        } else {
            tablaEmpleados.setItems(filtrados);
        }
        txtBusqueda.clear();
    }
    
    /**
     * Limpia la búsqueda actual y vuelve a mostrar a todos los empleados.
     */
    @FXML
    private void handleMostrarTabla(){
        tablaEmpleados.setItems(datosMaestros);
        txtBusqueda.clear();
    }

    /**
     * Abre la ventana para registrar la entrada o salida del empleado seleccionado.
     * Bloquea la ventana principal hasta que se cierre el registro de asistencia.
     */
    @FXML
    private void manejarAsistencia(ActionEvent event) {
        Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/RegistroAsistencia.fxml"));
                Parent root = loader.load();

                AsistenciaController controller = loader.getController();
                controller.initData(seleccionado);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Asistencia - " + seleccionado.getNombre());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                refrescarTabla(); // Recargamos datos para ver el cambio de estado (Presente/Completado)

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Atención", "Por favor, selecciona un empleado.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Abre un formulario vacío para registrar a un nuevo trabajador.
     * Si se guarda con éxito, lo añade a la tabla de inmediato.
     */
    @FXML
    private void manejarAgregar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/RegistrarEmpleado.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Saveurs Paris - Registro de Personal");
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.setScene(new Scene(root));
            stage.showAndWait();

            RegistrarEmpleadoController controller = loader.getController();
            Empleado nuevo = controller.getNuevoEmpleado();

            if (nuevo != null) {
                int idGenerado = EmpleadoDAO.insertar(nuevo);
                if (idGenerado != -1) {
                    nuevo.setId(idGenerado);
                    datosMaestros.add(nuevo);
                    tablaEmpleados.refresh();
                    mostrarAlerta("Éxito", "Empleado " + nuevo.getNombre() + " registrado correctamente.", Alert.AlertType.INFORMATION);
                }
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar la ventana de registro.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Toma al empleado seleccionado y abre el formulario lleno con su información 
     * para que podamos cambiar sus datos (nombre, puesto, teléfono).
     */
    @FXML
    private void manejarEditar() {
        Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un empleado de la tabla para editar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/RegistrarEmpleado.fxml"));
            Parent root = loader.load();

            RegistrarEmpleadoController controller = loader.getController();
            controller.cargarDatos(seleccionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Empleado - Saveurs Paris");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            Empleado editado = controller.getNuevoEmpleado();

            if (editado != null) {
                seleccionado.setNombre(editado.getNombre());
                seleccionado.setPuesto(editado.getPuesto());
                seleccionado.setTelefono(editado.getTelefono());

                tablaEmpleados.refresh();
                EmpleadoDAO.actualizar(seleccionado);
                mostrarAlerta("Éxito", "Datos actualizados correctamente.", Alert.AlertType.INFORMATION);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir la ventana de edición.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Método auxiliar para mostrar ventanitas de aviso (Éxito, Error o Advertencia).
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    /**
     * Este método nos ayuda a movernos entre las diferentes pantallas del sistema 
     * (Menú, Almacén, etc.) cerrando la actual y abriendo la nueva.
     */
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
    
    //Handlers para la navegación del menú lateral
    @FXML private void handleEmpleados(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionarEmpleado.fxml", "Gestionar Empleado"); }
    @FXML private void handleMenu(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionMenu.fxml", "Gestionar Menu"); }
    @FXML private void handleAlmacen(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionAlmacen.fxml", "Gestionar Almacen"); }
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        // 1. Creamos la alerta de confirmación para los empleados
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema de empleados?");

        // 2. Mostramos la ventana y esperamos a que el usuario elija
        Optional<ButtonType> resultado = alerta.showAndWait();

        // 3. Si presiona OK, lo mandamos directo al Login usando tu método del controlador
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            cambiarPantalla(event, "/com/mycompany/restaurante/fxml/LoginPantalla.fxml", "Iniciar sesión");
        }
        // Si cancela, no entra al IF, la alerta se destruye sola y el usuario se queda en la gestión de empleados
    }
}
