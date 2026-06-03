package com.dfems;

import com.dfems.db.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Stage primaryStage;

    // Session variables — set at login, used across all controllers
    public static int    currentUserId   = -1;
    public static String currentUserName = "";
    public static String currentUserRole = "";

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("Digital Forensic Evidence Management System");
        stage.setMinWidth(1200);
        stage.setMinHeight(750);
        stage.setResizable(true);

        switchScene("/fxml/LoginView.fxml");
        stage.show();
    }

    public static void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                MainApp.class.getResource("/css/dfems.css").toExternalForm()
            );
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + fxmlPath);
        }
    }

    @Override
    public void stop() {
        DBConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
