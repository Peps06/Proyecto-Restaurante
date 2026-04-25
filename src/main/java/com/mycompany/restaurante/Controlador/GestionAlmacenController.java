/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Controlador;

/**
 *
 * @author mrubi
 */
import com.mycompany.restaurante.Modelo.Insumo;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

        // 2. Cargar algunos datos de prueba para Saveurs Paris
        cargarDatosPrueba();

        // 3. Asignar la lista a la tabla
        tablaAlmacen.setItems(listaInsumos);
        
        // Mensaje por si la tabla está vacía
        tablaAlmacen.setPlaceholder(new Label("No hay insumos en el inventario"));
        
        //Añade color depende el estado
        colEstado.setCellFactory(column -> {
        return new TableCell<Insumo, String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setText(null);
                setStyle(""); // Limpia el estilo si la celda está vacía
            } else {
                setText(item);
                
                // Aplicamos estilos según el contenido del texto
                if (item.equalsIgnoreCase("Disponible")) {
                    setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;"); 
                    // verde 
                } else if (item.equalsIgnoreCase("Por agotarse")) {
                    setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold;"); 
                    //amarillo
                } else if (item.equalsIgnoreCase("Agotado") || item.equalsIgnoreCase("No disponible")) {
                    setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;"); 
                    // rojo
                } else {
                    setStyle(""); // Estilo por defecto para otros textos
                }
            }
        }
    };
});
    }

    private void cargarDatosPrueba() {
        listaInsumos.add(new Insumo(1, "Harina de Trigo", "Abarrotes", 50.0, "kg", "Disponible"));
        listaInsumos.add(new Insumo(2, "Mantequilla", "Lácteos", 4.5, "kg", "Por agotarse"));
        listaInsumos.add(new Insumo(3, "Queso Camembert", "Lácteos", 10.0, "piezas", "Disponible"));
        listaInsumos.add(new Insumo(4, "Vino Tinto", "Bebidas", 0.0, "botellas", "Agotado"));
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
    private void manejarAgregar() throws IOException {
    // 1. Cargamos el FXML del formulario de insumos
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FormularioInsumo.fxml"));
    Parent root = loader.load();
    
    // 2. Obtenemos el controlador del formulario
    FormularioInsumoController controlador = loader.getController();
    
    // 3. Le pasamos 'null' porque es un registro NUEVO
    controlador.setInsumo(null); 
    
    // 4. Configuramos y mostramos la ventana
    Stage stage = new Stage();
    stage.setTitle("Saveurs Paris - Registrar Insumo");
    stage.setScene(new Scene(root));
    stage.showAndWait();
    
    // 5. Si se guardó con éxito, lo agregamos a nuestra lista de la tabla
    if (controlador.isGuardadoExitoso()) {
        Insumo nuevoInsumo = controlador.getInsumo();
        listaInsumos.add(nuevoInsumo);
    }
}

    @FXML
    private void manejarEditar() throws IOException {
    // 1. Verificamos que el usuario haya seleccionado un insumo de la tabla
    Insumo seleccionado = tablaAlmacen.getSelectionModel().getSelectedItem();
    
    if (seleccionado != null) {
        // 2. Cargamos el mismo FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/restaurante/fxml/FormularioInsumo.fxml"));
        Parent root = loader.load();
        
        FormularioInsumoController controlador = loader.getController();
        
        // 3. ¡Aquí está la diferencia! Le pasamos el objeto 'seleccionado'
        controlador.setInsumo(seleccionado); 
        
        // 4. Mostramos la ventana
        Stage stage = new Stage();
        stage.setTitle("Saveurs Paris - Editar Insumo");
        stage.setScene(new Scene(root));
        stage.showAndWait();
        
        // 5. Si hubo cambios, refrescamos la tabla para que se vean
        if (controlador.isGuardadoExitoso()) {
            tablaAlmacen.refresh();
        }
    } else {
        // Si no seleccionó nada, le avisamos
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Atención");
        alerta.setHeaderText(null);
        alerta.setContentText("Por favor, selecciona un insumo de la tabla para editar.");
        alerta.showAndWait();
    }
}

    @FXML
    private void manejarEliminar() {
    // 1. Obtenemos el insumo seleccionado de la tabla
    Insumo seleccionado = tablaAlmacen.getSelectionModel().getSelectedItem();

    if (seleccionado != null) {
        // 2. Creamos la alerta de confirmación
        Alert alertaConfirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        alertaConfirmacion.setTitle("Confirmar eliminación");
        alertaConfirmacion.setHeaderText("¿Estás seguro de eliminar este insumo?");
        alertaConfirmacion.setContentText("Insumo: " + seleccionado.getNombre() + "\nEsta acción no se puede deshacer.");

        // 3. Mostramos la alerta y esperamos la respuesta del usuario
        java.util.Optional<ButtonType> resultado = alertaConfirmacion.showAndWait();

        // 4. Si el usuario le dio clic a "Aceptar"
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // Borramos de la lista (esto actualiza la tabla automáticamente)
            listaInsumos.remove(seleccionado);

            // 5. Mostramos mensaje de éxito
            Alert alertaExito = new Alert(Alert.AlertType.INFORMATION);
            alertaExito.setTitle("Eliminación exitosa");
            alertaExito.setHeaderText(null);
            alertaExito.setContentText("El insumo ha sido eliminado correctamente.");
            alertaExito.showAndWait();
        }
        // Si le da a Cancelar, no pasa nada y se cierra la alerta.
        
    } else {
        // Si no seleccionó nada, le avisamos
        Alert alertaError = new Alert(Alert.AlertType.WARNING);
        alertaError.setTitle("Atención");
        alertaError.setHeaderText(null);
        alertaError.setContentText("Por favor, selecciona un insumo de la tabla para eliminar.");
        alertaError.showAndWait();
    }
}

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
