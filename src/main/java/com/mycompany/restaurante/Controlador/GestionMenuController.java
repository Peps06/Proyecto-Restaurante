package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.ProductoDAO;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import com.mycompany.restaurante.Modelo.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

/**
 * Esta clase es la encargada de administrar el Menú de "Saveurs Paris".
 * Aquí es donde controlo la lista de platillos y bebidas, permitiendo que el 
 * administrador pueda agregar cosas nuevas, editarlas o quitarlas si ya no se ofrecen.
 * @author Rubi y Citlaly
 * @version 2.0
 */
public class GestionMenuController {

    //  Elementos de la interfaz
    @FXML private TableView<Producto> tablaMenu; // La tabla donde se muestra todo el menú
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colTipo;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, String> colDescripcion;
    @FXML private TextField txtBusqueda; // Campo de texto para filtrar platillos
    
    // Botones de navegación lateral
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnEmpleados;
    @FXML private Button btnMenu;
    @FXML private Button btnAlmacen;
    @FXML private Button btnReportes;
    @FXML private Button btnHistorial;

    /**
     * Esta lista guarda todos los productos que traemos desde la base de datos.
     */
    private ObservableList<Producto> datosMenu = FXCollections.observableArrayList();

    /**
     * Este método prepara la pantalla apenas se abre. 
     * Configura las columnas para que sepan qué dato mostrar y tiene un truco especial 
     * para que las descripciones largas no se corten y se vean en varios renglones.
     */
    @FXML
    public void initialize() {
        // 1. Vinculamos las columnas con los atributos del objeto Producto
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        // 2. TRUCO VISUAL: Hacemos que la descripción se ajuste al ancho de la columna
        // para que si el texto es largo, se salte de renglón automáticamente.
        colDescripcion.setCellFactory(tc -> new TableCell<>() {
            private final Text text = new Text();
            {
                text.wrappingWidthProperty().bind(colDescripcion.widthProperty());
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });
        
        // Esto permite que las filas tengan una altura flexible según el texto
        tablaMenu.setFixedCellSize(-1);
        
        // 3. Cargamos los datos reales desde la base de datos
        refrescarTabla();
        
        // Mensaje por si no hay nada que mostrar todavía
        tablaMenu.setPlaceholder(new Label("No hay platillos en el menú de Saveurs Paris"));
    }

    /**
     * Limpia la tabla y vuelve a traer la lista de productos actualizada desde el DAO.
     */
    private void refrescarTabla() {
        datosMenu = ProductoDAO.obtenerTodos();
        tablaMenu.setItems(datosMenu);
    }

    /**
     * Permite buscar platillos por nombre o por tipo (ej. "Postre" o "Bebida") 
     * mientras el usuario escribe.
     */
    @FXML
    private void handleBuscar() {
        String textoBusqueda = txtBusqueda.getText().toLowerCase().trim();

        if (textoBusqueda.isEmpty()) {
            tablaMenu.setItems(datosMenu);
            return;
        }

        // Filtramos la lista maestra y mostramos solo lo que coincide
        FilteredList<Producto> filtrados = datosMenu.filtered(p -> 
            p.getNombre().toLowerCase().contains(textoBusqueda) || 
            p.getTipo().toLowerCase().contains(textoBusqueda)
        );
        
        tablaMenu.setItems(filtrados);
    }

    /**
     * Borra el filtro de búsqueda y muestra el menú completo de nuevo.
     */
    @FXML
    private void handleMostrarTabla() {
        txtBusqueda.clear();
        refrescarTabla();
    }

    /**
     * Abre una ventana emergente con un formulario vacío para registrar un nuevo platillo.
     * La ventana es "modal", lo que significa que no puedes usar la pantalla de atrás 
     * hasta que termines de registrar o canceles.
     */
    @FXML
    private void manejarAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FormularioProducto.fxml"));
            Parent root = loader.load();

            FormularioProductoController controlador = loader.getController();
            controlador.setProducto(null); // Le avisamos al formulario que es uno nuevo

            Stage stage = new Stage();
            stage.setTitle("Saveurs Paris - Nuevo Producto");
            
            // Bloqueamos la ventana de atrás por seguridad
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node)event.getSource()).getScene().getWindow());
            
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Si se guardó correctamente, actualizamos la tabla para ver el nuevo platillo
            if (controlador.isGuardadoExitoso()) {
                refrescarTabla(); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Toma el platillo seleccionado de la tabla y abre el formulario lleno con su 
     * información para poder editarla (cambiar precio, descripción, etc.).
     */
    @FXML
    private void manejarEditar(ActionEvent event) {
        Producto seleccionado = tablaMenu.getSelectionModel().getSelectedItem();
        
        if (seleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FormularioProducto.fxml"));
                Parent root = loader.load();
                
                FormularioProductoController controlador = loader.getController();
                controlador.setProducto(seleccionado); // Le pasamos los datos del elegido

                Stage stage = new Stage();
                stage.setTitle("Saveurs Paris - Editar Producto");
                
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(((Node)event.getSource()).getScene().getWindow());
                
                stage.setScene(new Scene(root));
                stage.showAndWait();

                // Si hubo cambios, refrescamos la vista
                if (controlador.isGuardadoExitoso()) {
                    tablaMenu.refresh();
                    refrescarTabla();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Atención", "Por favor, selecciona un producto de la tabla para editarlo.");
        }
    }

    /**
     * Elimina permanentemente un platillo del menú. 
     * Siempre pide una confirmación para evitar accidentes.
     */
    @FXML
    private void manejarEliminar() {
        Producto seleccionado = tablaMenu.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText("¿Estás seguro de eliminar este " + seleccionado.getTipo() + "?");
            confirmacion.setContentText("Se eliminará: " + seleccionado.getNombre());

            confirmacion.showAndWait().ifPresent(respuesta -> {
                if (respuesta == ButtonType.OK) {
                    ProductoDAO.eliminar(seleccionado.getCantidadPedida()); 
                    refrescarTabla();

                    Alert exito = new Alert(Alert.AlertType.INFORMATION);
                    exito.setTitle("Producto eliminado");
                    exito.setContentText("El producto se ha eliminado correctamente.");
                    exito.showAndWait();
                }
            });
        } else {
            mostrarAlerta("Atención", "Por favor, selecciona un producto.");
        }
    }
    
    /**
     * Método de apoyo para mostrar mensajes de advertencia al usuario.
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Método general para movernos entre las diferentes secciones del sistema.
     */
    private void cambiarPantalla(ActionEvent event, String fxmlPath, String titulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Node nodoOrigen = (Node) event.getSource();
            Stage stageActual = (Stage) nodoOrigen.getScene().getWindow();

            Scene nuevaEscena = new Scene(root);
            stageActual.setScene(nuevaEscena);
            stageActual.setTitle(titulo + " - Saveurs Paris");
            stageActual.centerOnScreen();
            stageActual.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la pantalla: " + fxmlPath);
            e.printStackTrace();
        }
    }
    
    // Atajos para los botones del menú lateral
    @FXML private void handleEmpleados(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionarEmpleado.fxml", "Gestionar Empleado"); }
    @FXML private void handleMenu(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionMenu.fxml", "Gestionar Menu"); }
    @FXML private void handleAlmacen(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionAlmacen.fxml", "Gestionar Almacen"); }
    @FXML private void handleReportes(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/Reportes.fxml", "Reportes"); }
    @FXML private void handleHistorial(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/Historial.fxml", "Historial de Pedidos"); }
    @FXML private void handleCerrarSesion(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/LoginPantalla.fxml", "Iniciar sesión"); }
}