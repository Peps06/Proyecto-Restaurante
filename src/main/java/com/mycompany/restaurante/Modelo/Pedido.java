/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Modelo;

/**
 *
 * @author dana0
 */

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private int idOrden;
    private int idMesa;
    private int idEmpleado;
    private String fechaHora;
    private String estado;
    private String notas; 
    private List<Producto> productos; 

    public Pedido(int idMesa, int idEmpleado, String notas) {
        this.idMesa = idMesa;
        this.idEmpleado = idEmpleado;
        this.notas = notas;
        this.estado = "Abierta";
        this.productos = new ArrayList<>();
    }

    public void agregarProducto(Producto producto) {
        this.productos.add(producto);
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public void setIdOrden(int idOrden) { 
        this.idOrden = idOrden; 
    }
}
