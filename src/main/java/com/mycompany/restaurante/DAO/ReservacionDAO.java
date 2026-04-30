package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Reservacion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

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
     */
    public static ObservableList<Reservacion> obtenerTodos() {
        ObservableList<Reservacion> lista = FXCollections.observableArrayList();
        
        // 1. Limpieza automática: Cancela reservaciones con 15 min de retraso
        String sqlCancelar = "UPDATE reservaciones SET estado = 'Cancelada' "
                           + "WHERE estado IN ('Pendiente', 'Confirmada') "
                           + "AND TIMESTAMP(fecha, hora) <= DATE_SUB(NOW(), INTERVAL 15 MINUTE)";
                           
        // 2. Consulta de todas las reservaciones
        String sqlSelect = "SELECT idReservacion, nombreCliente, telefono, fecha, hora, numeroPersonas, idMesa, estado "
                         + "FROM reservaciones ORDER BY fecha DESC, hora DESC";

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
     * Elimina una reservación por ID.
     */
    public static boolean eliminar(int id) {
        String sql = "DELETE FROM reservaciones WHERE idReservacion=?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ReservacionDAO.eliminar(): " + e.getMessage());
        }
        return false;
    }
}