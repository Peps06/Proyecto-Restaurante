package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Insumo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para gestionar la persistencia de los Insumos en la base de datos.
 * 
 * @author Rubi
 */
public class InsumoDAO {

    // metodo para listar
    public List<Insumo> listar() {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumos";

        try {
            // Conexión mediante el Singleton
            Connection con = ConexionDB.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Insumo i = new Insumo(
                    rs.getInt("idInsumo"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    rs.getDouble("stock"),
                    rs.getString("unidad"),
                    rs.getString("estado")
                );
                lista.add(i);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar insumos: " + e.getMessage());
        }
        return lista;
    }

    // --- Método para insertar
    public boolean insertar(Insumo insumo) {
        String sql = "INSERT INTO insumos (nombre, categoria, stock, unidad, estado) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection con = ConexionDB.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            
            ps.setString(1, insumo.getNombre());
            ps.setString(2, insumo.getCategoria());
            ps.setDouble(3, insumo.getStock());
            ps.setString(4, insumo.getUnidad());
            ps.setString(5, insumo.getEstado());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar insumo: " + e.getMessage());
            return false;
        }
    }

    // --- Método para editar
    public boolean editar(Insumo insumo) {
        String sql = "UPDATE insumos SET nombre=?, categoria=?, stock=?, unidad=?, estado=? WHERE idInsumo=?";
        try {
            Connection con = ConexionDB.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            
            ps.setString(1, insumo.getNombre());
            ps.setString(2, insumo.getCategoria());
            ps.setDouble(3, insumo.getStock());
            ps.setString(4, insumo.getUnidad());
            ps.setString(5, insumo.getEstado());
            ps.setInt(6, insumo.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al editar insumo: " + e.getMessage());
            return false;
        }
    }

    // Método para eliminar
    public boolean eliminar(int id) {
        String sql = "DELETE FROM insumos WHERE idInsumo = ?";
        try {
            Connection con = ConexionDB.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar insumo: " + e.getMessage());
            return false;
        }
    }
}
