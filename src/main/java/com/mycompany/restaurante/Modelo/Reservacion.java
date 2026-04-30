package com.mycompany.restaurante.Modelo;

/**
 *
 * @author dana0
 */

import java.time.LocalDate;

public class Reservacion {
    private int id;
    private String nombreCliente;
    private String telefono;
    private LocalDate fecha;
    private String hora; // Usamos String para que sea fácil vincularlo en la tabla (ej. "14:30")
    private int numeroPersonas;
    private int idMesa;
    private String estado;

    public Reservacion() {}

    public Reservacion(int id, String nombreCliente, String telefono, LocalDate fecha, String hora, int numeroPersonas, int idMesa, String estado) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.fecha = fecha;
        this.hora = hora;
        this.numeroPersonas = numeroPersonas;
        this.idMesa = idMesa;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public int getNumeroPersonas() {
        return numeroPersonas;
    }

    public void setNumeroPersonas(int numeroPersonas) {
        this.numeroPersonas = numeroPersonas;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
