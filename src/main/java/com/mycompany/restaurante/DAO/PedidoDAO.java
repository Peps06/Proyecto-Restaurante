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
 * @version 2.1
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
    public static int insertarOrdenCompleta(int idMesa, int idEmpleado, List<Producto> items, String detalles) {

        String sqlOrden = "INSERT INTO ordenes (idMesa, idEmpleado, detalles) VALUES (?,?,?)";
        String sqlDetalle = "INSERT INTO detalle_orden (idOrden, idProducto, cantidad, precioUnit) VALUES (?,?,?,?)";
        String sqlMesa = "UPDATE mesas SET estado='Ocupada' WHERE idMesa=?";

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

                // 2. INSERTAR DETALLES
                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                    for (Producto p : items) {
                        int cant = p.getCantidadPedida();
                        if (cant <= 0) continue;

                        int idProducto = p.getId();
                        System.out.println("[PedidoDAO] Insertando detalle: "
                            + "idOrden=" + idOrden
                            + " idProducto=" + idProducto
                            + " cant=" + cant
                            + " precio=" + p.getPrecio());

                        psDetalle.setInt(1, idOrden);
                        psDetalle.setInt(2, idProducto);
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
                        "PedidoDAO: Error en la transacción. Se realizó Rollback. Mesa: "
                        + idMesa, e);
                throw e;
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "PedidoDAO: Error crítico de conexión a la base de datos.", e);
            return -1;
        }
    }
    
    /**
    * Añade productos adicionales a una orden existente utilizando Batch.
    * 
    * @param idOrden ID de la orden abierta.
    * @param items Lista de productos donde se filtrarán los que tengan cantidad > 0.
    * @return true si la inserción fue exitosa, false en caso contrario.
    */
   public static boolean añadirPlatillosAOrden(int idOrden, List<Producto> items) {
       String sqlDetalle = "INSERT INTO detalle_orden ("+
                                "idOrden, idProducto, cantidad, precioUnit"+
                            ") VALUES (?,?,?,?)";

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
               LOG.info("PedidoDAO: Platillos adicionales agregados a la orden #"
                       + idOrden);
               return true;

           } catch (SQLException e) {
               con.rollback();
               LOG.log(Level.SEVERE,
                       "PedidoDAO: Error al añadir platillos a la orden #" +
                        idOrden + ". Rollback ejecutado.", e);
               throw e;
           }
       } catch (SQLException e) {
           LOG.log(Level.SEVERE,
                   "PedidoDAO: Error de conexión al añadir platillos.", e);
           return false;
       }
   }

    /**
     * Consulta si existe una orden con estado 'Abierta' vinculada a una mesa específica.
     *
     * @param idMesa ID de la mesa a consultar.
     * @return El idOrden si existe una cuenta abierta, 0 en caso contrario.
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
     * Consulta el campo {@code preparacion} de una orden específica.
     * Útil para validar si una orden ya fue marcada como 'Entregado' antes
     * de permitir el cobro en caja.
     *
     * @param idOrden ID de la orden a consultar.
     * @return El valor del campo preparacion ("En espera", "Preparado" o "Entregado"),
     *         o una cadena vacía si la orden no existe o hay un error.
     */
    public static String obtenerPreparacionOrden(int idOrden) {
        String sql = "SELECT preparacion FROM ordenes WHERE idOrden = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("preparacion");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE,
                "PedidoDAO: Error al consultar preparacion de la orden #" + idOrden, e);
        }
        return "";
    }

    /**
     * Recupera el listado de productos y cantidades asociados a una orden.
     *
     * @param idOrden ID de la orden a consultar.
     * @return ObservableList de OrdenItem listos para vincular a TableView.
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
            LOG.log(Level.SEVERE,
                "PedidoDAO: Error obteniendo detalle de orden #" + idOrden, e);
        }
        return lista;
    }

    /**
     * Consulta todas las órdenes abiertas cuya columna {@code preparacion}
     * sea 'En espera', junto con todos sus ítems de detalle_orden.
     *
     * @return Lista de {@link OrdenCocina} ordenada por idOrden ascendente.
     */
    public static List<OrdenCocina> obtenerOrdenesEnEspera() {
        List<OrdenCocina> resultado = new ArrayList<>();

        String sql = """
            SELECT o.idOrden,
                   o.idMesa,
                   o.detalles,
                   o.preparacion,
                   p.nombre AS nombreProducto,
                   d.cantidad AS cantidadProducto,
                   d.precioUnit
            FROM ordenes o
            JOIN detalle_orden d ON d.idOrden = o.idOrden
            JOIN productos p ON p.idProductos = d.idProducto
            WHERE o.estado = 'Abierta'
            AND o.preparacion = 'En espera'
            ORDER BY o.idOrden ASC, p.nombre ASC
            """;

        Map<Integer, OrdenCocina> mapa = new LinkedHashMap<>();

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int    idOrden  = rs.getInt("idOrden");
                int    idMesa   = rs.getInt("idMesa");
                String detalles = rs.getString("detalles");
                String prep     = rs.getString("preparacion");

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
                     + " órdenes en espera recuperadas.");

        } catch (SQLException e) {
            LOG.log(Level.SEVERE,
                "PedidoDAO: Error al obtener órdenes en espera.", e);
        }

        return resultado;
    }

    /**
     * Actualiza el campo {@code preparacion} de la orden a 'Preparado'.
     *
     * @param idOrden ID de la orden a actualizar.
     * @return {@code true} si la actualización afectó al menos una fila.
     */
    public static boolean marcarOrdenPreparada(int idOrden) {
        String sql = "UPDATE ordenes "
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
                "PedidoDAO: Error al marcar orden #" + idOrden + " como preparada.", e);
            return false;
        }
    }
}