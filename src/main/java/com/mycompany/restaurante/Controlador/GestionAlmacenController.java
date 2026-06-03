package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.DAO.InsumoDAO;
import com.mycompany.restaurante.Modelo.Insumo;
import java.io.IOException;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Esta clase controla la pantalla de Gestión del Almacén en "Saveurs Paris".
 * Su función principal es permitir que el administrador lleve un control estricto 
 * de los ingredientes e insumos disponibles, vigilando el stock para que nunca 
 * falte nada en la cocina.
 * 
 * @author Rubi
 * @version 2.0
 */
public class GestionAlmacenController {

    // Componentes de la Tabla (La lista visual del inventario) 
    @FXML private TableView<Insumo> tablaAlmacen;
    @FXML private TableColumn<Insumo, Integer> colId;
    @FXML private TableColumn<Insumo, String> colProducto;
    @FXML private TableColumn<Insumo, String> colCategoria;
    @FXML private TableColumn<Insumo, Double> colStock;
    @FXML private TableColumn<Insumo, String> colUnidad;
    @FXML private TableColumn<Insumo, String> colEstado;

    //Herramientas de búsqueda y botones de acción
    @FXML private TextField txtBusqueda; // Cuadro para buscar productos rápido
    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    //Conector con la base de datos para manejar los insumos.
     
    private final InsumoDAO insumoDAO = new InsumoDAO();
    
    // Lista donde guardamos temporalmente los insumos para mostrarlos en la tabla.
     
    private ObservableList<Insumo> listaInsumos = FXCollections.observableArrayList();

    /**
     * Este método se encarga de preparar la pantalla al abrirse.
     * Configura la tabla para recibir datos, aplica el sistema de alertas por colores 
     * y carga la lista inicial de productos desde la base de datos.
     */
    @FXML
    public void initialize() {
        // 1. Vinculamos las columnas con los datos de la clase Insumo
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // 2. Aplicamos colores al estado para detectar visualmente qué falta
        configurarColoresEstado();

        // 3. Traemos los datos reales de la base de datos
        refrescarDesdeBD();
        tablaAlmacen.setItems(listaInsumos);
        
        // Mensaje por si el almacén está totalmente vacío
        tablaAlmacen.setPlaceholder(new Label("No hay insumos en el inventario"));
    }

