package com.mycompany.restaurante.Modelo;

/**
 * @author Rubi
 */

public class Empleado {
    private int id;
    private String nombre;
    private String password;
    private String puesto;
    private String asistencia;
    private String telefono;

    // Constructor
    public Empleado(int id, String nombre, String password, String puesto, String asistencia, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.password = password;
        this.puesto = puesto;
        this.asistencia = asistencia;
        this.telefono = telefono;
    }

    // Getters
    public int getId() { 
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getPassword() {
        return password;
    }
    public String getPuesto() {
        return puesto;
    }
    public String getAsistencia() {
        return asistencia;
    }
    public String getTelefono() {
        return telefono;
    }
    
    // Setters 
    public void setId(int id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setPuesto(String puesto) {
        this.puesto = puesto; 
    }
    public void setAsistencia(String asistencia) {
        this.asistencia = asistencia; 
    }
    public void setTelefono(String telefono) { 
        this.telefono = telefono;
    }
}