package com.mycompany.restaurante.Modelo;

/**
 * @author Rubi
 */

public class Empleado {
    private int id;
    private String nombre;
    private String puesto;
    private String asistencia;
    private String telefono;

    // Constructor
    public Empleado(int id, String nombre, String puesto, String asistencia, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.puesto = puesto;
        this.asistencia = asistencia;
        this.telefono = telefono;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getPuesto() { return puesto; }
    public String getAsistencia() { return asistencia; }
    public String getTelefono() { return telefono; }
}