// BombermanMainApp.java
package com.example.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class BombermanMainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainMenu.fxml"));
        Parent root = loader.load();

        // Créer la scène
        Scene scene = new Scene(root, 800, 800);

        // Ajouter une feuille de style CSS si nécessaire
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Configurer la fenêtre
        primaryStage.setTitle("Bomberman - Menu Principal");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        // Afficher la fenêtre
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}