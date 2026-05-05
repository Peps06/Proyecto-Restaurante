package com.mycompany.dulceria.Modelo;

/**
 * @class Dulce 
 * Representa un producto dulce individual dentro del inventario.
 * Contiene información sobre el producto, incluyendo su identificación,
 * precios, marca, fecha de caducidad y la cantidad en stock.
 *
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class Dulce {
    /**
     * Identificador único del dulce (clave principal).
     */
    private String id;
    
    /**
     * Nombre descriptivo del dulce.
     */
    private String nombre;
    
    /**
     * Precio al que se compra el dulce al proveedor.
     * Almacenado como String.
     */
    private String precio_compra;
    
    /**
     * Precio al que se vende el dulce al cliente.
     * Almacenado como String.
     */
    private String precio_venta;
    
    /**
     * Marca o fabricante del dulce.
     */
    private String marca;
    
    /**
     * Fecha de caducidad del producto.
     * Almacenado como String.
     */
    private String fecha_caducidad;
    
    /**
     * Cantidad actual de unidades en inventario.
     * Almacenado como String.
     */
    private String cantidad;
    
    /**
     * Constructor para inicializar un nuevo objeto {@code Dulce} con todos sus atributos.
     *
     * @param id El identificador único del dulce.
     * @param nombre El nombre del dulce.
     * @param precio_compra El precio de compra del dulce.
     * @param precio_venta El precio de venta del dulce.
     * @param marca La marca del dulce.
     * @param fecha_caducidad La fecha de caducidad del dulce.
     * @param cantidad La cantidad inicial en inventario.
     */
    public Dulce(String id, String nombre, String precio_compra, String precio_venta, String marca, String fecha_caducidad, String cantidad){
        this.id = id;
        this.nombre = nombre;
        this.precio_compra = precio_compra;
        this.precio_venta = precio_venta;
        this.marca = marca;
        this.fecha_caducidad = fecha_caducidad;
        this.cantidad = cantidad;
    }

    /**
     * Obtiene el identificador único del dulce.
     * @return El ID del dulce.
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el nombre del dulce.
     * @return El nombre del dulce.
     */
    public String getNombre() {
        return nombre;
    }
    
    /**
     * Obtiene el precio de compra del dulce.
     * @return El precio de compra como String.
     */
    public String getPrecioCompra() {
        return precio_compra;
    }
    
    /**
     * Obtiene el precio de venta del dulce.
     * @return El precio de venta como String.
     */
    public String getPrecioVenta() {
        return precio_venta;
    }

    /**
     * Obtiene la marca del dulce.
     * @return La marca del dulce.
     */
    public String getMarca() {
        return marca;
    }

    /**
     * Obtiene la fecha de caducidad del dulce.
     * @return La fecha de caducidad como String.
     */
    public String getFecha_caducidad() {
        return fecha_caducidad;
    }

    /**
     * Obtiene la cantidad actual de unidades del dulce en inventario.
     * @return La cantidad como String.
     */
    public String getCantidad() {
        return cantidad;
    }

    

    /**
     * Establece o actualiza el identificador único del dulce.
     * @param id El nuevo ID del dulce.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Establece o actualiza el nombre del dulce.
     * @param nombre El nuevo nombre del dulce.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    /**
     * Establece o actualiza el precio de compra del dulce.
     * @param precio_compra El nuevo precio de compra como String.
     */
    public void setPrecioCompra(String precio_compra) {
        this.precio_compra = precio_compra;
    }
    
    /**
     * Establece o actualiza el precio de venta del dulce.
     * @param precio_venta El nuevo precio de venta como String.
     */
    public void setPrecioVenta(String precio_venta) {
        this.precio_venta = precio_venta;
    }

    /**
     * Establece o actualiza la marca del dulce.
     * @param marca La nueva marca del dulce.
     */
    public void setMarca(String marca) {
        this.marca = marca;
    }

    /**
     * Establece o actualiza la fecha de caducidad del dulce.
     * @param fecha_caducidad La nueva fecha de caducidad como String.
     */
    public void setFecha_caducidad(String fecha_caducidad) {
        this.fecha_caducidad = fecha_caducidad;
    }

    /**
     * Establece o actualiza la cantidad de unidades del dulce en inventario.
     * @param cantidad La nueva cantidad como String.
     */
    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }
}