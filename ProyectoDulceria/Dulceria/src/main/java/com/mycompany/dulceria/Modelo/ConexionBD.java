package com.mycompany.dulceria.Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase para gestionar la conexión a la base de datos MySQL.
 * Proporciona métodos para obtener y cerrar conexiones.
 * 
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class ConexionBD {
    
    /**
     * URL de conexión a la base de datos.
     */
    private static final String URL = "jdbc:mysql://localhost:3306/dulceria_db";
    
    /**
     * Usuario de MySQL.
     */
    private static final String USUARIO = "root";
    
    /**
     * Contraseña del usuario de MySQL.
     */
    private static final String PASSWORD = "Pe951LInDr0:)";
    
    /**
     * Driver de MySQL.
     */
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    /**
     * Obtiene una conexión a la base de datos.
     * 
     * @return Un objeto Connection activo
     * @throws SQLException Si no se puede conectar a la base de datos
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Cargar el driver de MySQL
            Class.forName(DRIVER);
            
            // Establecer la conexión
            Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Error: No se encontró el driver de MySQL. " +
                                 "Asegúrate de tener la dependencia mysql-connector-java en tu pom.xml", e);
        } catch (SQLException e) {
            throw new SQLException("Error al conectar con la base de datos. " +
                                 "Verifica que MySQL esté corriendo y que los datos de conexión sean correctos.", e);
        }
    }
    
    /**
     * Cierra una conexión a la base de datos.
     * 
     * @param conn La conexión a cerrar
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
