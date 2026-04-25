package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Empleado;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Acceso a datos para la tabla {@code empleados}.
 * 
 * Se encarga de llenar la tabla en empleados, de dar la información para las
 * credenciales de ingreso de sesión, registra los nuevos empleaddos agregados,
 * sus ediciones o eliminaciones en la base de datos, así como el estado (presente
 * o ausente)
 *
 *
 * @author Citlaly
 */
public class EmpleadoDAO {

    /**
     * Devuelve todos los empleados de la BD como ObservableList.
     */
    public static ObservableList<Empleado> obtenerTodos() {
        ObservableList<Empleado> lista = FXCollections.observableArrayList();
        String sql = "SELECT idEmpleado, nombre, password, puesto, asistencia, telefono FROM empleados";

        try (Connection con = ConexionDB.getInstancia().getConexion();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Empleado(
                    rs.getInt("idEmpleado"),
                    rs.getString("nombre"),
                    rs.getString("password"),
                    rs.getString("puesto"),
                    rs.getString("asistencia"),
                    rs.getString("telefono")
                ));
            }

        } catch (SQLException e) {
            System.err.println("EmpleadoDAO.obtenerTodos(): " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca un empleado por nombre (ignorando mayúsculas) y contraseña.
     * Devuelve el puesto si las credenciales son correctas, {@code null} si no.
     */
    public static String autenticar(String nombre, String password) {
        String sql = "SELECT puesto FROM empleados "
                   + "WHERE LOWER(nombre) LIKE ? AND password = ?";

        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // El login acepta el primer nombre (ej. "rubi" → busca "rubi%")
            ps.setString(1, nombre.toLowerCase().trim() + "%");
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("puesto");
                }
            }

        } catch (SQLException e) {
            System.err.println("EmpleadoDAO.autenticar(): " + e.getMessage());
        }
        return null;
    }

    /**
     * Inserta un empleado nuevo en la BD y devuelve el ID generado.
     * Devuelve -1 si hubo error.
     *
     */
    public static int insertar(Empleado e) {
        String sql = "INSERT INTO empleados (nombre, password, puesto, asistencia, telefono) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getPassword());
            ps.setString(3, e.getPuesto());
            ps.setString(4, e.getAsistencia());   // "Ausente" por defecto desde el controlador
            ps.setString(5, e.getTelefono());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException ex) {
            System.err.println("EmpleadoDAO.insertar(): " + ex.getMessage());
        }
        return -1;
    }

    /**
     * Actualiza nombre, password, puesto y teléfono de un empleado.
     */
    public static boolean actualizar(Empleado e) {
        String sql = "UPDATE empleados SET nombre=?, password=?, puesto=?, telefono=? "
                   + "WHERE idEmpleado=?";

        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getPassword());
            ps.setString(3, e.getPuesto());
            ps.setString(4, e.getTelefono());
            ps.setInt(5, e.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            System.err.println("EmpleadoDAO.actualizar(): " + ex.getMessage());
        }
        return false;
    }

    /**
     * Cambia solo el campo asistencia de un empleado.
     */
    public static boolean actualizarAsistencia(Empleado e) {
        String sql = "UPDATE empleados SET asistencia=? WHERE idEmpleado=?";

        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, e.getAsistencia());
            ps.setInt(2, e.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            System.err.println("EmpleadoDAO.actualizarAsistencia(): " + ex.getMessage());
        }
        return false;
    }

    /**
     * Elimina un empleado por ID.
     */
    public static boolean eliminar(int id) {
        String sql = "DELETE FROM empleados WHERE idEmpleado=?";

        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            System.err.println("EmpleadoDAO.eliminar(): " + ex.getMessage());
        }
        return false;
    }
}
