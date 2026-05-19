package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.OrdenCocina;
import com.mycompany.restaurante.Modelo.OrdenItem;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para el historial de órdenes visto desde cocina.
 * 
 * Permite consultar las órdenes ya preparadas o entregadas,
 * filtrando por fecha y turno (Matutino / Vespertino).
 *
 * Horarios para los turnos:
 *   - Matutino: 06:00 – 14:59
 *   - Vespertino: 15:00 – 22:59
 *
 * @author Citlaly
 * @version 1.0
 */
public class HistorialCocinaDAO {

    private static final Logger LOG = Logger.getLogger(HistorialCocinaDAO.class.getName());

    //  Rango de hora de cada turno 
    public static final String TURNO_MATUTINO   = "Matutino";
    public static final String TURNO_VESPERTINO = "Vespertino";

    private static final String HORA_MAT_INICIO = "06:00:00";
    private static final String HORA_MAT_FIN    = "14:59:59";
    private static final String HORA_VES_INICIO = "15:00:00";
    private static final String HORA_VES_FIN    = "22:59:59";

    //  SQL base
    /**
     * Trae todas las órdenes cuyo estado sea 'Cerrada' o 'Facturada'
     * (es decir, ya fueron atendidas) y cuya preparacion sea 'Preparado'
     * o 'Entregado', para la fecha y turno indicados.
     *
     * Se hace JOIN con detalle_orden y productos para traer los ítems
     * en una sola consulta y agrupar en Java.
     */
    private static final String SQL_HISTORIAL = """
            SELECT
                o.idOrden,
                o.idMesa,
                o.detalles,
                o.preparacion,
                o.fechaHora AS fechaOrden,
                p.nombre AS nombreProducto,
                d.cantidad AS cantidadProducto,
                d.precioUnit
            FROM ordenes o
            JOIN detalle_orden d ON d.idOrden = o.idOrden
            JOIN productos p ON p.idProductos = d.idProducto
            WHERE DATE(o.fechaHora) = ?
              AND TIME(o.fechaHora) BETWEEN ? AND ?
              AND o.preparacion IN ('Preparado', 'Entregado')
            ORDER BY o.fechaHora ASC, o.idOrden ASC, p.nombre ASC
            """;

    /**
     * Cuenta cuántas órdenes hubo en el turno indicado para esa fecha.
     * Útil para el resumen rápido del encabezado.
     */
    private static final String SQL_RESUMEN = """
            SELECT COUNT(DISTINCT o.idOrden) AS total,
                   COALESCE(SUM(d.cantidad * d.precioUnit), 0) AS montoTotal
            FROM ordenes o
            JOIN detalle_orden d ON d.idOrden = o.idOrden
            WHERE DATE(o.fechaHora) = ?
              AND TIME(o.fechaHora) BETWEEN ? AND ?
              AND o.preparacion IN ('Preparado', 'Entregado')
            """;

    // ─

    /**
     * Devuelve la lista de {@link OrdenCocina} (con sus ítems) para
     * la fecha y turno seleccionados.
     *
     * @param fecha  Fecha a consultar.
     * @param turno  {@link #TURNO_MATUTINO} o {@link #TURNO_VESPERTINO}.
     * @return Lista ordenada cronológicamente.
     */
    public static List<OrdenCocina> obtenerHistorial(LocalDate fecha, String turno) {
        String[] rango = rangoHoras(turno);
        List<OrdenCocina> resultado = new ArrayList<>();
        Map<Integer, OrdenCocina> mapa = new LinkedHashMap<>();

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_HISTORIAL)) {

            ps.setDate(1, Date.valueOf(fecha));
            ps.setString(2, rango[0]);
            ps.setString(3, rango[1]);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int    idOrden  = rs.getInt("idOrden");
                    int    idMesa   = rs.getInt("idMesa");
                    String detalles = rs.getString("detalles");
                    String prep     = rs.getString("preparacion");

                    OrdenCocina orden = mapa.computeIfAbsent(idOrden,
                            id -> new OrdenCocina(id, idMesa, detalles, prep, new ArrayList<>()));

                    orden.getItems().add(new OrdenItem(
                            rs.getString("nombreProducto"),
                            rs.getInt("cantidadProducto"),
                            rs.getDouble("precioUnit")
                    ));
                }
            }

            resultado.addAll(mapa.values());
            LOG.info("HistorialCocinaDAO: " + resultado.size() + " órdenes en historial.");

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "HistorialCocinaDAO: error al obtener historial.", e);
        }

        return resultado;
    }

    /**
     * Devuelve un arreglo con totalOrdenes y montoTotal para el resumen
     * del encabezado de la pantalla.
     *
     * @param fecha  Fecha a consultar.
     * @param turno  Turno a consultar.
     * @return int[0] = cantidad de órdenes, double en [1] no cabe en int[]
     *         → se devuelve como double[]{totalOrdenes, montoTotal}.
     */
    public static double[] obtenerResumen(LocalDate fecha, String turno) {
        String[] rango = rangoHoras(turno);
        double[] resumen = {0, 0};

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_RESUMEN)) {

            ps.setDate(1, Date.valueOf(fecha));
            ps.setString(2, rango[0]);
            ps.setString(3, rango[1]);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resumen[0] = rs.getInt("total");
                    resumen[1] = rs.getDouble("montoTotal");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "HistorialCocinaDAO: error al obtener resumen.", e);
        }

        return resumen;
    }

    // Helpers 

    /**
     * Devuelve el par (horaInicio, horaFin) según el turno.
     */
    private static String[] rangoHoras(String turno) {
        if (TURNO_VESPERTINO.equals(turno)) {
            return new String[]{HORA_VES_INICIO, HORA_VES_FIN};
        }
        return new String[]{HORA_MAT_INICIO, HORA_MAT_FIN};
    }
}