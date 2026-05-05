/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.dulceria.Modelo;

import java.io.IOException;
import java.util.*;

/**
 * @interface VentaDAO 
 * Define el contrato de acceso a datos (DAO) para la entidad {@code Venta}. 
 * Esta interfaz establece los métodos esenciales para la manipulación y consulta 
 * de las transacciones de ventas, asegurando que cualquier implementación 
 * cumpla con estas operaciones.
 *
 * @author Alejandra
 * @version 1.0
 */
public interface VentaDAO {
    
    /**
     * Registra una nueva venta en el sistema de persistencia.
     * Esta es la operación fundamental para guardar el detalle de una venta finalizada.
     * * @param v El objeto {@code Venta} a ser registrado.
     * @throws IOException Si ocurre un error durante la operación de escritura de datos.
     */
    void registrarVenta(Venta v) throws IOException;
    
    /**
     * Recupera la colección completa de todas las ventas registradas.
     * * @return Un {@code ArrayList} que contiene todos los objetos {@code Venta} del historial.
     */
    ArrayList<Venta> obtenerVentas();
    
    /**
     * Filtra y recupera todas las ventas que coinciden con una fecha específica.
     * * @param fecha La cadena de texto que representa la fecha utilizada como criterio de búsqueda.
     * @return Un {@code ArrayList} con las ventas realizadas en la fecha indicada.
     */
    ArrayList<Venta> ventasPorFecha(String fecha);
}