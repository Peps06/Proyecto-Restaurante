package com.mycompany.restaurante.Modelo;

/**
 * Representa un ítem (producto) dentro de una orden/pedido de mesa.
 * 
 * @author Citlaly
 * @version 1
 */
public class OrdenItem {

    private String producto;
    private int cantidad;
    private double precio;

    public OrdenItem(String producto, int cantidad, double precio) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }    

    /** Subtotal del ítem: cantidad × precio unitario. */
    public double getSubtotalItem() { return cantidad * precio; }
}