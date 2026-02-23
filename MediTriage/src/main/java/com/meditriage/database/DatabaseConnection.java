package com.meditriage.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la conexión JDBC a MySQL.
 *
 * Las credenciales se leen de {@code /app.properties} en el classpath,
 * evitando hard-code de contraseñas en el código fuente.
 *
 * Patrón: conexión directa (sin pool) adecuada para aplicaciones de escritorio
 * con un único usuario concurrente.
 */
public class DatabaseConnection {

    private static String url;
    private static String user;
    private static String password;

    // Carga las propiedades una sola vez al inicializar la clase
    static {
        try (InputStream is = DatabaseConnection.class
                .getResourceAsStream("/app.properties")) {
            if (is == null) {
                throw new RuntimeException(
                    "No se encontró /app.properties en el classpath. " +
                    "Copia src/main/resources/app.properties y configura las credenciales.");
            }
            Properties props = new Properties();
            props.load(is);
            url      = props.getProperty("db.url");
            user     = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo app.properties: " + e.getMessage(), e);
        }
    }

    /**
     * Abre y retorna una nueva conexión a la base de datos.
     * El llamador es responsable de cerrarla (try-with-resources recomendado).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Verifica si la conexión es posible.
     * @return true si la BD es alcanzable, false en caso contrario.
     */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c.isValid(2); // timeout 2 segundos
        } catch (SQLException e) {
            return false;
        }
    }

    // No instanciable
    private DatabaseConnection() {}
}
