package com.meditriage.controller;

import com.meditriage.model.Patient;
import com.meditriage.service.TriageService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la vista "Historial".
 *
 * Muestra los pacientes atendidos con filtros por nivel, nombre y tiempo.
 * Los datos vienen de la BD (PatientDAO) para consulta completa,
 * y de la DoublyLinkedList en memoria para el historial reciente.
 *
 * Implementa Refreshable para recargar al navegar aquí.
 */
public class HistoryController implements Initializable, Refreshable {

    // Filtros
    @FXML private ComboBox<String> cmbFilterLevel;
    @FXML private TextField        txtFilterName;
    @FXML private ComboBox<String> cmbFilterTime;

    // Tabla
    @FXML private TableView<Patient>           tableHistory;
    @FXML private TableColumn<Patient, String> colId;
    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, String> colAge;
    @FXML private TableColumn<Patient, String> colLevel;
    @FXML private TableColumn<Patient, String> colSymptoms;
    @FXML private TableColumn<Patient, String> colArrival;
    @FXML private TableColumn<Patient, String> colAttended;
    @FXML private TableColumn<Patient, String> colWait;

    // Info
    @FXML private Label lblHistoryInfo;

    private final TriageService service = TriageService.getInstance();
    private Patient[] currentData = new Patient[0]; // para exportar CSV

    // Inicialización
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Filtro nivel
        cmbFilterLevel.getItems().setAll(
            "Todos",
            "Nivel 1 — Resucitación",
            "Nivel 2 — Emergente",
            "Nivel 3 — Urgente",
            "Nivel 4 — Menos Urgente",
            "Nivel 5 — No Urgente"
        );
        cmbFilterLevel.getSelectionModel().select(0);

        // Filtro tiempo
        cmbFilterTime.getItems().setAll("Hoy", "Última hora", "Todo");
        cmbFilterTime.getSelectionModel().select(0);

        setupTableColumns();
        tableHistory.setPlaceholder(new Label("Sin pacientes atendidos con los filtros seleccionados."));
    }

    @Override
    public void refresh() {
        applyFilters();
    }

    // Filtros
    @FXML
    private void onApplyFilters() {
        applyFilters();
    }

    @FXML
    private void onClearFilters() {
        cmbFilterLevel.getSelectionModel().select(0);
        txtFilterName.clear();
        cmbFilterTime.getSelectionModel().select(0);
        applyFilters();
    }

    private void applyFilters() {
        int    levelIdx  = cmbFilterLevel.getSelectionModel().getSelectedIndex(); // 0=todos
        int    level     = levelIdx; // 0=todos, 1-5=nivel exacto
        String name      = txtFilterName.getText();
        String timeStr   = cmbFilterTime.getSelectionModel().getSelectedItem();
        String timeFilter;
        if      ("Hoy".equals(timeStr))          timeFilter = "TODAY";
        else if ("Última hora".equals(timeStr))  timeFilter = "LAST_HOUR";
        else                                     timeFilter = "ALL";

        currentData = service.getDao().listAttendedWithFilters(level, name, timeFilter);
        tableHistory.getItems().setAll(currentData);
        lblHistoryInfo.setText("Mostrando " + currentData.length + " registro(s).");
    }

    // Exportar CSV
    @FXML
    private void onExportCsv() {
        if (currentData.length == 0) {
            showAlert(Alert.AlertType.INFORMATION, "Sin datos",
                "No hay datos para exportar con los filtros actuales.");
            return;
        }

        // Diálogo simple para elegir destino (se escribe en escritorio por defecto)
        String path = System.getProperty("user.home") + "/meditriage_historial.csv";

        try (FileWriter fw = new FileWriter(path)) {
            fw.write("ID,Nombre,Edad,Nivel,Descripcion,Sintomas,Llegada,Atendido,EsperaMin\n");
            for (Patient p : currentData) {
                if (p == null) continue;
                fw.write(
                    csvField(String.valueOf(p.getId()))        + "," +
                    csvField(p.getName())                      + "," +
                    csvField(String.valueOf(p.getAge()))       + "," +
                    csvField(String.valueOf(p.getLevel()))     + "," +
                    csvField(p.getLevelDescription())          + "," +
                    csvField(p.getSymptoms())                  + "," +
                    csvField(p.getArrivalFormatted())          + "," +
                    csvField(p.getAttendedFormatted())         + "," +
                    csvField(String.valueOf(p.getWaitMinutes()))+ "\n"
                );
            }
            showAlert(Alert.AlertType.INFORMATION, "Exportado",
                "CSV guardado en:\n" + path);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                "No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    /** Envuelve un campo en comillas si contiene comas o saltos de línea. */
    private String csvField(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Columnas
    private void setupTableColumns() {
        colId.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colId.setPrefWidth(55);

        colName.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getName()));
        colName.setPrefWidth(155);

        colAge.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getAge())));
        colAge.setPrefWidth(50);

        colLevel.setCellValueFactory(d ->
            new SimpleStringProperty(
                "N" + d.getValue().getLevel() + " " + d.getValue().getLevelDescription()));
        colLevel.setPrefWidth(150);
        colLevel.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle(""); return;
                }
                Patient p = getTableRow().getItem();
                setText(item);
                setStyle(getLevelBg(p.getLevel()));
                setTextFill(javafx.scene.paint.Color.WHITE);
            }
        });

        colSymptoms.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getSymptoms()));
        colSymptoms.setPrefWidth(200);

        colArrival.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getArrivalFormatted()));
        colArrival.setPrefWidth(110);

        colAttended.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getAttendedFormatted()));
        colAttended.setPrefWidth(110);

        colWait.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getWaitMinutes() + " min"));
        colWait.setPrefWidth(75);
    }

    private String getLevelBg(int level) {
        return switch (level) {
            case 1 -> "-fx-background-color: #e74c3c;";
            case 2 -> "-fx-background-color: #e67e22;";
            case 3 -> "-fx-background-color: #f39c12;";
            case 4 -> "-fx-background-color: #27ae60;";
            case 5 -> "-fx-background-color: #3498db;";
            default -> "";
        };
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
