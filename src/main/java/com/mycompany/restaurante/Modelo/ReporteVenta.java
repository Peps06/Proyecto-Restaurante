/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Modelo;

/**
 *
 * @author mrubi
 */


import java.time.LocalDate;

public class ReporteVenta {
    private int idPedido;
    private LocalDate fecha;
    private String producto;
    private int cantidad;
    private double total;

    // Constructor vacío
    public ReporteVenta() {}

    // Constructor lleno
    public ReporteVenta(int idPedido, LocalDate fecha, String producto, int cantidad, double total) {
        this.idPedido = idPedido;
        this.fecha = fecha;
        this.producto = producto;
        this.cantidad = cantidad;
        this.total = total;
    }

    // Getters y Setters
    public int getIdPedido() {
        return idPedido; }
    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido; }

    public LocalDate getFecha() {
        return fecha; }
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha; }

    public String getProducto() {
        return producto; }
    public void setProducto(String producto) {
        this.producto = producto; }

    public int getCantidad() {
        return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad; }

    public double getTotal() {
        return total; }
    public void setTotal(double total) {
        this.total = total; }
}