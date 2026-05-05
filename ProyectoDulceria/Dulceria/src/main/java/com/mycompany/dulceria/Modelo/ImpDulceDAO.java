package com.mycompany.dulceria.Modelo;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Implementación de la interfaz DulceDao usando MySQL.
 * Esta versión reemplaza la persistencia en archivos .txt por base de datos.
 * 
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class ImpDulceDAO implements DulceDao {
    
    /**
     * Agrega un nuevo dulce al inventario o incrementa su cantidad si ya existe.
     * 
     * @param dulce El objeto Dulce a agregar
     * @throws Exception Si ocurre un error en la base de datos
     */
    @Override
    public void agregarDulce(Dulce dulce) throws Exception {
        Connection conn = null;
        
        try {
            conn = ConexionBD.getConnection();
            
            // Verificar si el dulce ya existe
            Dulce existente = obtenerDulce(dulce.getId());
            
            if (existente != null) {
                int cantidadActual = Integer.parseInt(existente.getCantidad());
                int cantidadNueva = Integer.parseInt(dulce.getCantidad());
                int cantidadTotal = cantidadActual + cantidadNueva;
                
                String sql = "UPDATE dulces SET cantidad = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, cantidadTotal);
                ps.setString(2, dulce.getId());
                ps.executeUpdate();
                
            } else {
                // Si no existe, insertarlo
                String sql = "INSERT INTO dulces (id, nombre, precio_compra, precio_venta, marca, fecha_caducidad, cantidad) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, dulce.getId());
                ps.setString(2, dulce.getNombre());
                ps.setDouble(3, Double.parseDouble(dulce.getPrecioCompra()));
                ps.setDouble(4, Double.parseDouble(dulce.getPrecioVenta()));
                ps.setString(5, dulce.getMarca());
                ps.setString(6, dulce.getFecha_caducidad());
                ps.setInt(7, Integer.parseInt(dulce.getCantidad()));
                
                ps.executeUpdate();
            }
            
        } finally {
            ConexionBD.closeConnection(conn);
        }
    }
    
    /**
     * Obtiene un dulce por su ID.
     * 
     * @param id El ID del dulce a buscar
     * @return El objeto Dulce si se encuentra, null en caso contrario
     * @throws Exception Si ocurre un error en la base de datos
     */
    @Override
    public Dulce obtenerDulce(String id) throws Exception {
        Connection conn = null;
        Dulce dulce = null;
        
        try {
            conn = ConexionBD.getConnection();
            
            String sql = "SELECT * FROM dulces WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                dulce = new Dulce(
                    rs.getString("id"),
                    rs.getString("nombre"),
                    String.valueOf(rs.getDouble("precio_compra")),
                    String.valueOf(rs.getDouble("precio_venta")),
                    rs.getString("marca"),
                    rs.getString("fecha_caducidad"),
                    String.valueOf(rs.getInt("cantidad"))
                );
            }
            
        } finally {
            ConexionBD.closeConnection(conn);
        }
        
        return dulce;
    }
    
    /**
     * Busca un dulce por ID (alias para compatibilidad).
     * 
     * @param id El ID del dulce a buscar
     * @return El objeto Dulce si se encuentra, null en caso contrario
     */
    public Dulce obtenerDulceDesdeArbol(String id) {
        try {
            return obtenerDulce(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Actualiza la información de un dulce existente.
     * 
     * @param dulce El objeto Dulce con los nuevos datos
     * @return true si se actualizó correctamente, false en caso contrario
     * @throws Exception Si ocurre un error en la base de datos
     */
    @Override
    public boolean actualizarDulce(Dulce dulce) throws Exception {
        Connection conn = null;
        boolean actualizado = false;
        
        try {
            conn = ConexionBD.getConnection();
            
            String sql = "UPDATE dulces SET nombre = ?, precio_compra = ?, precio_venta = ?, " +
                       "marca = ?, fecha_caducidad = ?, cantidad = ? WHERE id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, dulce.getNombre());
            ps.setDouble(2, Double.parseDouble(dulce.getPrecioCompra()));
            ps.setDouble(3, Double.parseDouble(dulce.getPrecioVenta()));
            ps.setString(4, dulce.getMarca());
            ps.setString(5, dulce.getFecha_caducidad());
            ps.setInt(6, Integer.parseInt(dulce.getCantidad()));
            ps.setString(7, dulce.getId());
            
            int filasAfectadas = ps.executeUpdate();
            actualizado = (filasAfectadas > 0);
            
        } finally {
            ConexionBD.closeConnection(conn);
        }
        
        return actualizado;
    }
    
    /**
     * Elimina un dulce del inventario.
     * 
     * @param id El ID del dulce a eliminar
     * @return Un mensaje indicando el resultado
     * @throws Exception Si ocurre un error en la base de datos
     */
    @Override
    public String eliminarDulce(String id) throws Exception {
        Connection conn = null;
        
        try {
            conn = ConexionBD.getConnection();
            
            String sql = "DELETE FROM dulces WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            
            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                return "Dulce eliminado";
            } else {
                return "No se encontró el Dulce";
            }
            
        } finally {
            ConexionBD.closeConnection(conn);
        }
    }
    
    /**
     * Obtiene la lista completa de dulces.
     * 
     * @return ArrayList con todos los dulces
     */
    @Override
    public ArrayList<Dulce> obtenerDulces() {
        Connection conn = null;
        ArrayList<Dulce> lista = new ArrayList<>();
        
        try {
            conn = ConexionBD.getConnection();
            
            String sql = "SELECT * FROM dulces ORDER BY id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Dulce dulce = new Dulce(
                    rs.getString("id"),
                    rs.getString("nombre"),
                    String.valueOf(rs.getDouble("precio_compra")),
                    String.valueOf(rs.getDouble("precio_venta")),
                    rs.getString("marca"),
                    rs.getString("fecha_caducidad"),
                    String.valueOf(rs.getInt("cantidad"))
                );
                lista.add(dulce);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexionBD.closeConnection(conn);
        }
        
        return lista;
    }
    
    /**
     * Resta cantidad del inventario de un dulce.
     * Si la cantidad llega a 0 o menos, elimina el dulce.
     * 
     * @param id El ID del producto
     * @param cantidad La cantidad a restar
     * @return Mensaje indicando el resultado
     * @throws IOException Si ocurre un error
     */
    public String restarCantidad(String id, int cantidad) throws IOException {
        Connection conn = null;
        
        try {
            conn = ConexionBD.getConnection();
            
            Dulce dulce = obtenerDulce(id);
            if (dulce == null) {
                return "Producto no encontrado";
            }
            
            int cantidadActual = Integer.parseInt(dulce.getCantidad());
            if (cantidad > cantidadActual) {
                return "No hay suficiente inventario";
            }
            
            int nuevaCantidad = cantidadActual - cantidad;
            
            if (nuevaCantidad <= 0) {
                // Eliminar el producto
                String sql = "DELETE FROM dulces WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, id);
                ps.executeUpdate();
            } else {
                // Actualizar la cantidad
                String sql = "UPDATE dulces SET cantidad = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, nuevaCantidad);
                ps.setString(2, id);
                ps.executeUpdate();
            }
            
            return "Cantidad actualizada correctamente";
            
        } catch (Exception e) {
            throw new IOException("Error al restar cantidad: " + e.getMessage());
        } finally {
            ConexionBD.closeConnection(conn);
        }
    }
    
    /**
     * Suma cantidad al inventario de un dulce.
     * 
     * @param idProd El ID del producto
     * @param cantidad La cantidad a sumar
     * @return Mensaje indicando el resultado
     * @throws IOException Si ocurre un error
     */
    public String sumarCantidad(String idProd, int cantidad) throws IOException {
        Connection conn = null;
        
        try {
            conn = ConexionBD.getConnection();
            
            Dulce dulce = obtenerDulce(idProd);
            if (dulce == null) {
                return "ERROR: Dulce no encontrado con ID: " + idProd;
            }
            
            int cantidadActual = Integer.parseInt(dulce.getCantidad());
            int nuevaCantidad = cantidadActual + cantidad;
            
            String sql = "UPDATE dulces SET cantidad = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, nuevaCantidad);
            ps.setString(2, idProd);
            ps.executeUpdate();
            
            return "ÉXITO: Se ha actualizado la cantidad de " + idProd + " a " + nuevaCantidad + ".";
            
        } catch (Exception e) {
            throw new IOException("Error al sumar cantidad: " + e.getMessage());
        } finally {
            ConexionBD.closeConnection(conn);
        }
    }
}
