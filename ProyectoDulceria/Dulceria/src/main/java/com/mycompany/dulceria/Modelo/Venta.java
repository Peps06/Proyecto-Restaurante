/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dulceria.Modelo;

import java.util.*;

/**
 * @class Venta
 * Representa una venta completa en el sistema. 
 * Contiene información(ID y fecha) y almacena los detalles de los productos 
 * vendidos (líneas de venta) finalizando con el monto total.
 *
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class Venta {
    
    /**
     * Identificador único de la de venta.
     */
    private String idVenta;
    
    /**
     * Fecha en la que se realizó la venta.
     */
    private String fecha;
    
    /**
     * Lista de nombres de los productos incluidos en la venta.
     */
    private List<String> nombres;
    
    /**
     * Lista de identificadores (IDs) de los productos vendidos.
     */
    private ArrayList<String> ids;
    
    /**
     * Lista de las cantidades vendidas para cada producto.
     */
    private ArrayList<String> cantidades;
    
    /**
     * Lista de los subtotales calculados para cada línea de producto.
     */
    private ArrayList<String> subtotales;
    
    /**
     * Monto total final de la venta.
     */
    private String total;

    /**
     * Constructor para iniciar una nueva venta. Inicializa la cabecera y prepara las listas 
     * para agregar productos.
     *
     * @param idVenta El ID único para esta venta.
     * @param fecha La fecha de la transacción.
     */
    public Venta(String idVenta, String fecha) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.nombres = new ArrayList<>();
        this.ids = new ArrayList<>();
        this.cantidades = new ArrayList<>();
        this.subtotales = new ArrayList<>();
        this.total = "0";
    }

    /**
     * Agrega un nuevo producto (línea de venta) a la transacción. 
     * Los atributos del producto se añaden a sus respectivas listas paralelas.
     *
     * @param idProd El ID del producto.
     * @param nombre El nombre del producto.
     * @param cant La cantidad vendida.
     * @param subtotal El subtotal de la línea.
     */
    public void agregarProducto(String idProd, String nombre, String cant, String subtotal){
        ids.add(idProd);
        nombres.add(nombre);
        cantidades.add(cant);
        subtotales.add(subtotal);
    }

    /**
     * Obtiene el identificador de la venta.
     * @return El ID de la venta.
     */
    public String getIdVenta(){
        return idVenta;
    }
    
    /**
     * Obtiene la fecha de la venta.
     * @return La fecha de la transacción.
     */
    public String getFecha(){
        return fecha;
    }
    
    /**
     * Obtiene la lista de nombres de productos vendidos.
     * @return La lista de nombres.
     */
    public List<String> getNombres(){
        return nombres;
    }
    
    /**
     * Obtiene la lista de IDs de productos vendidos.
     * @return La lista de IDs.
     */
    public ArrayList<String> getIds(){
        return ids;
    }
    
    /**
     * Obtiene la lista de cantidades vendidas por producto.
     * @return La lista de cantidades.
     */
    public ArrayList<String> getCantidades(){
        return cantidades;
    }
    
    /**
     * Obtiene la lista de subtotales por cada línea de venta.
     * @return La lista de subtotales.
     */
    public ArrayList<String> getSubtotales(){
        return subtotales;
    }
    
    /**
     * Obtiene el monto total final de la venta.
     * @return El total de la venta como String.
     */
    public String getTotal(){
        return total;
    }

    /**
     * Establece o actualiza el monto total de la venta.
     * @param total El nuevo monto total.
     */
    public void setTotal(String total) {
        this.total = total;
    }

    /**
     * Devuelve una representación en cadena del objeto {@code Venta}.
     * El formato está diseñado para ser utilizado específicamente por la capa DAO 
     * para la escritura en archivos de persistencia, incluyendo la cabecera, 
     * los detalles de las líneas de producto y el total.
     *
     * @return Una cadena formateada que representa la venta completa.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(idVenta).append(" , ")
          .append(fecha).append(" , ")
          .append(ids.size());

        sb.append("\n");

      
        for (int i = 0; i < ids.size(); i++) {
            sb.append("> ")
              .append(ids.get(i)).append("|")
              .append(nombres.get(i)).append("|")
              .append(cantidades.get(i)).append("|")
              .append(subtotales.get(i))
              .append("\n");
        }

     
        sb.append("TOTAL=").append(total);

        return sb.toString();
    }

}