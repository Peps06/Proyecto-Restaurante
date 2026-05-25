package com.mycompany.restaurante.DAO;

/**
* Dana
*/

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.ClienteEspera;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ListaEsperaDAO {
    
    
    public static ObservableList<ClienteEspera> obtenerEsperando() {
        ObservableList<ClienteEspera> lista = FXCollections.observableArrayList();
        
        // Ordena ascendente para que el más viejo (el que llegó primero) salga hasta arriba
        String sql = "SELECT * FROM lista_espera WHERE estado = 'Esperando' ORDER BY horaLlegada ASC";

        try (Connection con = ConexionDB.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ClienteEspera cliente = new ClienteEspera(
                    rs.getInt("idEspera"),
                    rs.getString("nombreCliente"),
                    rs.getString("telefono"),
                    rs.getInt("numeroPersonas"),
                    rs.getTimestamp("horaLlegada").toLocalDateTime(),
                    rs.getString("estado")
                );
                lista.add(cliente);
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerEsperando(): " + e.getMessage());
        }
        return lista;
    }

    /**
     * Agrega un nuevo cliente a la lista de espera.
     */
    public static boolean insertar(ClienteEspera c) {
        String sql = "INSERT INTO lista_espera (nombreCliente, telefono, numeroPersonas, estado) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombreCliente());
            ps.setString(2, c.getTelefono());
            ps.setInt(3, c.getNumeroPersonas());
            ps.setString(4, "Esperando"); // Por defecto siempre entra esperando

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en ListaEsperaDAO.insertar(): " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza todos los datos de una reservación existente.
     */
    public static boolean actualizar(ClienteEspera r) {
        String sql = "UPDATE lista_espera SET nombreCliente=?, telefono=?, numeroPersonas=?, estado=? WHERE idEspera=?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, r.getNombreCliente());
            ps.setString(2, r.getTelefono());
            ps.setInt(3, r.getNumeroPersonas());
            ps.setString(4, r.getEstado());
            ps.setInt(5, r.getIdEspera());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ReservacionDAO.actualizar(): " + e.getMessage());
        }
        return false;
    }

    /**
     * Cambia el estado del cliente a "Asignado" o "Cancelado".
     */
    public static boolean actualizarEstado(int idEspera, String nuevoEstado) {
        String sql = "UPDATE lista_espera SET estado = ? WHERE idEspera = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idEspera);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en actualizarEstado(): " + e.getMessage());
            return false;
        }
    }
}