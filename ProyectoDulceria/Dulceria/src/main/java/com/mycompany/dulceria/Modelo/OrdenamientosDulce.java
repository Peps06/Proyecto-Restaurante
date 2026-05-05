package com.mycompany.dulceria.Modelo;

import java.util.ArrayList;

/**
 * @class OrdenamientosDulce 
 * Clase de utilidad que proporciona métodos estáticos para ordenar colecciones 
 * de objetos {@code Dulce} utilizando diferentes algoritmos de alta eficiencia 
 * (QuickSort, MergeSort, HeapSort).
 * * Cada algoritmo ordena la lista basándose en un atributo específico del dulce.
 *
 * @author Dana, Rubi, Citlaly
 * @version 2.0
 */
public class OrdenamientosDulce {
    

    /**
     * Aplica el algoritmo QuickSort a una sublista de {@code Dulce}.
     * El ordenamiento se realiza de forma ascendente, comparando los nombres de los dulces 
     * sin distinguir mayúsculas de minúsculas.
     *
     * @param lista La lista de dulces a ordenar.
     * @param low El índice inicial de la sublista.
     * @param high El índice final de la sublista.
     */
    public static void quickSort(ArrayList<Dulce> lista, int low, int high) {
        if (low < high) {
            int pi = partition(lista, low, high);
            quickSort(lista, low, pi - 1);
            quickSort(lista, pi + 1, high);
        }
    }

    /**
     * Función auxiliar del QuickSort que selecciona un pivote (el último elemento), 
     * reordena la sublista de tal manera que todos los elementos menores o iguales al 
     * pivote queden a su izquierda, y los mayores a su derecha.
     *
     * @param lista La lista de dulces.
     * @param low El índice de inicio de la sublista.
     * @param high El índice del pivote (y final de la sublista).
     * @return El índice de la posición final del pivote.
     */
    private static int partition(ArrayList<Dulce> lista, int low, int high) {
        String pivot = lista.get(high).getNombre();
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (lista.get(j).getNombre().compareToIgnoreCase(pivot) <= 0) {
                i++;
                Dulce temp = lista.get(i);
                lista.set(i, lista.get(j));
                lista.set(j, temp);
            }
        }

        Dulce temp = lista.get(i + 1);
        lista.set(i + 1, lista.get(high));
        lista.set(high, temp);

        return i + 1;
    }

   

    /**
     * Aplica el algoritmo MergeSort a la lista de {@code Dulce}.
     * El ordenamiento se realiza de forma ascendente, utilizando el ID del dulce 
     * como clave de comparación.
     *
     * @param lista La lista de dulces a ordenar.
     */
    public static void mergeSort(ArrayList<Dulce> lista) {
        if (lista.size() <= 1) return;

        int mid = lista.size() / 2;
        ArrayList<Dulce> left = new ArrayList<>(lista.subList(0, mid));
        ArrayList<Dulce> right = new ArrayList<>(lista.subList(mid, lista.size()));

        mergeSort(left);
        mergeSort(right);

        merge(lista, left, right);
    }

    /**
     * Función auxiliar del MergeSort que fusiona dos sublistas ordenadas (left y right) 
     * en una única lista ordenada (lista). La comparación se realiza por el ID del dulce.
     *
     * @param lista La lista destino donde se fusionarán los elementos.
     * @param left La sublista izquierda ordenada.
     * @param right La sublista derecha ordenada.
     */
    private static void merge(ArrayList<Dulce> lista, ArrayList<Dulce> left, ArrayList<Dulce> right) {
        lista.clear();
        int i = 0, j = 0; 

        while (i < left.size() && j < right.size()) {
            if (left.get(i).getId().compareToIgnoreCase(right.get(j).getId()) <= 0) {
                lista.add(left.get(i++));
            } else {
                lista.add(right.get(j++));
            }
        }

        lista.addAll(left.subList(i, left.size()));
        lista.addAll(right.subList(j, right.size()));
    }

    /**
     * Aplica el algoritmo HeapSort a la lista de {@code Dulce}.
     * El ordenamiento se realiza de forma ascendente, utilizando el precio de venta 
     * del dulce como clave de comparación.
     *
     * @param lista La lista de dulces a ordenar.
     */
    public static void heapSort(ArrayList<Dulce> lista) {
        int n = lista.size();

        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(lista, n, i);
        }

        for (int i = n - 1; i > 0; i--) {
            Dulce temp = lista.get(0);
            lista.set(0, lista.get(i));
            lista.set(i, temp);

            heapify(lista, i, 0);
        }
    }

    /**
     * Función auxiliar del HeapSort que convierte un subárbol con raíz en el índice 'i' 
     * en un Heap Máximo, asumiendo que los subárboles izquierdo y derecho ya son heaps. 
     * La comparación se realiza sobre el precio de venta (convertido a {@code double}).
     *
     * @param lista La lista que representa el heap.
     * @param n El tamaño del heap (puede ser menor que el tamaño total de la lista).
     * @param i El índice de la raíz del subárbol a convertir en heap.
     */
    private static void heapify(ArrayList<Dulce> lista, int n, int i) {
        int largest = i; 
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        double precioLargest = Double.parseDouble(lista.get(largest).getPrecioVenta());

        if (left < n && Double.parseDouble(lista.get(left).getPrecioVenta()) > precioLargest)
            largest = left;

        if (right < n && Double.parseDouble(lista.get(right).getPrecioVenta()) > 
                Double.parseDouble(lista.get(largest).getPrecioVenta()))
            largest = right;

        if (largest != i) {
            Dulce temp = lista.get(i);
            lista.set(i, lista.get(largest));
            lista.set(largest, temp);

            heapify(lista, n, largest);
        }
    }
}