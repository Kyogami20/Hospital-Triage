package com.meditriage.database;

import com.meditriage.model.Patient;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Data Access Object para la entidad Patient.
 *
 * Todas las operaciones usan PreparedStatement para prevenir SQL Injection.
 * Los arreglos nativos Patient[] se usan en lugar de ArrayList (regla del proyecto).
 */
public class PatientDAO {

    // Columnas comunes
    private static final String SELECT_COLS =
        "id, name, age, symptoms, level, status, arrival_at, attended_at, created_at";

    // INSERT

    /**
     * Persiste un nuevo paciente y retorna el ID generado por AUTO_INCREMENT.
     */
    public int insertPatient(Patient p) {
        String sql = "INSERT INTO patients (name, age, symptoms, level, status, arrival_at, created_at) " +
                     "VALUES (?, ?, ?, ?, 'WAITING', ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getName());
            ps.setInt   (2, p.getAge());
            ps.setString(3, p.getSymptoms());
            ps.setInt   (4, p.getLevel());
            ps.setTimestamp(5, Timestamp.valueOf(p.getArrivalAt()));
            ps.setTimestamp(6, Timestamp.valueOf(p.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error insertPatient: " + e.getMessage());
        }
        return -1;
    }

    // UPDATE

    /**
     * Marca un paciente como ATTENDED y registra la hora de atención.
     */
    public boolean updateStatusToAttended(int id, LocalDateTime attendedAt) {
        String sql = "UPDATE patients SET status='ATTENDED', attended_at=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(attendedAt));
            ps.setInt      (2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] Error updateStatusToAttended: " + e.getMessage());
            return false;
        }
    }

    /**
     * Revierte un paciente de ATTENDED a WAITING (para undo de ATTEND).
     * Pone attended_at = NULL y status = 'WAITING'.
     */
    public boolean revertToWaiting(int id) {
        String sql = "UPDATE patients SET status='WAITING', attended_at=NULL WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] Error revertToWaiting: " + e.getMessage());
            return false;
        }
    }

    // DELETE

    /**
     * Elimina físicamente un paciente (para undo de REGISTER).
     */
    public boolean deletePatient(int id) {
        String sql = "DELETE FROM patients WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] Error deletePatient: " + e.getMessage());
            return false;
        }
    }

    // SELECT

    /** Busca un paciente por ID exacto. Retorna null si no existe. */
    public Patient findById(int id) {
        String sql = "SELECT " + SELECT_COLS + " FROM patients WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error findById: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca pacientes cuyo nombre contenga {@code nameLike} (búsqueda parcial).
     * Retorna arreglo nativo (sin java.util.List).
     */
    public Patient[] findByNameLike(String nameLike) {
        String sql = "SELECT " + SELECT_COLS +
                     " FROM patients WHERE name LIKE ? ORDER BY arrival_at DESC LIMIT 50";
        return queryMultiple(sql, "%" + nameLike + "%");
    }

    /** Lista todos los pacientes en estado WAITING, ordenados por prioridad. */
    public Patient[] listWaiting() {
        String sql = "SELECT " + SELECT_COLS +
                     " FROM patients WHERE status='WAITING' ORDER BY level ASC, arrival_at ASC";
        return queryMultiple(sql, null);
    }

    /**
     * Lista pacientes ATTENDED con filtros opcionales.
     *
     * @param level      0 = todos los niveles; 1-5 = filtro exacto.
     * @param nameFilter null o vacío = sin filtro de nombre.
     * @param timeFilter "TODAY" | "LAST_HOUR" | "ALL"
     */
    public Patient[] listAttendedWithFilters(int level, String nameFilter, String timeFilter) {
        StringBuilder sql = new StringBuilder(
            "SELECT " + SELECT_COLS + " FROM patients WHERE status='ATTENDED'"
        );

        if (level > 0)
            sql.append(" AND level=").append(level);

        if (nameFilter != null && !nameFilter.isBlank())
            sql.append(" AND name LIKE '%").append(nameFilter.replace("'", "''")).append("%'");

        if ("TODAY".equals(timeFilter))
            sql.append(" AND DATE(attended_at) = CURDATE()");
        else if ("LAST_HOUR".equals(timeFilter))
            sql.append(" AND attended_at >= NOW() - INTERVAL 1 HOUR");

        sql.append(" ORDER BY attended_at DESC LIMIT 500");

        return queryMultiple(sql.toString(), null);
    }

    /** Cuenta pacientes ATTENDED hoy (para KPI del Dashboard). */
    public int countAttendedToday() {
        String sql = "SELECT COUNT(*) FROM patients WHERE status='ATTENDED' AND DATE(attended_at)=CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DAO] Error countAttendedToday: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Tiempo promedio de espera (minutos) de los últimos 100 pacientes atendidos hoy.
     */
    public double avgWaitMinutesToday() {
        String sql = "SELECT AVG(TIMESTAMPDIFF(MINUTE, arrival_at, attended_at)) " +
                     "FROM patients WHERE status='ATTENDED' AND DATE(attended_at)=CURDATE() LIMIT 100";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[DAO] Error avgWaitMinutesToday: " + e.getMessage());
        }
        return 0;
    }

    // Helpers

    /** Ejecuta una consulta que puede retornar múltiples filas. */
    private Patient[] queryMultiple(String sql, String param) {
        // Primera pasada: recolectar en arreglo temporal de tamaño fijo
        Patient[] temp = new Patient[512];
        int count = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps;
            if (param != null) {
                ps = conn.prepareStatement(sql);
                ps.setString(1, param);
            } else {
                ps = conn.prepareStatement(sql);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next() && count < temp.length) {
                    temp[count++] = mapRow(rs);
                }
            }
            ps.close();
        } catch (SQLException e) {
            System.err.println("[DAO] Error queryMultiple: " + e.getMessage());
        }

        // Copiar al tamaño exacto
        Patient[] result = new Patient[count];
        for (int i = 0; i < count; i++) result[i] = temp[i];
        return result;
    }

    /** Mapea una fila del ResultSet a un objeto Patient. */
    private Patient mapRow(ResultSet rs) throws SQLException {
        Timestamp arrTs  = rs.getTimestamp("arrival_at");
        Timestamp attTs  = rs.getTimestamp("attended_at");
        Timestamp creTs  = rs.getTimestamp("created_at");

        return new Patient(
            rs.getInt   ("id"),
            rs.getString("name"),
            rs.getInt   ("age"),
            rs.getString("symptoms"),
            rs.getInt   ("level"),
            rs.getString("status"),
            arrTs != null ? arrTs.toLocalDateTime() : null,
            attTs != null ? attTs.toLocalDateTime() : null,
            creTs != null ? creTs.toLocalDateTime() : null
        );
    }
}
