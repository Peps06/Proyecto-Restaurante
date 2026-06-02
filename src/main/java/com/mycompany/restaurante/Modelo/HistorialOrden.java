/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.Modelo;

/**
 *
 * @author mrubi
 */

import java.time.LocalDateTime;

public class HistorialOrden {
    private int idOrden;
    private int idMesa;
    private String mesero;
    private LocalDateTime fechaHora;
    private String estado;
    private double total;

    // Constructor Completo
    public HistorialOrden(int idOrden, int idMesa, String mesero, LocalDateTime fechaHora, String estado, double total) {
        this.idOrden = idOrden;
        this.idMesa = idMesa;
        this.mesero = mesero;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.total = total;
    }

    // Getters y Setters
    public int getIdOrden() {
        return idOrden; }
    
    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden; }

    public int getIdMesa() {
        return idMesa; }
    
    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa; }

    public String getMesero() {
        return mesero; }
    
    public void setMesero(String mesero) {
        this.mesero = mesero; }

    public LocalDateTime getFechaHora() {
        return fechaHora; }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora; }

    public String getEstado() {
        return estado; }
    
    public void setEstado(String estado) {
        this.estado = estado; }

    public double getTotal() {
        return total; }
    
    public void setTotal(double total) {
        this.total = total; }
}
