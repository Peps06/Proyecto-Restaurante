package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.OrdenCocina;
import com.mycompany.restaurante.Modelo.OrdenItem;
import com.mycompany.restaurante.Modelo.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * @version 2.2 (preparación por detalle_orden)
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
     * @param detalles Texto libre del mesero (puede ser null o vacío).
     * @return El idOrden generado por la base de datos, o -1 en caso de error.
     */
     public static int insertarOrdenCompleta(int idMesa, int idEmpleado,
                                             List<Producto> items, String detalles) {
 
        String sqlOrden = "INSERT INTO ordenes (idMesa, idEmpleado, detalles) VALUES (?,?,?)";
        // Incluimos 'preparacion' explícitamente; el DEFAULT 'En espera' ya lo cubre
        // pero lo ponemos para claridad y compatibilidad con versiones anteriores.
        String sqlDetalle = "INSERT INTO detalle_orden "
                          + "(idOrden, idProducto, cantidad, precioUnit, preparacion) "
                          + "VALUES (?,?,?,?,'En espera')";
        String sqlMesa    = "UPDATE mesas SET estado='Ocupada' WHERE idMesa=?";
 
        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);
 
            try {
                // 1. INSERTAR CABECERA DE LA ORDEN
                int idOrden;
                try (PreparedStatement psOrden = con.prepareStatement(
                        sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
 
                    psOrden.setInt(1, idMesa);
                    psOrden.setInt(2, idEmpleado);
 
                    if (detalles != null && !detalles.isBlank()) {
                        psOrden.setString(3, detalles.trim());
                    } else {
                        psOrden.setNull(3, Types.VARCHAR);
                    }
 
                    psOrden.executeUpdate();
                    LOG.info("PedidoDAO: INSERT ordenes ejecutado.");
 
                    try (ResultSet keys = psOrden.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se recuperó el ID de la orden.");
                        }
                        idOrden = keys.getInt(1);
                    }
                }
                LOG.info("PedidoDAO: Cabecera creada. idOrden=" + idOrden);
 
                // 2. INSERTAR DETALLES (cada platillo en espera)
                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                    for (Producto p : items) {
                        int cant = p.getCantidadPedida();
                        if (cant <= 0) continue;
 
                        LOG.info("[PedidoDAO] Insertando detalle: idOrden=" + idOrden
                               + " idProducto=" + p.getId()
                               + " cant=" + cant
                               + " precio=" + p.getPrecio());
 
                        psDetalle.setInt(1, idOrden);
                        psDetalle.setInt(2, p.getId());
                        psDetalle.setInt(3, cant);
                        psDetalle.setDouble(4, p.getPrecio());
                        psDetalle.addBatch();
                    }
                    psDetalle.executeBatch();
                }
                LOG.info("PedidoDAO: Detalles insertados en detalle_orden.");
 
                // 3. ACTUALIZAR ESTADO DE LA MESA
                try (PreparedStatement psMesa = con.prepareStatement(sqlMesa)) {
                    psMesa.setInt(1, idMesa);
                    psMesa.executeUpdate();
                }
                LOG.info("PedidoDAO: Mesa " + idMesa + " marcada como Ocupada.");
 
                con.commit();
                LOG.info("PedidoDAO: Transacción completada. Orden=" + idOrden);
                return idOrden;
 
            } catch (SQLException e) {
                con.rollback();
                LOG.log(Level.SEVERE,
                        "PedidoDAO: Error en la transacción. Rollback ejecutado. Mesa: "
                        + idMesa, e);
                throw e;
            }
 
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "PedidoDAO: Error crítico de conexión.", e);
            return -1;
        }
    }

    /**
     * Añade platillos adicionales a una orden existente.
     * Los nuevos registros en {@code detalle_orden} se insertan con estado
     * 'En espera', de modo que el chef los reciba como una nueva tanda
     * sin mezclarlos con los platillos que ya marcó como 'Preparado'.
     *
     * @param idOrden ID de la orden abierta a la que se añaden platillos.
     * @param items Lista de productos; solo se insertan los de cantidad > 0.
     * @return {@code true} si la inserción fue exitosa, {@code false} si hubo error.
     */
     public static boolean añadirPlatillosAOrden(int idOrden, List<Producto> items) {
        String sqlDetalle = "INSERT INTO detalle_orden "
                          + "(idOrden, idProducto, cantidad, precioUnit, preparacion) "
                          + "VALUES (?,?,?,?,'En espera')";
 
        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);
 
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                boolean hayDetalles = false;
 
                for (Producto p : items) {
                    int cant = p.getCantidadPedida();
                    if (cant <= 0) continue;
 
                    psDetalle.setInt(1, idOrden);
                    psDetalle.setInt(2, p.getId());
                    psDetalle.setInt(3, cant);
                    psDetalle.setDouble(4, p.getPrecio());
                    psDetalle.addBatch();
                    hayDetalles = true;
                }
 
                if (hayDetalles) {
                    psDetalle.executeBatch();
                }
 
                con.commit();
                LOG.info("PedidoDAO: Platillos adicionales agregados a la orden #" + idOrden);
                return true;
 
            } catch (SQLException e) {
                con.rollback();
                LOG.log(Level.SEVERE,
                        "PedidoDAO: Error al añadir platillos a la orden #"
                        + idOrden + ". Rollback ejecutado.", e);
                throw e;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "PedidoDAO: Error de conexión al añadir platillos.", e);
            return false;
        }
    }

    /**
     * Cancela una unidad de un producto en una orden activa buscando por nombre.
     * Si la cantidad es mayor a 1, resta 1 unidad. Si es exactamente 1, elimina
     * el registro de {@code detalle_orden} por completo.
     *
     * @param idOrden ID de la orden activa.
     * @param nombreProducto Nombre del producto a cancelar.
     * @return {@code true} si se canceló exitosamente, {@code false} en caso contrario.
     */
    public static boolean cancelarPlatillo(int idOrden, String nombreProducto) {
        String sqlCheck = "SELECT d.idDetalle, d.cantidad FROM detalle_orden d "
                         + "JOIN productos p ON d.idProducto = p.idProductos "
                         + "WHERE d.idOrden = ? AND p.nombre = ? LIMIT 1";
        
        String sqlUpdate = "UPDATE detalle_orden SET cantidad = cantidad - 1 "
                         + "WHERE idDetalle = ?";
        
        String sqlDelete = "DELETE FROM detalle_orden WHERE idDetalle = ?";
 
        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);
 
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, idOrden);
                psCheck.setString(2, nombreProducto);
 
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        int idDetalle = rs.getInt("idDetalle");
                        int cantidadActual = rs.getInt("cantidad");
 
                        if (cantidadActual > 1) {
                            try (PreparedStatement psUpdate =
                                         con.prepareStatement(sqlUpdate)) {
                                psUpdate.setInt(1, idDetalle);
                                psUpdate.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement psDelete =
                                         con.prepareStatement(sqlDelete)) {
                                psDelete.setInt(1, idDetalle);
                                psDelete.executeUpdate();
                            }
                        }
 
                        con.commit();
                        return true;
                    } else {
                        con.rollback();
                        return false;
                    }
                }
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "PedidoDAO: Error al cancelar platillo por nombre.", e);
            return false;
        }
    }
    
    /**
     * Verifica si existe una orden con estado 'Abierta' vinculada a una mesa.
     *
     * @param idMesa ID de la mesa a consultar.
     * @return El idOrden si existe una orden abierta, 0 en caso contrario.
     */
    public static int obtenerOrdenAbiertaPorMesa(int idMesa) {
        String sql = "SELECT idOrden FROM ordenes "
                   + "WHERE idMesa=? AND estado='Abierta' LIMIT 1";
 
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("idOrden");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE,
                "PedidoDAO: Error buscando orden abierta para mesa " + idMesa, e);
        }
        return 0;
    }
 
    /**
     * Consulta el campo {@code preparacion} de la cabecera de una orden.
     * Se usa en el módulo de cobro para validar que la orden ya fue entregada.
     *
     * @param idOrden ID de la orden a consultar.
     * @return Valor del campo preparacion, o cadena vacía si hay error.
     */
    public static String obtenerPreparacionOrden(int idOrden) {
        String sql = "SELECT preparacion FROM ordenes WHERE idOrden = ?";
 
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("preparacion");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE,
                "PedidoDAO: Error al consultar preparacion de la orden #" + idOrden, e);
        }
        return "";
    }
 
    /**
     * Recupera el detalle completo de una orden incluyendo el precio unitario.
     * Trae TODOS los platillos sin importar su estado de preparacion,
     * ya que se usa para calcular el cobro y mostrar la cuenta al mesero.
     *
     * @param idOrden ID de la orden a consultar.
     * @return ObservableList de OrdenItem listos para vincular a un TableView.
     */
    public static ObservableList<OrdenItem> obtenerDetalleOrden(int idOrden) {
        ObservableList<OrdenItem> lista = FXCollections.observableArrayList();
 
        String sql = "SELECT p.nombre, d.cantidad, d.precioUnit "
                   + "FROM detalle_orden d "
                   + "JOIN productos p ON d.idProducto = p.idProductos "
                   + "WHERE d.idOrden = ?";
 
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
            LOG.log(Level.SEVERE,
                "PedidoDAO: Error obteniendo detalle de orden #" + idOrden, e);
        }
        return lista;
    }

    /**
     * Consulta las órdenes abiertas que tienen al menos un platillo con
     * {@code detalle_orden.preparacion = 'En espera'}.
     * Solo se devuelven los platillos pendientes de cada orden, de modo que
     * cuando el mesero añade platillos nuevos a una orden ya existente, el chef
     * solo ve la nueva tanda y no los que ya marcó como 'Preparado'.
     *
     * @return Lista de {@link OrdenCocina} ordenada por idOrden ascendente.
     */
    public static List<OrdenCocina> obtenerOrdenesEnEspera() {
        List<OrdenCocina> resultado = new ArrayList<>();
 
        // Filtramos tanto la orden (Abierta) como los detalles (En espera)
        String sql = "SELECT o.idOrden, o.idMesa, o.detalles, o.preparacion, "
                   + "       p.nombre AS nombreProducto, "
                   + "       d.cantidad AS cantidadProducto, "
                   + "       d.precioUnit "
                   + "FROM ordenes o "
                   + "JOIN detalle_orden d ON d.idOrden = o.idOrden "
                   + "JOIN productos p ON p.idProductos = d.idProducto "
                   + "WHERE o.estado = 'Abierta' "
                   + "  AND d.preparacion = 'En espera' "
                   + "ORDER BY o.idOrden ASC, p.nombre ASC";
 
        Map<Integer, OrdenCocina> mapa = new LinkedHashMap<>();
 
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                int idOrden = rs.getInt("idOrden");
                int idMesa = rs.getInt("idMesa");
                String detalles = rs.getString("detalles");
                String prep = rs.getString("preparacion");
 
                // computeIfAbsent agrupa todos los platillos de la misma orden
                OrdenCocina orden = mapa.computeIfAbsent(idOrden,
                    id -> new OrdenCocina(id, idMesa, detalles, prep, new ArrayList<>())
                );
 
                orden.getItems().add(new OrdenItem(
                    rs.getString("nombreProducto"),
                    rs.getInt("cantidadProducto"),
                    rs.getDouble("precioUnit")
                ));
            }
 
            resultado.addAll(mapa.values());
            LOG.info("PedidoDAO: " + resultado.size()
                     + " órdenes con platillos en espera recuperadas.");
 
        } catch (SQLException e) {
            LOG.log(Level.SEVERE,
                "PedidoDAO: Error al obtener órdenes en espera.", e);
        }
 
        return resultado;
    }

    /**
     * Marca como 'Preparado' todos los platillos de una orden que actualmente
     * estén en estado 'En espera' en {@code detalle_orden}.
     *
     * Solo afecta a los platillos presentes en el momento del clic; los que
     * se agreguen después conservarán el estado 'En espera' y volverán a
     * aparecer en la pantalla del chef como una nueva tanda.
     *
     * @param idOrden ID de la orden cuyos detalles pendientes se marcan listos.
     * @return {@code true} si se actualizó al menos un platillo.
     */
    public static boolean marcarOrdenPreparada(int idOrden) {
        String sql = "UPDATE detalle_orden "
                   + "SET preparacion = 'Preparado' "
                   + "WHERE idOrden = ? AND preparacion = 'En espera'";
 
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idOrden);
            int filas = ps.executeUpdate();
            LOG.info("PedidoDAO: marcarOrdenPreparada idOrden=" + idOrden
                     + " filas afectadas=" + filas);
            return filas > 0;
 
        } catch (SQLException e) {
            LOG.log(Level.SEVERE,
                "PedidoDAO: Error al marcar platillos de la orden #"
                + idOrden + " como preparados.", e);
            return false;
        }
    }
}