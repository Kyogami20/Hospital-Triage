package com.meditriage.structures;

/**
 * Lista Doblemente Enlazada genérica implementada desde cero.
 *
 * No usa ninguna colección de java.util.
 * Soporta capacidad máxima opcional (historial de últimos N atendidos).
 *
 * Operaciones: addLast O(1), addFirst O(1), removeFirst O(1), removeLast O(1).
 *
 * Uso en MediTriage:
 *   DoublyLinkedList&lt;Patient&gt; — historial en memoria de pacientes atendidos.
 */
public class DoublyLinkedList<T> {

    // Nodo
    private static class Node<T> {
        T       data;
        Node<T> prev;
        Node<T> next;

        Node(T data) { this.data = data; }
    }

    // Campos
    private Node<T> head;
    private Node<T> tail;
    private int     size;
    private final int maxSize; // 0 = sin límite

    // Constructores
    public DoublyLinkedList() {
        this.maxSize = 0;
    }

    /** Lista con capacidad máxima: cuando se supera, elimina el más antiguo (head). */
    public DoublyLinkedList(int maxSize) {
        this.maxSize = maxSize;
    }

    // API pública

    /** Agrega al final de la lista. Si supera maxSize, elimina el primero. */
    public void addLast(T data) {
        Node<T> newNode = new Node<>(data);
        if (tail == null) {
            head = tail = newNode;
        } else {
            newNode.prev = tail;
            tail.next    = newNode;
            tail         = newNode;
        }
        size++;
        if (maxSize > 0 && size > maxSize) removeFirst();
    }

    /** Agrega al inicio de la lista. */
    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev    = newNode;
            head         = newNode;
        }
        size++;
    }

    /** Elimina y retorna el primer elemento. Retorna null si está vacía. */
    public T removeFirst() {
        if (head == null) return null;
        T data = head.data;
        head   = head.next;
        if (head == null) tail = null;
        else              head.prev = null;
        size--;
        return data;
    }

    /** Elimina y retorna el último elemento. Retorna null si está vacía. */
    public T removeLast() {
        if (tail == null) return null;
        T data = tail.data;
        tail   = tail.prev;
        if (tail == null) head = null;
        else              tail.next = null;
        size--;
        return data;
    }

    /**
     * Elimina la primera ocurrencia del elemento que sea igual a {@code data}
     * usando el método equals().
     * @return true si se encontró y eliminó.
     */
    public boolean removeElement(T data) {
        Node<T> cur = head;
        while (cur != null) {
            if (cur.data.equals(data)) {
                // Desencadenar nodo
                if (cur.prev != null) cur.prev.next = cur.next;
                else                  head = cur.next;
                if (cur.next != null) cur.next.prev = cur.prev;
                else                  tail = cur.prev;
                size--;
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    /** Primer elemento sin eliminarlo. */
    public T peekFirst() { return head == null ? null : head.data; }

    /** Último elemento sin eliminarlo. */
    public T peekLast()  { return tail == null ? null : tail.data; }

    public int     size()    { return size; }
    public boolean isEmpty() { return size == 0; }

    /**
     * Convierte la lista a un arreglo de Object[].
     * Orden: head → tail (más antiguo → más reciente).
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        T[] arr = (T[]) new Object[size];
        Node<T> cur = head;
        int i = 0;
        while (cur != null) { arr[i++] = cur.data; cur = cur.next; }
        return arr;
    }

    /**
     * Retorna un arreglo con los elementos que cumplen el criterio de filtro.
     * El filtro se expresa como una interfaz funcional propia (sin java.util.function).
     */
    public T[] filter(SimpleFilter<T> filterFn) {
        // Primera pasada: contar
        int count = 0;
        Node<T> cur = head;
        while (cur != null) {
            if (filterFn.test(cur.data)) count++;
            cur = cur.next;
        }
        // Segunda pasada: recolectar
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[count];
        int idx = 0;
        cur = head;
        while (cur != null) {
            if (filterFn.test(cur.data)) result[idx++] = cur.data;
            cur = cur.next;
        }
        return result;
    }

    /**
     * Interfaz funcional propia para filtros (reemplaza Predicate de java.util.function).
     */
    @FunctionalInterface
    public interface SimpleFilter<T> {
        boolean test(T item);
    }

    /** Itera la lista de head a tail ejecutando la acción dada. */
    public void iterate(SimpleAction<T> action) {
        Node<T> cur = head;
        while (cur != null) {
            action.accept(cur.data);
            cur = cur.next;
        }
    }

    /**
     * Interfaz funcional propia para acciones (reemplaza Consumer de java.util.function).
     */
    @FunctionalInterface
    public interface SimpleAction<T> {
        void accept(T item);
    }

    // Debug
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DoublyLinkedList[");
        Node<T> cur = head;
        while (cur != null) {
            sb.append(cur.data);
            if (cur.next != null) sb.append(" <-> ");
            cur = cur.next;
        }
        return sb.append("]").toString();
    }
}
