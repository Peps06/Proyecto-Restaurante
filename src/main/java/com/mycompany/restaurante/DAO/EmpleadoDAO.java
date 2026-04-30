package com.mycompany.restaurante.DAO;

import com.mycompany.restaurante.Modelo.ConexionDB;
import com.mycompany.restaurante.Modelo.Empleado;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Provee los métodos de acceso a datos (DAO) para la entidad {@code Empleado}.
 * Gestiona el ciclo de vida de los empleados en la base de datos, incluyendo
 * el control de credenciales para el inicio de sesión y el seguimiento de asistencia.
 * 
 * @author Citlaly
 * @version 1.0
 */
public class EmpleadoDAO {

    /**
     * Recupera todos los empleados registrados en la base de datos.
     * 
     * @return Una {@link ObservableList} con los objetos {@link Empleado}.
     *         Devuelve una lista vacía si ocurre un error en la consulta.
     */
    public static ObservableList<Empleado> obtenerTodos() {
        ObservableList<Empleado> lista = FXCollections.observableArrayList();
        String sql = "SELECT idEmpleado, nombre, password, puesto, asistencia, telefono FROM empleados";

        try (Connection con = ConexionDB.getConexion();
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
            System.err.println("Error en EmpleadoDAO.obtenerTodos(): " + e.getMessage());
        }
        return lista;
    }

    /**
     * Verifica las credenciales de un empleado para permitir el acceso al sistema.
     * Utiliza una búsqueda insensible a mayúsculas para el nombre.
     * 
     * @param nombre   Nombre del empleado ingresado en el login.
     * @param password Contraseña asociada a la cuenta.
     * @return El puesto del empleado (ej. "Administrador", "Mesero") si los datos 
     *         son correctos; {@code null} si las credenciales no coinciden.
     */
    public static String autenticar(String nombre, String password) {
        String sql = "SELECT puesto FROM empleados "
                   + "WHERE LOWER(nombre) LIKE ? AND password = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Se aplica trim() y toLowerCase() para mayor flexibilidad en el login
            ps.setString(1, nombre.toLowerCase().trim() + "%");
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("puesto");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en EmpleadoDAO.autenticar(): " + e.getMessage());
        }
        return null;
    }

    /**
     * Registra un nuevo empleado en la base de datos.
     * 
     * @param e Objeto {@link Empleado} que contiene la información a registrar.
     * @return El ID autogenerado por la base de datos; -1 si ocurre un error.
     */
    public static int insertar(Empleado e) {
        String sql = "INSERT INTO empleados (nombre, password, puesto, asistencia, telefono) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getPassword());
            ps.setString(3, e.getPuesto());
            ps.setString(4, e.getAsistencia());   // Normalmente "Ausente" al inicio
            ps.setString(5, e.getTelefono());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException ex) {
            System.err.println("Error en EmpleadoDAO.insertar(): " + ex.getMessage());
        }
        return -1;
    }

    /**
     * Actualiza la información personal y laboral de un empleado existente.
     * 
     * @param e Objeto {@link Empleado} con los datos actualizados y su ID.
     * @return {@code true} si la actualización fue exitosa.
     */
    public static boolean actualizar(Empleado e) {
        String sql = "UPDATE empleados SET nombre=?, password=?, puesto=?, telefono=? "
                   + "WHERE idEmpleado=?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getPassword());
            ps.setString(3, e.getPuesto());
            ps.setString(4, e.getTelefono());
            ps.setInt(5, e.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            System.err.println("Error en EmpleadoDAO.actualizar(): " + ex.getMessage());
        }
        return false;
    }

    /**
     * Actualiza exclusivamente el estado de asistencia de un empleado.
     * Útil para módulos de "Check-in/Check-out".
     * 
     * @param e Objeto {@link Empleado} que contiene el nuevo estado y el ID.
     * @return {@code true} si se modificó el estado de asistencia.
     */
    public static boolean actualizarAsistencia(Empleado e) {
        String sql = "UPDATE empleados SET asistencia=? WHERE idEmpleado=?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, e.getAsistencia());
            ps.setInt(2, e.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            System.err.println("Error en EmpleadoDAO.actualizarAsistencia(): " + ex.getMessage());
        }
        return false;
    }

    /**
     * Elimina el registro de un empleado mediante su identificador único.
     * 
     * @param id El ID del empleado a eliminar.
     * @return {@code true} si el registro fue borrado exitosamente.
     */
    public static boolean eliminar(int id) {
        String sql = "DELETE FROM empleados WHERE idEmpleado=?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            System.err.println("Error en EmpleadoDAO.eliminar(): " + ex.getMessage());
        }
        return false;
    }
}