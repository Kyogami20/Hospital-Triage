package com.meditriage.structures;

/**
 * Tabla Hash con encadenamiento separado (Separate Chaining).
 *
 * Cada bucket es una lista enlazada simple de nodos propios.
 * No usa ninguna colección de java.util.
 *
 * Operaciones promedio: put O(1), get O(1), remove O(1).
 * El factor de carga dispara un redimensionamiento automático.
 *
 * Uso en MediTriage:
 *   HashTable&lt;Integer, Patient&gt; — acceso O(1) a pacientes activos por ID.
 */
public class HashTable<K, V> {

    // Nodo de la lista enlazada (bucket)
    private static class Node<K, V> {
        final K key;
        V       value;
        Node<K, V> next;

        Node(K key, V value) {
            this.key   = key;
            this.value = value;
        }
    }

    // Constantes
    private static final int   DEFAULT_CAPACITY   = 32;
    private static final float LOAD_FACTOR_LIMIT  = 0.75f;

    // Campos
    @SuppressWarnings("unchecked")
    private Node<K, V>[] buckets;
    private int capacity;
    private int size;
    private int collisions; // colisiones acumuladas (métrica educativa)

    // Constructor
    @SuppressWarnings("unchecked")
    public HashTable() {
        capacity = DEFAULT_CAPACITY;
        buckets  = new Node[capacity];
        size     = 0;
        collisions = 0;
    }

    // API pública

    /**
     * Inserta o actualiza la clave {@code key} con {@code value}.
     * Si el factor de carga supera el umbral, redimensiona la tabla.
     */
    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Clave no puede ser null");
        if (loadFactor() > LOAD_FACTOR_LIMIT) resize();

        int index = hash(key);
        Node<K, V> head = buckets[index];

        // ¿La clave ya existe? → actualizar
        for (Node<K, V> cur = head; cur != null; cur = cur.next) {
            if (cur.key.equals(key)) {
                cur.value = value;
                return;
            }
        }

        // Clave nueva: insertar al frente del bucket
        if (head != null) collisions++; // hay al menos un nodo en este bucket
        Node<K, V> newNode = new Node<>(key, value);
        newNode.next  = head;
        buckets[index] = newNode;
        size++;
    }

    /**
     * Retorna el valor asociado a {@code key}, o null si no existe.
     */
    public V get(K key) {
        if (key == null) return null;
        int index = hash(key);
        for (Node<K, V> cur = buckets[index]; cur != null; cur = cur.next) {
            if (cur.key.equals(key)) return cur.value;
        }
        return null;
    }

    /**
     * Elimina la entrada con {@code key}.
     * @return true si existía y fue eliminada, false en caso contrario.
     */
    public boolean remove(K key) {
        if (key == null) return false;
        int index = hash(key);
        Node<K, V> cur  = buckets[index];
        Node<K, V> prev = null;

        while (cur != null) {
            if (cur.key.equals(key)) {
                if (prev == null) buckets[index] = cur.next;
                else              prev.next = cur.next;
                size--;
                return true;
            }
            prev = cur;
            cur  = cur.next;
        }
        return false;
    }

    /** Indica si la clave existe en la tabla. */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int    size()           { return size; }
    public boolean isEmpty()       { return size == 0; }
    public float  loadFactor()     { return (float) size / capacity; }
    public int    collisionsCount(){ return collisions; }
    public int    capacity()       { return capacity; }

    // Función hash

    /**
     * Hash con mezcla de bits para distribuir mejor las claves enteras.
     * Usa Object.hashCode() (java.lang), admisible por las reglas del proyecto.
     */
    private int hash(K key) {
        int h = key.hashCode();
        // Mezcla de bits estilo Knuth
        h ^= (h >>> 16);
        h *= 0x45d9f3b;
        h ^= (h >>> 16);
        return Math.abs(h % capacity);
    }

    // Redimensionamiento

    /**
     * Duplica la capacidad y rehashea todos los nodos existentes.
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        capacity *= 2;
        Node<K, V>[] newBuckets = new Node[capacity];

        for (int i = 0; i < buckets.length; i++) {
            Node<K, V> cur = buckets[i];
            while (cur != null) {
                Node<K, V> next = cur.next;
                int newIndex    = hash(cur.key); // recalcular con nueva capacidad
                cur.next              = newBuckets[newIndex];
                newBuckets[newIndex]  = cur;
                cur = next;
            }
        }
        buckets = newBuckets;
    }

    // Debug
    @Override
    public String toString() {
        return String.format("HashTable[size=%d, capacity=%d, loadFactor=%.2f, collisions=%d]",
                size, capacity, loadFactor(), collisions);
    }
}
