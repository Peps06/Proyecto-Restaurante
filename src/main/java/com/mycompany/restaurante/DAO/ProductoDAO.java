package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Provee los métodos de acceso a datos (DAO) para la entidad {@code Producto}.
 * Se encarga de las operaciones CRUD (Crear, Leer, Actualizar, Eliminar) 
 * sobre la tabla {@code productos} en la base de datos.
 * 
 * @author Citlaly
 * @version 1.0
 */
public class ProductoDAO {

    /**
     * Consulta y devuelve la lista completa de productos registrados.
     * Ideal para llenar componentes de JavaFX como TableView o ListView.
     * 
     * @return Una {@link ObservableList} de objetos {@link Producto}. 
     *         Si hay un error o no hay datos, devuelve una lista vacía.
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
     * Registra un nuevo producto en la base de datos.
     * 
     * @param p El objeto {@link Producto} con los datos a insertar.
     * @return El ID generado por la base de datos para el nuevo registro; 
     *         -1 si la operación falla.
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
     * Actualiza la información existente de un producto en la base de datos.
     * 
     * @param p Objeto {@link Producto} que contiene los nuevos datos y el ID correspondiente.
     * @return {@code true} si se actualizó al menos un registro; {@code false} en caso contrario.
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
            ps.setInt(5, p.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException | NumberFormatException e) {
            System.err.println("ProductoDAO.actualizar(): " + e.getMessage());
        }
        return false;
    }

    /**
     * Elimina de forma permanente un producto de la base de datos mediante su ID.
     * 
     * @param id El identificador único del producto a eliminar.
     * @return {@code true} si la eliminación fue exitosa.
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