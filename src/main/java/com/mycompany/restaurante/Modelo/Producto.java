package com.mycompany.restaurante.Modelo;

/**
 *
 * @author Dana
 */
public class Producto {

    private String nombre;
    private int id;
    private String descripcion;
    private int cantidadPedida;
    private double precio;  
    private String tipo;
    
    public Producto(String nombre, String descripcion, int cantidadPedida) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidadPedida = cantidadPedida;
    }
    public Producto(String nombre, String tipo, double precio, String descripcion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.precio = precio;
        this.descripcion = descripcion;
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

    public int getCantidadPedida() {
        return cantidadPedida;
    }

    public void setCantidadPedida(int cantidadPedida) {
        this.cantidadPedida = cantidadPedida;
    }     

    public void setId(int id){
        this.id = id;
    }
    
    public int getId(){
        return id;
    }
    
    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
