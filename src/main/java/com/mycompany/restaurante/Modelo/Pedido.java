package com.mycompany.restaurante.Modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa la entidad Pedido dentro del sistema Saveurs Paris.
 * Encargada de gestionar las comandas u órdenes del restaurante, vinculándolas
 * con una mesa, un empleado, su lista de productos solicitados y el estado de la orden.
 * * @author Dana
 * @version 1.0
 */
public class Pedido {
    private int idOrden;
    private int idMesa;
    private int idEmpleado;
    private String fechaHora;
    private String estado;
    private String notas; 
    private List<Producto> productos; 

    /**
     * Constructor para inicializar un nuevo pedido en el sistema.
     * Por defecto, establece el estado como "Abierta" y crea una lista vacía de productos.
     * * @param idMesa Identificador de la mesa que realiza el pedido.
     * @param idEmpleado Identificador del empleado (mesero) que atiende la mesa.
     * @param notas Comentarios u observaciones especiales (ej. "Sin cebolla").
     */
    public Pedido(int idMesa, int idEmpleado, String notas) {
        this.idMesa = idMesa;
        this.idEmpleado = idEmpleado;
        this.notas = notas;
        this.estado = "Abierta";
        this.productos = new ArrayList<>();
    }

    /**
     * Añade un producto de manera individual a la lista de consumo del pedido.
     * * @param producto Objeto Producto que se desea agregar a la comanda.
     */
    public void agregarProducto(Producto producto) {
        this.productos.add(producto);
    }

    /**
     * Calcula el monto total acumulado del pedido con base en los productos agregados.
     * * @return El costo total reflejado en formato numérico decimal.
     */
    public double calcularTotal() {
        double total = 0;
        for (Producto prod : productos) {
            // Si el producto maneja cantidad pedida se multiplica, de lo contrario suma el precio base
            int cantidad = prod.getCantidadPedida() > 0 ? prod.getCantidadPedida() : 1;
            total += prod.getPrecio() * cantidad;
        }
        return total;
    }

    // GETTERS Y SETTERS

    /** @return El identificador numérico único de la orden. */
    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) { 
        this.idOrden = idOrden; 
    }

    /** @return El identificador de la mesa asociada al pedido. */
    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    /** @return El identificador del empleado que registró la orden. */
    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    /** @return La fecha y hora exacta en la que se generó el pedido. */
    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    /** @return El estado operativo actual de la orden (ej. "Abierta", "Pagada"). */
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    /** @return Las especificaciones o notas adicionales del pedido. */
    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    /** @return La lista completa de productos incluidos en el pedido. */
    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    /**
     * Sobrescritura del método toString para facilitar el seguimiento visual
     * del pedido en la administración del sistema o depuración.
     * * @return Una cadena descriptiva de la orden.
     */
    @Override
    public String toString() {
        return "Pedido #" + idOrden + " (Mesa " + idMesa + ") - Estado: " + estado;
    }
}