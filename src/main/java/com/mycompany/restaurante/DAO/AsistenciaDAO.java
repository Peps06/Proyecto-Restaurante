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
 * Esta clase es el "gestor de tiempo" de Saveurs Paris. 
 * Su trabajo es hablar con la base de datos para anotar a qué hora llegan los empleados, 
 * a qué hora se van y guardar todo ese historial para que no se pierda nada.
 * También nos ayuda a saber en qué estado se encuentra un empleado en el momento 
 * exacto (si ya llegó, si sigue trabajando o si ya se fue a descansar).
 * @author Rubi
 * @version 1.0
 */
public class AsistenciaDAO {

    /**
     * Anota la entrada de un empleado. 
     * Guarda el ID del trabajador, la fecha de hoy, la hora exacta en la que picó el botón
     * y le pone el estado de 'Presente'. También guarda el mes para que sea fácil filtrarlo después.
     * @param idEmpleado El número de identificación del trabajador.
     * @return true si se guardó la entrada, false si hubo algún problema.
     */
    public boolean registrarEntrada(int idEmpleado) {
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

    /**
     * Anota la salida de un empleado.
     * Este método busca la entrada que el empleado hizo HOY y le agrega la hora de salida.
     * Lo más importante es que cambia el estado de 'Presente' a 'Completado' para 
     * indicar que ya terminó su jornada.
     * @param idEmpleado El ID del trabajador que se va a retirar.
     * @return true si se encontró la entrada de hoy y se cerró correctamente.
     */
    public boolean registrarSalida(int idEmpleado) {
        // Buscamos el registro de hoy que todavía diga 'Presente' para cerrarlo
        String sql = "UPDATE asistencias SET horaSalida = CURRENT_TIME, estado = 'Completado' " +
                     "WHERE idEmpleado = ? AND fecha = CURRENT_DATE AND estado = 'Presente'";
    
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idEmpleado);
            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("Salida registrada con éxito para ID: " + idEmpleado);
                return true;
            } else {
                System.out.println("No se encontró una entrada abierta para hoy.");
                return false;
            }
            
        } catch (SQLException e) {
            System.out.println("Error al registrar salida: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca en los archivos del pasado todos los registros de un empleado en un mes específico.
     * Sirve para que el administrador pueda ver el historial completo y revisar faltas o retardos.
     * @param idEmpleado El trabajador que queremos investigar.
     * @param mes El número de mes (1 para enero, 2 para febrero, etc.).
     * @return Una lista con todas las fechas, entradas y salidas de ese mes.
     */
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

    /**
     * Este método le dice al sistema en qué situación está el empleado HOY mismo.
     * Es fundamental para que el programa sepa si debe mostrar el botón de "Entrada" 
     * o el de "Salida".
     * @param idEmpleado El ID del empleado a revisar.
     * @return "Ausente" si no ha llegado, "Presente" si ya entró, o "Completado" si ya se fue.
     */
    public String verificarEstadoHoy(int idEmpleado) {
        // Miramos el último registro de hoy para ver en qué se quedó
        String sql = "SELECT estado FROM asistencias WHERE idEmpleado = ? AND fecha = CURRENT_DATE ORDER BY idAsistencia DESC LIMIT 1";
        
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idEmpleado);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("estado"); 
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar estado: " + e.getMessage());
        }
        // Si no hay ningún registro hoy, significa que todavía no llega
        return "Ausente"; 
    }
}