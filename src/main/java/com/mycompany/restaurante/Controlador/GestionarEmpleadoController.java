package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.Controlador.RegistrarEmpleadoController;
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
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color; 

/**
 * Controlador para la gestión de empleados en el sistema Saveurs Paris.
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
    
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnEmpleados;
    @FXML private Button btnMenu;
    @FXML private Button btnAlmacen;
    @FXML private Button btnReportes;
    @FXML private Button btnHistorial;

    private ObservableList<Empleado> datosMaestros = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        refrescarTabla();
    
        // 1. Vincular columnas con los atributos de la clase Empleado
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPuesto.setCellValueFactory(new PropertyValueFactory<>("puesto"));
        colAsistencia.setCellValueFactory(new PropertyValueFactory<>("asistencia"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        
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
                        
                        if (item.equalsIgnoreCase("Presente")) {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        }
                    }
                }
            };
        });
    }

    private void refrescarTabla() {
    // Volvemos a pedir los datos a la base de datos
    datosMaestros = EmpleadoDAO.obtenerTodos();
    
    // Los ponemos en la tabla para que se actualice la vista
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

    @FXML
    private void manejarBusqueda() {
        String textoBusqueda = txtBusqueda.getText().toLowerCase().trim();

        // Filtra por ID, Nombre o Puesto
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
    
    @FXML
    private void handleMostrarTabla(){
        tablaEmpleados.setItems(datosMaestros);
        txtBusqueda.clear();
    }

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
            
            // CAMBIO AQUÍ: showAndWait detiene la ejecución del código principal
            // hasta que cierras la ventana de asistencia.
            stage.showAndWait();

            // Cuando la ventana se cierra, ejecutamos tu método para volver a cargar la tabla
            refrescarTabla();// O el nombre del método que uses para llenar tu tabla principal

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    else {
        mostrarAlerta("Atención", "Por favor, selecciona un empleado.", Alert.AlertType.WARNING);
    }
}
      

    @FXML
    private void manejarAgregar() {
        try {
            // el FXML de la nueva ventana de registro

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/RegistrarEmpleado.fxml"));
            Parent root = loader.load();

            // Escenario para la nueva ventana
            Stage stage = new Stage();
            stage.setTitle("Saveurs Paris - Registro de Personal");

            // No se puede interactuar con la tabla hasta cerrar el registro
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.setScene(new Scene(root));

            // Se muestra la ventana y se espera a que el usuario termine
            stage.showAndWait();

            RegistrarEmpleadoController controller = loader.getController();
            Empleado nuevo = controller.getNuevoEmpleado();

            // Si el empleado  no dio clic en Cancelar
            if (nuevo != null) {
                int idGenerado = EmpleadoDAO.insertar(nuevo);
                
                if (idGenerado == -1) {
                    mostrarAlerta("Error", "No se pudo guardar en la base de datos.", Alert.AlertType.ERROR);
                    return;
                }
                nuevo.setId(idGenerado);

                // Se agrega a la lista "MAestra"
                datosMaestros.add(nuevo);

                // Refrescamos la tabla para que aparezca de inmediato
                tablaEmpleados.refresh();

                mostrarAlerta("Éxito", "Empleado " + nuevo.getNombre() + " registrado correctamente.", Alert.AlertType.INFORMATION);
            }

        } catch (IOException e) {
            mostrarAlerta("Error de Sistema", "No se pudo cargar la ventana de registro: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace(); // Esto te ayuda a ver el error en la consola de NetBeans
        }
    }

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

            // 1. Obtener el controlador de la ventana de registro
            RegistrarEmpleadoController controller = loader.getController();

            // 2. Pasar los datos del empleado seleccionado al formulario
            controller.cargarDatos(seleccionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Empleado - Saveurs Paris");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // 3. Al cerrar la ventana, se verifica si hubo cambios
            Empleado editado = controller.getNuevoEmpleado();

            if (editado != null) {
                // Se actualizan los datos del objeto seleccionado originalmente
                seleccionado.setNombre(editado.getNombre());
                seleccionado.setPuesto(editado.getPuesto());
                seleccionado.setTelefono(editado.getTelefono());

                // Se refreca la tabla para ver los cambios
                tablaEmpleados.refresh();
                mostrarAlerta("Éxito", "Datos actualizados correctamente.", Alert.AlertType.INFORMATION);
                EmpleadoDAO.actualizar(seleccionado);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir la ventana de edición.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
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
    
    @FXML
    private void handleEmpleados(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionarEmpleado.fxml", "Gestionar Empleado");
    }

    @FXML
    private void handleMenu(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionMenu.fxml", "Gestionar Menu");
    }

    @FXML
    private void handleAlmacen(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionAlmacen.fxml", "Gestionar Almacen");
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        cambiarPantalla(event, "/com/mycompany/restaurante/fxml/LoginPantalla.fxml", "Iniciar sesión");
    }
}