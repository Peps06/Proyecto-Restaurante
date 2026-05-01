package com.mycompany.restaurante.Modelo;

/**
 * Representa un ítem (producto) individual dentro de una orden o pedido de mesa.
 * Contiene la información del producto, la cantidad solicitada y su precio unitario.
 *
 * 
 * @author Citlaly
 * @version 1.0
 */
public class OrdenItem {

    private String producto;
    private int cantidad;
    private double precio;

    /**
     * Constructor para crear una nueva instancia de un ítem de la orden.
     * 
     * @param producto Nombre del producto.
     * @param cantidad Cantidad de unidades.
     * @param precio   Precio unitario del producto.
     */
    public OrdenItem(String producto, int cantidad, double precio) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    /**
     * Obtiene el nombre del producto.
     * @return El nombre del producto.
     */
    public String getProducto() {
        return producto;
    }

    /**
     * Establece o modifica el nombre del producto.
     * @param producto Nuevo nombre del producto.
     */
    public void setProducto(String producto) {
        this.producto = producto;
    }

    /**
     * Obtiene la cantidad de unidades solicitadas.
     * @return Cantidad de productos.
     */
    public int getCantidad() {
        return cantidad;
    }

    /**
     * Establece o modifica la cantidad de productos.
     * @param cantidad Nueva cantidad de productos.
     */
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * Obtiene el precio unitario del producto.
     * @return Precio por unidad.
     */
    public double getPrecio() {
        return precio;
    }

    /**
     * Establece o modifica el precio unitario del producto.
     * @param precio Nuevo precio unitario.
     */
    public void setPrecio(double precio) {
        this.precio = precio;
    }    

    /** 
     * Calcula el subtotal del ítem multiplicando la cantidad por el precio unitario.
     * 
     * @return El costo total acumulado por este ítem específico.
     */
    public double getSubtotalItem() { return cantidad * precio; }
}