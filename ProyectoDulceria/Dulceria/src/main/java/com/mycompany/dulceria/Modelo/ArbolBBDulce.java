package com.mycompany.dulceria.Modelo;

import java.util.List;

/**
 * @class ArbolBBDulce
 * 
 * Implementa un Árbol Binario de Búsqueda (ABB) para almacenar y gestionar
 * objetos de tipo {@code Dulce}.
 * La clave de búsqueda y ordenamiento del árbol es el {@code id} (String) del dulce.
 * 
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class ArbolBBDulce {
    /**
     * El nodo raíz del Árbol Binario de Búsqueda.
     */
    private NodoArbolDulce raiz;

    /**
     * Constructor por defecto.
     * Inicializa la raíz del árbol a {@code null}, indicando que el árbol está vacío.
     */
    public ArbolBBDulce() {
        raiz = null;
    }

    /**
     * Obtiene el nodo raíz actual del árbol.
     *
     * @return El {@code NodoArbolDulce} que es la raíz del árbol.
     */
    public NodoArbolDulce getRaiz() {
        return raiz;
    }

    

    /**
     * Inserta un nuevo objeto {@code Dulce} en el ABB utilizando su ID como clave.
     * Si el ID ya existe, el dato no es insertado (el árbol no maneja duplicados en esta implementación).
     *
     * @param id La clave (ID) del dulce a insertar.
     * @param dato El objeto {@code Dulce} asociado a la clave.
     */
    public void insertar(String id, Dulce dato) {
        raiz = insertarRec(raiz, id, dato);
    }

    /**
     * Método recursivo para la inserción de un nodo.
     * Sigue la lógica del ABB: si el ID es menor que el nodo actual, va a la izquierda;
     * si es mayor, va a la derecha. Si el ID es igual, no hace nada (ignora duplicados).
     *
     * @param actual El nodo actual en la recursión.
     * @param id La clave (ID) del dulce a insertar.
     * @param dato El objeto {@code Dulce} asociado a la clave.
     * @return El nodo que reemplaza o es el nodo actual (para enlazar la rama).
     */
    private NodoArbolDulce insertarRec(NodoArbolDulce actual, String id, Dulce dato) {
        if (actual == null)
            // Caso base: se encontró la posición para el nuevo nodo.
            return new NodoArbolDulce(id, dato);

        if (id.compareTo(actual.id) < 0)
            actual.izq = insertarRec(actual.izq, id, dato);
        else if (id.compareTo(actual.id) > 0)
            actual.der = insertarRec(actual.der, id, dato);

        // Si id.compareTo(actual.id) == 0, se ignora el duplicado.
        return actual;
    }


   

    /**
     * Busca un {@code Dulce} en el árbol utilizando su ID.
     *
     * @param id La clave (ID) del dulce a buscar.
     * @return El objeto {@code Dulce} si es encontrado, o {@code null} si no existe en el árbol.
     */
    public Dulce buscar(String id) {
        NodoArbolDulce nodo = buscarRec(raiz, id);
        return (nodo != null) ? nodo.getDulce() : null;
    }

    /**
     * Método recursivo para buscar un nodo por su ID.
     * Recorre el árbol siguiendo la regla del ABB hasta encontrar el nodo o
     * llegar a una posición {@code null}.
     *
     * @param nodo El nodo actual en la recursión.
     * @param id La clave (ID) del dulce a buscar.
     * @return El {@code NodoArbolDulce} que coincide con el ID, o {@code null} si no se encuentra.
     */
    private NodoArbolDulce buscarRec(NodoArbolDulce nodo, String id) {
        if (nodo == null) return null;

        int cmp = id.compareTo(nodo.getId());

        if (cmp == 0)
            return nodo; // Encontrado
        else if (cmp < 0)
            return buscarRec(nodo.getIzq(), id); // Buscar a la izquierda
        else
            return buscarRec(nodo.getDer(), id); // Buscar a la derecha
    }


   

    /**
     * Elimina un nodo del árbol basado en el ID del dulce.
     * La eliminación mantiene la propiedad de Árbol Binario de Búsqueda.
     *
     * @param id La clave (ID) del dulce a eliminar.
     */
    public void eliminar(String id) {
        raiz = eliminarRec(raiz, id);
    }

    /**
     * Método recursivo para eliminar un nodo del ABB.
     * Maneja los tres casos de eliminación:
     * 1. Nodo sin hijos o con un solo hijo.
     * 2. Nodo con dos hijos (usando el sucesor in-order, que es el mínimo de la rama derecha).
     *
     * @param nodo El nodo actual en la recursión.
     * @param id La clave (ID) del dulce a eliminar.
     * @return El nodo que reemplaza o es el nodo actual (para re-enlazar la rama).
     */
    private NodoArbolDulce eliminarRec(NodoArbolDulce nodo, String id) {
        if (nodo == null) return null;

        int cmp = id.compareTo(nodo.getId());

        if (cmp < 0) {
            
            nodo.izq = eliminarRec(nodo.izq, id);
        } else if (cmp > 0) {
            
            nodo.der = eliminarRec(nodo.der, id);
        } else {
            
            if (nodo.izq == null) return nodo.der;
            if (nodo.der == null) return nodo.izq;

            NodoArbolDulce sucesor = min(nodo.der);

            nodo.id = sucesor.id;
            nodo.dulce = sucesor.dulce;

            nodo.der = eliminarRec(nodo.der, sucesor.id);
        }

        return nodo;
    }

    /**
     * Encuentra el nodo con la clave más pequeña (mínimo) en un subárbol dado.
     * Esto se logra siguiendo la rama izquierda hasta encontrar un nodo con hijo izquierdo nulo.
     *
     * @param nodo El nodo raíz del subárbol a buscar.
     * @return El {@code NodoArbolDulce} con el valor mínimo del subárbol.
     */
    private NodoArbolDulce min(NodoArbolDulce nodo) {
        while (nodo.izq != null)
            nodo = nodo.izq;
        return nodo;
    }


    
}