    /**
     * Sistema de semáforo visual:
     * Verde: Disponible.
     * Amarillo/Dorado: Por agotarse.
     * Rojo: Agotado o no disponible.
     */
    private void configurarColoresEstado() {
        colEstado.setCellFactory(column -> new TableCell<Insumo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Aplicamos colores según la palabra clave del estado
                    if (item.equalsIgnoreCase("Disponible")) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;"); 
                    } else if (item.equalsIgnoreCase("Por agotarse")) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold;"); 
                    } else if (item.equalsIgnoreCase("Agotado") || item.equalsIgnoreCase("No disponible")) {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;"); 
                    } else {
                        setStyle(""); 
                    }
                }
            }
        });
    }

    /**
     * Vuelve a consultar la base de datos para asegurar que la información 
     * que vemos en pantalla sea la más reciente.
     */
    private void refrescarDesdeBD() {
        listaInsumos.setAll(insumoDAO.listar());
    }

    /**
     * Permite buscar insumos en tiempo real por su nombre o por su ID.
     */
    @FXML
    private void handleBuscar() {
        String texto = txtBusqueda.getText().toLowerCase().trim();
        
        if (texto.isEmpty()) {
            tablaAlmacen.setItems(listaInsumos);
            return;
        }

        FilteredList<Insumo> filtrados = listaInsumos.filtered(insumo -> {
            boolean coincideNombre = insumo.getNombre().toLowerCase().contains(texto);
            boolean coincideId = false;
            try {
                int idBuscado = Integer.parseInt(texto);
                coincideId = (insumo.getId() == idBuscado);
            } catch (NumberFormatException e) {
                coincideId = false;
            }
            return coincideNombre || coincideId;
        });

        tablaAlmacen.setItems(filtrados);
    }

    /**
     * Limpia el buscador y muestra la lista completa del almacén.
     */
    @FXML
    private void handleMostrarTabla() {
        txtBusqueda.setText("");
        refrescarDesdeBD();
        tablaAlmacen.setItems(listaInsumos);
        tablaAlmacen.getSelectionModel().clearSelection();
    }

    /**
     * Abre el formulario para registrar un nuevo insumo. 
     * La ventana es modal, bloqueando la pantalla principal para evitar confusiones.
     */
    @FXML
    private void manejarAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FormularioInsumo.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Saveurs Paris - Nuevo Insumo");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node)event.getSource()).getScene().getWindow());
            
            stage.setScene(new Scene(root));
            stage.showAndWait(); 

            refrescarDesdeBD(); // Actualizamos la tabla al cerrar la ventana

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar el formulario.");
            e.printStackTrace();
        }
    }

    /**
     * Abre el formulario de edición cargando los datos del insumo seleccionado 
     * para modificar su stock, categoría o nombre.
     */
    @FXML
    private void manejarEditar(ActionEvent event) {
        Insumo seleccionado = tablaAlmacen.getSelectionModel().getSelectedItem();
        
        if (seleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FormularioInsumo.fxml"));
                Parent root = loader.load();
                
                FormularioInsumoController controlador = loader.getController();
                controlador.setInsumo(seleccionado); 
                
                Stage stage = new Stage();
                stage.setTitle("Saveurs Paris - Editar Insumo");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(((Node)event.getSource()).getScene().getWindow());
                
                stage.setScene(new Scene(root));
                stage.showAndWait();
                
                if (controlador.isGuardadoExitoso()) {
                    refrescarDesdeBD();
                }
            } catch (IOException e) {
                mostrarAlerta("Error", "Error al cargar el formulario de edición.");
            }
        } else {
            mostrarAlerta("Atención", "Por favor, selecciona un insumo de la tabla para editar.");
        }
    }

    /**
     * Borra un insumo del sistema tras pedir una confirmación al usuario.
     */
    @FXML
    private void manejarEliminar() {
        Insumo seleccionado = tablaAlmacen.getSelectionModel().getSelectedItem();

        if (seleccionado != null) {
            Alert alertaConfirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            alertaConfirmacion.setTitle("Confirmar eliminación");
            alertaConfirmacion.setHeaderText("¿Estás seguro de eliminar este insumo?");
            alertaConfirmacion.setContentText("Insumo: " + seleccionado.getNombre());

            java.util.Optional<ButtonType> resultado = alertaConfirmacion.showAndWait();

            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (insumoDAO.eliminar(seleccionado.getId())) {
                    refrescarDesdeBD();
                    mostrarAlerta("Éxito", "El insumo ha sido eliminado.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar de la base de datos.");
                }
            }
        } else {
            mostrarAlerta("Atención", "Selecciona un insumo de la tabla.");
        }
    }

    /**
     * Muestra mensajes de advertencia o información al administrador.
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Método general para navegar entre las diferentes secciones del programa.
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
            System.err.println("Error al cargar pantalla: " + fxmlPath);
            e.printStackTrace();
        }
    }

    //  Métodos de acceso rápido para los botones del menú lateral
    @FXML private void handleEmpleados(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionarEmpleado.fxml", "Gestionar Empleado"); }
    @FXML private void handleMenu(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionMenu.fxml", "Gestionar Menu"); }
    @FXML private void handleAlmacen(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/GestionAlmacen.fxml", "Gestionar Almacen"); }
    @FXML private void handleReportes(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/Reportes.fxml", "Reportes"); }
    @FXML private void handleHistorial(ActionEvent event) { cambiarPantalla(event, "/com/mycompany/restaurante/fxml/Historial.fxml", "Historial de Pedidos"); }
    @FXML private void handleCerrarSesion(ActionEvent event) {
        // Crear la alerta de confirmación
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");

        // Mostrar y esperar respuesta
        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Usamos tu método genérico que ya maneja el try-catch por dentro
            cambiarPantalla(event, "/com/mycompany/restaurante/fxml/LoginPantalla.fxml", "Iniciar sesión");
        }
        // Si el usuario cancela, simplemente no hace nada y la alerta se cierra solita.
    }
}
