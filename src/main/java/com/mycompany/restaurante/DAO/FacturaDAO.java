// FacturaDAO.java — versión corregida
package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.DatosFacturacion;
import com.mycompany.restaurante.Modelo.Factura;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Acceso a datos para la tabla {@code facturas}.
 * 
 * Se encarga de insertar facturas, el idOrden, el subtotal de la venta y el iva
 * y todo ello lo guarda en la base de datos
 *
 * @author Citlaly
 * @version 1.2
 */

public class FacturaDAO {

    private static final Logger LOG = Logger.getLogger(FacturaDAO.class.getName());

    /**
     * Inserta una factura dentro de una transacción explícita.
     * Si algo falla se hace rollback completo.
     *
     * @return true si se insertó correctamente.
     */
    public static boolean insertar(Factura factura, int idOrden,
                                   double subtotal, double iva) {

        // Verifica que la orden exista
        if (idOrden <= 0) {
            LOG.warning("FacturaDAO.insertar(): idOrden inválido = " + idOrden
                        + ". La orden no fue guardada antes de facturar.");
            return false;
        }

        String sqlFactura = """
            INSERT INTO facturas
              (folio, idOrden, nombreRazonSoc, rfc, codigoPostal,
               correo, regimenFiscal, usoCfdi, subtotal, iva, total, fechaHora)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
            """;

        String sqlCerrarOrden =
            "UPDATE ordenes SET estado='Facturada' WHERE idOrden=?";

        DatosFacturacion d = factura.getDatosFiscales();

        try (Connection con = ConexionDB.getConexion()) {

            con.setAutoCommit(false); // inicio de transacción

            try (PreparedStatement psFactura = con.prepareStatement(sqlFactura);
                 PreparedStatement psOrden   = con.prepareStatement(sqlCerrarOrden)) {

                // INSERT factura 
                psFactura.setString(1, factura.getFolio());
                psFactura.setInt(2, idOrden);
                psFactura.setString(3, d.getNombreRazonSocial());
                psFactura.setString(4, d.getRfc());
                psFactura.setString(5, d.getCodigoPostal());
                psFactura.setString(6, d.getCorreo());
                psFactura.setString(7, d.getRegimenFiscal());
                psFactura.setString(8, d.getUsoCfdi());
                psFactura.setDouble(9,  subtotal);
                psFactura.setDouble(10, iva);
                psFactura.setDouble(11, factura.getTotal());
                psFactura.setTimestamp(12, Timestamp.valueOf(factura.getFechaHora()));

                int filasFactura = psFactura.executeUpdate();
                LOG.info("FacturaDAO: INSERT factura → filas afectadas = " + filasFactura);

                // orden actualizada
                psOrden.setInt(1, idOrden);
                int filasOrden = psOrden.executeUpdate();
                LOG.info("FacturaDAO: UPDATE ordenes → filas afectadas = " + filasOrden);

                con.commit();
                LOG.info("FacturaDAO: transacción confirmada. Folio: " + factura.getFolio());
                return true;

            } catch (SQLException e) {
                con.rollback();  // ✅ cualquier falla: deshacer todo
                LOG.log(Level.SEVERE,
                    "FacturaDAO.insertar(): ERROR, rollback ejecutado. idOrden=" + idOrden, e);
                throw e;         // re-lanzar para que el controlador muestre alerta
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "FacturaDAO.insertar(): no se pudo abrir conexión", e);
            return false;
        }
    }

    /** Verifica si ya existe una factura para una orden (evita doble facturación). */
    public static boolean existeFacturaParaOrden(int idOrden) {
        String sql = "SELECT COUNT(*) FROM facturas WHERE idOrden=?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "FacturaDAO.existeFacturaParaOrden()", e);
        }
        return false;
    }
}