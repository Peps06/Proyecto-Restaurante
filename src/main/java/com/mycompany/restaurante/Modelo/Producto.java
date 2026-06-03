package com.mycompany.restaurante.Modelo;

/**
 * Clase que representa la entidad Producto dentro del sistema Saveurs Paris.
 * Se utiliza para gestionar la información de los alimentos, bebidas y artículos
 * del menú, controlando sus precios, tipos, descripciones y cantidades en pedidos.
 * 
 * @author Dana
 * @version 1.0
 */
public class Producto {

    private String nombre;
    private int id;
    private String descripcion;
    private int cantidadPedida;
    private double precio;  
    private String tipo;
    private String imagen;
    
    /**
     * Constructor sobrecargado para inicializar un producto enfocado en la gestión de pedidos.
     * 
     * @param nombre Nombre del producto.
     * @param descripcion Detalle o descripción del producto.
     * @param cantidadPedida Número de unidades solicitadas en el pedido.
     */
    public Producto(String nombre, String descripcion, int cantidadPedida) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidadPedida = cantidadPedida;
    }

    /**
     * Constructor completo para inicializar un producto con su información base del menú.
     * 
     * @param nombre Nombre comercial del producto.
     * @param tipo Categoría del producto (ej. "Entrada", "Bebida", "Postre").
     * @param precio Costo unitario del producto.
     * @param descripcion Detalle o ingredientes del producto.
     */
    public Producto(String nombre, String tipo, double precio, String descripcion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.precio = precio;
        this.descripcion = descripcion;
    }

    // GETTERS Y SETTERS

    /** @return El identificador numérico único del producto. */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /** @return El nombre del producto. */
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** @return La descripción o detalles del producto. */
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String description) {
        this.descripcion = description;
    }

    /** @return La cantidad de unidades que han sido solicitadas. */
    public int getCantidadPedida() {
        return cantidadPedida;
    }

    public void setCantidadPedida(int cantidadPedida) {
        this.cantidadPedida = cantidadPedida;
    }     

    /** @return El precio unitario del producto. */
    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    /** @return El tipo o categoría a la que pertenece el producto. */
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    /** @return La imagen del producto. */
    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    /**
     * Sobrescritura del método toString para facilitar la depuración 
     * y la correcta visualización del nombre en componentes de la interfaz gráfica.
     * * @return El nombre del producto.
     */
    @Override
    public String toString() {
        return nombre;
    }
}