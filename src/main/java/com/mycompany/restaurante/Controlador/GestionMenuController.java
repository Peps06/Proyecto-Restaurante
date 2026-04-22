/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Controlador;

/**
 *
 * @author mrubi
 */
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import com.mycompany.restaurante.Modelo.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class GestionMenuController {

    @FXML private TableView<Producto> tablaMenu;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colTipo;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, String> colDescripcion;
    @FXML private TextField txtBusqueda;

    // Lista maestra donde viven todos los platillos
    private ObservableList<Producto> datosMenu = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Vincular columnas con los atributos de Producto
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // 2. Cargar algunos datos para probar (como en tu prototipo)
        datosMenu.add(new Producto("Ratatouille", "Plato fuerte", 400.0, "Platillo cocinado por ratones"));
        datosMenu.add(new Producto("Macarrón", "Postre", 60.0, "Galleta rellena"));
        datosMenu.add(new Producto("Suflé", "Postre", 200.0, "Platillo esponjoso"));

        // 3. Setear los items a la tabla
        tablaMenu.setItems(datosMenu);
        
        // Mensaje si la tabla está vacía
        tablaMenu.setPlaceholder(new Label("No hay platillos en el menú de Saveurs Paris"));
    }

    @FXML
    private void handleBuscar() {
    // 1. Convertimos lo que el usuario escribió a minúsculas
    String textoBusqueda = txtBusqueda.getText().toLowerCase().trim();

    if (textoBusqueda == null || textoBusqueda.isEmpty()) {
        tablaMenu.setItems(datosMenu);
        return;
    }

    // 2. Al filtrar, convertimos los datos de la lista también a minúsculas
    FilteredList<Producto> filtrados = datosMenu.filtered(p -> 
        p.getNombre().toLowerCase().contains(textoBusqueda) || 
        p.getTipo().toLowerCase().contains(textoBusqueda)
    );
    
    tablaMenu.setItems(filtrados);
}

    @FXML
    private void handleMostrarTabla() {
        txtBusqueda.clear();
        tablaMenu.setItems(datosMenu);
    }

    @FXML
private void manejarAgregar() throws IOException {
    // 1. Cargamos el FXML de la misma ventanita
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FormularioProducto.fxml"));
    Parent root = loader.load();
    
    // 2. Obtenemos el controlador de la ventanita
    FormularioProductoController controlador = loader.getController();
    
    // 3. ¡Aquí está el truco! Le pasamos 'null' porque es un producto NUEVO
    controlador.setProducto(null); 
    
    // 4. Mostramos la ventana
    Stage stage = new Stage();
    stage.setTitle("Saveurs Paris - Nuevo Producto");
    stage.setScene(new Scene(root));
    stage.showAndWait();
    
    // 5. Si la usuaria dio clic en "Guardar", añadimos el nuevo producto a la lista
    if (controlador.isGuardadoExitoso()) {
        Producto nuevo = controlador.getProducto();
        datosMenu.add(nuevo); // Esto lo agrega automáticamente a la tabla
    }
}

    @FXML
private void manejarEliminar() {
    // 1. Obtener el producto seleccionado
    Producto seleccionado = tablaMenu.getSelectionModel().getSelectedItem();

    if (seleccionado != null) {
        // 2. Crear la alerta de CONFIRMACIÓN
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        // Esto pondrá: "¿Estás seguro de eliminar este Postre?" o "¿Estás seguro de eliminar este Bebida?"
        confirmacion.setHeaderText("¿Estás seguro de eliminar este " + seleccionado.getTipo() + "?");
        confirmacion.setContentText("Se eliminará: " + seleccionado.getNombre());

        // 3. Esperar a que el usuario responda
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                // Si aceptó, lo borramos de la lista
                datosMenu.remove(seleccionado);

                // 4. Mostrar mensaje de ÉXITO
                Alert exito = new Alert(Alert.AlertType.INFORMATION);
                exito.setTitle("Producto eliminado");
                exito.setHeaderText(null);
                exito.setContentText("El producto se ha eliminado correctamente del menú.");
                exito.showAndWait();
            }
        });
    } else {
        // 5. Si no seleccionó nada, avisarle
        Alert error = new Alert(Alert.AlertType.WARNING);
        error.setTitle("Atención");
        error.setHeaderText(null);
        error.setContentText("Por favor, selecciona un producto de la tabla para eliminarlo.");
        error.showAndWait();
    }
}
    
    @FXML
    private void manejarEditar() throws IOException {
    Producto seleccionado = tablaMenu.getSelectionModel().getSelectedItem();
    if (seleccionado != null) {
        // 1. Cargar el FXML de la ventanita
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FormularioProducto.fxml"));
        Parent root = loader.load();
        
        // 2. Obtener el controlador y PASARLE el producto seleccionado
        FormularioProductoController controlador = loader.getController();
        controlador.setProducto(seleccionado);
        
        // 3. Mostrar la ventana
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.showAndWait();
        
        // 4. Si guardó cambios, refrescar la tabla
        if (controlador.isGuardadoExitoso()) {
            tablaMenu.refresh();
        }
    }
}
}
