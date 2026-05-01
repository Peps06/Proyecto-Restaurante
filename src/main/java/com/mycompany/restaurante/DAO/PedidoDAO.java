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
 * Capa de Acceso a Datos (DAO) para la entidad Pedido y sus detalles.
 * Gestiona la persistencia de las órdenes en la base de datos, asegurando
 * la atomicidad de las operaciones mediante transacciones.
 * 
 * @author Citlaly
 * @version 1.0
 */
public class PedidoDAO {

    private static final Logger LOG = Logger.getLogger(PedidoDAO.class.getName());

    /**
     * Registra una orden completa en la base de datos.
     * Realiza tres operaciones en una sola transacción:
     * 1. Inserta la cabecera de la orden.
     * 2. Inserta los productos en el detalle_orden (vía Batch).
     * 3. Actualiza el estado de la mesa a 'Ocupada'.
     *
     * @param idMesa ID de la mesa física asignada.
     * @param idEmpleado ID del mesero que toma el pedido.
     * @param items Lista de productos seleccionados.
     * @return El idOrden generado por la base de datos, o -1 en caso de error.
     */
    public static int insertarOrdenCompleta(int idMesa, int idEmpleado, List<Producto> items) {

        String sqlOrden = "INSERT INTO ordenes (idMesa, idEmpleado) VALUES (?,?)";
        String sqlDetalle = "INSERT INTO detalle_orden (idOrden, idProducto, cantidad, precioUnit) VALUES (?,?,?,?)";
        String sqlMesa = "UPDATE mesas SET estado='Ocupada' WHERE idMesa=?";

        try (Connection con = ConexionDB.getConexion()) {
            // Desactivar auto-commit para manejar la transacción manualmente (ACID)
            con.setAutoCommit(false); 

            try {
                // 1. INSERTAR CABECERA DE LA ORDEN
                int idOrden;
                try (PreparedStatement psOrden = con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                    psOrden.setInt(1, idMesa);
                    psOrden.setInt(2, idEmpleado);
                    psOrden.executeUpdate();

                    try (ResultSet keys = psOrden.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Error: No se pudo recuperar el ID de la nueva orden.");
                        idOrden = keys.getInt(1);
                    }
                }
                LOG.info("PedidoDAO: Cabecera creada exitosamente. ID: " + idOrden);

                // 2. INSERTAR DETALLES
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

                // 3. ACTUALIZAR ESTADO DE LA MESA
                try (PreparedStatement psMesa = con.prepareStatement(sqlMesa)) {
                    psMesa.setInt(1, idMesa);
                    psMesa.executeUpdate();
                }

                // Si todo fue bien, se guardan los cambios permanentemente
                con.commit();
                LOG.info("PedidoDAO: Transacción completada con éxito. Orden: " + idOrden);
                return idOrden;

            } catch (SQLException e) {
                // Si algo falla, se revierte todos los cambios para evitar datos huérfanos
                con.rollback();
                LOG.log(Level.SEVERE, "PedidoDAO: Error en la transacción. Se realizó Rollback. Mesa: " + idMesa, e);
                throw e;
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "PedidoDAO: Error crítico de conexión a la base de datos.", e);
            return -1;
        }
    }

    /**
     * Consulta si existe una orden con estado 'Abierta' vinculada a una mesa específica.
     * 
     * @param idMesa ID de la mesa a consultar.
     * @return El idOrden si existe una cuenta abierta, 0 en caso contrario.
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
            LOG.log(Level.SEVERE, "PedidoDAO: Error al buscar orden abierta para mesa " + idMesa, e);
        }
        return 0;
    }

    /**
     * Recupera el listado de productos y cantidades asociados a una orden.
     * Realiza un JOIN con la tabla de productos para obtener los nombres descriptivos.
     * 
     * @param idOrden ID de la orden a consultar.
     * @return ObservableList de OrdenItem, ideal para vincular directamente a una TableView de JavaFX.
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
            LOG.log(Level.SEVERE, "PedidoDAO: Error al obtener detalle de la orden #" + idOrden, e);
        }
        return lista;
    }
}