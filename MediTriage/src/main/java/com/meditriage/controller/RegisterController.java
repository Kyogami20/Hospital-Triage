package com.meditriage.controller;

import com.meditriage.model.Patient;
import com.meditriage.service.TriageService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la vista "Registrar Paciente".
 *
 * Valida el formulario y delega el registro al TriageService.
 * Muestra confirmación tipo "toast" que desaparece automáticamente.
 */
public class RegisterController implements Initializable {

    // Formulario
    @FXML private TextField        txtName;
    @FXML private TextField        txtAge;
    @FXML private TextArea         txtSymptoms;
    @FXML private ComboBox<String> cmbLevel;
    @FXML private Button           btnRegister;

    // Feedback
    @FXML private Label            lblMessage;
    @FXML private Label            lblLastRegistered;

    private final TriageService service = TriageService.getInstance();
    private Timeline            toastTimeline;

    // Inicialización
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbLevel.getItems().setAll(
            "1 — Resucitación (inmediata)",
            "2 — Emergente    (< 10 min)",
            "3 — Urgente      (< 30 min)",
            "4 — Menos Urgente (< 2 h)",
            "5 — No Urgente   (< 4 h)"
        );
        cmbLevel.getSelectionModel().select(2); // Urgente por defecto
        lblMessage.setVisible(false);
    }

    // Registro
    @FXML
    private void onRegister() {
        if (!validateForm()) return;

        String name     = txtName.getText().trim();
        int    age      = Integer.parseInt(txtAge.getText().trim());
        String symptoms = txtSymptoms.getText().trim();
        int    level    = cmbLevel.getSelectionModel().getSelectedIndex() + 1;

        try {
            Patient registered = service.registerPatient(name, age, symptoms, level);
            showSuccess("✓ Paciente registrado: " + registered.getName()
                + " (ID " + registered.getId() + ", Nivel " + registered.getLevel() + ")");
            lblLastRegistered.setText(
                "Último: #" + registered.getId() + " — " + registered.getName()
                + " | Nivel " + registered.getLevel() + " | Edad " + registered.getAge()
            );
            clearForm();
        } catch (Exception ex) {
            showError("⚠ Error al registrar: " + ex.getMessage());
        }
    }

    @FXML
    private void onClear() {
        clearForm();
        lblMessage.setVisible(false);
    }

    // Validación
    private boolean validateForm() {
        // Nombre
        if (txtName.getText() == null || txtName.getText().isBlank()) {
            showError("El nombre es obligatorio.");
            txtName.requestFocus();
            return false;
        }
        if (txtName.getText().trim().length() < 3) {
            showError("El nombre debe tener al menos 3 caracteres.");
            txtName.requestFocus();
            return false;
        }

        // Edad
        try {
            int age = Integer.parseInt(txtAge.getText().trim());
            if (age < 0 || age > 120) {
                showError("La edad debe estar entre 0 y 120.");
                txtAge.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("La edad debe ser un número entero válido.");
            txtAge.requestFocus();
            return false;
        }

        // Síntomas
        if (txtSymptoms.getText() == null || txtSymptoms.getText().isBlank()) {
            showError("Los síntomas son obligatorios.");
            txtSymptoms.requestFocus();
            return false;
        }

        // Nivel
        if (cmbLevel.getSelectionModel().isEmpty()) {
            showError("Selecciona el nivel de urgencia.");
            return false;
        }

        return true;
    }

    // Helpers
    private void clearForm() {
        txtName.clear();
        txtAge.clear();
        txtSymptoms.clear();
        cmbLevel.getSelectionModel().select(2);
        txtName.requestFocus();
    }

    private void showSuccess(String msg) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().setAll("msg-success");
        lblMessage.setVisible(true);
        scheduleToastDismiss();
    }

    private void showError(String msg) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().setAll("msg-error");
        lblMessage.setVisible(true);
        scheduleToastDismiss();
    }

    /** El mensaje de confirmación desaparece automáticamente después de 4 segundos. */
    private void scheduleToastDismiss() {
        if (toastTimeline != null) toastTimeline.stop();
        toastTimeline = new Timeline(
            new KeyFrame(Duration.seconds(4), e -> lblMessage.setVisible(false))
        );
        toastTimeline.play();
    }
}
