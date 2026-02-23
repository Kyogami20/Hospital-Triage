package com.meditriage.structures;

/**
 * Pila (Stack) genérica implementada sobre arreglo nativo.
 *
 * No usa ninguna colección de java.util.
 * Soporta redimensionamiento dinámico automático.
 *
 * Operaciones: push O(1) amortizado, pop O(1), peek O(1).
 *
 * Uso en MediTriage:
 *   Stack&lt;UndoAction&gt; — historial de acciones para deshacer (REGISTER / ATTEND).
 */
public class Stack<T> {

    private static final int DEFAULT_CAPACITY = 64;

    private Object[] data;
    private int      top;  // índice del tope (-1 si vacía)

    // Constructor
    public Stack() {
        data = new Object[DEFAULT_CAPACITY];
        top  = -1;
    }

    // API pública

    /** Agrega un elemento al tope de la pila. */
    public void push(T item) {
        if (item == null) throw new IllegalArgumentException("El elemento no puede ser null");
        ensureCapacity();
        data[++top] = item;
    }

    /**
     * Extrae y retorna el elemento del tope.
     * @throws RuntimeException si la pila está vacía.
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new RuntimeException("La pila está vacía");
        T item    = (T) data[top];
        data[top] = null; // liberar referencia
        top--;
        return item;
    }

    /**
     * Retorna el elemento del tope sin extraerlo.
     * Retorna null si la pila está vacía.
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        return isEmpty() ? null : (T) data[top];
    }

    public boolean isEmpty() { return top == -1; }
    public int     size()    { return top + 1; }

    // Redimensionamiento
    private void ensureCapacity() {
        if (top < data.length - 1) return;
        Object[] bigger = new Object[data.length * 2];
        for (int i = 0; i < data.length; i++) bigger[i] = data[i];
        data = bigger;
    }

    // Debug
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Stack[");
        for (int i = top; i >= 0; i--) {
            sb.append(data[i]);
            if (i > 0) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
