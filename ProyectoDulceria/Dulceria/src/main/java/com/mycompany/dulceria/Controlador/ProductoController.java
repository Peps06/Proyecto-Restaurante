package com.mycompany.dulceria.Controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.mycompany.dulceria.Modelo.*;
import java.util.ArrayList;

/**
 * Controlador principal de la ventana unificada del administrador.
 *
 * Gestiona la navegación entre secciones (Inventario, Agregar, Eliminar,
 * Actualizar) dentro del mismo stage, sin abrir nuevas ventanas.
 *
 * Los fx:id de cada sección coinciden con los nombres originales de los
 * controladores separados, por lo que la lógica de negocio NO cambia.
 *
 * @author Dana, Rubi, Citlaly
 * @version 3.0 (Ventana unificada)
 */
public class ProductoController {

    @FXML private StackPane stackContenido;

    @FXML private BorderPane panelInventario;
    @FXML private AnchorPane panelAgregar;
    @FXML private AnchorPane panelEliminar;
    @FXML private BorderPane panelActualizar;

    @FXML private Button btnMenuInventario;
    @FXML private Button btnMenuAgregar;
    @FXML private Button btnMenuEliminar;
    @FXML private Button btnMenuActualizar;

    @FXML private TextArea textAreaProductos;
    @FXML private Button btnQuick;
    @FXML private Button btnHeap;
    @FXML private Button btnMerge;

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtMarca;
    @FXML private TextField txtFechaCaducidad;
    @FXML private TextField txtCantidad;
    @FXML private Button btnAgregar;

    @FXML private TextField txtCodigo;
    @FXML private TextField txtCantidadEliminar;
    @FXML private Button btnEliminar;

    @FXML private TextField txtIdBuscar;
    @FXML private TextField txtNombreAct;
    @FXML private TextField txtPrecioCompraAct;
    @FXML private TextField txtPrecioVentaAct;
    @FXML private TextField txtMarcaAct;
    @FXML private TextField txtFechaCaducidadAct;
    @FXML private TextField txtCantidadAct;
    @FXML private TextField txtNuevoValor;
    @FXML private ComboBox<String> cmbCampo;
    @FXML private Label lblNuevoValor;
    @FXML private Button btnBuscar;
    @FXML private Button btnActualizar;
    @FXML private Button btnRegresar;

    private ImpDulceDAO dao;
    private Dulce dulceActual;

    /**
     * Se ejecuta automáticamente al cargar el FXML.
     */
    @FXML
    private void initialize() {
        // Campos de datos del producto (Actualizar) son solo lectura
        txtNombreAct.setEditable(false);
        txtPrecioCompraAct.setEditable(false);
        txtPrecioVentaAct.setEditable(false);
        txtMarcaAct.setEditable(false);
        txtFechaCaducidadAct.setEditable(false);
        txtCantidadAct.setEditable(false);

        cmbCampo.getSelectionModel().selectFirst();
        if (lblNuevoValor != null) lblNuevoValor.setText("Nuevo precio de compra:");

        textAreaProductos.setEditable(false);
    }

    /**
     * Establece el DAO compartido y carga el inventario inicial.
     *
     * @param dao La instancia del DAO de dulces
     */
    public void setDao(ImpDulceDAO dao) {
        this.dao = dao;
        cargarInventario();
    }

    /** Muestra el panel de inventario. */
    @FXML
    private void mostrarInventario() {
        cambiarPanel(panelInventario);
        cargarInventario();
    }

    /** Muestra el panel de agregar producto. */
    @FXML
    private void mostrarAgregar() {
        cambiarPanel(panelAgregar);
    }

    /** Muestra el panel de eliminar producto. */
    @FXML
    private void mostrarEliminar() {
        cambiarPanel(panelEliminar);
    }

    /** Muestra el panel de actualizar producto. */
    @FXML
    private void mostrarActualizar() {
        cambiarPanel(panelActualizar);
    }

    /**
     * Hace visible únicamente el panel indicado dentro del StackPane.
     *
     * @param panelActivo El panel que debe quedar visible
     */
    private void cambiarPanel(Pane panelActivo) {
        panelInventario.setVisible(false);
        panelAgregar.setVisible(false);
        panelEliminar.setVisible(false);
        panelActualizar.setVisible(false);
        panelActivo.setVisible(true);
    }

    /** Carga el inventario en el área de texto sin ordenar. */
    private void cargarInventario() {
        if (dao == null) { textAreaProductos.setText("Error: DAO no inicializado"); return; }
        mostrarEnTextArea(dao.obtenerDulces());
    }

