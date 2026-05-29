package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.DatosFacturacion;
import com.mycompany.restaurante.Modelo.Factura;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Capa de Acceso a Datos (DAO) para la gestión de facturas fiscales.
 * Centraliza la inserción en la tabla {@code facturas} y las actualizaciones
 * de estado de las órdenes y mesas correspondientes.
 *
 * A partir de la versión 1.3, al generar una factura la mesa pasa al estado
 * 'Cobrada' en lugar de 'Libre', igual que cuando se cobra sin factura.
 * El mesero liberará físicamente la mesa desde su pantalla.
 *
 * @author Citlaly
 * @version 1.3 (mesa a estado Cobrada tras la facturación)
 */
public class FacturaDAO {

    private static final Logger LOG = Logger.getLogger(FacturaDAO.class.getName());

    /**
     * Registra una factura en la base de datos dentro de una transacción.
     * Pasos que ejecuta:
     * 1. Inserta el registro en {@code facturas}.
     * 2. Actualiza la orden a estado 'Facturada'.
     * 3. Cambia el estado de la mesa a 'Cobrada'.
     *
     * Si cualquier paso falla se ejecuta un rollback completo.
     *
     * @param factura  Objeto con los datos de la factura (folio, fecha, total).
     * @param idOrden  Identificador de la orden que se está facturando.
     * @param subtotal Valor de la venta antes de impuestos.
     * @param iva      Monto del impuesto calculado.
     * @return {@code true} si la transacción se confirmó correctamente.
     * @throws SQLException Si ocurre un error durante la ejecución SQL.
     */
    public static boolean insertar(Factura factura, int idOrden,
                                    double subtotal, double iva) throws SQLException {
 
        // Validación previa para evitar registros huérfanos
        if (idOrden <= 0) {
            LOG.warning("FacturaDAO: Intento de facturación fallido. "
                      + "idOrden inválido (" + idOrden + ").");
            return false;
        }
 
        String sqlFactura = "INSERT INTO facturas "
                + "(folio, idOrden, nombreRazonSoc, rfc, codigoPostal, "
                + " correo, regimenFiscal, usoCfdi, subtotal, iva, total, fechaHora) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
 
        String sqlCerrarOrden = "UPDATE ordenes SET estado='Facturada' WHERE idOrden=?";
 
        // La mesa pasa a 'Cobrada', no a 'Libre'; el mesero la libera manualmente
        String sqlMesaCobrada =
            "UPDATE mesas SET estado = 'Cobrada' "
            + "WHERE idMesa = (SELECT idMesa FROM ordenes WHERE idOrden = ?)";
 
        DatosFacturacion d = factura.getDatosFiscales();
 
        try (Connection con = ConexionDB.getConexion()) {
 
            // Transacción manual para garantizar consistencia
            con.setAutoCommit(false);
 
            try (PreparedStatement psFactura = con.prepareStatement(sqlFactura);
                 PreparedStatement psOrden = con.prepareStatement(sqlCerrarOrden);
                 PreparedStatement psMesa = con.prepareStatement(sqlMesaCobrada)) {
 
                // 1. Mapeo de parámetros para la FACTURA
                psFactura.setString(1, factura.getFolio());
                psFactura.setInt(2, idOrden);
                psFactura.setString(3, d.getNombreRazonSocial());
                psFactura.setString(4, d.getRfc());
                psFactura.setString(5, d.getCodigoPostal());
                psFactura.setString(6, d.getCorreo());
                psFactura.setString(7, d.getRegimenFiscal());
                psFactura.setString(8, d.getUsoCfdi());
                psFactura.setDouble(9, subtotal);
                psFactura.setDouble(10, iva);
                psFactura.setDouble(11, factura.getTotal());
                psFactura.setTimestamp(12, Timestamp.valueOf(factura.getFechaHora()));
 
                int filasFactura = psFactura.executeUpdate();
                LOG.info("FacturaDAO: Registro de factura exitoso. Filas: "
                       + filasFactura);
 
                // 2. Cerrar la orden como 'Facturada'
                psOrden.setInt(1, idOrden);
                int filasOrden = psOrden.executeUpdate();
                LOG.info("FacturaDAO: Estado de orden #" + idOrden
                       + " actualizado a 'Facturada'.");
 
                // 3. Marcar la mesa como 'Cobrada' (no libre aún)
                psMesa.setInt(1, idOrden);
                int filasMesa = psMesa.executeUpdate();
                LOG.info("FacturaDAO: Mesa asociada a orden #" + idOrden
                       + " marcada como Cobrada. Filas: " + filasMesa);
 
                con.commit();
                LOG.info("FacturaDAO: Transacción completada "
                       + "(Factura creada + Orden cerrada + Mesa Cobrada).");
                return true;
 
            } catch (SQLException e) {
                // Revertir todos los cambios si algo falló
                con.rollback();
                LOG.log(Level.SEVERE,
                    "FacturaDAO: Error en proceso. Rollback ejecutado. idOrden="
                    + idOrden, e);
                throw e;
            }
 
        } catch (SQLException e) {
            LOG.log(Level.SEVERE,
                "FacturaDAO: Error al establecer conexión. idOrden=" + idOrden, e);
            return false;
        }
    }

    /**
     * Valida si ya existe una factura para una orden específica.
     * Se usa para prevenir duplicidad de folios fiscales.
     *
     * @param idOrden ID de la orden a verificar.
     * @return {@code true} si ya existe al menos un registro de factura.
     */
    public static boolean existeFacturaParaOrden(int idOrden) {
        String sql = "SELECT COUNT(*) FROM facturas WHERE idOrden=?";
 
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE,
                "FacturaDAO: Error al verificar existencia de factura para orden "
                + idOrden, e);
        }
        return false;
    }
}