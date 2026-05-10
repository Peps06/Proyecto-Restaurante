package com.mycompany.restaurante.Controlador;


import com.mycompany.restaurante.DAO.ProductoDAO;
import com.mycompany.restaurante.Modelo.Producto;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;

/**
 *
 * @author Dana, Citlaly
 * @version 3 (detalles de pedido)
 */

public class RegistrarPedidoController {

    @FXML private TextField txtBusqueda; 
    @FXML private TableView<Producto> tableMenu;
    @FXML private TableColumn<Producto, String> ColumnaNombre;
    @FXML private TableColumn<Producto, String> ColumnaDescripcion;
    @FXML private TableColumn<Producto, String> ColumnaCantidad;
    @FXML private TextArea txtDescripcion;
    
    @FXML private Button btnRealizarP;
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnCancelarPedido;

    private ObservableList<Producto> masterData = FXCollections.observableArrayList();
    private FilteredList<Producto> filteredData;
    
    private int idEmpleadoReal;
    private int idMesaReal;

    public void initialize() {
        // 1. Cargar datos
        for (Producto p : ProductoDAO.obtenerTodos()) {
            p.setCantidadPedida(0);   // reinicia cantidad para el pedido
            masterData.add(p);
        }

        // 2. Configurar columnas
        ColumnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        ColumnaDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        ColumnaDescripcion.setCellFactory(tc -> new TableCell<>() {
            private final Text text = new Text();
            {
                text.wrappingWidthProperty().bind(ColumnaDescripcion.widthProperty());
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
        
        tableMenu.setFixedCellSize(-1);

        // 3. Columna Cantidad con botones
        ColumnaCantidad.setCellFactory(param -> new TableCell<>() {
            private final Button btnMenos = new Button("-");
            private final Button btnMas = new Button("+");
            private final Label lblCant = new Label();
            private final HBox container = new HBox(10, btnMenos, lblCant, btnMas);

            {
                container.setAlignment(Pos.CENTER);
                btnMenos.setStyle("-fx-background-color: #8b1a1a;"
                                  + "-fx-text-fill: white;"
                                  + "-fx-cursor: hand;");
                btnMas.setStyle("-fx-background-color: #8b1a1a;"
                                + "-fx-text-fill: white;"
                                + "-fx-cursor: hand;");
                
                btnMenos.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    int actual = p.getCantidadPedida();
                    if (actual > 0) {
                        p.setCantidadPedida((actual - 1));
                        getTableView().refresh();
                    }
                });

                btnMas.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    int actual = p.getCantidadPedida();
                    p.setCantidadPedida((actual + 1));
                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Producto p = getTableView().getItems().get(getIndex());
                    lblCant.setText(String.valueOf(p.getCantidadPedida()));
                    setGraphic(container);
                }
            }
        });

        // 4. Configurar filtrado (pero no se aplica hasta dar clic en buscar)
        filteredData = new FilteredList<>(masterData, p -> true);
        tableMenu.setItems(filteredData);
    }
    
    // Metodo para recibir la mesa
    public void setMesaSeleccionada(int numMesa) {
        this.idMesaReal = numMesa;
        System.out.println("Mesa seleccionada en el controlador de pedidos: "
                            + numMesa);
    }

    // --- MÉTODOS DE ACCIÓN ---

    @FXML
    private void handleBuscar() {
        String texto = txtBusqueda.getText().toLowerCase();
        filteredData.setPredicate(producto -> {
            return producto.getNombre().toLowerCase().contains(texto);
        });
        txtBusqueda.clear();
    }
    
    @FXML
    private void handleMostrarTabla(){
        filteredData.setPredicate(p -> true);
        txtBusqueda.clear();
    }

    @FXML
    private void handleConfirmarPedido() {
        boolean hayProductos = false;
        StringBuilder resumen = new StringBuilder();

        // 1. Verificamos si hay productos en el pedido para el resumen
        for (Producto p : masterData) {
            int cantidad = p.getCantidadPedida();
            if (cantidad > 0) {
                resumen.append("- ").append(p.getNombre()).append(" (x")
                        .append(cantidad).append(")\n");
                hayProductos = true;
            }
        }

        // 2. Si no seleccionó nada, mostrar error
        if (!hayProductos) {
            Alert alertError = new Alert(Alert.AlertType.ERROR);
            alertError.setTitle("Pedido Vacío");
            alertError.setHeaderText(null);
            alertError.setContentText("No has seleccionado ningún producto."
                                + "Usa los botones + para añadir elementos.");
            alertError.showAndWait();
            return;
        }

        // 3. Crear la alerta de Confirmación
        Alert alertConf = new Alert(Alert.AlertType.CONFIRMATION);
        alertConf.setTitle("Resumen del Pedido");
        alertConf.setHeaderText("¿Confirmar el siguiente pedido?");
        alertConf.setContentText(resumen.toString());

        Optional<ButtonType> result = alertConf.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            // --- 4. Se manda el pedido a DAO ---
            int idEmpleadoTemporal = 3; // El ID de Dana
            
            String notasDelMesero = txtDescripcion.getText();
            
            int idGenerado = com.mycompany.restaurante.DAO.PedidoDAO
                .insertarOrdenCompleta(idMesaReal, idEmpleadoTemporal,
                                       masterData, notasDelMesero);

            if (idGenerado != -1) {
                // Éxito: Limpiamos la pantalla
                masterData.forEach(p -> p.setCantidadPedida(0));
                tableMenu.refresh();
                txtDescripcion.clear();
                
                Alert alertExito = new Alert(Alert.AlertType.INFORMATION);
                alertExito.setTitle("Confirmado");
                alertExito.setHeaderText(null);
                alertExito.setContentText("Pedido #" + idGenerado +
                            " enviado a cocina y mesa marcada como ocupada.");
                alertExito.showAndWait();
                
            } else {
                Alert alertFallo = new Alert(Alert.AlertType.ERROR);
                alertFallo.setTitle("Error");
                alertFallo.setHeaderText(null);
                alertFallo.setContentText("Ocurrió un problema al guardar la"
                                            + "orden en la base de datos.");
                alertFallo.showAndWait();
            }
            
        } else {
            System.out.println("El mesero canceló el envío de la orden.");
        }
        
        cerrarVentana();
    }
    
    @FXML
    private void handleCancelarPedido(ActionEvent event){
        cerrarVentana();
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        // Crear la alerta de confirmación
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Salida");
        alerta.setHeaderText("Cerrar Sesión");
        alerta.setContentText("¿Estás seguro de que deseas salir del sistema?");

        // Mostrar y esperar respuesta
        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // Código para regresar al Login (ejemplo)
                Parent root = FXMLLoader.load(getClass().getResource(
                        "/com/mycompany/restaurante/fxml/LoginPantalla.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // El usuario canceló, no se hace nada y se queda en la ventana
            alerta.close();
        }
    }
    
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelarPedido.getScene().getWindow();
        stage.close();
    }
}