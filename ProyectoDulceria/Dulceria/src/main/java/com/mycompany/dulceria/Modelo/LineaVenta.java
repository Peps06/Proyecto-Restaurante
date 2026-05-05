package com.mycompany.dulceria.Modelo;

/**
 * @class LineaVenta 🏷️
 * Representa un artículo individual o un producto dentro de una transacción de venta.
 * Cada instancia de esta clase almacena los detalles de un producto específico 
 * vendido, incluyendo su identificación, precio unitario y la cantidad adquirida.
 * * Esta clase es fundamental para componer el detalle de una {@code Venta}.
 *
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class LineaVenta {
    /**
     * Identificador único del producto vendido.
     */
    private String idProducto;
    
    /**
     * Nombre descriptivo del producto.
     */
    private String nombre;
    
    /**
     * Precio individual al que se vendió el producto. Utiliza el tipo {@code double} 
     * para manejar valores monetarios.
     */
    private double precioUnitario;
    
    /**
     * Cantidad de unidades del producto que fueron incluidas en esta línea de la venta.
     */
    private int cantidad;

    /**
     * Constructor para inicializar una nueva línea de venta con todos sus detalles.
     * * @param idProducto El ID del producto.
     * @param nombre El nombre del producto.
     * @param precioUnitario El precio unitario de venta.
     * @param cantidad La cantidad de unidades vendidas.
     */
    public LineaVenta(String idProducto, String nombre, double precioUnitario, int cantidad) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    /**
     * Obtiene el identificador del producto.
     * * @return El ID del producto.
     */
    public String getIdProducto(){
        return idProducto;
    }
    
    /**
     * Obtiene el nombre del producto.
     * * @return El nombre del producto.
     */
    public String getNombre(){
        return nombre;
    }
    
    /**
     * Obtiene el precio unitario del producto.
     * * @return El precio de una sola unidad.
     */
    public double getPrecioUnitario(){
        return precioUnitario;
    }
    
    /**
     * Obtiene la cantidad de unidades vendidas en esta línea.
     * * @return La cantidad de unidades.
     */
    public int getCantidad(){
        return cantidad;
    }
    
    /**
     * Calcula y devuelve el subtotal de esta línea de venta.
     * El subtotal es el resultado de multiplicar el precio unitario por la cantidad.
     * * @return El valor total de esta línea de venta (precioUnitario * cantidad).
     */
    public double getSubtotal() {
        return this.precioUnitario * this.cantidad;
    }
}