    @FXML
    private void ordenarQuickSort() {
        if (dao == null) return;
        ArrayList<Dulce> lista = dao.obtenerDulces();
        OrdenamientosDulce.quickSort(lista, 0, lista.size() - 1);
        mostrarEnTextArea(lista);
    }

    @FXML
    private void ordenarHeapSort() {
        if (dao == null) return;
        ArrayList<Dulce> lista = dao.obtenerDulces();
        OrdenamientosDulce.heapSort(lista);
        mostrarEnTextArea(lista);
    }

    @FXML
    private void ordenarMergeSort() {
        if (dao == null) return;
        ArrayList<Dulce> lista = dao.obtenerDulces();
        OrdenamientosDulce.mergeSort(lista);
        mostrarEnTextArea(lista);
    }

    private void mostrarEnTextArea(ArrayList<Dulce> lista) {
        StringBuilder sb = new StringBuilder();
        for (Dulce d : lista) {
            sb.append("ID: ").append(d.getId()).append("\n");
            sb.append("Nombre: ").append(d.getNombre()).append("\n");
            sb.append("Precio Compra: ").append(d.getPrecioCompra()).append("\n");
            sb.append("Precio Venta: ").append(d.getPrecioVenta()).append("\n");
            sb.append("Marca: ").append(d.getMarca()).append("\n");
            sb.append("Fecha Caducidad: ").append(d.getFecha_caducidad()).append("\n");
            sb.append("Cantidad: ").append(d.getCantidad()).append("\n");
            sb.append("--------------------------------------\n");
        }
        textAreaProductos.setText(sb.toString());
    }

    @FXML
    private void agregarProducto() {
        String id          = txtId.getText().trim();
        String nombre      = txtNombre.getText().trim();
        String precioComp  = txtPrecioCompra.getText().trim();
        String precioVent  = txtPrecioVenta.getText().trim();
        String marca       = txtMarca.getText().trim();
        String fecha       = txtFechaCaducidad.getText().trim();
        String cantidad    = txtCantidad.getText().trim();

        if (!id.matches("\\d{1,10}")) {
            alerta("ID inválido", "El ID debe ser numérico y tener máximo 10 dígitos.", Alert.AlertType.WARNING); return;
        }
        if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            alerta("Nombre inválido", "El nombre solo puede contener letras y espacios.", Alert.AlertType.WARNING); return;
        }
        if (!precioComp.matches("\\d+(\\.\\d+)?")) {
            alerta("Precio inválido", "El precio de compra debe ser un número válido.", Alert.AlertType.WARNING); return;
        }
        if (!precioVent.matches("\\d+(\\.\\d+)?")) {
            alerta("Precio inválido", "El precio de venta debe ser un número válido.", Alert.AlertType.WARNING); return;
        }
        if (marca.isEmpty()) {
            alerta("Marca inválida", "La marca no puede estar vacía.", Alert.AlertType.WARNING); return;
        }
        if (!cantidad.matches("\\d+")) {
            alerta("Cantidad inválida", "La cantidad debe ser un número entero.", Alert.AlertType.WARNING); return;
        }

