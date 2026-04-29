// ConexionDB.java — versión corregida
package com.mycompany.restaurante.Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fábrica de conexiones para Saveurs Paris.
 * Cada llamada a getConexion() devuelve una Connection nueva e independiente.
 * Los DAO son responsables de cerrarla con try-with-resources.
 *
 * @author Citlaly
 * @version 2
 */
public class ConexionDB {

    private static final Logger LOG = Logger.getLogger(ConexionDB.class.getName());

    private static final String URL =
        "jdbc:mysql://localhost:3306/restaurantedb"
        + "?useSSL=false"
        + "&serverTimezone=America/Mexico_City"
        + "&autoReconnect=true" // reconecta si MySQL cerró la conexión
        + "&connectTimeout=5000" // 5 s para establecer conexión
        + "&socketTimeout=30000"; // 30 s para esperar respuesta

    private static final String USER = "root";
    private static final String PASSWORD = "Mend1503";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Driver MySQL no encontrado. Revisa pom.xml.");
        }
    }

    /**
     * Devuelve una conexión NUEVA cada vez.
     * Úsala siempre dentro de try-with-resources para garantizar el cierre.
     *
     * Ejemplo de uso correcto en un DAO:
     *   try (Connection con = ConexionDB.getConexion()) { ... }
     */
    public static Connection getConexion() throws SQLException {
        Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
        LOG.log(Level.FINE, "Conexión abierta: {0}", con);
        return con;
    }

    // Constructor privado: esta clase no se instancia
    private ConexionDB() {}
}