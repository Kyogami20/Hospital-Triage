package com.meditriage.controller;

import com.meditriage.model.Patient;
import com.meditriage.service.TriageService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la vista "Buscar Paciente".
 *
 * Permite buscar por ID exacto (AVL por ID) o por nombre (AVL por nombre /
 * traversal filtrado). Muestra la ficha del paciente encontrado.
 */
public class SearchController implements Initializable {

    // Búsqueda
    @FXML private TextField txtSearch;
    @FXML private Button    btnSearchById;
    @FXML private Button    btnSearchByName;

    // Resultado único
    @FXML private VBox      vboxResult;
    @FXML private Label     lblResultId;
    @FXML private Label     lblResultName;
    @FXML private Label     lblResultAge;
    @FXML private Label     lblResultLevel;
    @FXML private Label     lblResultStatus;
    @FXML private Label     lblResultSymptoms;
    @FXML private Label     lblResultArrival;
    @FXML private Label     lblResultAttended;
    @FXML private Label     lblResultWait;

    // Resultados múltiples (búsqueda por nombre)
    @FXML private TableView<Patient>          tableResults;
    @FXML private TableColumn<Patient, String> colResId;
    @FXML private TableColumn<Patient, String> colResName;
    @FXML private TableColumn<Patient, String> colResAge;
    @FXML private TableColumn<Patient, String> colResLevel;
    @FXML private TableColumn<Patient, String> colResStatus;

    // Status
    @FXML private Label lblSearchStatus;

    private final TriageService service = TriageService.getInstance();

    // Inicialización
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vboxResult.setVisible(false);
        tableResults.setVisible(false);
        setupTableColumns();

        // Enter en el campo dispara búsqueda por ID
        txtSearch.setOnAction(e -> onSearchById());
    }

    // Búsqueda por ID
    @FXML
    private void onSearchById() {
        String text = txtSearch.getText().trim();
        if (text.isBlank()) { showStatus("Introduce un ID numérico."); return; }

        try {
            int id = Integer.parseInt(text);
            Patient p = service.searchById(id);
            if (p != null) {
                showSingleResult(p);
                showStatus("Paciente encontrado via AVL (ID) — O(log n).");
            } else {
                hidePanels();
                showStatus("No se encontró ningún paciente con ID " + id + ".");
            }
        } catch (NumberFormatException e) {
            showStatus("El ID debe ser un número entero.");
        }
    }

    // Búsqueda por Nombre
    @FXML
    private void onSearchByName() {
        String text = txtSearch.getText().trim();
        if (text.isBlank()) { showStatus("Introduce un nombre o fragmento."); return; }

        // Primero intenta búsqueda exacta en AVL de nombres
        Patient exact = service.searchByNameExact(text);
        if (exact != null) {
            showSingleResult(exact);
            showStatus("Encontrado via AVL (nombre exacto) — O(log n).");
            return;
        }

        // Si no, traversal del AVL + filtro "contiene"
        Patient[] results = service.searchByNameContains(text);
        if (results.length == 0) {
            hidePanels();
            showStatus("No se encontraron pacientes con nombre que contenga «" + text + "».");
        } else if (results.length == 1) {
            showSingleResult(results[0]);
            showStatus("1 resultado encontrado (traversal AVL + filtro).");
        } else {
            showMultipleResults(results);
            showStatus(results.length + " resultados encontrados (traversal AVL + filtro).");
        }
    }

    @FXML
    private void onClearSearch() {
        txtSearch.clear();
        hidePanels();
        lblSearchStatus.setText("");
        txtSearch.requestFocus();
    }

    // Mostrar ficha de paciente
    private void showSingleResult(Patient p) {
        tableResults.setVisible(false);
        vboxResult.setVisible(true);

        lblResultId.setText      ("#" + p.getId());
        lblResultName.setText    (p.getName());
        lblResultAge.setText     (p.getAge() + " años");
        lblResultLevel.setText   ("Nivel " + p.getLevel() + " — " + p.getLevelDescription());
        lblResultLevel.getStyleClass().setAll("field-value", p.getLevelCssClass(), "level-badge");
        lblResultStatus.setText  (p.getStatus());
        lblResultStatus.getStyleClass().setAll(
            "field-value",
            "WAITING".equals(p.getStatus()) ? "status-waiting" : "status-attended"
        );
        lblResultSymptoms.setText(p.getSymptoms());
        lblResultArrival.setText (p.getArrivalFormatted());
        lblResultAttended.setText(p.getAttendedFormatted());
        lblResultWait.setText    (p.getWaitMinutes() + " min");
    }

    private void showMultipleResults(Patient[] patients) {
        vboxResult.setVisible(false);
        tableResults.setVisible(true);
        tableResults.getItems().setAll(patients);
    }

    private void hidePanels() {
        vboxResult.setVisible(false);
        tableResults.setVisible(false);
    }

    private void showStatus(String msg) {
        lblSearchStatus.setText(msg);
    }

    // Configurar tabla de resultados múltiples
    private void setupTableColumns() {
        colResId.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colResName.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));
        colResAge.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getAge() + " años"));
        colResLevel.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(
                "N" + d.getValue().getLevel() + " " + d.getValue().getLevelDescription()
            ));
        colResStatus.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));

        tableResults.setPlaceholder(new Label("Sin resultados."));
    }
}
