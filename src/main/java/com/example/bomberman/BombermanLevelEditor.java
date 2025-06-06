package com.example.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BombermanLevelEditor extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BombermanLevelEditor.fxml"));
            Parent root = loader.load();

            // Créer la scène
            Scene scene = new Scene(root, 600, 650);

            // Charger le fichier CSS
            scene.getStylesheets().add(getClass().getResource("/bomberman-editor.css").toExternalForm());

            // Configurer la fenêtre
            primaryStage.setTitle("Éditeur de Niveau Bomberman");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'interface: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}