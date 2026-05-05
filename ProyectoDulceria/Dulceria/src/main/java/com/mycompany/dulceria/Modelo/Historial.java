package com.mycompany.dulceria.Modelo;

import java.util.Stack;

/**
 * @class Historial
 * La clase Historial gestiona un registro de operaciones o acciones
 * realizadas en el sistema (por ejemplo, en un inventario o CRUD).
 *
 * <p>Utiliza una(Pila) estática, lo que significa que las últimas
 * acciones registradas serán las primeras en ser visualizadas (LIFO: Last In, First Out).</p>
 *
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class Historial {
    
    /**
     * Pila estática que almacena las acciones o eventos registrados como Strings.
     * Al ser estática, la pila se comparte en toda la aplicación.
     */
    private static Stack<String> pila = new Stack<>();

    /**
     * Registra una nueva acción en el historial.
     * La acción se agrega a la parte superior de la pila.
     *
     * @param accion La descripción de la operación realizada (ej. "Dulce X agregado").
     */
    public static void registrar(String accion) {
        pila.push(accion);
    }

    /**
     * Muestra el contenido del historial, vaciando una copia de la pila
     * para presentar las acciones en orden cronológico inverso (de la más reciente a la más antigua).
     * El historial original permanece intacto.
     *
     * @return Un {@code String} que contiene una lista formateada de todas las operaciones registradas,
     * o un mensaje si la pila está vacía.
     */
    public static String verHistorial() {
        if (pila.isEmpty()) return "No hay operaciones registradas.";

        StringBuilder sb = new StringBuilder("--- HISTORIAL DE OPERACIONES ---\n");

        
        @SuppressWarnings("unchecked")
        Stack<String> copia = (Stack<String>) pila.clone();
        
        while (!copia.isEmpty()) {
           
            sb.append("- ").append(copia.pop()).append("\n");
        }

        return sb.toString();
    }
}