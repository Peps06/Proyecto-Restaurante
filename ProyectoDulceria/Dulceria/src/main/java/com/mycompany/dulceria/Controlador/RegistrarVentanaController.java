package com.mycompany.dulceria.Controlador; 

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.mycompany.dulceria.Modelo.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class RegistrarVentanaController {
    
    @FXML private TextField txtIdVenta;
    @FXML private TextField txtFecha;
    @FXML private TextField txtIdProduc;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtTotal;
    
    @FXML private Button btnBuscar;
    @FXML private Button btnAgregar;
    @FXML private Button btnRegistrar;
    @FXML private Button btnCancelar;
    @FXML private Button btnRegresar;
    
    @FXML private TableView<ProductoVenta> tabla;
    @FXML private TableColumn<ProductoVenta, String> colId;
    @FXML private TableColumn<ProductoVenta, String> colNombre;
    @FXML private TableColumn<ProductoVenta, String> colCantidad;
    @FXML private TableColumn<ProductoVenta, String> colPrecio;
    @FXML private TableColumn<ProductoVenta, String> colSubtotal;
    
    private ImpDulceDAO dao;
    private ImpVentaDAO daoVenta;
    private double total = 0;
    private String ultimaVentaRegistrada = null;
    private ObservableList<ProductoVenta> listaProductos = FXCollections.observableArrayList();
    
    public void setDao(ImpDulceDAO dao) {
        this.dao = dao;
    }

    public void setVentaDao(ImpVentaDAO daoVenta) {
        this.daoVenta = daoVenta;
    }
    
    @FXML
    private void initialize() {
        colId.setCellValueFactory(data       -> new javafx.beans.property.SimpleStringProperty(data.getValue().getId()));
        colNombre.setCellValueFactory(data    -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        colCantidad.setCellValueFactory(data  -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCantidad()));
        colPrecio.setCellValueFactory(data    -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPrecio()));
        colSubtotal.setCellValueFactory(data  -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSubtotal()));
        
        tabla.setItems(listaProductos);
        
        txtIdVenta.setEditable(false);
        txtFecha.setEditable(false);
        txtNombre.setEditable(false);
        txtPrecio.setEditable(false);
        txtTotal.setEditable(false);

        // Fecha y ID inicial (sin tocar daoVenta)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtFecha.setText(LocalDate.now().format(formatter));
        txtIdVenta.setText("V" + System.currentTimeMillis());
        txtTotal.setText("0.00");
    }
    
    @FXML
    private void buscarProducto() {
        String id = txtIdProduc.getText().trim();
        
        if (id.isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor ingresa un ID de producto", Alert.AlertType.WARNING);
            return;
        }
        
        if (dao == null) {
            mostrarAlerta("Error", "No hay conexión con la base de datos", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            Dulce dulce = dao.obtenerDulce(id);
            
            if (dulce == null) {
                mostrarAlerta("Producto no encontrado", 
                            "No se encontró un producto con el ID: " + id, 
                            Alert.AlertType.WARNING);
                limpiarCamposProducto();
                return;
            }
            
            txtNombre.setText(dulce.getNombre());
            txtPrecio.setText(dulce.getPrecioVenta());
            txtCantidad.clear();
            txtCantidad.requestFocus();
            
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al buscar producto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void agregarProducto() {
        if (txtNombre.getText().isEmpty()) {
            mostrarAlerta("Advertencia", "Primero busca un producto", Alert.AlertType.WARNING);
            return;
        }
        
        String cantidadStr = txtCantidad.getText().trim();
        if (cantidadStr.isEmpty()) {
            mostrarAlerta("Advertencia", "Por favor ingresa una cantidad", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            int cantidad = Integer.parseInt(cantidadStr);
            
            if (cantidad <= 0) {
                mostrarAlerta("Advertencia", "La cantidad debe ser mayor a 0", Alert.AlertType.WARNING);
                return;
            }
            
            double precio = Double.parseDouble(txtPrecio.getText());
            double subtotal = precio * cantidad;
            
            ProductoVenta producto = new ProductoVenta(
                txtIdProduc.getText(),
                txtNombre.getText(),
                String.valueOf(cantidad),
                txtPrecio.getText(),
                String.format("%.2f", subtotal)
            );
            
            listaProductos.add(producto);
            total += subtotal;
            txtTotal.setText(String.format("%.2f", total));
            limpiarCamposProducto();
            txtIdProduc.requestFocus();
            
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad debe ser un número válido", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void registrarVenta() {
        if (listaProductos.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay productos para registrar", Alert.AlertType.WARNING);
            return;
        }
        
        if (dao == null || daoVenta == null) {
            mostrarAlerta("Error", "No hay conexión con la base de datos", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            String idVenta = "V" + System.currentTimeMillis();
            String fecha = LocalDate.now().toString(); // YYYY-MM-DD para MySQL
            
            Venta venta = new Venta(idVenta, fecha);
            
            // Validar inventario
            for (ProductoVenta pv : listaProductos) {
                Dulce d = dao.obtenerDulce(pv.getId());
                if (d == null) {
                    mostrarAlerta("Error", "El producto " + pv.getId() + " no existe.", Alert.AlertType.ERROR);
                    return;
                }
                int disponible = Integer.parseInt(d.getCantidad());
                int cantSolicitada = Integer.parseInt(pv.getCantidad());
                if (cantSolicitada > disponible) {
                    mostrarAlerta("Inventario Insuficiente",
                        "Producto: " + d.getNombre() +
                        "\nDisponible: " + disponible +
                        "\nSolicitado: " + cantSolicitada,
                        Alert.AlertType.ERROR);
                    return;
                }
            }
            
            // Registrar
            for (ProductoVenta pv : listaProductos) {
                venta.agregarProducto(pv.getId(), pv.getNombre(), pv.getCantidad(), pv.getSubtotal());
                dao.restarCantidad(pv.getId(), Integer.parseInt(pv.getCantidad()));
            }
            
            venta.setTotal(txtTotal.getText());
            daoVenta.registrarVenta(venta);
            ultimaVentaRegistrada = venta.getIdVenta();
            
            mostrarAlerta("Éxito", "Venta registrada correctamente", Alert.AlertType.INFORMATION);
            limpiarFormulario();
            
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al registrar venta: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void cancelarVenta() {
        // Si hay productos en la tabla → quitar el seleccionado (sin tocar BD)
        if (!listaProductos.isEmpty()) {
            ProductoVenta seleccionado = tabla.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                mostrarAlerta("Advertencia",
                    "Selecciona un producto de la tabla para quitarlo.",
                    Alert.AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Quitar producto");
            confirm.setHeaderText("¿Quitar este producto de la venta?");
            confirm.setContentText("Producto: " + seleccionado.getNombre() +
                                   "  |  Subtotal: $" + seleccionado.getSubtotal());
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) {
                    double subtotal = Double.parseDouble(seleccionado.getSubtotal());
                    total -= subtotal;
                    txtTotal.setText(String.format("%.2f", total));
                    listaProductos.remove(seleccionado);
                    tabla.getSelectionModel().clearSelection();
                }
            });
            return;
        }

        // Si la tabla está vacía → cancelar la última venta registrada en BD
        if (ultimaVentaRegistrada == null) {
            mostrarAlerta("Advertencia",
                "No hay ninguna venta registrada para cancelar.",
                Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar venta registrada");
        dialog.setHeaderText("Cancelar venta: " + ultimaVentaRegistrada +
                             "\nSe revertirá el inventario de todos sus productos.");
        dialog.setContentText("Contraseña de administrador:");
        dialog.showAndWait().ifPresent(password -> {
            if (!password.equals("1234")) {
                mostrarAlerta("Error", "Contraseña incorrecta.", Alert.AlertType.ERROR);
                return;
            }
            try {
                boolean ok = daoVenta.eliminarVenta(ultimaVentaRegistrada);
                if (ok) {
                    mostrarAlerta("Éxito",
                        "Venta " + ultimaVentaRegistrada + " cancelada y revertida exitosamente.",
                        Alert.AlertType.INFORMATION);
                    ultimaVentaRegistrada = null;
                } else {
                    mostrarAlerta("Error", "No fue posible eliminar la venta.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al cancelar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }
    
    @FXML
    private void regresar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/dulceria/fxml/LoginVentana.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegresar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al regresar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void limpiarCamposProducto() {
        txtIdProduc.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtCantidad.clear();
    }
    
    private void limpiarFormulario() {
        limpiarCamposProducto();
        txtTotal.setText("0.00");
        total = 0;
        listaProductos.clear();
        txtIdVenta.setText("V" + System.currentTimeMillis());
    }
    
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    public static class ProductoVenta {
        private String id, nombre, cantidad, precio, subtotal;
        
        public ProductoVenta(String id, String nombre, String cantidad, String precio, String subtotal) {
            this.id = id;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precio = precio;
            this.subtotal = subtotal;
        }
        
        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getCantidad() { return cantidad; }
        public String getPrecio() { return precio; }
        public String getSubtotal() { return subtotal; }
    }
}