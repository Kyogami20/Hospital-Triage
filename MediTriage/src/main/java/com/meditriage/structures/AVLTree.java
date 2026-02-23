package com.meditriage.structures;

import com.meditriage.structures.DoublyLinkedList;

/**
 * Árbol AVL genérico (árbol binario de búsqueda auto-balanceado).
 *
 * Restricción: K debe implementar Comparable (java.lang, no java.util).
 * No usa ninguna colección de java.util.
 *
 * Operaciones: insert O(log n), search O(log n), delete O(log n).
 * Rotaciones simples y dobles para mantener |balanceFactor| &le; 1.
 *
 * Usos en MediTriage:
 *   AVLTree&lt;Integer, Patient&gt; — índice por ID
 *   AVLTree&lt;String,  Patient&gt; — índice por nombre
 */
public class AVLTree<K extends Comparable<K>, V> {

    // Nodo interno
    private static class Node<K, V> {
        K       key;
        V       value;
        Node<K, V> left, right;
        int     height; // altura del subárbol con raíz en este nodo

        Node(K key, V value) {
            this.key    = key;
            this.value  = value;
            this.height = 1;
        }
    }

    // Campos
    private Node<K, V> root;
    private int        size;

    // API pública

    /**
     * Inserta o actualiza la clave con el valor dado.
     * Si la clave ya existe, sobreescribe el valor.
     */
    public void insert(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Clave no puede ser null");
        root = insertRec(root, key, value);
    }

    /** Busca el valor asociado a {@code key}. Retorna null si no existe. */
    public V search(K key) {
        Node<K, V> node = searchRec(root, key);
        return node == null ? null : node.value;
    }

    /** Elimina la entrada con la clave dada. No hace nada si no existe. */
    public void delete(K key) {
        if (key == null) return;
        root = deleteRec(root, key);
    }

    public int     size()    { return size; }
    public boolean isEmpty() { return size == 0; }

    /**
     * Recorre el árbol in-order y agrega todos los valores a la lista dada.
     * Útil para búsquedas tipo "contiene" sobre el árbol de nombres.
     */
    public void collectAllInto(DoublyLinkedList<V> list) {
        inOrderCollect(root, list);
    }

    /**
     * Retorna la altura del árbol (0 si vacío).
     * Usada para diagnóstico/visualización académica.
     */
    public int height() {
        return height(root);
    }

    // Inserción
    private Node<K, V> insertRec(Node<K, V> node, K key, V value) {
        if (node == null) {
            size++;
            return new Node<>(key, value);
        }
        int cmp = key.compareTo(node.key);
        if      (cmp < 0) node.left  = insertRec(node.left,  key, value);
        else if (cmp > 0) node.right = insertRec(node.right, key, value);
        else              { node.value = value; return node; } // actualizar

        updateHeight(node);
        return balance(node);
    }

    // Búsqueda
    private Node<K, V> searchRec(Node<K, V> node, K key) {
        if (node == null) return null;
        int cmp = key.compareTo(node.key);
        if (cmp < 0) return searchRec(node.left,  key);
        if (cmp > 0) return searchRec(node.right, key);
        return node;
    }

    // Eliminación
    private Node<K, V> deleteRec(Node<K, V> node, K key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = deleteRec(node.left, key);
        } else if (cmp > 0) {
            node.right = deleteRec(node.right, key);
        } else {
            // Nodo encontrado
            size--;
            if (node.left == null)  return node.right;
            if (node.right == null) return node.left;

            // Tiene dos hijos: sustituir por sucesor in-order (mínimo del subárbol derecho)
            Node<K, V> successor = findMin(node.right);
            node.key   = successor.key;
            node.value = successor.value;
            // Eliminar el sucesor del subárbol derecho
            // (reincrementa size porque deleteRec lo decrementará de nuevo)
            size++;
            node.right = deleteRec(node.right, successor.key);
        }
        updateHeight(node);
        return balance(node);
    }

    private Node<K, V> findMin(Node<K, V> node) {
        while (node.left != null) node = node.left;
        return node;
    }

    // Balanceo AVL

    /**
     * Aplica las rotaciones necesarias para restaurar la propiedad AVL.
     * Factor de balance (BF) = altura(izq) - altura(der).
     *   BF >  1 → izquierda pesada → LL o LR
     *   BF < -1 → derecha  pesada → RR o RL
     */
    private Node<K, V> balance(Node<K, V> node) {
        int bf = balanceFactor(node);

        if (bf > 1) {
            // Caso LR: el hijo izquierdo está sesgado a la derecha
            if (balanceFactor(node.left) < 0)
                node.left = rotateLeft(node.left);
            return rotateRight(node); // LL o después de corregir LR
        }
        if (bf < -1) {
            // Caso RL: el hijo derecho está sesgado a la izquierda
            if (balanceFactor(node.right) > 0)
                node.right = rotateRight(node.right);
            return rotateLeft(node); // RR o después de corregir RL
        }
        return node;
    }

    /**
     * Rotación simple a la derecha (caso LL).
     *
     *       y                x
     *      / \              / \
     *     x   T3   →      T1   y
     *    / \                  / \
     *   T1  T2               T2  T3
     */
    private Node<K, V> rotateRight(Node<K, V> y) {
        Node<K, V> x  = y.left;
        Node<K, V> t2 = x.right;
        x.right = y;
        y.left  = t2;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    /**
     * Rotación simple a la izquierda (caso RR).
     *
     *     x                  y
     *    / \                / \
     *   T1   y    →        x   T3
     *       / \           / \
     *      T2  T3        T1  T2
     */
    private Node<K, V> rotateLeft(Node<K, V> x) {
        Node<K, V> y  = x.right;
        Node<K, V> t2 = y.left;
        y.left  = x;
        x.right = t2;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    // Utilidades
    private int height(Node<K, V> node) {
        return node == null ? 0 : node.height;
    }

    private void updateHeight(Node<K, V> node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private int balanceFactor(Node<K, V> node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    private void inOrderCollect(Node<K, V> node, DoublyLinkedList<V> list) {
        if (node == null) return;
        inOrderCollect(node.left, list);
        list.addLast(node.value);
        inOrderCollect(node.right, list);
    }

    // Debug
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AVLTree[size=").append(size)
                .append(", height=").append(height()).append("]");
        return sb.toString();
    }
}
