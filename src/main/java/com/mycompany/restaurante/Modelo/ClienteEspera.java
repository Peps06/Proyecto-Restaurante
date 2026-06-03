package com.mycompany.restaurante.Modelo;

import java.time.LocalDateTime;

/**
 * Clase que representa la entidad ClienteEspera dentro del sistema Saveurs Paris.
 * Se utiliza para gestionar la lista de espera de los clientes que aguardan por 
 * la asignación de una mesa física en el restaurante.
 * * @author Dana
 * @version 1.0
 */
public class ClienteEspera {
    private int idEspera;
    private String nombreCliente;
    private String telefono;
    private int numeroPersonas;
    private LocalDateTime horaLlegada;
    private String estado;

    /**
     * Constructor vacío para inicializar una instancia de ClienteEspera sin valores previos.
     */
    public ClienteEspera() {}

    /**
     * Constructor completo para inicializar una instancia con todos los atributos de la base de datos.
     * * @param idEspera Identificador único del registro en la lista de espera.
     * @param nombreCliente Nombre del cliente que solicita la espera.
     * @param telefono Número telefónico de contacto del cliente.
     * @param numeroPersonas Cantidad de comensales en el grupo.
     * @param horaLlegada Fecha y hora exacta del registro de llegada.
     * @param estado Situación actual en la lista (ej. "Esperando", "Asignado", "Cancelado").
     */
    public ClienteEspera(int idEspera, String nombreCliente, String telefono, int numeroPersonas, LocalDateTime horaLlegada, String estado) {
        this.idEspera = idEspera;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.numeroPersonas = numeroPersonas;
        this.horaLlegada = horaLlegada;
        this.estado = estado;
    }

    /**
     * Constructor simplificado para el registro rápido de un nuevo cliente en la lista de espera.
     * Por defecto, define el estado inicial como "Esperando".
     * * @param nombreCliente Nombre del cliente titular.
     * @param telefono Número telefónico de contacto.
     * @param numeroPersonas Cantidad de comensales que integran el grupo.
     */
    public ClienteEspera(String nombreCliente, String telefono, int numeroPersonas) {
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.numeroPersonas = numeroPersonas;
        this.estado = "Esperando";
    }

    // GETTERS Y SETTERS

    /** @return El identificador numérico en la lista de espera. */
    public int getIdEspera() { 
        return idEspera; 
    }
    
    public void setIdEspera(int idEspera) { 
        this.idEspera = idEspera; 
    }
    
    /** @return El nombre del cliente en espera. */
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
    
    /** @return La cantidad de personas que componen el grupo del cliente. */
    public int getNumeroPersonas() { 
        return numeroPersonas; 
    }
    
    public void setNumeroPersonas(int numeroPersonas) { 
        this.numeroPersonas = numeroPersonas; 
    }
    
    /** @return La fecha y hora exactas en las que llegó el cliente. */
    public LocalDateTime getHoraLlegada() { 
        return horaLlegada; 
    }
    
    public void setHoraLlegada(LocalDateTime horaLlegada) { 
        this.horaLlegada = horaLlegada; 
    }
    
    /** @return El estado actual del cliente en la lista (ej. "Esperando"). */
    public String getEstado() { 
        return estado; 
    }
    
    public void setEstado(String estado) { 
        this.estado = estado; 
    }

    /**
     * Sobrescritura del método toString para facilitar la visualización del cliente
     * en componentes de interfaz gráfica como listas de espera dinámicas.
     * * @return Una cadena descriptiva con el nombre y tamaño del grupo en espera.
     */
    @Override
    public String toString() {
        return nombreCliente + " (" + numeroPersonas + " pers.) - " + estado;
    }
}