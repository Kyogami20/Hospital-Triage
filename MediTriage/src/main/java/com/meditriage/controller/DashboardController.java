package com.meditriage.controller;

import com.meditriage.model.Patient;
import com.meditriage.service.TriageService;
import com.meditriage.service.TriageStats;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controlador del Dashboard.
 *
 * Muestra KPIs en tiempo real y gestiona el simulador de llegada de pacientes.
 * Implementa Refreshable para actualizar datos al navegar a esta vista.
 */
public class DashboardController implements Initializable, Refreshable {

    // KPI Cards
    @FXML private Label lblQueueSize;
    @FXML private Label lblMostUrgent;
    @FXML private Label lblMostUrgentLevel;
    @FXML private Label lblAttendedToday;
    @FXML private Label lblAvgWait;

    // métricas de estructuras
    @FXML private Label lblHashInfo;
    @FXML private Label lblAvlInfo;
    @FXML private Label lblUndoInfo;
    @FXML private Label lblHistoryInfo;

    // simulación
    @FXML private ToggleButton toggleSimulation;
    @FXML private Slider       sliderInterval;
    @FXML private Label        lblIntervalValue;
    @FXML private ComboBox<String> cmbDistribution;
    @FXML private Button       btnCriticalCase;

    // log de eventos
    @FXML private TextArea taSimLog;

    // internals
    private final TriageService service = TriageService.getInstance();
    private Timeline            simulationTimeline;
    private Timeline            refreshTimeline;

    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm:ss");

    // Nombres y síntomas para generación de manera random
    private static final String[] NAMES = {
        "Carlos García",    "María López",      "Juan Martínez",   "Ana Rodríguez",
        "Luis González",    "Carmen Fernández", "Pedro Sánchez",   "Laura Torres",
        "Miguel Flores",    "Isabel Díaz",      "Antonio Morales", "Elena Jiménez",
        "Francisco Ruiz",   "Rosa Herrera",     "Manuel Castro",   "Sofía Romero",
        "Diego Vargas",     "Patricia Mendoza", "Andrés Vega",     "Claudia Ramos",
        "Ernesto Fuentes",  "Valentina Cruz",   "Roberto Ríos",    "Natalia Parra"
    };

    private static final String[] SYMPTOMS = {
        "Dolor en el pecho y dificultad para respirar",
        "Fiebre alta 40°C y convulsiones",
        "Traumatismo craneal con pérdida de conciencia",
        "Dolor abdominal severo con vómitos",
        "Fractura expuesta en miembro inferior",
        "Reacción alérgica severa (anafilaxia)",
        "Hemorragia interna sospechada",
        "Quemaduras de segundo grado en tronco",
        "Paro cardiorrespiratorio",
        "Accidente cerebrovascular (ACV)",
        "Dolor de cabeza intenso (cefalea)",
        "Mareos, náuseas y vómitos persistentes",
        "Tos con sangre",
        "Dolor torácico de esfuerzo",
        "Fractura de tobillo",
        "Herida cortante superficial en mano",
        "Contusión menor en pierna",
        "Dolor muscular generalizado",
        "Picadura de insecto sin anafilaxia",
        "Resfriado común con fiebre leve"
    };

    // inicialización
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Combo de distribución
        cmbDistribution.getItems().setAll("REALISTA", "UNIFORME");
        cmbDistribution.getSelectionModel().select("REALISTA");

        // Slider de intervalo
        sliderInterval.setMin(1);
        sliderInterval.setMax(15);
        sliderInterval.setValue(5);
        sliderInterval.setBlockIncrement(1);
        lblIntervalValue.setText("5 s");
        sliderInterval.valueProperty().addListener((obs, oldV, newV) -> {
            int secs = newV.intValue();
            lblIntervalValue.setText(secs + " s");
            if (simulationTimeline != null && toggleSimulation.isSelected()) {
                restartSimulation(secs);
            }
        });

