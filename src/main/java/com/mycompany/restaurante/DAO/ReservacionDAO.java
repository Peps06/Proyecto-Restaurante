package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Reservacion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

/**
 * Acceso a datos para la tabla 
 * * Es el encargado de leer, registrar, actualizar y eliminar
 * las reservaciones, así como de cancelar automáticamente las expiradas.
 *
 * @author Dana
 */
public class ReservacionDAO {

    /**
     * Devuelve todas las reservaciones de la BD.
     * Antes de consultar, cancela automáticamente las que tienen +15 minutos de retraso.
     * @return Una lista observable con todas las reservaciones registradas.
     */
    public static ObservableList<Reservacion> obtenerTodos() {
        ObservableList<Reservacion> lista = FXCollections.observableArrayList();
        
        // 1. Limpieza automática: Cancela reservaciones con 15 min de retraso
        String sqlCancelar = "UPDATE reservaciones SET estado = 'Cancelada' "
                           + "WHERE estado IN ('Pendiente', 'Confirmada') "
                           + "AND TIMESTAMP(fecha, hora) <= DATE_SUB(NOW(), INTERVAL 15 MINUTE)";
                           
        // 2. Consulta de todas las reservaciones
        String sqlSelect = "SELECT idReservacion, nombreCliente, telefono, fecha, hora, numeroPersonas, idMesa, estado "
                 + "FROM reservaciones ORDER BY fecha ASC, hora ASC";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement psCancel = con.prepareStatement(sqlCancelar);
             Statement st = con.createStatement()) {

            // Ejecutamos la cancelación primero
            psCancel.executeUpdate();

            // Luego leemos los datos
            try (ResultSet rs = st.executeQuery(sqlSelect)) {
                while (rs.next()) {
                    Reservacion r = new Reservacion(
                        rs.getInt("idReservacion"),
                        rs.getString("nombreCliente"),
                        rs.getString("telefono"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getTime("hora").toString(),
                        rs.getInt("numeroPersonas"),
                        rs.getInt("idMesa"),
                        rs.getString("estado")
                    );
                    lista.add(r);
                }
            }

        } catch (SQLException e) {
            System.err.println("ReservacionDAO.obtenerTodos(): " + e.getMessage());
        }
        return lista;
    }

    /**
     * Inserta una reservación nueva. Devuelve el ID generado, -1 si hay error.
     * @param r Objeto con la información de la reservación a insertar.
     * @return El ID numérico generado por la base de datos, o -1 en caso de error.
     */
    public static int insertar(Reservacion r) {
        String sql = "INSERT INTO reservaciones (nombreCliente, telefono, fecha, hora, numeroPersonas, idMesa, estado) VALUES (?,?,?,?,?,?,?)";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getNombreCliente());
            ps.setString(2, r.getTelefono());
            ps.setDate(3, java.sql.Date.valueOf(r.getFecha()));
            // Aseguramos que la hora tenga formato correcto para MySQL (HH:MM:SS)
            ps.setTime(4, java.sql.Time.valueOf(r.getHora() + (r.getHora().length() == 5 ? ":00" : ""))); 
            ps.setInt(5, r.getNumeroPersonas());
            ps.setInt(6, r.getIdMesa());
            ps.setString(7, r.getEstado());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("ReservacionDAO.insertar(): " + e.getMessage());
        }
        return -1;
    }

    /**
     * Actualiza todos los datos de una reservación existente.
     * @param r Objeto reservación que contiene los datos modificados y su ID.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public static boolean actualizar(Reservacion r) {
        String sql = "UPDATE reservaciones SET nombreCliente=?, telefono=?, fecha=?, hora=?, numeroPersonas=?, idMesa=?, estado=? WHERE idReservacion=?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, r.getNombreCliente());
            ps.setString(2, r.getTelefono());
            ps.setDate(3, java.sql.Date.valueOf(r.getFecha()));
            ps.setTime(4, java.sql.Time.valueOf(r.getHora() + (r.getHora().length() == 5 ? ":00" : "")));
            ps.setInt(5, r.getNumeroPersonas());
            ps.setInt(6, r.getIdMesa());
            ps.setString(7, r.getEstado());
            ps.setInt(8, r.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ReservacionDAO.actualizar(): " + e.getMessage());
        }
        return false;
    }

    /**
     * Cancela una reservación por ID.
     * @param idReserva Identificador único de la reservación a cancelar.
     * @return true si se cambió el estado correctamente, false si ocurrió un error.
     */
    public static boolean cancelarReserva(int idReserva) {
        String sql = "UPDATE reservaciones SET estado = 'Cancelada' WHERE idReservacion = ?";

        try (Connection con = ConexionDB.getConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReserva);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("ReservacionDAO.cancelarReserva(): " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida la disponibilidad una reservación.
     * @param idMesa Número de mesa que se desea validar.
     * @param fecha Fecha en la que se solicita la reserva.
     * @param hora Hora asignada para evaluar el horario disponible.
     * @param idReservaIgnorar ID de la reserva actual para omitirla al editar.
     * @return true si la mesa está disponible en ese horario, false si hay cruces.
     */
    public static boolean validarDisponibilidad(int idMesa, LocalDate fecha, String hora, int idReservaIgnorar) {
        // Busca cuántas reservas se empalman con un margen de +/- 3 horas
        String sql = "SELECT COUNT(*) FROM reservaciones "
                   + "WHERE idMesa = ? AND fecha = ? "
                   + "AND estado IN ('Pendiente', 'Confirmada') "
                   + "AND idReservacion != ? "
                   + "AND hora BETWEEN SUBTIME(?, '02:59:00') AND ADDTIME(?, '02:59:00')";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            ps.setInt(3, idReservaIgnorar); 
            
            // Asegura que el formato de hora no marque error al convertir
            String horaFormateada = hora + (hora.length() == 5 ? ":00" : "");
            ps.setTime(4, java.sql.Time.valueOf(horaFormateada));
            ps.setTime(5, java.sql.Time.valueOf(horaFormateada));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int conflictos = rs.getInt(1);
                    return conflictos == 0; 
                }
            }
        } catch (SQLException e) {
            System.err.println("ReservacionDAO.validarDisponibilidad(): " + e.getMessage());
        }
        return false; 
    }
    
    /**
    * Verifica si una mesa tiene una reserva activa dentro de las próximas 3 horas.
    * @param idMesa ID de la mesa a verificar.
    * @return true si hay una reserva en ese rango de tiempo, false si no.
    */
   public static boolean tieneReservaProxima(int idMesa) {
       String sql = "SELECT COUNT(*) FROM reservaciones "
                  + "WHERE idMesa = ? "
                  + "AND estado IN ('Pendiente', 'Confirmada') "
                  + "AND TIMESTAMP(fecha, hora) BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 3 HOUR)";

       try (Connection con = ConexionDB.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
           ps.setInt(1, idMesa);
           try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) return rs.getInt(1) > 0;
           }
       } catch (SQLException e) {
           System.err.println("ReservacionDAO.tieneReservaProxima(): " + e.getMessage());
       }
       return false;
   }
}