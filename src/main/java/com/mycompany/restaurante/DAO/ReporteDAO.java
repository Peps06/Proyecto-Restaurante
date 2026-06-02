/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.DAO;

/**
 *
 * @author mrubi
 */


import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.ReporteVenta;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {
    private Connection conexion;

    public ReporteDAO() {
        try {
            // Llamamos al método estático directamente usando el nombre de la clase
            this.conexion = ConexionDB.getConexion();
        } catch (SQLException e) {
            System.out.println("Error al obtener la conexión en ReporteDAO");
            e.printStackTrace();
        }
    }

    // 1. Obtener reporte DIARIO (Usando la estructura real de tu BD)
    public List<ReporteVenta> obtenerReporteDiario(LocalDate fechaBusqueda) {
        List<ReporteVenta> lista = new ArrayList<>();
        
        // Consulta adaptada a tus tablas: ordenes, detalle_orden y productos
        String sql = "SELECT o.idOrden, o.fechaHora AS fecha, p.nombre AS producto, do.cantidad, (do.cantidad * do.precioUnit) AS total " +
                     "FROM ordenes o " +
                     "JOIN detalle_orden do ON o.idOrden = do.idOrden " +
                     "JOIN productos p ON do.idProducto = p.idProductos " +
                     "WHERE DATE(o.fechaHora) = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            // Pasamos la fecha seleccionada convirtiéndola al tipo de fecha de SQL
            ps.setDate(1, java.sql.Date.valueOf(fechaBusqueda));
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                ReporteVenta reporte = new ReporteVenta(
                    rs.getInt("idOrden"),
                    rs.getTimestamp("fecha").toLocalDateTime().toLocalDate(), // Convierte DATETIME a LocalDate
                    rs.getString("producto"),
                    rs.getInt("cantidad"),
                    rs.getDouble("total")
                );
                lista.add(reporte);
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerReporteDiario de ReporteDAO:");
            e.printStackTrace();
        }
        return lista;
    }

    // 2. Obtener reporte MENSUAL (Usando la estructura real de tu BD)
    public List<ReporteVenta> obtenerReporteMensual(int anio, int mes) {
        List<ReporteVenta> lista = new ArrayList<>();
        
        // Consulta usando las funciones YEAR() y MONTH() sobre tu columna fechaHora
        String sql = "SELECT o.idOrden, o.fechaHora AS fecha, p.nombre AS producto, do.cantidad, (do.cantidad * do.precioUnit) AS total " +
                     "FROM ordenes o " +
                     "JOIN detalle_orden do ON o.idOrden = do.idOrden " +
                     "JOIN productos p ON do.idProducto = p.idProductos " +
                     "WHERE YEAR(o.fechaHora) = ? AND MONTH(o.fechaHora) = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, anio);
            ps.setInt(2, mes);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                ReporteVenta reporte = new ReporteVenta(
                    rs.getInt("idOrden"),
                    rs.getTimestamp("fecha").toLocalDateTime().toLocalDate(), // Convierte DATETIME a LocalDate
                    rs.getString("producto"),
                    rs.getInt("cantidad"),
                    rs.getDouble("total")
                );
                lista.add(reporte);
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerReporteMensual de ReporteDAO:");
            e.printStackTrace();
        }
        return lista;
    }
}
