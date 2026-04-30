package com.mycompany.dulceria.Modelo;

import java.util.ArrayList;

/**
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public interface DulceDao {
      
    /**
     * Agrega un nuevo dulce al repertorio
     * @param dulce a agregar
     * @throws Exception si ocurre un error al agregar
     */
    public void agregarDulce (Dulce dulce) throws Exception;
    
    /**
     * obtiene un dulce a partir de su id
     * @param id del pan a buscar
     * @return retorna el objeto de tipo dulce, null si no se encuentra
     * @throws Exception si ocurre un error durante la busqueda
     */
    public Dulce obtenerDulce (String id) throws Exception;
    
    /**
     * Actualiza los datos del dulce
     * @param dulce con los nuevos datos
     * @return true si la actualización se hizo con exito
     * @throws Exception si ocurrio un error durante la actualización
     */
    public boolean actualizarDulce (Dulce dulce) throws Exception;
    
    /**
     * Elimina un pan de la lista 
     * @param id del pan que se va a eliminar
     * @return un mensaje indicando el resultado de la operación
     * @throws Exception si hubo algun error durante el proceso
     */
    public String eliminarDulce (String id) throws Exception;
    
    /**
     * obtiene la lista completa de panes registrados
     * @return una lista de panes registrados
     */
    public ArrayList<Dulce> obtenerDulces ();
}