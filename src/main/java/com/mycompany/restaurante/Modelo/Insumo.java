/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Modelo;

/**
 *
 * @author mrubi
 */


public class Insumo {
    private int id;
    private String nombre;
    private String categoria;
    private double stock;
    private String unidad;
    private String estado;

    // Constructor 
    public Insumo(int id, String nombre, String categoria, double stock, String unidad, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.stock = stock;
        this.unidad = unidad;
        this.estado = estado;
    }

    // Getters
    public int getId() { 
        return id; }
    public String getNombre() { 
        return nombre; }
    public String getCategoria() { 
        return categoria; }
    public double getStock() { 
        return stock; }
    public String getUnidad() { 
        return unidad; }
    public String getEstado() { 
        return estado; }

    // Setters 
    public void setId(int id) { 
        this.id = id; }
    public void setNombre(String nombre) { 
        this.nombre = nombre; }
    public void setCategoria(String categoria) { 
        this.categoria = categoria; }
    public void setStock(double stock) { 
        this.stock = stock; }
    public void setUnidad(String unidad) { 
        this.unidad = unidad; }
    public void setEstado(String estado) { 
        this.estado = estado; }
}