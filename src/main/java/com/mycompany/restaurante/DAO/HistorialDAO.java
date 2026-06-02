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
    public List<HistorialOrden> obtenerHistorialPorFecha(LocalDate fecha) {
        List<HistorialOrden> lista = new ArrayList<>();
        String sql = "SELECT o.idOrden, o.idMesa, e.nombre AS mesero, o.fechaHora, o.estado, " +
                     "IFNULL(SUM(do.cantidad * do.precioUnit), 0) AS totalOrden " +
                     "FROM ordenes o " +
                     "JOIN empleados e ON o.idEmpleado = e.idEmpleado " +
                     "LEFT JOIN detalle_orden do ON o.idOrden = do.idOrden " +
                     "WHERE DATE(o.fechaHora) = ? " +
                     "GROUP BY o.idOrden, o.idMesa, e.nombre, o.fechaHora, o.estado " +
                     "ORDER BY o.fechaHora DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 2. Buscar una orden específica por su ID único
    public HistorialOrden obtenerOrdenPorId(int idOrden) {
        String sql = "SELECT o.idOrden, o.idMesa, e.nombre AS mesero, o.fechaHora, o.estado, " +
                     "IFNULL(SUM(do.cantidad * do.precioUnit), 0) AS totalOrden " +
                     "FROM ordenes o " +
                     "JOIN empleados e ON o.idEmpleado = e.idEmpleado " +
                     "LEFT JOIN detalle_orden do ON o.idOrden = do.idOrden " +
                     "WHERE o.idOrden = ? " +
                     "GROUP BY o.idOrden, o.idMesa, e.nombre, o.fechaHora, o.estado";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new HistorialOrden(
                    rs.getInt("idOrden"),
                    rs.getInt("idMesa"),
                    rs.getString("mesero"),
                    rs.getTimestamp("fechaHora").toLocalDateTime(),
                    rs.getString("estado"),
                    rs.getDouble("totalOrden")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}