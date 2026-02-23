package com.meditriage.controller;

import com.meditriage.App;
import com.meditriage.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la ventana principal.
 *
 * Gestiona el sidebar de navegación y la carga dinámica de vistas
 * en el área central (contentArea).
 */
public class MainController implements Initializable {

    // Sidebar
    @FXML private Button    btnDashboard;
    @FXML private Button    btnRegister;
    @FXML private Button    btnQueue;
    @FXML private Button    btnSearch;
    @FXML private Button    btnHistory;

    // Área de contenido central
    @FXML private StackPane contentArea;

    // Status bar
    @FXML private Label     lblDbStatus;

    // Botón activo actualmente
    private Button activeButton;

    // Inicialización
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        checkDbConnection();
        loadView("dashboard");
        setActive(btnDashboard);
    }

    // Manejadores de navegación
    @FXML private void onDashboard() { loadView("dashboard"); setActive(btnDashboard); }
    @FXML private void onRegister()  { loadView("register");  setActive(btnRegister);  }
    @FXML private void onQueue()     { loadView("queue");      setActive(btnQueue);     }
    @FXML private void onSearch()    { loadView("search");    setActive(btnSearch);    }
    @FXML private void onHistory()   { loadView("history");   setActive(btnHistory);   }

    // Carga dinámica de vistas

    /**
     * Carga la vista FXML correspondiente y la coloca en contentArea.
     * Si el controlador de la vista implementa Refreshable, llama a refresh().
     */
    private void loadView(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/meditriage/fxml/" + viewName + ".fxml")
            );
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);

            // Refrescar datos si el controlador lo soporta
            Object ctrl = loader.getController();
            if (ctrl instanceof Refreshable r) {
                r.refresh();
            }
        } catch (IOException ex) {
            System.err.println("[Main] Error cargando vista '" + viewName + "': " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Estilo del botón activo
    private void setActive(Button btn) {
        if (activeButton != null) activeButton.getStyleClass().remove("active");
        activeButton = btn;
        if (btn != null && !btn.getStyleClass().contains("active")) {
            btn.getStyleClass().add("active");
        }
    }

    // Estado de la BD
    private void checkDbConnection() {
        new Thread(() -> {
            boolean ok = DatabaseConnection.testConnection();
            javafx.application.Platform.runLater(() -> {
                if (ok) {
                    lblDbStatus.setText("● BD Conectada");
                    lblDbStatus.getStyleClass().setAll("db-status-ok");
                } else {
                    lblDbStatus.setText("● BD Sin conexión");
                    lblDbStatus.getStyleClass().setAll("db-status-error");
                }
            });
        }).start();
    }
}
