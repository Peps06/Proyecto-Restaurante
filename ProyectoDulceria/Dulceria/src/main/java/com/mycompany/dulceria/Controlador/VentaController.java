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
import java.util.ArrayList;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * Controlador principal de la ventana unificada de Ventas.
 * Gestiona la navegación entre Reporte de Ventas y Registrar Venta
 * dentro del mismo stage, sin abrir nuevas ventanas.
 *
 * @author Dana, Rubi, Citlaly
 * @version 3.0 (Ventana unificada)
 */
public class VentaController {

    @FXML private StackPane stackContenido;
    @FXML private BorderPane panelReporte;
    @FXML private BorderPane panelRegistrar;
    @FXML private Button btnMenuReporte;
    @FXML private Button btnMenuRegistrar;
    @FXML private Button btnRegresar;

    @FXML private TextField txtFechaBuscada;
    @FXML private Button btnBuscar;
    @FXML private Button btnMostrarTodas;
    @FXML private TableView<VentaReporte> tablaVentas;
    @FXML private TableColumn<VentaReporte, String> colIdVenta;
    @FXML private TableColumn<VentaReporte, String> colFecha;
    @FXML private TableColumn<VentaReporte, String> colTotal;

    @FXML private TextField txtIdVenta;
    @FXML private TextField txtFecha;
    @FXML private TextField txtIdProduc;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtTotal;
    @FXML private Button btnBuscarProducto;
    @FXML private Button btnAgregar;
    @FXML private Button btnRegistrar;
    @FXML private Button btnCancelar;
    @FXML private TableView<ProductoVenta> tabla;
    @FXML private TableColumn<ProductoVenta, String> colId;
    @FXML private TableColumn<ProductoVenta, String> colNombre;
    @FXML private TableColumn<ProductoVenta, String> colCantidad;
    @FXML private TableColumn<ProductoVenta, String> colPrecio;
    @FXML private TableColumn<ProductoVenta, String> colSubtotal;

    private ImpDulceDAO dao;
    private ImpVentaDAO ventaDao;
    private double total = 0;
    private String ultimaVentaRegistrada = null;
    private ObservableList<VentaReporte> listaVentas   = FXCollections.observableArrayList();
    private ObservableList<ProductoVenta> listaProductos = FXCollections.observableArrayList();

    public void setDao(ImpDulceDAO dao) {
        this.dao = dao;
    }

    public void setVentaDao(ImpVentaDAO ventaDao) {
        this.ventaDao = ventaDao;
        cargarTodasLasVentas();
    }

