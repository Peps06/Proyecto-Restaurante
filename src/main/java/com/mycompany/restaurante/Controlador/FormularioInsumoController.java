package com.mycompany.restaurante.Controlador;

import com.mycompany.restaurante.Modelo.Insumo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Esta clase es el controlador del formulario para los insumos del almacén. 
 * @author Rubi
 * @version 2.0
 */
public class FormularioInsumoController {

    // Campos de texto y selección de la interfaz (FXML) 
    @FXML private TextField txtNombre; // Nombre del producto (ej. Jitomate)
    @FXML private TextField txtStock;  // Cuánto hay actualmente
    @FXML private ComboBox<String> cbCategoria; // Tipo de producto (Carnes, Lácteos, etc.)
    @FXML private ComboBox<String> cbUnidad;    // Cómo se mide (kg, litros, piezas)
    @FXML private Label lblTitulo;              // Título (Editar)

    // Variables de control
    private Insumo insumo; // El objeto donde guardamos la info del ingrediente
    private boolean guardadoExitoso = false; // Indica si se guardó bien en la base de datos
    private com.mycompany.restaurante.DAO.InsumoDAO insumoDAO = new com.mycompany.restaurante.DAO.InsumoDAO();

    /**
     * Prepara el formulario al abrirse.
     * Aquí configuro las categorías y las unidades de medida que aparecerán 
     * en los menús desplegables.
     */
    @FXML
    public void initialize() {
        // Llenamos las opciones de categorías
        cbCategoria.getItems().addAll("Abarrotes", "Lácteos", "Carnes", "Bebidas", "Vegetales");
        // Llenamos las opciones de unidades de medida
        cbUnidad.getItems().addAll("kg", "litros", "piezas", "gramos", "ml", "botellas");
    }

    /**
     * Este método decide si el formulario servirá para crear un insumo nuevo 
     * o para editar uno existente. Si le pasamos un insumo, rellena los 
     * campos automáticamente para que podamos modificarlos.
     * * @param i El insumo que queremos editar (o null si es uno nuevo).
     */
    public void setInsumo(Insumo i) {
        this.insumo = i;
        if (i != null) {
            lblTitulo.setText("Editar Insumo");
            txtNombre.setText(i.getNombre());
            txtStock.setText(String.valueOf(i.getStock()));
            cbUnidad.setValue(i.getUnidad()); 
            cbCategoria.setValue(i.getCategoria());
        } else {
            lblTitulo.setText("Nuevo Insumo");
        }
    }

    /**
     * Se ejecuta al hacer clic en el botón de Guardar.
     * Recoge los datos, calcula el estado del stock y decide si debe 
     * insertar un registro nuevo o actualizar uno que ya existía en la base de datos.
     */
    @FXML
    private void guardar() {
        if (validarCampos()) {
            try {
                String nombre = txtNombre.getText();
                double stock = Double.parseDouble(txtStock.getText());
                String unidad = cbUnidad.getValue(); 
                String cat = cbCategoria.getValue();
                
                // El estado (Agotado, Disponible, etc.) se calcula solito aquí
                String est = definirEstado(stock); 

                boolean exitoOperacion = false;

                if (insumo == null) {
                    // Si es nuevo, creamos el objeto y lo insertamos en la BD
                    insumo = new Insumo(0, nombre, cat, stock, unidad, est);
                    exitoOperacion = insumoDAO.insertar(insumo); 
                } else {
                    // Si ya existe, actualizamos sus datos actuales
                    insumo.setNombre(nombre);
                    insumo.setStock(stock);
                    insumo.setUnidad(unidad);
                    insumo.setCategoria(cat);
                    insumo.setEstado(est); 
                    exitoOperacion = insumoDAO.editar(insumo); 
                }

                if (exitoOperacion) {
                    guardadoExitoso = true;
                    cerrarVentana();
                } else {
                    mostrarAlerta("No se pudieron guardar los cambios en el servidor.");
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("La cantidad de stock debe ser un número válido.");
            }
        }
    }

    /**
     * Lógica de semáforo para el almacén:
     * Si hay 0 o menos: "Agotado".
     * Si hay entre 1 y 5: "Por agotarse" (alerta).
     * Si hay más de 5: "Disponible".
     * @param stock La cantidad actual.
     * @return Una palabra que describe el estado.
     */
    private String definirEstado(double stock) {
       if (stock <= 0) return "Agotado";
       if (stock <= 5) return "Por agotarse";
       return "Disponible";
    }

    /**
     * Método auxiliar para mostrar errores de conexión o de datos.
     */
    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Revisa que el usuario no haya dejado campos en blanco y que 
     * el stock sea un número. Si falta algo, muestra una lista de 
     * advertencias al usuario.
     * @return true si todo está correcto.
     */
    private boolean validarCampos() {
        String mensaje = "";
        if (txtNombre.getText().trim().isEmpty()) mensaje += "- El nombre es obligatorio.\n";
        if (cbCategoria.getValue() == null) mensaje += "- Seleccione una categoría.\n";
        if (cbUnidad.getValue() == null) mensaje += "- Seleccione una unidad.\n";
        
        try {
            if (txtStock.getText().isEmpty()) {
                mensaje += "- El stock no puede estar vacío.\n";
            } else {
                Double.parseDouble(txtStock.getText());
            }
        } catch (NumberFormatException e) {
            mensaje += "- El stock debe ser un número.\n";
        }

        if (mensaje.isEmpty()) return true;
        
        // Si hubo errores, mostramos esta alerta de advertencia
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Datos incompletos");
        alerta.setHeaderText("Por favor corrija lo siguiente:");
        alerta.setContentText(mensaje);
        alerta.showAndWait();
        return false;
    }

    /**
     * Cierra la ventanita del formulario sin hacer nada.
     */
    @FXML private void cancelar() { cerrarVentana(); }
    
    /**
     * Método interno para cerrar el Stage actual.
     */
    private void cerrarVentana() { ((Stage) txtNombre.getScene().getWindow()).close(); }
    
    /**
     * Devuelve el insumo procesado a la pantalla anterior.
     */
    public Insumo getInsumo() { return insumo; }
    
    /**
     * Confirma si la operación en la BD fue exitosa.
     */
    public boolean isGuardadoExitoso() { return guardadoExitoso; }
}