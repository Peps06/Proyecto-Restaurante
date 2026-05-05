package com.mycompany.dulceria.Modelo;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Implementación de la interfaz UsuarioDao usando MySQL.
 * 
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class ImpUsuarioDAO implements UsuarioDao {
    
    /**
     * Agrega un nuevo usuario a la base de datos.
     * 
     * @param u El objeto Usuario a agregar
     * @throws IOException Si ocurre un error en la base de datos
     */
    @Override
    public void agregarUsuario(Usuario u) throws IOException {
        Connection conn = null;
        
        try {
            conn = ConexionBD.getConnection();
            
            String sql = "INSERT INTO usuarios (id, nombre, password, tipo) VALUES (?, ?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, u.getId());
            ps.setString(2, u.getNombre());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getTipo());
            
            ps.executeUpdate();
            
        } catch (SQLException e) {
            throw new IOException("Error al agregar usuario: " + e.getMessage());
        } finally {
            ConexionBD.closeConnection(conn);
        }
    }
    
    /**
     * Obtiene un usuario por su ID.
     * 
     * @param id El ID del usuario a buscar
     * @return El objeto Usuario si se encuentra, null en caso contrario
     */
    @Override
    public Usuario obtenerUsuario(String id) {
        Connection conn = null;
        Usuario usuario = null;
        
        try {
            conn = ConexionBD.getConnection();
            
            String sql = "SELECT * FROM usuarios WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                usuario = new Usuario(
                    rs.getString("id"),
                    rs.getString("nombre"),
                    rs.getString("password"),
                    rs.getString("tipo")
                );
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexionBD.closeConnection(conn);
        }
        
        return usuario;
    }
    
    /**
     * Actualiza la información de un usuario existente.
     * 
     * @param u El objeto Usuario con los nuevos datos
     * @return true si se actualizó correctamente, false en caso contrario
     * @throws IOException Si ocurre un error en la base de datos
     */
    @Override
    public boolean actualizarUsuario(Usuario u) throws IOException {
        Connection conn = null;
        boolean actualizado = false;
        
        try {
            conn = ConexionBD.getConnection();
            
            String sql = "UPDATE usuarios SET nombre = ?, password = ?, tipo = ? WHERE id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getTipo());
            ps.setString(4, u.getId());
            
            int filasAfectadas = ps.executeUpdate();
            actualizado = (filasAfectadas > 0);
            
        } catch (SQLException e) {
            throw new IOException("Error al actualizar usuario: " + e.getMessage());
        } finally {
            ConexionBD.closeConnection(conn);
        }
        
        return actualizado;
    }
    
    /**
     * Elimina un usuario de la base de datos.
     * 
     * @param id El ID del usuario a eliminar
     * @return Un mensaje indicando el resultado
     * @throws IOException Si ocurre un error en la base de datos
     */
    @Override
    public String eliminarUsuario(String id) throws IOException {
        Connection conn = null;
        
        try {
            conn = ConexionBD.getConnection();
            
            String sql = "DELETE FROM usuarios WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            
            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                return "Usuario eliminado correctamente";
            } else {
                return "Usuario no encontrado";
            }
            
        } catch (SQLException e) {
            throw new IOException("Error al eliminar usuario: " + e.getMessage());
        } finally {
            ConexionBD.closeConnection(conn);
        }
    }
    
    /**
     * Obtiene la lista completa de usuarios.
     * 
     * @return ArrayList con todos los usuarios
     */
    @Override
    public ArrayList<Usuario> obtenerUsuarios() {
        Connection conn = null;
        ArrayList<Usuario> lista = new ArrayList<>();
        
        try {
            conn = ConexionBD.getConnection();
            
            String sql = "SELECT * FROM usuarios ORDER BY id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getString("id"),
                    rs.getString("nombre"),
                    rs.getString("password"),
                    rs.getString("tipo")
                );
                lista.add(usuario);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexionBD.closeConnection(conn);
        }
        
        return lista;
    }
    
    /**
     * Valida las credenciales de un empleado o administrador.
     * Verifica el ID, contraseña y tipo de usuario.
     * 
     * @param id El ID del usuario
     * @param password La contraseña
     * @param tipo El tipo de usuario esperado (Empleado/Administrador)
     * @return El objeto Usuario si las credenciales son correctas, null en caso contrario
     */
    public Usuario validarEmpleado(String id, String password, String tipo) {
        Usuario u = obtenerUsuario(id);
        
        if (u == null) {
            return null; // Usuario no existe
        }
        
        if (!u.getPassword().equals(password)) {
            return null; // Contraseña incorrecta
        }
        
        if (!u.getTipo().equalsIgnoreCase(tipo)) {
            return null; // Tipo de usuario no coincide
        }
        
        return u; // Todo correcto
    }
}
