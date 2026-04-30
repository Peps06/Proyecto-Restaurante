package com.mycompany.dulceria.Modelo;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Implementación de la interfaz VentaDAO usando MySQL.
 * Esta versión reemplaza la persistencia en archivos .txt por base de datos.
 * Usa dos tablas: ventas (cabecera) y detalle_venta (productos).
 * 
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class ImpVentaDAO implements VentaDAO {
    
    /**
     * Registra una nueva venta en la base de datos.
     * Inserta la cabecera en la tabla ventas y los detalles en detalle_venta.
     * 
     * @param v El objeto Venta a registrar
     * @throws IOException Si ocurre un error en la base de datos
     */
    @Override
    public void registrarVenta(Venta v) throws IOException {
        Connection conn = null;
        
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción
            
            // 1. Insertar la cabecera de la venta
            String sqlVenta = "INSERT INTO ventas (id_venta, fecha, total) VALUES (?, ?, ?)";
            PreparedStatement psVenta = conn.prepareStatement(sqlVenta);
            psVenta.setString(1, v.getIdVenta());
            psVenta.setString(2, v.getFecha());
            psVenta.setDouble(3, Double.parseDouble(v.getTotal()));
            psVenta.executeUpdate();
            
            // 2. Insertar los detalles de la venta
            String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, nombre_producto, cantidad, subtotal) " +
                              "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psDetalle = conn.prepareStatement(sqlDetalle);
            
            for (int i = 0; i < v.getIds().size(); i++) {
                psDetalle.setString(1, v.getIdVenta());
                psDetalle.setString(2, v.getIds().get(i));
                psDetalle.setString(3, v.getNombres().get(i));
                psDetalle.setInt(4, Integer.parseInt(v.getCantidades().get(i)));
                psDetalle.setDouble(5, Double.parseDouble(v.getSubtotales().get(i)));
                psDetalle.executeUpdate();
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new IOException("Error al registrar venta: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            ConexionBD.closeConnection(conn);
        }
    }
    
    /**
     * Obtiene todas las ventas registradas.
     * 
     * @return ArrayList con todas las ventas (con sus detalles)
     */
    @Override
    public ArrayList<Venta> obtenerVentas() {
        Connection conn = null;
        ArrayList<Venta> listaVentas = new ArrayList<>();
        
        try {
            conn = ConexionBD.getConnection();
            
            // Obtener todas las ventas
            String sqlVentas = "SELECT * FROM ventas ORDER BY fecha_registro DESC";
            Statement stmt = conn.createStatement();
            ResultSet rsVentas = stmt.executeQuery(sqlVentas);
            
            while (rsVentas.next()) {
                String idVenta = rsVentas.getString("id_venta");
                String fecha = rsVentas.getString("fecha");
                String total = String.valueOf(rsVentas.getDouble("total"));
                
                Venta venta = new Venta(idVenta, fecha);
                
                // Obtener los detalles de esta venta
                String sqlDetalles = "SELECT * FROM detalle_venta WHERE id_venta = ?";
                PreparedStatement psDetalles = conn.prepareStatement(sqlDetalles);
                psDetalles.setString(1, idVenta);
                ResultSet rsDetalles = psDetalles.executeQuery();
                
                while (rsDetalles.next()) {
                    venta.agregarProducto(
                        rsDetalles.getString("id_producto"),
                        rsDetalles.getString("nombre_producto"),
                        String.valueOf(rsDetalles.getInt("cantidad")),
                        String.valueOf(rsDetalles.getDouble("subtotal"))
                    );
                }
                
                venta.setTotal(total);
                listaVentas.add(venta);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexionBD.closeConnection(conn);
        }
        
        return listaVentas;
    }
    
    /**
     * Obtiene las ventas de una fecha específica.
     * 
     * @param fecha La fecha a buscar
     * @return ArrayList con las ventas de esa fecha
     */
    @Override
    public ArrayList<Venta> ventasPorFecha(String fecha) {
        Connection conn = null;
        ArrayList<Venta> listaVentas = new ArrayList<>();
        
        try {
            conn = ConexionBD.getConnection();
            
            // Obtener ventas de la fecha específica
            String sqlVentas = "SELECT * FROM ventas WHERE fecha = ? ORDER BY fecha_registro DESC";
            PreparedStatement psVentas = conn.prepareStatement(sqlVentas);
            psVentas.setString(1, fecha);
            ResultSet rsVentas = psVentas.executeQuery();
            
            while (rsVentas.next()) {
                String idVenta = rsVentas.getString("id_venta");
                String fechaVenta = rsVentas.getString("fecha");
                String total = String.valueOf(rsVentas.getDouble("total"));
                
                Venta venta = new Venta(idVenta, fechaVenta);
                
                String sqlDetalles = "SELECT * FROM detalle_venta WHERE id_venta = ?";
                PreparedStatement psDetalles = conn.prepareStatement(sqlDetalles);
                psDetalles.setString(1, idVenta);
                ResultSet rsDetalles = psDetalles.executeQuery();
                
                while (rsDetalles.next()) {
                    venta.agregarProducto(
                        rsDetalles.getString("id_producto"),
                        rsDetalles.getString("nombre_producto"),
                        String.valueOf(rsDetalles.getInt("cantidad")),
                        String.valueOf(rsDetalles.getDouble("subtotal"))
                    );
                }
                
                venta.setTotal(total);
                listaVentas.add(venta);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexionBD.closeConnection(conn);
        }
        
        return listaVentas;
    }
    
    /**
     * Elimina una venta y revierte el inventario.
     * Elimina los detalles y la cabecera de la venta, y suma las cantidades
     * vendidas de vuelta al inventario, todo dentro de la misma transacción.
     * 
     * @param idVenta El ID de la venta a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     * @throws IOException Si ocurre un error en la base de datos
     */
    public boolean eliminarVenta(String idVenta) throws IOException {
        Connection conn = null;
        
        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción
            
            // 1. Obtener los detalles de la venta para revertir inventario
            String sqlDetalles = "SELECT id_producto, cantidad FROM detalle_venta WHERE id_venta = ?";
            PreparedStatement psDetalles = conn.prepareStatement(sqlDetalles);
            psDetalles.setString(1, idVenta);
            ResultSet rsDetalles = psDetalles.executeQuery();
            
            // 2. Revertir el inventario usando la MISMA conexión (dentro de la transacción)
            String sqlSumar = "UPDATE dulces SET cantidad = cantidad + ? WHERE id = ?";
            PreparedStatement psSumar = conn.prepareStatement(sqlSumar);
            
            while (rsDetalles.next()) {
                String idProducto = rsDetalles.getString("id_producto");
                int cantidad = rsDetalles.getInt("cantidad");
                
                psSumar.setInt(1, cantidad);
                psSumar.setString(2, idProducto);
                psSumar.executeUpdate();
            }
            
            // 3. Eliminar los detalles (CASCADE haría esto automáticamente,
            //    pero lo hacemos explícito para claridad)
            String sqlDeleteDetalles = "DELETE FROM detalle_venta WHERE id_venta = ?";
            PreparedStatement psDeleteDetalles = conn.prepareStatement(sqlDeleteDetalles);
            psDeleteDetalles.setString(1, idVenta);
            psDeleteDetalles.executeUpdate();
            
            // 4. Eliminar la cabecera de la venta
            String sqlDeleteVenta = "DELETE FROM ventas WHERE id_venta = ?";
            PreparedStatement psDeleteVenta = conn.prepareStatement(sqlDeleteVenta);
            psDeleteVenta.setString(1, idVenta);
            int filasAfectadas = psDeleteVenta.executeUpdate();
            
            conn.commit();
            
            return (filasAfectadas > 0);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new IOException("Error al eliminar venta: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            ConexionBD.closeConnection(conn);
        }
    }
}
