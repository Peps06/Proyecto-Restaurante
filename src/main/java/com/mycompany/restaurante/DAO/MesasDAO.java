package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Mesa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Citlaly
 * @version 1
 */
public class MesasDAO {
    public List<Mesa> obtenerTodasLasMesas() {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT idMesa, capacidad, estado FROM mesas";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Mesa mesa = new Mesa(
                    rs.getInt("idMesa"),
                    rs.getInt("capacidad"),
                    rs.getString("estado")
                );
                mesas.add(mesa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mesas;
    }
    
    public Mesa obtenerMesaPorId(int idMesa) {
        Mesa mesa = null;
        String sql = "SELECT idMesa, capacidad, estado FROM mesas WHERE idMesa = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idMesa);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                mesa = new Mesa(
                    rs.getInt("idMesa"),
                    rs.getInt("capacidad"),
                    rs.getString("estado")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mesa;
    }
    
    public boolean actualizarEstadoMesa(int idMesa, String nuevoEstado) {
        String sql = "UPDATE mesas SET estado = ? WHERE idMesa = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idMesa);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Mesa> obtenerMesasPorEstado(String estado) {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT idMesa, capacidad, estado FROM mesas WHERE estado = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, estado);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Mesa mesa = new Mesa(
                    rs.getInt("idMesa"),
                    rs.getInt("capacidad"),
                    rs.getString("estado")
                );
                mesas.add(mesa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mesas;
    }
}
