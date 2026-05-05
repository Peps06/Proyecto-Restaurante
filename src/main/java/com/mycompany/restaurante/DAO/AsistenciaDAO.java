/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.Asistencia;
import com.mycompany.restaurante.Modelo.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author mrubi
 */
public class AsistenciaDAO {
    // Registrar Entrada 
    public boolean registrarEntrada(int idEmpleado) {
        // Usamos MONTH(CURRENT_DATE) para guardar automáticamente el mes del filtro
        String sql = "INSERT INTO asistencias (idEmpleado, fecha, horaEntrada, estado, mes_registro) " +
                     "VALUES (?, CURRENT_DATE, CURRENT_TIME, 'Presente', MONTH(CURRENT_DATE))";
        
        try (Connection con = ConexionDB.getConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idEmpleado);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al registrar entrada: " + e.getMessage());
            return false;
        }
    }

    // Registrar Salida 
    public boolean registrarSalida(int idEmpleado) {
        String sql = "UPDATE asistencias SET horaSalida = CURRENT_TIME, estado = 'Completado' " +
                     "WHERE idEmpleado = ? AND fecha = CURRENT_DATE AND horaSalida IS NULL";
        
        try (Connection con = ConexionDB.getConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idEmpleado);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al registrar salida: " + e.getMessage());
            return false;
        }
    }

    // Obtener Historial Filtrado por Mes
    public ObservableList<Asistencia> obtenerHistorialPorMes(int idEmpleado, int mes) {
        ObservableList<Asistencia> lista = FXCollections.observableArrayList();
        String sql = "SELECT fecha, horaEntrada, horaSalida, estado FROM asistencias " +
                     "WHERE idEmpleado = ? AND mes_registro = ? ORDER BY fecha DESC";
        
        try (Connection con = ConexionDB.getConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idEmpleado);
            ps.setInt(2, mes);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                lista.add(new Asistencia(
                    rs.getString("fecha"),
                    rs.getString("horaEntrada"),
                    rs.getString("horaSalida"),
                    rs.getString("estado")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener historial: " + e.getMessage());
        }
        return lista;
    }

    // Verificar si ya registró entrada hoy 
    public String verificarEstadoHoy(int idEmpleado) {
        String sql = "SELECT estado FROM asistencias WHERE idEmpleado = ? AND fecha = CURRENT_DATE";
        
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idEmpleado);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("estado"); // Retorna "Presente" o "Completado"
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar estado: " + e.getMessage());
        }
        return "Ausente"; // Si no hay registro hoy
    }
}
