package com.meditriage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación MediTriage.
 * Carga la ventana principal con sidebar de navegación.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            App.class.getResource("/com/meditriage/fxml/main.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(
            App.class.getResource("/com/meditriage/css/styles.css").toExternalForm()
        );

        primaryStage.setTitle("MediTriage — Sistema de Triaje Hospitalario");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
