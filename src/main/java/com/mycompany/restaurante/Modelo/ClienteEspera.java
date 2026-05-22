package com.mycompany.restaurante.Modelo;

import java.time.LocalDateTime;

/**
 *
 * @author Dana
 */

public class ClienteEspera {
    private int idEspera;
    private String nombreCliente;
    private String telefono;
    private int numeroPersonas;
    private LocalDateTime horaLlegada;
    private String estado;

    public ClienteEspera() {}

    public ClienteEspera(int idEspera, String nombreCliente, String telefono, int numeroPersonas, LocalDateTime horaLlegada, String estado) {
        this.idEspera = idEspera;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.numeroPersonas = numeroPersonas;
        this.horaLlegada = horaLlegada;
        this.estado = estado;
    }

    public ClienteEspera(String nombreCliente, String telefono, int numeroPersonas) {
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.numeroPersonas = numeroPersonas;
        this.estado = "Esperando";
    }

    public int getIdEspera() { 
        return idEspera; 
    }
    
    public void setIdEspera(int idEspera) { 
        this.idEspera = idEspera; 
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
    
    public int getNumeroPersonas() { 
        return numeroPersonas; 
    }
    
    public void setNumeroPersonas(int numeroPersonas) { 
        this.numeroPersonas = numeroPersonas; 
    }
    
    public LocalDateTime getHoraLlegada() { 
        return horaLlegada; 
    }
    
    public void setHoraLlegada(LocalDateTime horaLlegada) { 
        this.horaLlegada = horaLlegada; 
    }
    
    public String getEstado() { 
        return estado; 
    }
    
    public void setEstado(String estado) { 
        this.estado = estado; 
    }
}