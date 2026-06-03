package com.mycompany.restaurante.Modelo;

import java.time.LocalDate;

/**
 * Clase que representa la entidad Reservacion dentro del sistema Saveurs Paris.
 * Se encarga de gestionar la información de las reservas realizadas por los clientes,
 * vinculándolas con una fecha, hora y mesa específica.
 * * @author dana0
 * @version 1.0
 */
public class Reservacion {
    
    private int id;
    private String nombreCliente;
    private String telefono;
    private LocalDate fecha;
    private String hora; 
    private int numeroPersonas;
    private int idMesa;
    private String estado;

    /**
     * Constructor vacío para inicializar una instancia de Reservacion sin valores previos.
     */
    public Reservacion() {}

    /**
     * Constructor completo para inicializar una instancia de Reservacion con todos sus atributos.
     * * @param id Identificador único de la reservación.
     * @param nombreCliente Nombre completo del cliente que realiza la reserva.
     * @param telefono Número de contacto del cliente.
     * @param fecha Fecha agendada para la reservación.
     * @param hora Hora de la reservación en formato de texto 
     * @param numeroPersonas Cantidad de comensales que asistirán.
     * @param idMesa Identificador de la mesa asignada a la reservación.
     * @param estado Situación actual de la reserva 
     */
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

    // GETTERS Y SETTERS

    /** @return El identificador numérico de la reservación. */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /** @return El nombre del cliente titular de la reserva. */
    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    /** @return El teléfono de contacto del cliente. */
    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /** @return La fecha programada para la reserva. */
    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /** @return La hora programada en formato String. */
    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    /** @return El número de personas registradas para la reserva. */
    public int getNumeroPersonas() {
        return numeroPersonas;
    }

    public void setNumeroPersonas(int numeroPersonas) {
        this.numeroPersonas = numeroPersonas;
    }

    /** @return El identificador de la mesa asociada. */
    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    /** @return El estado actual de la reserva (ej. "Confirmada"). */
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Sobrescritura del método toString para facilitar la depuración 
     * y visualización de la información básica de la reserva.
     * * @return Una cadena descriptiva de la reservación.
     */
    @Override
    public String toString() {
        return "Reserva #" + id + " - " + nombreCliente + " (Mesa " + idMesa + ") [" + estado + "]";
    }
}