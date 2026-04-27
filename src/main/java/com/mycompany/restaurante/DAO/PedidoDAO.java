// PedidoDAO.java — versión corregida con insertar() y cerrar()
package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.OrdenItem;
import com.mycompany.restaurante.Modelo.Producto;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Acceso a datos sobre la creación y gestión de pedidos {@code ordenes}.
 * 
 * Se encarga de agregar, eliminar y gestionar los pedidos generados por el
 * mesero.
 *
 * @author Citlaly
 * @version 1
 */
public class PedidoDAO {

    private static final Logger LOG = Logger.getLogger(PedidoDAO.class.getName());

    /**
     * Inserta una orden con su detalle en una sola transacción ACID.
     *
     * @param idMesa → Mesa que genera la orden.
     * @param idEmpleado → Mesero que registra (necesitas pasarlo desde LoginController).
     * @param items → Lista de productos con cantidad > 0.
     * @return idOrden generado, o -1 si falló.
     */
    public static int insertarOrdenCompleta(int idMesa, int idEmpleado,
                                             List<Producto> items) {

        String sqlOrden = "INSERT INTO ordenes (idMesa, idEmpleado) VALUES (?,?)";
        String sqlDetalle = "INSERT INTO detalle_orden (idOrden, idProducto, cantidad, precioUnit)"
                          + " VALUES (?,?,?,?)";
        String sqlMesa = "UPDATE mesas SET estado='Ocupada' WHERE idMesa=?";

        try (Connection con = ConexionDB.getConexion()) {

            con.setAutoCommit(false); 

            try {
                // 1. Insertar cabecera de la orden
                int idOrden;
                try (PreparedStatement psOrden =
                         con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                    psOrden.setInt(1, idMesa);
                    psOrden.setInt(2, idEmpleado);
                    psOrden.executeUpdate();

                    try (ResultSet keys = psOrden.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No se generó ID de orden.");
                        idOrden = keys.getInt(1);
                    }
                }
                LOG.info("PedidoDAO: orden creada con idOrden=" + idOrden);

                // 2. Insertar cada ítem del detalle
                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                    for (Producto p : items) {
                        int cant = p.getCantidadPedida();
                        if (cant <= 0) continue;

                        int idProducto = p.getCantidadPedida(); 
                        
                        psDetalle.setInt(1, idOrden);
                        psDetalle.setInt(2, idProducto);
                        psDetalle.setInt(3, cant);
                        psDetalle.setDouble(4, p.getPrecio());
                        psDetalle.addBatch(); 
                    }
                    psDetalle.executeBatch();
                }

                // 3. Marcar la mesa como Ocupada
                try (PreparedStatement psMesa = con.prepareStatement(sqlMesa)) {
                    psMesa.setInt(1, idMesa);
                    psMesa.executeUpdate();
                }

                con.commit();
                LOG.info("PedidoDAO: transacción confirmada. idOrden=" + idOrden);
                return idOrden;

            } catch (SQLException e) {
                con.rollback();
                LOG.log(Level.SEVERE,
                    "PedidoDAO.insertarOrdenCompleta(): rollback. Mesa=" + idMesa, e);
                throw e;
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "PedidoDAO: error de conexión", e);
            return -1;
        }
    }

    /**
     * Retorna el idOrden abierto para una mesa, o 0 si no hay ninguno.
     */
    public static int obtenerOrdenAbiertaPorMesa(int idMesa) {
        String sql = "SELECT idOrden FROM ordenes WHERE idMesa=? AND estado='Abierta' LIMIT 1";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("idOrden");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "PedidoDAO.obtenerOrdenAbiertaPorMesa()", e);
        }
        return 0;
    }

    /**
     * Retorna los ítems de una orden para mostrarlos en la pantalla del cajero.
     */
    public static ObservableList<OrdenItem> obtenerDetalleOrden(int idOrden) {
        ObservableList<OrdenItem> lista = FXCollections.observableArrayList();
        String sql = """
            SELECT p.nombre, d.cantidad, d.precioUnit
            FROM detalle_orden d
            JOIN productos p ON d.idProducto = p.idProductos
            WHERE d.idOrden = ?
            """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new OrdenItem(
                        rs.getString("nombre"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precioUnit")
                    ));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "PedidoDAO.obtenerDetalleOrden()", e);
        }
        return lista;
    }
}