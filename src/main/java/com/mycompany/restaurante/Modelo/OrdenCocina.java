package com.mycompany.restaurante.Modelo;

import java.util.List;

/**
 * Representa una orden abierta vista desde la perspectiva de cocina.
 * Agrupa el encabezado de la orden (mesa, id, detalles) con la lista
 * de ítems (cantidad + nombre del plato) provenientes de detalle_orden.
 *
 * @author Citlaly
 * @version 1.0
 */

public class OrdenCocina {
    private int idOrden;
    private int idMesa;
    private String detalles; // notas escritas por el mesero (puede ser null)
    private String preparacion; // 'En espera' | 'Preparado' | 'Entregado'
 
    //  Ítems del pedido
    private List<OrdenItem> items; // cada ítem tiene nombre + cantidad + precio
 
    // Constructor
    public OrdenCocina(int idOrden, int idMesa, String detalles,
                       String preparacion, List<OrdenItem> items) {
        this.idOrden = idOrden;
        this.idMesa = idMesa;
        this.detalles = detalles;
        this.preparacion = preparacion;
        this.items = items;
    }

    // Getters
    
    /** @return El ID de la orden (usado como "#ord-XXX" en la UI). */
    public int getIdOrden() {
        return idOrden;
    }
 
    /** @return El número de mesa (usado como "Mesa X" en la UI). */
    public int getIdMesa() {
        return idMesa; 
    }
 
    /**
     * @return Las notas especiales del mesero, o cadena vacía si no hay.
     *         Nunca devuelve null para simplificar el manejo en la UI.
     */
    public String getDetalles() {
        return detalles != null ? detalles : "";
    }
 
    /** @return El estado de preparación de la orden ('En espera', etc.). */
    public String getPreparacion() {
        return preparacion;
    }
 
    /** @return La lista de ítems (platos) asociados a esta orden. */
    public List<OrdenItem> getItems() {
        return items;
    }
 
    // Setters necesarios
    public void setPreparacion(String preparacion) {
        this.preparacion = preparacion;
    }

}
