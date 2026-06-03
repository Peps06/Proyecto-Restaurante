package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Insumo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase es el "mensajero" oficial entre mi programa y la base de datos MySQL 
 * para todo lo que tenga que ver con el Almacén.
 * * Aquí escribo las consultas SQL necesarias para guardar, ver, editar o borrar 
 * los insumos (ingredientes y productos). Se encarga de traducir los datos de la 
 * base de datos a objetos de tipo 'Insumo' que Java pueda entender.
 * 
 * @author Rubi
 * @version 1.0
 */
public class InsumoDAO {

    /**
     * Trae todos los insumos que están guardados en la tabla 'insumos'.
     * @return Una lista llena de objetos Insumo con toda su información (ID, nombre, stock, etc.).
     */
    public List<Insumo> listar() {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumos";

        try (
            // Pedimos la conexión a nuestra base de datos
            Connection con = ConexionDB.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            ){
            // Vamos recorriendo fila por fila lo que nos mandó la base de datos
            while (rs.next()) {
                // Convertimos cada fila en un objeto Insumo de Java
                Insumo i = new Insumo(
                    rs.getInt("idInsumo"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    rs.getDouble("stock"),
                    rs.getString("unidad"),
                    rs.getString("estado")
                );
                // Lo agregamos a nuestra lista
                lista.add(i);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar insumos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Toma un nuevo insumo y lo guarda permanentemente en la base de datos.
     * @param insumo El objeto con los datos del producto a registrar.
     * @return true si se guardó correctamente, false si hubo algún error.
     */
    public boolean insertar(Insumo insumo) {
        String sql = "INSERT INTO insumos (nombre, categoria, stock, unidad, estado) VALUES (?, ?, ?, ?, ?)";
        try (
            Connection con = ConexionDB.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ){
            // Reemplazamos los signos '?' por los datos reales del insumo
            ps.setString(1, insumo.getNombre());
            ps.setString(2, insumo.getCategoria());
            ps.setDouble(3, insumo.getStock());
            ps.setString(4, insumo.getUnidad());
            ps.setString(5, insumo.getEstado());
            
            // Ejecutamos la instrucción y devolvemos éxito si se afectó al menos una fila
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar insumo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un insumo por su ID y actualiza toda su información con los nuevos datos.
     * @param insumo El objeto con los datos ya modificados.
     * @return true si la actualización fue exitosa.
     */
    public boolean editar(Insumo insumo) {
        String sql = "UPDATE insumos SET nombre=?, categoria=?, stock=?, unidad=?, estado=? WHERE idInsumo=?";
        try (
            Connection con = ConexionDB.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ){
            ps.setString(1, insumo.getNombre());
            ps.setString(2, insumo.getCategoria());
            ps.setDouble(3, insumo.getStock());
            ps.setString(4, insumo.getUnidad());
            ps.setString(5, insumo.getEstado());
            ps.setInt(6, insumo.getId()); // Usamos el ID para saber exactamente cuál fila editar
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al editar insumo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina definitivamente un insumo de la base de datos usando su ID.
     * @param id El número identificador del producto que queremos borrar.
     * @return true si el producto fue eliminado con éxito.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM insumos WHERE idInsumo = ?";
        try (
            Connection con = ConexionDB.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ){
            ps.setInt(1, id);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar insumo: " + e.getMessage());
            return false;
        }
    }
}