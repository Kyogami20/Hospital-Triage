/**
 * Módulo principal de MediTriage.
 * Sistema de Triaje Hospitalario con estructuras de datos implementadas desde cero.
 */
module com.meditriage {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // JDBC para MySQL
    requires java.sql;

    // Abre paquetes al sistema FXML (necesario para reflexión de controladores)
    opens com.meditriage             to javafx.fxml;
    opens com.meditriage.controller  to javafx.fxml;
    opens com.meditriage.model       to javafx.fxml;

    // Exporta paquetes públicos
    exports com.meditriage;
    exports com.meditriage.model;
    exports com.meditriage.structures;
    exports com.meditriage.database;
    exports com.meditriage.service;
    exports com.meditriage.controller;
}
