package com.meditriage.model;

/**
 * Representa una acción que puede deshacerse mediante el Stack de undo.
 *
 * REGISTER → guardar referencia al paciente para poder eliminarlo de todas las
 *            estructuras y de la BD.
 * ATTEND   → guardar referencia al paciente para poder reinsertarlo en la cola
 *            y quitarlo del historial, revirtiendo también la BD.
 */
public class UndoAction {

    public enum ActionType {
        REGISTER("Registrar"),
        ATTEND  ("Atender");

        private final String label;
        ActionType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    // Campos
    private final ActionType actionType;
    private final Patient    patient;
    private final long       timestampMs; // para logs

    // Constructor
    public UndoAction(ActionType actionType, Patient patient) {
        this.actionType  = actionType;
        this.patient     = patient;
        this.timestampMs = System.currentTimeMillis();
    }

    // Getters
    public ActionType getActionType()  { return actionType; }
    public Patient    getPatient()     { return patient; }
    public long       getTimestampMs() { return timestampMs; }

    @Override
    public String toString() {
        return String.format("UndoAction[%s, patient=%s]",
                actionType.getLabel(), patient.getName());
    }
}
