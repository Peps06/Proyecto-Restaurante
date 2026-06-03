/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.DAO;

/**
 *
 * @author mrubi
 */

import com.mycompany.restaurante.Modelo.HistorialOrden;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistorialDAO {
    private Connection conexion;

    public HistorialDAO(Connection conexion) {
        this.conexion = conexion;
    }

    // 1. Buscar todas las órdenes de una fecha específica (calculando el total acumulado)
    public List<HistorialOrden> obtenerHistorialPorFechaYTurno(LocalDate fecha, String turno) {
    List<HistorialOrden> lista = new ArrayList<>();
    
    // Base de la consulta SQL
    String sql = "SELECT o.idOrden, o.idMesa, e.nombre AS mesero, o.fechaHora, o.estado, " +
                 "IFNULL(SUM(do.cantidad * do.precioUnit), 0) AS totalOrden " +
                 "FROM ordenes o " +
                 "JOIN empleados e ON o.idEmpleado = e.idEmpleado " +
                 "LEFT JOIN detalle_orden do ON o.idOrden = do.idOrden " +
                 "WHERE DATE(o.fechaHora) = ? ";

    // Añadimos el filtro de hora según el turno seleccionado
    if (turno.equalsIgnoreCase("Matutino")) {
        sql += "AND TIME(o.fechaHora) BETWEEN '06:00:00' AND '14:00:00' ";
    } else if (turno.equalsIgnoreCase("Vespertino")) {
        sql += "AND TIME(o.fechaHora) BETWEEN '15:00:00' AND '22:00:00' ";
    }

    sql += "GROUP BY o.idOrden, o.idMesa, e.nombre, o.fechaHora, o.estado " +
           "ORDER BY o.fechaHora DESC";

    try (PreparedStatement ps = conexion.prepareStatement(sql)) {
        ps.setDate(1, java.sql.Date.valueOf(fecha));
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HistorialOrden orden = new HistorialOrden(
                    rs.getInt("idOrden"),
                    rs.getInt("idMesa"),
                    rs.getString("mesero"),
                    rs.getTimestamp("fechaHora").toLocalDateTime(),
                    rs.getString("estado"),
                    rs.getDouble("totalOrden")
                );
                lista.add(orden);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error en obtenerHistorialPorFechaYTurno:");
        e.printStackTrace();
    }
    return lista;
}
}