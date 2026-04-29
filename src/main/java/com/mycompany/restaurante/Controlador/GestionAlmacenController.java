/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Controlador;

/**
 *
 * @author mrubi
 */

import com.mycompany.restaurante.DAO.InsumoDAO;
import com.mycompany.restaurante.Modelo.Insumo;
import java.io.IOException;
import java.util.List;
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

public class GestionAlmacenController {

    // Componentes de la Tabla
    @FXML private TableView<Insumo> tablaAlmacen;
    @FXML private TableColumn<Insumo, Integer> colId;
    @FXML private TableColumn<Insumo, String> colProducto;
    @FXML private TableColumn<Insumo, String> colCategoria;
    @FXML private TableColumn<Insumo, Double> colStock;
    @FXML private TableColumn<Insumo, String> colUnidad;
    @FXML private TableColumn<Insumo, String> colEstado;
    @FXML private InsumoDAO insumoDAO = new InsumoDAO();

    // Componentes de Búsqueda y Botones
    @FXML private TextField txtBusqueda;
    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    private ObservableList<Insumo> listaInsumos = FXCollections.observableArrayList();

  @FXML
    public void initialize() {
    // 1. Vincular las columnas con los atributos de la clase Insumo
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
    colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
    colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
    colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
    colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

    
    // Traemos la lista desde la base de datos usando el DAO
    List<Insumo> listaBD = insumoDAO.listar();
    listaInsumos = FXCollections.observableArrayList(listaBD);

    // Asignar la lista a la tabla
    tablaAlmacen.setItems(listaInsumos);
    
    // Mensaje por si la tabla está vacía
    tablaAlmacen.setPlaceholder(new Label("No hay insumos en el inventario"));
    
    // Formatear el ID
    colId.setCellFactory(column -> new TableCell<Insumo, Integer>() {
        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(String.format("%03d", item));
            }
        }
    });

    // 5. Añade color depende el estado 
    colEstado.setCellFactory(column -> {
        return new TableCell<Insumo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
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
        };
    });
}

    @FXML
    private void handleBuscar() {
    String texto = txtBusqueda.getText().toLowerCase().trim();
    
    if (texto.isEmpty()) {
        tablaAlmacen.setItems(listaInsumos);
        return;
    }

    FilteredList<Insumo> filtrados = listaInsumos.filtered(insumo -> {
        // 1. Filtro por Nombre (ignora mayúsculas/minúsculas)
        boolean coincideNombre = insumo.getNombre().toLowerCase().contains(texto);
        
        // 2. Filtro por ID
        boolean coincideId = false;
        try {
            // Intentamos convertir el texto a número para comparar con el ID
            int idBuscado = Integer.parseInt(texto);
            coincideId = (insumo.getId() == idBuscado);
        } catch (NumberFormatException e) {
            // Si no es un número, simplemente no coincidirá por ID, no pasa nada
            coincideId = false;
        }

        // Retorna verdadero si coincide con cualquiera de los dos
        return coincideNombre || coincideId;
    });

    tablaAlmacen.setItems(filtrados);
}
   @FXML
   private void handleMostrarTabla() {
    txtBusqueda.setText("");
    tablaAlmacen.setItems(listaInsumos);
    tablaAlmacen.getSelectionModel().clearSelection();
}

@FXML
    private void manejarAgregar() {
    try {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FormularioInsumo.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Saveurs Paris - Nuevo Insumo");
        stage.setScene(new Scene(root));
        stage.showAndWait(); 

        // Refrescar lista desde la BD
        listaInsumos.setAll(insumoDAO.listar());

    } catch (IOException e) {
        mostrarAlerta("Error", "No se pudo cargar el formulario.");
        e.printStackTrace();
    }
}

@FXML
    private void manejarEditar() throws IOException {
    Insumo seleccionado = tablaAlmacen.getSelectionModel().getSelectedItem();
    
    if (seleccionado != null) {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FormularioInsumo.fxml"));
        Parent root = loader.load();
        
        FormularioInsumoController controlador = loader.getController();
        controlador.setInsumo(seleccionado); 
        
        Stage stage = new Stage();
        stage.setTitle("Saveurs Paris - Editar Insumo");
        stage.setScene(new Scene(root));
        stage.showAndWait();
        
        if (controlador.isGuardadoExitoso()) {
            // Refrescamos desde la BD para ver los cambios reales
            listaInsumos.setAll(insumoDAO.listar());
        }
    } else {
        mostrarAlerta("Atención", "Por favor, selecciona un insumo de la tabla para editar.");
    }
}



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
            //Llamada al DAO
            boolean eliminadoExitoso = insumoDAO.eliminar(seleccionado.getId());

            if (eliminadoExitoso) {
                listaInsumos.remove(seleccionado);
                mostrarAlerta("Éxito", "El insumo ha sido eliminado de la base de datos.");
            } else {
                mostrarAlerta("Error", "No se pudo eliminar de la base de datos.");
            }
        }
    } else {
        mostrarAlerta("Atención", "Selecciona un insumo de la tabla.");
    }
}

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
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