    @FXML
    private void initialize() {
        colIdVenta.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getIdVenta()));
        colFecha.setCellValueFactory(data   -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFecha()));
        colTotal.setCellValueFactory(data   -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTotal()));
        tablaVentas.setItems(listaVentas);
        tablaVentas.setPlaceholder(new Label("No hay ventas registradas"));

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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtFecha.setText(LocalDate.now().format(formatter));
        txtIdVenta.setText("V" + System.currentTimeMillis());
        txtTotal.setText("0.00");
    }

    @FXML
    private void mostrarReporte() {
        panelReporte.setVisible(true);
        panelRegistrar.setVisible(false);
    }

    @FXML
    private void mostrarRegistrar() {
        panelReporte.setVisible(false);
        panelRegistrar.setVisible(true);
    }

    @FXML
    private void regresar() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/dulceria/fxml/MenuAdmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegresar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            alerta("Error", "Error al regresar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void buscarPorFecha() {
        String fecha = txtFechaBuscada.getText().trim();
        if (fecha.isEmpty()) {
            alerta("Advertencia", "Por favor ingresa una fecha (YYYY-MM-DD)", Alert.AlertType.WARNING);
            return;
        }
        try {
            ArrayList<Venta> ventas = ventaDao.ventasPorFecha(fecha);
            listaVentas.clear();
            if (ventas.isEmpty()) {
                alerta("Sin resultados", "No se encontraron ventas para: " + fecha, Alert.AlertType.INFORMATION);
            } else {
                for (Venta v : ventas)
                    listaVentas.add(new VentaReporte(v.getIdVenta(), v.getFecha(), v.getTotal()));
            }
        } catch (Exception e) {
            alerta("Error", "Error al buscar ventas: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void mostrarTodasLasVentas() {
        txtFechaBuscada.clear();
        cargarTodasLasVentas();
    }

    private void cargarTodasLasVentas() {
        if (ventaDao == null) return;
        try {
            ArrayList<Venta> ventas = ventaDao.obtenerVentas();
            listaVentas.clear();
            for (Venta v : ventas)
                listaVentas.add(new VentaReporte(v.getIdVenta(), v.getFecha(), v.getTotal()));
        } catch (Exception e) {
            alerta("Error", "Error al cargar ventas: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void buscarProducto() {
        String id = txtIdProduc.getText().trim();
        if (id.isEmpty()) {
            alerta("Advertencia", "Por favor ingresa un ID de producto", Alert.AlertType.WARNING);
            return;
        }
        if (dao == null) {
            alerta("Error", "No hay conexión con la base de datos", Alert.AlertType.ERROR);
            return;
        }
        try {
            Dulce dulce = dao.obtenerDulce(id);
            if (dulce == null) {
                alerta("Producto no encontrado",
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
            alerta("Error", "Error al buscar producto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void agregarProducto() {
        if (txtNombre.getText().isEmpty()) {
            alerta("Advertencia", "Primero busca un producto", Alert.AlertType.WARNING);
            return;
        }
        String cantidadStr = txtCantidad.getText().trim();
        if (cantidadStr.isEmpty()) {
            alerta("Advertencia", "Por favor ingresa una cantidad", Alert.AlertType.WARNING);
            return;
        }
        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                alerta("Advertencia", "La cantidad debe ser mayor a 0", Alert.AlertType.WARNING);
                return;
            }
            double precio = Double.parseDouble(txtPrecio.getText());
            double subtotal = precio * cantidad;
            listaProductos.add(new ProductoVenta(
                txtIdProduc.getText(),
                txtNombre.getText(),
                String.valueOf(cantidad),
                txtPrecio.getText(),
                String.format("%.2f", subtotal)
            ));
            total += subtotal;
            txtTotal.setText(String.format("%.2f", total));
            limpiarCamposProducto();
            txtIdProduc.requestFocus();
        } catch (NumberFormatException e) {
            alerta("Error", "La cantidad debe ser un número válido", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void registrarVenta() {
        if (listaProductos.isEmpty()) {
            alerta("Advertencia", "No hay productos para registrar", Alert.AlertType.WARNING);
            return;
        }
        if (dao == null || ventaDao == null) {
            alerta("Error", "No hay conexión con la base de datos", Alert.AlertType.ERROR);
            return;
        }
        try {
            String idVenta = "V" + System.currentTimeMillis();
            String fecha = LocalDate.now().toString();
            Venta venta = new Venta(idVenta, fecha);

            for (ProductoVenta pv : listaProductos) {
                Dulce d = dao.obtenerDulce(pv.getId());
                if (d == null) {
                    alerta("Error", "El producto " + pv.getId() + " no existe.", Alert.AlertType.ERROR);
                    return;
                }
                int disponible = Integer.parseInt(d.getCantidad());
                int cantSolicitada = Integer.parseInt(pv.getCantidad());
                if (cantSolicitada > disponible) {
                    alerta("Inventario Insuficiente",
                        "Producto: " + d.getNombre() +
                        "\nDisponible: " + disponible +
                        "\nSolicitado: " + cantSolicitada,
                        Alert.AlertType.ERROR);
                    return;
                }
            }

            for (ProductoVenta pv : listaProductos) {
                venta.agregarProducto(pv.getId(), pv.getNombre(), pv.getCantidad(), pv.getSubtotal());
                dao.restarCantidad(pv.getId(), Integer.parseInt(pv.getCantidad()));
            }
            venta.setTotal(txtTotal.getText());
            ventaDao.registrarVenta(venta);
            ultimaVentaRegistrada = venta.getIdVenta();

            alerta("Éxito", "Venta registrada correctamente", Alert.AlertType.INFORMATION);
            limpiarFormulario();
        } catch (Exception e) {
            alerta("Error", "Error al registrar venta: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancelarVenta() {
        if (!listaProductos.isEmpty()) {
            ProductoVenta seleccionado = tabla.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                alerta("Advertencia",
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

        if (ultimaVentaRegistrada == null) {
            alerta("Advertencia",
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
                alerta("Error", "Contraseña incorrecta.", Alert.AlertType.ERROR);
                return;
            }
            try {
                boolean ok = ventaDao.eliminarVenta(ultimaVentaRegistrada);
                if (ok) {
                    alerta("Éxito",
                           "Venta " + ultimaVentaRegistrada + " cancelada y revertida exitosamente.",
                           Alert.AlertType.INFORMATION);
                    ultimaVentaRegistrada = null;
                } else {
                    alerta("Error", "No fue posible eliminar la venta.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                alerta("Error", "Error al cancelar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
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

    private void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static class VentaReporte {
        private String idVenta, fecha, total;

        public VentaReporte(String idVenta, String fecha, String total) {
            this.idVenta = idVenta; this.fecha = fecha; this.total = total;
        }

        public String getIdVenta() { return idVenta; }
        public String getFecha()   { return fecha; }
        public String getTotal()   { return total; }
    }

    public static class ProductoVenta {
        private String id, nombre, cantidad, precio, subtotal;

        public ProductoVenta(String id, String nombre, String cantidad, String precio, String subtotal) {
            this.id = id; this.nombre = nombre; this.cantidad = cantidad;
            this.precio = precio; this.subtotal = subtotal;
        }

        public String getId()       { return id; }
        public String getNombre()   { return nombre; }
        public String getCantidad() { return cantidad; }
        public String getPrecio()   { return precio; }
        public String getSubtotal() { return subtotal; }
    }
}
