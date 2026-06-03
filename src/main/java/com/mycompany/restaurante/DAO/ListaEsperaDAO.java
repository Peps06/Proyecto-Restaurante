package com.mycompany.restaurante.DAO;

/**
 * Acceso a datos para la tabla lista_espera.
 * Se encarga de controlar el flujo de clientes en espera, permitiendo su lectura,
 * inserción, actualización general y cambios rápidos de estado.
 * * @author Dana
 */

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.ClienteEspera;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ListaEsperaDAO {
    
    /**
     * Devuelve todas las personas en lista de espera de la BD.
     * @return Una lista observable con todas las personas registradas.
     */
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
     * @param c Objeto con los datos del cliente a insertar.
     * @return true si el registro se insertó correctamente, false de lo contrario.
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
     * Actualiza todos los datos de un cliente existente en la lista de espera.
     * @param c Objeto cliente que contiene los datos modificados y su ID.
     * @return true si la actualización fue exitosa en la BD, false de lo contrario.
     */
    public static boolean actualizar(ClienteEspera c) {
        String sql = "UPDATE lista_espera SET nombreCliente=?, telefono=?, numeroPersonas=?, estado=? WHERE idEspera=?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombreCliente());
            ps.setString(2, c.getTelefono());
            ps.setInt(3, c.getNumeroPersonas());
            ps.setString(4, c.getEstado());
            ps.setInt(5, c.getIdEspera());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ListaEsperaDAO.actualizar(): " + e.getMessage());
        }
        return false;
    }

    /**
     * Cambia el estado del cliente a "Asignado" o "Cancelado".
     * @param idEspera Identificador único del cliente en la lista de espera.
     * @param nuevoEstado Cadena de texto con el nuevo estado operativo a asignar.
     * @return true si el estado se actualizó correctamente, false en caso de error.
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
    
    /**
    * Cancela automáticamente todos los clientes en espera de días anteriores al actual.
    * Se llama al iniciar la pantalla de lista de espera para limpiar el día anterior.
    */
   public static void limpiarEsperasDiasAnteriores() {
       String sql = "UPDATE lista_espera SET estado = 'Cancelado' "
                  + "WHERE estado = 'Esperando' "
                  + "AND DATE(horaLlegada) < CURDATE()";

       try (Connection con = ConexionDB.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
           int filas = ps.executeUpdate();
           if (filas > 0) {
               System.out.println("ListaEsperaDAO: " + filas + " esperas del día anterior limpiadas.");
           }
       } catch (SQLException e) {
           System.err.println("ListaEsperaDAO.limpiarEsperasDiasAnteriores(): " + e.getMessage());
       }
   }
}