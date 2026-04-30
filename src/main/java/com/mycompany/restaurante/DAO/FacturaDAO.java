package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.DatosFacturacion;
import com.mycompany.restaurante.Modelo.Factura;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Capa de Acceso a Datos (DAO) para la gestión de facturas fiscales.
 * Centraliza la inserción de registros en la tabla {@code facturas} y 
 * la actualización de estados de las órdenes correspondientes.
 * 
 * @author Citlaly
 * @version 1.2
 */
public class FacturaDAO {

    private static final Logger LOG = Logger.getLogger(FacturaDAO.class.getName());

    /**
     * Registra una factura en la base de datos y actualiza el estado de la orden.
     * Si el registro de la factura falla o la actualización de la orden no
     * se concreta, se deshacen todos los cambios.
     *
     * @param factura Objeto con los datos de la factura (folio, fecha, total).
     * @param idOrden Identificador de la orden que se está facturando.
     * @param subtotal Valor de la venta antes de impuestos.
     * @param iva Monto del impuesto calculado.
     * @return true si la transacción se confirmó (commit); false si falló la conexión o validación.
     * @throws SQLException Si ocurre un error durante la ejecución de las sentencias SQL.
     */
    public static boolean insertar(Factura factura, int idOrden, double subtotal, double iva) throws SQLException {

        // Validación de seguridad para evitar registros solos
        if (idOrden <= 0) {
            LOG.warning("FacturaDAO: Intento de facturación fallido. idOrden inválido (" + idOrden + ").");
            return false;
        }

        String sqlFactura = """
            INSERT INTO facturas
              (folio, idOrden, nombreRazonSoc, rfc, codigoPostal,
               correo, regimenFiscal, usoCfdi, subtotal, iva, total, fechaHora)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
            """;

        String sqlCerrarOrden = "UPDATE ordenes SET estado='Facturada' WHERE idOrden=?";
        
        String sqlLiberarMesa = """
            UPDATE mesas 
            SET estado = 'Libre' 
            WHERE idMesa = (SELECT idMesa FROM ordenes WHERE idOrden = ?)
            """;

        DatosFacturacion d = factura.getDatosFiscales();

        try (Connection con = ConexionDB.getConexion()) {

            // Se inicializa la transacción manual para asegurar consistencia
            con.setAutoCommit(false); 

            try (PreparedStatement psFactura = con.prepareStatement(sqlFactura);
                 PreparedStatement psOrden = con.prepareStatement(sqlCerrarOrden);
                 PreparedStatement psMesa = con.prepareStatement(sqlLiberarMesa)) {

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
                
                psMesa.setInt(1, idOrden);
                int filasMesa = psMesa.executeUpdate();

                int filasFactura = psFactura.executeUpdate();
                LOG.info("FacturaDAO: Registro de factura exitoso. Filas: " + filasFactura);

                // 2. Mapeo de parámetros para cerrar la ORDEN 
                psOrden.setInt(1, idOrden);
                int filasOrden = psOrden.executeUpdate();
                LOG.info("FacturaDAO: Estado de orden #" + idOrden + " actualizado a 'Facturada'.");

                // Se confirman los cambios en la base de datos
                con.commit();
                LOG.info("FacturaDAO: Transacción completada (Factura creada + Orden cerrada + Mesa liberada).");
                return true;

            } catch (SQLException e) {
                // En caso de cualquier error, ses revierten ambos INSERT/UPDATE
                con.rollback();
                LOG.log(Level.SEVERE, "FacturaDAO: Error en proceso. Rollback ejecutado. idOrden=" + idOrden, e);
                throw e; // Re-lanzamos para que la UI pueda capturarlo y avisar al usuario
            }

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "FacturaDAO: Error al establecer conexión con el servidor de datos.", e);
            return false;
        }
    }

    /**
     * Valida la existencia de una factura previa para una orden específica.
     * Método de control para prevenir duplicidad de folios fiscales.
     * 
     * @param idOrden ID de la orden a verificar.
     * @return true si ya existe al menos un registro; false si la orden no ha sido facturada.
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
            LOG.log(Level.SEVERE, "FacturaDAO: Error al verificar existencia de factura para orden " + idOrden, e);
        }
        return false;
    }
}