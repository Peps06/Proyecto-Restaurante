package com.mycompany.restaurante.Modelo;

/**
 *
 * @author Dana
 */
public class Producto {

    private String nombre;
    private String descripcion;
    private String cantidad;
    
    public Producto(String nombre, String descripcion, String cantidad) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }    
}
