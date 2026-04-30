/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dulceria.Modelo;

/**
 * @class Usuario
 * Clase que representa una entidad de usuario dentro del sistema. 
 * Contiene las credenciales e información básica necesaria para la identificación 
 * y gestión de accesos, como empleados o administradores.
 *
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class Usuario {
    /**
     * Identificador único del usuario, utilizado como clave principal para el acceso.
     */
    private String id;
    
    /**
     * Nombre completo del usuario.
     */
    private String nombre;
    
    /**
     * Contraseña del usuario.
     */
    private String password;
    
    /**
     * Define el nivel de acceso o rol del usuario (e.g., "Administrador" o "Empleado").
     */
    private String tipo;

    /**
     * Constructor vacío por defecto.
     */
    public Usuario() {}

    /**
     * Constructor que inicializa un nuevo objeto {@code Usuario} con todos sus atributos.
     *
     * @param id El identificador único del usuario.
     * @param nombre El nombre completo del usuario.
     * @param password La contraseña para el acceso.
     * @param tipo El tipo de rol o acceso del usuario (Administrador/Empleado).
     */
    public Usuario(String id, String nombre, String password, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.password = password;
        this.tipo = tipo;
    }

    /**
     * Obtiene el identificador del usuario.
     * @return El ID del usuario.
     */
    public String getId() {
        return id;
    }

    /**
     * Establece el identificador del usuario.
     * @param id El nuevo ID del usuario.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre completo del usuario.
     * @return El nombre del usuario.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre completo del usuario.
     * @param nombre El nuevo nombre del usuario.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la contraseña del usuario.
     * @return La contraseña del usuario.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña del usuario.
     * @param password La nueva contraseña del usuario.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtiene el tipo o rol de acceso del usuario.
     * @return El tipo (e.g., "Administrador", "Empleado").
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Establece el tipo o rol de acceso del usuario.
     * @param tipo El nuevo tipo o rol.
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * Devuelve una representación en cadena del objeto {@code Usuario}.
     * El formato está diseñado para ser utilizado fácilmente en operaciones de 
     * lectura y escritura en archivos de texto (persistencia).
     *
     * @return Una cadena con los atributos separados por " , ".
     */
    @Override
    public String toString() {
        return id + " , " + nombre + " , " + password + " , " + tipo;
    }
}