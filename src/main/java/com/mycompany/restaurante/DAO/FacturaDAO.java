package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.DatosFacturacion;
import com.mycompany.restaurante.Modelo.Factura;

import java.sql.*;

/**
 * Acceso a datos para la tabla {@code facturas}.
 * 
 * Se encarga de insertar facturas, el idOrden, el subtotal de la venta y el iva
 * y todo ello lo guarda en la base de datos
 *
 * @author Dana, Rubi y Citlaly
 */
public class FacturaDAO {

    /**
     * Inserta una factura completa en la tabla {@code facturas}.
     *
     * @param factura → Objeto Factura ya construido (tiene folio, fechaHora, datos fiscales).
     * @param idOrden → ID de la orden asociada a la mesa cobrada.
     * @param subtotal → Subtotal antes de IVA.
     * @param iva → Monto del IVA calculado (subtotal × 0.16).
     * @return {@code true} si se insertó correctamente.
     */
    public static boolean insertar(Factura factura, int idOrden, double subtotal, double iva) {
        String sql = """
            INSERT INTO facturas
              (folio, idOrden, nombreRazonSoc, rfc, codigoPostal,
               correo, regimenFiscal, usoCfdi, subtotal, iva, total, fechaHora)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
            """;

        DatosFacturacion d = factura.getDatosFiscales();

        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1,  factura.getFolio());
            ps.setInt(2,     idOrden);
            ps.setString(3,  d.getNombreRazonSocial());
            ps.setString(4,  d.getRfc());
            ps.setString(5,  d.getCodigoPostal());
            ps.setString(6,  d.getCorreo());
            ps.setString(7,  d.getRegimenFiscal());
            ps.setString(8,  d.getUsoCfdi());
            ps.setDouble(9,  subtotal);
            ps.setDouble(10, iva);
            ps.setDouble(11, factura.getTotal());
            ps.setTimestamp(12, Timestamp.valueOf(factura.getFechaHora()));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("FacturaDAO.insertar(): " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si ya existe una factura para una orden dada.
     * Ayuda a evitar doble facturación.
     */
    public static boolean existeFacturaParaOrden(int idOrden) {
        String sql = "SELECT COUNT(*) FROM facturas WHERE idOrden=?";

        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("FacturaDAO.existeFacturaParaOrden(): " + e.getMessage());
        }
        return false;
    }
}
