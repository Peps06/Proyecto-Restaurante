package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Acceso a datos para la tabla {@code productos}.
 * 
 * Es el encargado de llenar la tabla de menú, de regisstrar los pedidos, de 
 * guardar si se crea, edita o elimina un producto, todo ello en la base de datos.
 *
 * @author Citlaly
 * @version 1
 */
public class ProductoDAO {

    /**
     * Devuelve todos los productos de la BD.
     */
    public static ObservableList<Producto> obtenerTodos() {
        ObservableList<Producto> lista = FXCollections.observableArrayList();
        String sql = "SELECT idProductos, nombre, tipo, precio, descripcion FROM productos";

        try (Connection con = ConexionDB.getConexion();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) {
                Producto p = new Producto(
                    rs.getString("nombre"),
                    rs.getString("tipo"),
                    rs.getDouble("precio"),
                    rs.getString("descripcion")
                );
                // Se guarda el id en cantidad (temporal) para poder usarlo en altas y bajas.
                p.setId(rs.getInt("idProductos"));
                p.setCantidadPedida(0);
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("ProductoDAO.obtenerTodos(): " + e.getMessage());
        }
        return lista;
    }

    /**
     * Inserta un producto nuevo. Devuelve el ID generado, -1 si hay error.
     */
    public static int insertar(Producto p) {
        String sql = "INSERT INTO productos (nombre, tipo, precio, descripcion) VALUES (?,?,?,?)";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getTipo());
            ps.setDouble(3, p.getPrecio());
            ps.setString(4, p.getDescripcion());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("ProductoDAO.insertar(): " + e.getMessage());
        }
        return -1;
    }

    /**
     * Actualiza nombre, tipo, precio y descripción de un producto.
     */
    public static boolean actualizar(Producto p) {
        String sql = "UPDATE productos SET nombre=?, tipo=?, precio=?, descripcion=? "
                   + "WHERE idProductos=?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getTipo());
            ps.setDouble(3, p.getPrecio());
            ps.setString(4, p.getDescripcion());
            ps.setInt(5, p.getCantidadPedida());
            return ps.executeUpdate() > 0;

        } catch (SQLException | NumberFormatException e) {
            System.err.println("ProductoDAO.actualizar(): " + e.getMessage());
        }
        return false;
    }

    /**
     * Elimina un producto por ID.
     */
    public static boolean eliminar(int id) {
        String sql = "DELETE FROM productos WHERE idProductos=?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ProductoDAO.eliminar(): " + e.getMessage());
        }
        return false;
    }
}
