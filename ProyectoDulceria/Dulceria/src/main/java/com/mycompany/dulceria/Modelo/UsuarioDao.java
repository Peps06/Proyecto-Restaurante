package com.mycompany.dulceria.Modelo;

import java.io.*;
import java.util.*;

/**
 * @interface UsuarioDao
 * Define el contrato de acceso a datos (DAO) para la entidad {@code Usuario}.
 * Esta interfaz establece los métodos estándar de manipulación de datos (CRUD) 
 * que cualquier implementación de persistencia para usuarios debe seguir, 
 * asegurando la abstracción de la fuente de datos subyacente
 *
 * @author Dana, Rubi, Citlaly
 * @version 1.0
 */
public interface UsuarioDao {
    
    /**
     * Agrega un nuevo usuario al sistema de persistencia.
     * * @param u El objeto {@code Usuario} a ser registrado.
     * @throws IOException Si ocurre un error durante la operación de escritura de datos.
     */
    void agregarUsuario(Usuario u) throws IOException;

    /**
     * Recupera un usuario específico utilizando su identificador único.
     * * @param id El identificador (ID) del usuario a buscar.
     * @return El objeto {@code Usuario} encontrado, o {@code null} si no existe.
     */
    Usuario obtenerUsuario(String id);

    /**
     * Actualiza la información de un usuario existente en el sistema.
     * La operación se realiza basándose en la coincidencia del ID del objeto proporcionado.
     * * @param u El objeto {@code Usuario} con los datos actualizados.
     * @return {@code true} si el usuario fue actualizado con éxito, {@code false} si el ID no fue encontrado.
     * @throws IOException Si ocurre un error durante la operación de escritura de datos.
     */
    boolean actualizarUsuario(Usuario u) throws IOException;

    /**
     * Elimina un usuario del sistema de persistencia mediante su identificador.
     * * @param id El identificador del usuario a eliminar.
     * @return Un mensaje indicando el resultado de la eliminación (éxito o fracaso).
     * @throws IOException Si ocurre un error durante la operación de escritura de datos.
     */
    String eliminarUsuario(String id) throws IOException;

    /**
     * Obtiene una colección completa de todos los usuarios registrados en el sistema.
     * * @return Un {@code ArrayList} que contiene todos los objetos {@code Usuario}.
     */
    ArrayList<Usuario> obtenerUsuarios();
}