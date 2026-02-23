package com.meditriage.service;

/**
 * DTO inmutable con las métricas del sistema de triaje.
 * Lo retorna TriageService.getStats() para el Dashboard.
 */
public class TriageStats {

    private final int    queueSize;        // pacientes en espera (heap)
    private final int    attendedToday;    // atendidos hoy (BD)
    private final double avgWaitMinutes;   // promedio espera hoy (BD)
    private final int    undoStackSize;    // acciones deshaciables
    private final int    hashTableSize;    // pacientes activos en hash
    private final float  hashLoadFactor;   // factor de carga del hash
    private final int    hashCollisions;   // colisiones acumuladas
    private final int    avlByIdHeight;    // altura del AVL por ID
    private final int    avlByNameHeight;  // altura del AVL por nombre
    private final int    historySize;      // pacientes en historial memoria

    public TriageStats(int queueSize, int attendedToday, double avgWaitMinutes,
                       int undoStackSize, int hashTableSize, float hashLoadFactor,
                       int hashCollisions, int avlByIdHeight, int avlByNameHeight,
                       int historySize) {
        this.queueSize       = queueSize;
        this.attendedToday   = attendedToday;
        this.avgWaitMinutes  = avgWaitMinutes;
        this.undoStackSize   = undoStackSize;
        this.hashTableSize   = hashTableSize;
        this.hashLoadFactor  = hashLoadFactor;
        this.hashCollisions  = hashCollisions;
        this.avlByIdHeight   = avlByIdHeight;
        this.avlByNameHeight = avlByNameHeight;
        this.historySize     = historySize;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int    getQueueSize()       { return queueSize; }
    public int    getAttendedToday()   { return attendedToday; }
    public double getAvgWaitMinutes()  { return avgWaitMinutes; }
    public int    getUndoStackSize()   { return undoStackSize; }
    public int    getHashTableSize()   { return hashTableSize; }
    public float  getHashLoadFactor()  { return hashLoadFactor; }
    public int    getHashCollisions()  { return hashCollisions; }
    public int    getAvlByIdHeight()   { return avlByIdHeight; }
    public int    getAvlByNameHeight() { return avlByNameHeight; }
    public int    getHistorySize()     { return historySize; }

    @Override
    public String toString() {
        return String.format(
            "Stats[queue=%d, attendedToday=%d, avgWait=%.1f min, " +
            "hash=%.2f LF / %d col, avlId h=%d, avlName h=%d]",
            queueSize, attendedToday, avgWaitMinutes,
            hashLoadFactor, hashCollisions, avlByIdHeight, avlByNameHeight
        );
    }
}
