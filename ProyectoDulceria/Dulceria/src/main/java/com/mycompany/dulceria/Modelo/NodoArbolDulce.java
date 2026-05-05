package com.mycompany.dulceria.Modelo;

/**
 * @class NodoArbolDulce
 * Representa un nodo dentro de la estructura del {@code ArbolBBDulce}. 
 * Cada nodo almacena un objeto {@code Dulce} asociado a una clave única (ID), 
 * y mantiene referencias a sus posibles nodos hijos (izquierdo y derecho) 
 * para construir la estructura del Árbol Binario de Búsqueda.
 *
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class NodoArbolDulce {
    /**
     * La clave de búsqueda del nodo, generalmente el ID del producto.
     * Es utilizado para ordenar la estructura del árbol.
     */
    public String id;
    
    /**
     * El objeto de datos almacenado en este nodo.
     */
    public Dulce dulce;
    
    /**
     * Referencia al nodo hijo izquierdo. Contiene claves menores que el nodo actual.
     */
    public NodoArbolDulce izq;
    
    /**
     * Referencia al nodo hijo derecho. Contiene claves mayores que el nodo actual.
     */
    public NodoArbolDulce der;

    /**
     * Constructor para crear un nuevo nodo. Inicializa el ID y el objeto Dulce, 
     * dejando las referencias de los hijos (izq y der) como {@code null}.
     *
     * @param id La clave (ID) del dulce.
     * @param dulce El objeto {@code Dulce} a almacenar.
     */
    public NodoArbolDulce(String id, Dulce dulce) {
        this.id = id;
        this.dulce = dulce;
    }

    /**
     * Obtiene la clave de búsqueda del nodo.
     * @return El ID del dulce.
     */
    public String getId(){
        return id; 
    }
    
    /**
     * Obtiene el objeto {@code Dulce} almacenado en el nodo.
     * @return El objeto Dulce.
     */
    public Dulce getDulce(){
        return dulce; 
    }

    /**
     * Obtiene la referencia al nodo hijo izquierdo.
     * @return El {@code NodoArbolDulce} izquierdo, o {@code null} si no existe.
     */
    public NodoArbolDulce getIzq(){
        return izq; 
    }
    
    /**
     * Obtiene la referencia al nodo hijo derecho.
     * @return El {@code NodoArbolDulce} derecho, o {@code null} si no existe.
     */
    public NodoArbolDulce getDer(){
        return der; 
    }

    /**
     * Establece la referencia al nodo hijo izquierdo.
     * @param izq El nodo que será el nuevo hijo izquierdo.
     */
    public void setIzq(NodoArbolDulce izq) { 
        this.izq = izq; 
    }
    
    /**
     * Establece la referencia al nodo hijo derecho.
     * @param der El nodo que será el nuevo hijo derecho.
     */
    public void setDer(NodoArbolDulce der){
        
        this.der = der; 
    }
}