        try {
            Dulce nuevo = new Dulce(id, nombre, precioComp, precioVent, marca, fecha, cantidad);
            dao.agregarDulce(nuevo);
            alerta("Éxito", "Dulce agregado correctamente", Alert.AlertType.INFORMATION);
            limpiarCamposAgregar();
        } catch (Exception ex) {
            alerta("Error", "Error al guardar: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void limpiarCamposAgregar() {
        txtId.clear(); txtNombre.clear(); txtPrecioCompra.clear();
        txtPrecioVenta.clear(); txtMarca.clear();
        txtFechaCaducidad.clear(); txtCantidad.clear();
        txtId.requestFocus();
    }

    @FXML
    private void eliminarProducto() {
        String id          = txtCodigo.getText().trim();
        String cantidadStr = txtCantidadEliminar.getText().trim();

        if (id.isEmpty()) {
            alerta("Advertencia", "Por favor ingresa el código del producto", Alert.AlertType.WARNING); return;
        }
        if (cantidadStr.isEmpty()) {
            alerta("Advertencia", "Por favor ingresa la cantidad a eliminar", Alert.AlertType.WARNING); return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                alerta("Error", "La cantidad debe ser mayor a 0", Alert.AlertType.ERROR); return;
            }
            String resultado = dao.restarCantidad(id, cantidad);
            alerta("Resultado", resultado, Alert.AlertType.INFORMATION);
            txtCodigo.clear(); txtCantidadEliminar.clear(); txtCodigo.requestFocus();
        } catch (NumberFormatException e) {
            alerta("Error", "La cantidad debe ser un número válido", Alert.AlertType.ERROR);
        } catch (Exception e) {
            alerta("Error", "Error al eliminar producto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void buscarProducto() {
        String id = txtIdBuscar.getText().trim();
        if (id.isEmpty()) { alerta("Advertencia", "Ingrese el ID del dulce", Alert.AlertType.WARNING); return; }

        try {
            dulceActual = dao.obtenerDulceDesdeArbol(id);
            if (dulceActual == null) {
                alerta("No encontrado", "No se encontró el dulce con ID: " + id, Alert.AlertType.WARNING);
                limpiarCamposActualizar(); return;
            }
            txtNombreAct.setText(dulceActual.getNombre());
            txtPrecioCompraAct.setText(dulceActual.getPrecioCompra());
            txtPrecioVentaAct.setText(dulceActual.getPrecioVenta());
            txtMarcaAct.setText(dulceActual.getMarca());
            txtFechaCaducidadAct.setText(dulceActual.getFecha_caducidad());
            txtCantidadAct.setText(dulceActual.getCantidad());
            txtNuevoValor.clear();
        } catch (Exception e) {
            alerta("Error", "Error al buscar producto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void actualizarProducto() {
        String id         = txtIdBuscar.getText().trim();
        String campo      = cmbCampo.getValue();
        String nuevoValor = txtNuevoValor.getText().trim();

        if (id.isEmpty())        { alerta("Advertencia", "Primero busca un producto",    Alert.AlertType.WARNING); return; }
        if (nuevoValor.isEmpty()) { alerta("Advertencia", "Ingrese el nuevo valor",        Alert.AlertType.WARNING); return; }
        if (dulceActual == null)  { alerta("Advertencia", "No hay un producto cargado",   Alert.AlertType.WARNING); return; }

        try {
            switch (campo) {
                case "Precio de compra":
                    try { Double.parseDouble(nuevoValor); }
                    catch (NumberFormatException e) { alerta("Error", "El precio debe ser un número válido", Alert.AlertType.ERROR); return; }
                    dulceActual.setPrecioCompra(nuevoValor);
                    txtPrecioCompraAct.setText(nuevoValor);
                    break;
                case "Precio de venta":
                    try { Double.parseDouble(nuevoValor); }
                    catch (NumberFormatException e) { alerta("Error", "El precio debe ser un número válido", Alert.AlertType.ERROR); return; }
                    dulceActual.setPrecioVenta(nuevoValor);
                    txtPrecioVentaAct.setText(nuevoValor);
                    break;
                case "Cantidad":
                    try { Integer.parseInt(nuevoValor); }
                    catch (NumberFormatException e) { alerta("Error", "La cantidad debe ser un número entero válido", Alert.AlertType.ERROR); return; }
                    dulceActual.setCantidad(nuevoValor);
                    txtCantidadAct.setText(nuevoValor);
                    break;
            }
            dao.actualizarDulce(dulceActual);
            alerta("Éxito", "Producto actualizado correctamente", Alert.AlertType.INFORMATION);
            txtNuevoValor.clear();
        } catch (Exception ex) {
            alerta("Error", "Error al guardar cambios: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cambiarCampo() {
        String campo = cmbCampo.getValue();
        if (campo != null) {
            switch (campo) {
                case "Precio de compra": lblNuevoValor.setText("Nuevo precio de compra:"); break;
                case "Precio de venta":  lblNuevoValor.setText("Nuevo precio de venta:");  break;
                case "Cantidad":         lblNuevoValor.setText("Nueva cantidad:");          break;
            }
            txtNuevoValor.clear();
            txtNuevoValor.requestFocus();
        }
    }

    private void limpiarCamposActualizar() {
        txtNombreAct.clear(); txtPrecioCompraAct.clear(); txtPrecioVentaAct.clear();
        txtMarcaAct.clear(); txtFechaCaducidadAct.clear(); txtCantidadAct.clear();
        txtNuevoValor.clear();
        dulceActual = null;
    }

    /** Regresa al menú anterior. */
    @FXML
    private void regresar() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/mycompany/dulceria/fxml/MenuAdmin.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) btnRegresar.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            alerta("Error", "Error al regresar: " + e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    private void alerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
