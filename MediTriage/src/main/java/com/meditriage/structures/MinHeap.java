package com.meditriage.structures;

import com.meditriage.model.Patient;

/**
 * Cola de Prioridad implementada como Min-Heap sobre arreglo nativo.
 *
 * Criterio de orden:
 *   1) Level más bajo = más urgente (level 1 > level 5 en urgencia).
 *   2) Desempate: arrivalAt más antiguo primero (FIFO dentro del mismo nivel).
 *
 * Operaciones: push O(log n), pop O(log n), peek O(1).
 * No usa ninguna clase de java.util.
 */
public class MinHeap {

    private static final int DEFAULT_CAPACITY = 64;

    private Patient[] heap;
    private int       size;

    // Constructores
    public MinHeap() {
        heap = new Patient[DEFAULT_CAPACITY];
        size = 0;
    }

    /** Constructor con capacidad inicial. */
    public MinHeap(int initialCapacity) {
        heap = new Patient[Math.max(initialCapacity, 4)];
        size = 0;
    }

    // API pública

    /**
     * Inserta un paciente en el heap.
     * Coloca al final y sube hasta restaurar la propiedad de heap.
     */
    public void push(Patient patient) {
        if (patient == null) throw new IllegalArgumentException("Patient no puede ser null");
        ensureCapacity();
        heap[size] = patient;
        heapifyUp(size);
        size++;
    }

    /**
     * Extrae y retorna el paciente más urgente.
     * Mueve el último elemento a la raíz y baja para restaurar el heap.
     */
    public Patient pop() {
        if (isEmpty()) throw new RuntimeException("El heap está vacío");
        Patient top = heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) heapifyDown(0);
        return top;
    }

    /** Retorna el paciente más urgente sin extraerlo. */
    public Patient peek() {
        return isEmpty() ? null : heap[0];
    }

    /** Elimina el paciente con el ID dado. O(n) búsqueda + O(log n) heapify. */
    public boolean removeById(int id) {
        int index = -1;
        for (int i = 0; i < size; i++) {
            if (heap[i].getId() == id) { index = i; break; }
        }
        if (index == -1) return false;

        size--;
        heap[index] = heap[size];
        heap[size] = null;

        if (index < size) {
            heapifyUp(index);
            heapifyDown(index);
        }
        return true;
    }

    public int     size()    { return size; }
    public boolean isEmpty() { return size == 0; }

    /**
     * Retorna una copia ordenada por prioridad sin modificar el heap original.
     * Se construye un heap temporal y se extrae todo.
     */
    public Patient[] getSortedSnapshot() {
        if (size == 0) return new Patient[0];

        // Copiar arreglo interno
        Patient[] copy = new Patient[size];
        for (int i = 0; i < size; i++) copy[i] = heap[i];

        // Construir heap temporal y extraer en orden
        MinHeap temp = new MinHeap(size);
        for (int i = 0; i < size; i++) temp.push(copy[i]);

        Patient[] sorted = new Patient[size];
        for (int i = 0; i < size; i++) sorted[i] = temp.pop();
        return sorted;
    }

    // Operaciones internas

    /**
     * Sube el elemento en posición {@code i} mientras sea menor que su padre.
     */
    public void heapifyUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (compare(heap[i], heap[parent]) < 0) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    /**
     * Baja el elemento en posición {@code i} mientras sea mayor que algún hijo.
     */
    public void heapifyDown(int i) {
        while (true) {
            int left     = 2 * i + 1;
            int right    = 2 * i + 2;
            int smallest = i;

            if (left  < size && compare(heap[left],  heap[smallest]) < 0) smallest = left;
            if (right < size && compare(heap[right], heap[smallest]) < 0) smallest = right;

            if (smallest != i) {
                swap(i, smallest);
                i = smallest;
            } else {
                break;
            }
        }
    }

    /**
     * Comparador de prioridad:
     *   Negativo → a es más urgente que b.
     *   Positivo → b es más urgente que a.
     *   Cero     → igual urgencia.
     */
    private int compare(Patient a, Patient b) {
        // 1) Nivel más bajo = más urgente
        if (a.getLevel() != b.getLevel()) {
            return Integer.compare(a.getLevel(), b.getLevel());
        }
        // 2) Desempate: llegó antes = más urgente
        if (a.getArrivalAt() != null && b.getArrivalAt() != null) {
            return a.getArrivalAt().compareTo(b.getArrivalAt());
        }
        return 0;
    }

    private void swap(int i, int j) {
        Patient tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    private void ensureCapacity() {
        if (size < heap.length) return;
        Patient[] bigger = new Patient[heap.length * 2];
        for (int i = 0; i < size; i++) bigger[i] = heap[i];
        heap = bigger;
    }

    // Debug
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MinHeap[");
        for (int i = 0; i < size; i++) {
            sb.append(heap[i].getName()).append("(L").append(heap[i].getLevel()).append(")");
            if (i < size - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
