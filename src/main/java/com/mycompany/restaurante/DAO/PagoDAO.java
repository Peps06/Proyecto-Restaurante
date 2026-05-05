package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Pago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.logging.Level;

import java.util.logging.Logger;

/**
 * DAO para la entidad de Pago.
 * Gestiona la persistencia del pago en la base de datos y coordina las actualizaciones
 * de estado que deben ocurrir de forma atómica al procesar un cobro.
 * 
 * @author Citlaly
 * @version 1.0
 */
public class PagoDAO {
    
    private static final Logger LOG = Logger.getLogger(PagoDAO.class.getName());

    /**
     * Inserta un nuevo pago en la tabla {@code pagos}.
     */
    private static final String SQL_INSERT_PAGO =
        "INSERT INTO pagos " +
        "(idOrden, idEmpleado, montoTotal, efectivo, cambio, tarjeta, tipoTarjeta, formaPago) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
 
    /**
     * Cierra la orden tras el cobro exitoso.
     */
    private static final String SQL_CERRAR_ORDEN =
        "UPDATE ordenes SET estado = 'Cerrada' WHERE idOrden = ?";
 
    /**
     * Libera la mesa una vez que la cuenta ha sido cobrada.
     */
    private static final String SQL_LIBERAR_MESA =
        "UPDATE mesas SET estado = 'Libre' WHERE idMesa = ?";

    /**
     * Registra un pago completo en la base de datos dentro de una transacción.
     *
     * @param pago → Objeto {@link Pago} con todos los datos del cobro ya cargados.
     * @param idMesa → Identificador de la mesa que se liberará tras el pago.
     * @return El {@code idPago} autogenerado por la BD si la transacción fue exitosa,
     *         o {@code -1} si ocurrió cualquier error.
     */
    public static int registrarPago(Pago pago, int idMesa) {
        try (Connection con = ConexionDB.getConexion()) {
 
            con.setAutoCommit(false); 
 
            try {
                // 1. Insertar el pago
                int idPagoGenerado = insertarPago(con, pago);
                pago.setIdPago(idPagoGenerado);
                LOG.info("[PagoDAO] Pago insertado. idPago=" + idPagoGenerado);
 
                // 2. Cerrar la orden
                cerrarOrden(con, pago.getIdOrden());
                LOG.info("[PagoDAO] Orden #" + pago.getIdOrden() + " marcada como Cerrada.");
 
                // 3. Liberar la mesa
                liberarMesa(con, idMesa);
                LOG.info("[PagoDAO] Mesa " + idMesa + " marcada como Libre.");
 
                con.commit(); // confirmar lo anterior
                LOG.info("[PagoDAO] Transacción completada con éxito.");
                return idPagoGenerado;
 
            } catch (SQLException e) {
                con.rollback(); // Revertir todo si algo falla
                LOG.log(Level.SEVERE,
                    "[PagoDAO] Error en transacción. ROLLBACK ejecutado. idOrden=" +
                    pago.getIdOrden(), e);
                throw e;
            }
 
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "[PagoDAO] Error crítico de conexión.", e);
            return -1;
        }
    }
        
    /**
     * Ejecuta el INSERT en la tabla {@code pagos} usando la conexión activa.
     *
     * @param con → Conexión activa con {@code autoCommit=false}.
     * @param pago → Datos del pago a persistir.
     * @return → El {@code idPago} generado por {@code AUTO_INCREMENT}.
     * @throws → SQLException Si ocurre un error de base de datos o no se puede
     *            recuperar la clave generada.
     */
    private static int insertarPago(Connection con, Pago pago) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                SQL_INSERT_PAGO, Statement.RETURN_GENERATED_KEYS)) {
 
            ps.setInt(1, pago.getIdOrden());
            ps.setInt(2, pago.getIdEmpleado());
            ps.setDouble(3, pago.getMontoTotal());
 
            // Campos opcionales → se insertan como NULL si no aplican
            setDoubleNullable(ps, 4, pago.getEfectivo());
            setDoubleNullable(ps, 5, pago.getCambio());
            setDoubleNullable(ps, 6, pago.getTarjeta());
 
            // tipoTarjeta es ENUM en la BD; no se puede insertar cadena vacía
            if (pago.getTipoTarjeta() != null && !pago.getTipoTarjeta().isBlank()) {
                ps.setString(7, pago.getTipoTarjeta());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
 
            ps.setString(8, pago.getFormaPago());
            ps.executeUpdate();
 
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("[PagoDAO] No se pudo recuperar el idPago generado.");
                }
                return keys.getInt(1);
            }
        }
    }

    /**
     * Actualiza el estado de la orden a {@code 'Cerrada'}.
     *
     * @param con → Conexión activa con {@code autoCommit=false}.
     * @param idOrden → ID de la orden que se cierra.
     * @throws → SQLException Si la orden no existe o falla la actualización.
     */
    private static void cerrarOrden(Connection con, int idOrden) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_CERRAR_ORDEN)) {
            ps.setInt(1, idOrden);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("[PagoDAO] La orden #" + idOrden + " no fue encontrada.");
            }
        }
    }
    
    /**
     * Actualiza el estado de la mesa a {@code 'Libre'} tras el cobro.
     *
     * @param con → Conexión activa con {@code autoCommit=false}.
     * @param idMesa → ID de la mesa que se libera.
     * @throws → SQLException Si la mesa no existe o falla la actualización.
     */
    private static void liberarMesa(Connection con, int idMesa) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_LIBERAR_MESA)) {
            ps.setInt(1, idMesa);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("[PagoDAO] La mesa " + idMesa + " no fue encontrada.");
            }
        }
    }
 
    /**
     * Establece un valor {@code Double} en un {@link PreparedStatement}, 
     * insertando {@code NULL} si el valor es {@code null}.
     * Necesario para los campos opcionales de {@code pagos} (efectivo, cambio, tarjeta).
     *
     * @param ps → El {@link PreparedStatement} a configurar.
     * @param index → Posición del parámetro (base 1).
     * @param valor → Valor a insertar; puede ser {@code null}.
     * @throws → SQLException Si ocurre un error al establecer el parámetro.
     */
    private static void setDoubleNullable(PreparedStatement ps, int index, Double valor)
            throws SQLException {
        if (valor != null) {
            ps.setDouble(index, valor);
        } else {
            ps.setNull(index, Types.DECIMAL);
        }
    }
}