        // Auto-refresh del dashboard cada 3 s
        refreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> updateKpis())
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();

        updateKpis();
        appendLog("Sistema MediTriage iniciado.");
    }

    // Refreshable
    @Override
    public void refresh() {
        updateKpis();
    }

    // KPIs
    private void updateKpis() {
        TriageStats stats = service.getStats();
        Patient mostUrgent = service.peekNext();

        lblQueueSize.setText(String.valueOf(stats.getQueueSize()));

        if (mostUrgent != null) {
            lblMostUrgent.setText(mostUrgent.getName());
            lblMostUrgentLevel.setText(
                "Nivel " + mostUrgent.getLevel() + " — " + mostUrgent.getLevelDescription()
            );
            lblMostUrgentLevel.getStyleClass().setAll("card-subtitle", mostUrgent.getLevelCssClass());
        } else {
            lblMostUrgent.setText("—");
            lblMostUrgentLevel.setText("Cola vacía");
        }

        lblAttendedToday.setText(String.valueOf(stats.getAttendedToday()));
        lblAvgWait.setText(String.format("%.1f min", stats.getAvgWaitMinutes()));

        // Métricas de estructuras
        lblHashInfo.setText(String.format(
            "Hash: %d entradas | LF=%.2f | %d colisiones",
            stats.getHashTableSize(), stats.getHashLoadFactor(), stats.getHashCollisions()
        ));
        lblAvlInfo.setText(String.format(
            "AVL ID: h=%d  |  AVL Nombre: h=%d",
            stats.getAvlByIdHeight(), stats.getAvlByNameHeight()
        ));
        lblUndoInfo.setText("Stack undo: " + stats.getUndoStackSize() + " acción(es)");
        lblHistoryInfo.setText("Historial memoria: " + stats.getHistorySize() + " paciente(s)");
    }

    // Simulación
    @FXML
    private void onToggleSimulation() {
        if (toggleSimulation.isSelected()) {
            toggleSimulation.setText("■ Detener Simulación");
            int secs = (int) sliderInterval.getValue();
            restartSimulation(secs);
            appendLog("▶ Simulación iniciada (cada " + secs + " s, modo "
                + cmbDistribution.getValue() + ").");
        } else {
            toggleSimulation.setText("▶ Iniciar Simulación");
            stopSimulation();
            appendLog("■ Simulación detenida.");
        }
    }

    private void restartSimulation(int seconds) {
        stopSimulation();
        simulationTimeline = new Timeline(
            new KeyFrame(Duration.seconds(seconds), e -> generateRandomPatient())
        );
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
        simulationTimeline.play();
    }

    private void stopSimulation() {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
            simulationTimeline = null;
        }
    }

    private void generateRandomPatient() {
        String distrib = cmbDistribution.getValue();
        int    level   = "REALISTA".equals(distrib) ? randomLevelRealistic() : randomLevelUniform();
        generatePatient(level);
    }

    @FXML
    private void onGenerateCriticalCase() {
        generatePatient(1);
        appendLog("🚨 Caso crítico (Nivel 1) generado manualmente.");
    }

    private void generatePatient(int level) {
        String name     = NAMES[   (int)(Math.random() * NAMES.length)];
        String symptoms = SYMPTOMS[(int)(Math.random() * SYMPTOMS.length)];
        int    age      = 1 + (int)(Math.random() * 90);

        try {
            Patient p = service.registerPatient(name, age, symptoms, level);
            appendLog("[" + LocalDateTime.now().format(TIME_FMT) + "] "
                + "Nivel " + level + " — " + p.getName()
                + " (ID " + p.getId() + ") registrado.");
            updateKpis();
        } catch (Exception e) {
            appendLog("⚠ Error generando paciente: " + e.getMessage());
        }
    }

    /** Nivel aleatorio con distribución realista de triaje. */
    private int randomLevelRealistic() {
        double r = Math.random();
        if (r < 0.05) return 1;   // 5%  Resucitación
        if (r < 0.20) return 2;   // 15% Emergente
        if (r < 0.50) return 3;   // 30% Urgente
        if (r < 0.80) return 4;   // 30% Menos urgente
        return 5;                  // 20% No urgente
    }

    /** Nivel aleatorio uniforme (20% cada uno). */
    private int randomLevelUniform() {
        return 1 + (int)(Math.random() * 5);
    }

    private void appendLog(String message) {
        if (taSimLog.getText().length() > 8000) {
            // Recortar el log para no sobrecargar la UI
            taSimLog.setText(taSimLog.getText().substring(2000));
        }
        taSimLog.appendText(message + "\n");
    }
}
