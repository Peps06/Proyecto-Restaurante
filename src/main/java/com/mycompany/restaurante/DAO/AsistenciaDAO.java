package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.Asistencia;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime; // Para la detección automática del turno
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Esta clase es el "gestor de tiempo" de Saveurs Paris. 
 * Su trabajo es hablar con la base de datos para anotar a qué hora llegan los empleados, 
 * a qué hora se van y guardar todo ese historial para que no se pierda nada.
 * @author Rubi
 * @version 2.1
 */
public class AsistenciaDAO {

    private final Connection conexion;

    /**
     * ¡NUEVO CONSTRUCTOR!
     * Recibe la conexión activa del controlador para evitar errores de compilación
     * y asegurar que se use una sola conexión segura.
     * @param conexion La conexión a la base de datos mysql.
     */
    public AsistenciaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    /**
     * Anota la entrada de un empleado y calcula el turno automáticamente. 
     * @param idEmpleado El número de identificación del trabajador.
     * @return true si se guardó la entrada, false si hubo algún problema.
     */
    public boolean registrarEntrada(int idEmpleado) {
        // Modificamos el SQL para agregar la columna 'turno'
        String sqlAsistencia = "INSERT INTO asistencias (idEmpleado, fecha, horaEntrada, estado, mes_registro, turno) " +
                               "VALUES (?, CURRENT_DATE, CURRENT_TIME, 'Presente', MONTH(CURRENT_DATE), ?)";
        
        // CORREGIDO: Eliminamos 'estado = 'Presente'' porque esa columna no existe en tu tabla empleados
        String sqlEmpleado = "UPDATE empleados SET asistencia = 'Presente' WHERE idEmpleado = ?";
        
        // Detección automática del turno en base a la hora actual del reloj
        String turnoAutomatico;
        if (LocalTime.now().isBefore(LocalTime.of(14, 0, 0))) {
            turnoAutomatico = "Matutino";
        } else {
            turnoAutomatico = "Vespertino";
        }
        
        try {
            // Manejo de transacciones seguras (Mismo principio que usamos en órdenes)
            conexion.setAutoCommit(false);
            
            // 1. Insertar en asistencias
            try (PreparedStatement psAsis = conexion.prepareStatement(sqlAsistencia)) {
                psAsis.setInt(1, idEmpleado);
                psAsis.setString(2, turnoAutomatico);
                psAsis.executeUpdate();
            }
            
            // 2. Actualizar maestro de empleados
            try (PreparedStatement psEmp = conexion.prepareStatement(sqlEmpleado)) {
                psEmp.setInt(1, idEmpleado);
                psEmp.executeUpdate();
            }
            
            conexion.commit();
            return true;
            
        } catch (SQLException e) {
            try {
                conexion.rollback(); // Si algo falla, deshace los cambios para no corromper la BD
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error al registrar entrada: " + e.getMessage());
            return false;
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Anota la salida de un empleado.
     * Cambia el estado de 'Presente' a 'Completado' y limpia su estado en la tabla empleados.
     * @param idEmpleado El ID del trabajador que se va a retirar.
     * @return true si se cerró correctamente.
     */
    public boolean registrarSalida(int idEmpleado) {
        String sqlAsistencia = "UPDATE asistencias SET horaSalida = CURRENT_TIME, estado = 'Completado' " +
                               "WHERE idEmpleado = ? AND fecha = CURRENT_DATE AND estado = 'Presente'";
        
        // CORREGIDO: Eliminamos 'estado = 'Ausente'' para evitar fallos de columna desconocida
        String sqlEmpleado = "UPDATE empleados SET asistencia = 'Ausente' WHERE idEmpleado = ?";
    
        try {
            conexion.setAutoCommit(false);
            int filasAfectadas = 0;
            
            // 1. Cerrar asistencia
            try (PreparedStatement psAsis = conexion.prepareStatement(sqlAsistencia)) {
                psAsis.setInt(1, idEmpleado);
                filasAfectadas = psAsis.executeUpdate();
            }
            
            // 2. Actualizar empleado a Ausente si se encontró su registro de entrada
            if (filasAfectadas > 0) {
                try (PreparedStatement psEmp = conexion.prepareStatement(sqlEmpleado)) {
                    psEmp.setInt(1, idEmpleado);
                    psEmp.executeUpdate();
                }
                conexion.commit();
                System.out.println("Salida registrada con éxito para ID: " + idEmpleado);
                return true;
            } else {
                conexion.rollback();
                System.out.println("No se encontró una entrada abierta para hoy.");
                return false;
            }
            
        } catch (SQLException e) {
            try {
                conexion.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error al registrar salida: " + e.getMessage());
            return false;
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Busca los registros de un empleado en un mes específico incluyendo la columna turno.
     * @param idEmpleado El trabajador que queremos investigar.
     * @param mes El número de mes (1 para enero, 2 para febrero, etc.).
     * @return Una lista observable para alimentar la TableView.
     */
    public ObservableList<Asistencia> obtenerHistorialPorMes(int idEmpleado, int mes) {
        ObservableList<Asistencia> lista = FXCollections.observableArrayList();
        String sql = "SELECT fecha, horaEntrada, horaSalida, estado, turno FROM asistencias " +
                     "WHERE idEmpleado = ? AND mes_registro = ? ORDER BY fecha DESC";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ps.setInt(2, mes);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Asistencia(
                        rs.getString("fecha"),
                        rs.getString("horaEntrada"),
                        rs.getString("horaSalida"),
                        rs.getString("estado"),
                        rs.getString("turno")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener historial: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Este método le dice al sistema en qué situación está el empleado HOY mismo.
     * @param idEmpleado El ID del empleado a revisar.
     * @return "Ausente", "Presente" o "Completado".
     */
    public String verificarEstadoHoy(int idEmpleado) {
        String sql = "SELECT estado FROM asistencias WHERE idEmpleado = ? AND fecha = CURRENT_DATE ORDER BY idAsistencia DESC LIMIT 1";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("estado"); 
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar estado: " + e.getMessage());
        }
        return "Ausente"; 
    }
}