package com.meditriage.controller;

import com.meditriage.model.Patient;
import com.meditriage.service.TriageService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la vista "Cola Prioritaria".
 *
 * Muestra todos los pacientes en espera, ordenados por el MinHeap.
 * Permite atender al siguiente (pop del heap) y deshacer la última acción.
 *
 * Implementa Refreshable para actualizar la tabla al navegar aquí.
 */
public class QueueController implements Initializable, Refreshable {

    // Tabla
    @FXML private TableView<Patient>           tableQueue;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String>  colName;
    @FXML private TableColumn<Patient, Integer> colAge;
    @FXML private TableColumn<Patient, String>  colLevel;
    @FXML private TableColumn<Patient, String>  colSymptoms;
    @FXML private TableColumn<Patient, String>  colArrival;
    @FXML private TableColumn<Patient, String>  colWait;

    // Acciones
    @FXML private Button btnAttendNext;
    @FXML private Button btnUndo;

    // Status
    @FXML private Label lblStatus;
    @FXML private Label lblQueueInfo;

    private final TriageService service = TriageService.getInstance();

    // Inicialización
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        tableQueue.setPlaceholder(new Label("La cola de espera está vacía."));
    }

    @Override
    public void refresh() {
        loadQueue();
    }

    // Columnas
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(160);

        colAge.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getAge()).asObject()
        );
        colAge.setPrefWidth(55);

        // Columna nivel con badge de color
        colLevel.setCellValueFactory(data -> new SimpleStringProperty(
            "  " + data.getValue().getLevel() + " — " + data.getValue().getLevelDescription()
        ));
        colLevel.setPrefWidth(160);
        colLevel.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                Patient p = getTableRow().getItem();
                setText(item);
                setStyle(getLevelStyle(p.getLevel()));
                setTextFill(Color.WHITE);
            }
        });

        colSymptoms.setCellValueFactory(new PropertyValueFactory<>("symptoms"));
        colSymptoms.setPrefWidth(240);

        colArrival.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getArrivalFormatted())
        );
        colArrival.setPrefWidth(110);

        colWait.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getWaitMinutes() + " min")
        );
        colWait.setPrefWidth(80);
    }

    //Carga de datos
    private void loadQueue() {
        Patient[] sorted = service.getQueueSnapshotSorted();
        tableQueue.getItems().setAll(sorted);
        int sz = sorted.length;
        lblQueueInfo.setText("Total en espera: " + sz + " paciente" + (sz != 1 ? "s" : ""));
        Patient next = service.peekNext();
        if (next != null) {
            lblStatus.setText("Siguiente: " + next.getName()
                + " | Nivel " + next.getLevel()
                + " — " + next.getLevelDescription()
                + " | Espera: " + next.getWaitMinutes() + " min");
        } else {
            lblStatus.setText("Cola vacía — no hay pacientes esperando.");
        }
    }

    // Atender siguiente
    @FXML
    private void onAttendNext() {
        Patient attended = service.attendNext();
        if (attended == null) {
            showAlert(Alert.AlertType.INFORMATION,
                "Cola vacía", "No hay pacientes en espera.");
            return;
        }
        showToast("✓ Atendido: " + attended.getName()
            + " (Nivel " + attended.getLevel() + ")");
        loadQueue();
    }

    // Deshacer
    @FXML
    private void onUndo() {
        String msg = service.undoLastAction();
        showToast(msg);
        loadQueue();
    }

    // Helpers
    private void showToast(String msg) {
        lblStatus.setText(msg);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /** Retorna el estilo CSS inline para el fondo de celda según nivel. */
    private String getLevelStyle(int level) {
        return switch (level) {
            case 1 -> "-fx-background-color: #e74c3c;";
            case 2 -> "-fx-background-color: #e67e22;";
            case 3 -> "-fx-background-color: #f39c12;";
            case 4 -> "-fx-background-color: #27ae60;";
            case 5 -> "-fx-background-color: #3498db;";
            default -> "";
        };
    }
}
