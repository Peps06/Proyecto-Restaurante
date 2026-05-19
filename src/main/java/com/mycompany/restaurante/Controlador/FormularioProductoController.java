package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.Modelo.Producto;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Esta clase es el controlador del formulario para los productos del menú.
 * Es una ventana "dos en uno": me sirve tanto para registrar un platillo nuevo 
 * desde cero, como para editar uno que ya existe. 
 * y se encarga de devolver el objeto Producto listo para guardarse en la base de datos.
 * @author mrubi
 * @version 2.0
 */
public class FormularioProductoController {

    // Campos de texto y controles del formulario (FXML)
    @FXML private Label lblTitulo;       // El título que cambia según si es "Nuevo" o "Modificar"
    @FXML private TextField txtNombre;    
    @FXML private TextField txtDescripcion; 
    @FXML private TextField txtPrecio;     
    @FXML private ComboBox<String> cbTipo; 
    @FXML private Button btnGuardar;     

    // variables de control interno
    private Producto producto;       
    private boolean esEdicion = false; 
    private boolean guardadoExitoso = false; 

    /**
     * Prepara el formulario apenas se abre. 
     * Aquí defino las categorías que aparecerán en el ComboBox del menú.
     */
    @FXML
    public void initialize() {
        // Llenamos las opciones del ComboBox con las categorías del restaurante
        cbTipo.getItems().addAll("Plato fuerte", "Postre", "Entrada", "Bebida");
    }

    /**
     * Este es el método clave para la edición. 
     * Si le pasamos un producto que ya existe, el formulario se "transforma":
     * cambia el título, el texto del botón y rellena todos los campos con la 
     * información actual para que solo tengamos que modificar lo necesario.
     * * @param p El producto seleccionado de la tabla principal.
     */
    public void setProducto(Producto p) {
        this.producto = p;
        if (p != null) {
            esEdicion = true;
            lblTitulo.setText("Modificar producto");
            btnGuardar.setText("Actualizar producto");
            
            // Rellenamos los cuadros de texto con los datos que ya tenemos
            txtNombre.setText(p.getNombre());
            txtDescripcion.setText(p.getDescripcion());
            txtPrecio.setText(String.valueOf(p.getPrecio()));
            cbTipo.setValue(p.getTipo());
        }
    }

    /**
     * Se ejecuta cuando el usuario hace clic en el botón de Guardar.
     * Primero revisa que todo esté bien escrito. Si es un producto nuevo, lo crea;
     * si es uno viejo, actualiza sus datos actuales.
     */
    @FXML
    private void handleGuardar() {
        if (validarCampos()) {
            if (!esEdicion) {
                // Caso A: Creamos un producto nuevo desde cero
                producto = new Producto(
                    txtNombre.getText(),
                    cbTipo.getValue(),
                    Double.parseDouble(txtPrecio.getText()),
                    txtDescripcion.getText()
                );
            } else {
                // Caso B: Solo actualizamos la información del producto que ya teníamos
                producto.setNombre(txtNombre.getText());
                producto.setDescripcion(txtDescripcion.getText());
                producto.setPrecio(Double.parseDouble(txtPrecio.getText()));
                producto.setTipo(cbTipo.getValue());
            }
            guardadoExitoso = true;
            cerrarVentana();
        }
    }

    /**
     * Cierra el formulario sin realizar ningún cambio si el usuario se arrepiente.
     */
    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    /**
     * Método interno para cerrar la ventana actual (el Stage).
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }

    /**
     * Verifica que el usuario no haya dejado campos obligatorios vacíos 
     * y que haya escrito un número válido en el campo de precio.
     * * @return true si los datos son correctos para guardarse.
     */
    private boolean validarCampos() {
        try {
            // Intentamos convertir el texto del precio a número
            Double.parseDouble(txtPrecio.getText());
            // Verificamos que el nombre no esté vacío y se haya elegido un tipo
            return !txtNombre.getText().isEmpty() && cbTipo.getValue() != null;
        } catch (NumberFormatException e) {
            // Si el precio no es un número (ej. tiene letras), marcamos error
            return false; 
        }
    }

    /**
     * Permite a la pantalla principal obtener el producto que se creó o modificó.
     */
    public Producto getProducto() { return producto; }

    /**
     * Indica si la operación terminó con éxito para poder refrescar las tablas.
     */
    public boolean isGuardadoExitoso() { return guardadoExitoso; }
}