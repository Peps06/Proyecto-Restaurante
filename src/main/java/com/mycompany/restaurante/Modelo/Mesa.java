package com.mycompany.restaurante.Modelo;

/**
 * Clase que representa la entidad Mesa dentro del sistema Saveurs Paris.
 * Se utiliza para gestionar la información de capacidad y disponibilidad 
 * de los espacios físicos del restaurante.
 * 
 * @author Citlaly
 * @version 1.1
 */
public class Mesa {
    
    private int idMesa;
    private int capacidad;
    private String estado; // Libre, ocupada o reserevada

    /**
     * Constructor completo para inicializar una instancia de Mesa.
     * 
     * @param idMesa Identificador único de la mesa en la base de datos.
     * @param capacidad Número máximo de comensales permitidos.
     * @param estado Situación actual de la mesa.
     */
    public Mesa(int idMesa, int capacidad, String estado) {
        this.idMesa = idMesa;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    // GETTERS Y SETTERS

    /** @return El identificador numérico de la mesa. */
    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    /** @return La cantidad de personas que pueden ocupar la mesa. */
    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    /** @return El estado operativo actual (ej. "Libre"). */
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Sobrescritura del método toString para facilitar la depuración 
     * y visualización en componentes de JavaFX como ComboBoxes.
     * 
     * @return Una cadena descriptiva de la mesa.
     */
    @Override
    public String toString() {
        return "Mesa #" + idMesa + " (" + capacidad + " pers.) - " + estado;
    }
}