package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Mesa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Capa de Acceso a Datos (DAO) para la gestión de las mesas físicas del restaurante.
 * Proporciona métodos para consultar disponibilidad, capacidades y actualizar 
 * el estado operativo de cada mesa.
 * 
 * @author Citlaly
 * @version 1.0
 */
public class MesasDAO {

    /**
     * Recupera el listado completo de mesas registradas en la base de datos.
     * 
     * @return List de objetos {@link Mesa} con toda su información.
     */
    public List<Mesa> obtenerTodasLasMesas() {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT idMesa, capacidad, estado FROM mesas ORDER BY idMesa";
 
        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
 
            while (rs.next()) {
                mesas.add(new Mesa(
                    rs.getInt("idMesa"),
                    rs.getInt("capacidad"),
                    rs.getString("estado")
                ));
            }
 
        } catch (SQLException e) {
            System.err.println("[MesasDAO] Error al obtener todas las mesas: " + e.getMessage());
            e.printStackTrace();
        }
        return mesas;
    }

    
    /**
     * Busca una mesa específica mediante su identificador único.
     * 
     * @param idMesa ID de la mesa a consultar.
     * @return Objeto {@link Mesa} si se encuentra, o null si no existe.
     */
    public Mesa obtenerMesaPorId(int idMesa) {
        Mesa mesa = null;
        String sql = "SELECT idMesa, capacidad, estado FROM mesas WHERE idMesa = ?";
 
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mesa = new Mesa(
                        rs.getInt("idMesa"),
                        rs.getInt("capacidad"),
                        rs.getString("estado")
                    );
                }
            }
 
        } catch (SQLException e) {
            System.err.println("[MesasDAO] Error al buscar mesa por ID (" + idMesa + "): " + e.getMessage());
            e.printStackTrace();
        }
        return mesa;
    }
    
    /**
     * Actualiza el estado actual de una mesa (ej. 'Libre', 'Ocupada', 'Sucia').
     * 
     * @param idMesa Identificador de la mesa.
     * @param nuevoEstado Texto con el nuevo estado a asignar.
     * @return true si la actualización fue exitosa; false en caso contrario.
     */
    public boolean actualizarEstadoMesa(int idMesa, String nuevoEstado) {
        String sql = "UPDATE mesas SET estado = ? WHERE idMesa = ?";
 
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idMesa);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("[MesasDAO] Error al actualizar estado de mesa " + idMesa + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Filtra y retorna una lista de mesas que coincidan con un estado específico.
     * Útil para obtener rápidamente todas las mesas 'Libres'.
     * 
     * @param estado El criterio de búsqueda (ej. 'Libre').
     * @return List de mesas filtradas.
     */
    public List<Mesa> obtenerMesasPorEstado(String estado) {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT idMesa, capacidad, estado FROM mesas WHERE estado = ? ORDER BY idMesa";
 
        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mesas.add(new Mesa(
                        rs.getInt("idMesa"),
                        rs.getInt("capacidad"),
                        rs.getString("estado")
                    ));
                }
            }
 
        } catch (SQLException e) {
            System.err.println("[MesasDAO] Error al filtrar mesas por estado '" + estado + "': " + e.getMessage());
            e.printStackTrace();
        }
        return mesas;
    }
}