package com.mycompany.restaurante.Modelo;

/**
 * Representa un ítem (producto) dentro de una orden/pedido de mesa.
 * 
 * @author Citlaly
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

    public String getProducto()  { return producto; }
    public void   setProducto(String v) { this.producto = v; }

    public int getCantidad()  { return cantidad; }
    public void setCantidad(int v) { this.cantidad = v; }

    public double getPrecio() { return precio; }
    public void setPrecio(double v) { this.precio = v; }

    /** Subtotal del ítem: cantidad × precio unitario. */
    public double getSubtotalItem() { return cantidad * precio; }
}