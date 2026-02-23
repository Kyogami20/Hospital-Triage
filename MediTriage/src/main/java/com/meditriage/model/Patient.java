package com.meditriage.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo de dominio: Paciente en el sistema de triaje.
 *
 * Niveles de prioridad (Manchester Triage System simplificado):
 *   1 = Resucitación   (rojo)    — atención inmediata
 *   2 = Emergente      (naranja) — &lt; 10 min
 *   3 = Urgente        (amarillo)— &lt; 30 min
 *   4 = Menos urgente  (verde)   — &lt; 2 h
 *   5 = No urgente     (azul)    — &lt; 4 h
 */
public class Patient {

    // Formato de fecha para mostrar en UI
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");

    // Campos
    private int           id;
    private String        name;
    private int           age;
    private String        symptoms;
    private int           level;       // 1..5
    private String        status;      // "WAITING" | "ATTENDED"
    private LocalDateTime arrivalAt;
    private LocalDateTime attendedAt;
    private LocalDateTime createdAt;

    // Constructores
    public Patient() {}

    public Patient(int id, String name, int age, String symptoms, int level,
                   String status, LocalDateTime arrivalAt,
                   LocalDateTime attendedAt, LocalDateTime createdAt) {
        this.id          = id;
        this.name        = name;
        this.age         = age;
        this.symptoms    = symptoms;
        this.level       = level;
        this.status      = status;
        this.arrivalAt   = arrivalAt;
        this.attendedAt  = attendedAt;
        this.createdAt   = createdAt;
    }

    // Getters y Setters
    public int           getId()          { return id; }
    public void          setId(int id)    { this.id = id; }

    public String        getName()                 { return name; }
    public void          setName(String name)      { this.name = name; }

    public int           getAge()                  { return age; }
    public void          setAge(int age)           { this.age = age; }

    public String        getSymptoms()                   { return symptoms; }
    public void          setSymptoms(String symptoms)    { this.symptoms = symptoms; }

    public int           getLevel()                { return level; }
    public void          setLevel(int level)       { this.level = level; }

    public String        getStatus()                     { return status; }
    public void          setStatus(String status)        { this.status = status; }

    public LocalDateTime getArrivalAt()                        { return arrivalAt; }
    public void          setArrivalAt(LocalDateTime arrivalAt) { this.arrivalAt = arrivalAt; }

    public LocalDateTime getAttendedAt()                         { return attendedAt; }
    public void          setAttendedAt(LocalDateTime attendedAt) { this.attendedAt = attendedAt; }

    public LocalDateTime getCreatedAt()                        { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Métodos de utilidad

    /** Descripción textual del nivel de urgencia. */
    public String getLevelDescription() {
        return switch (level) {
            case 1 -> "Resucitación";
            case 2 -> "Emergente";
            case 3 -> "Urgente";
            case 4 -> "Menos Urgente";
            case 5 -> "No Urgente";
            default -> "Desconocido";
        };
    }

    /** Clase CSS correspondiente al nivel (p. ej. "level-1"). */
    public String getLevelCssClass() {
        return "level-" + level;
    }

    /** Arrival formateado para la UI. */
    public String getArrivalFormatted() {
        return arrivalAt == null ? "—" : arrivalAt.format(FMT);
    }

    /** AttendedAt formateado para la UI. */
    public String getAttendedFormatted() {
        return attendedAt == null ? "—" : attendedAt.format(FMT);
    }

    /**
     * Tiempo de espera en minutos.
     * Si ya fue atendido, entre arrival y attended; si sigue esperando, hasta ahora.
     */
    public long getWaitMinutes() {
        if (arrivalAt == null) return 0;
        LocalDateTime end = (attendedAt != null) ? attendedAt : LocalDateTime.now();
        return java.time.Duration.between(arrivalAt, end).toMinutes();
    }

    // Igualdad basada en ID
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Patient other)) return false;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Patient[id=%d, name=%s, level=%d, status=%s]",
                id, name, level, status);
    }
}
