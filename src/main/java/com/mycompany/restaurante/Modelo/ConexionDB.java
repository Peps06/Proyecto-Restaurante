package com.mycompany.restaurante.Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton de conexión a MySQL para el sistema Saveurs Paris.
 *
 * @author Citlaly, Dana y Rubi
 * @version 2.0
 */
public class ConexionDB {

    // Parámetros de conexión
    private static final String URL = "jdbc:mysql://localhost:3306/restaurantedb"
                                         + "?useSSL=false&serverTimezone=America/Mexico_City";
    private static final String USER = "root";
    private static final String PASSWORD = "Pe951LInDr0:)";

    private static ConexionDB instancia;

    /**
     * Conexión activa.
     */
    private Connection conexion;

    private ConexionDB() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conexion = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado. Revisa pom.xml.", e);
        }
    }

    /**
     * Devuelve la instancia única. Si la conexión está cerrada o es nula,
     * crea una nueva.
     */
    public static ConexionDB getInstancia() throws SQLException {
        if (instancia == null || instancia.conexion.isClosed()) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    /**
     * Devuelve el objeto {@link Connection} para usar en los DAO.
     */
    public Connection getConexion() {
        return conexion;
    }

    /**
     * Cierra la conexión manualmente.
     */
    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
