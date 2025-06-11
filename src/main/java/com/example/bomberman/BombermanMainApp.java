package com.example.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale de l'application Bomberman.
 * Elle étend javafx.application.Application et sert de point d'entrée à l'application JavaFX.
 */
public class BombermanMainApp extends Application {

    /**
     * Méthode appelée automatiquement au lancement de l'application.
     * Configure la fenêtre principale (Stage), charge l'interface depuis un fichier FXML,
     * applique une feuille de style CSS, et affiche la fenêtre.
     *
     * @param primaryStage La fenêtre principale de l'application.
     * @throws Exception Si le chargement du fichier FXML échoue.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML qui décrit l'interface utilisateur du menu principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainMenu.fxml"));
        Parent root = loader.load();

        // Créer une scène avec la racine chargée du FXML et définir la taille de la fenêtre
        Scene scene = new Scene(root, 800, 600);

        // Ajouter une feuille de style CSS pour styliser l'interface
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Configurer la fenêtre principale : titre, scène, taille fixe
        primaryStage.setTitle("Bomberman - Menu Principal");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        // Afficher la fenêtre à l'écran
        primaryStage.show();
    }

    /**
     * Méthode main, point d'entrée de l'application Java.
     * Lance l'application JavaFX.
     *
     * @param args Arguments de la ligne de commande (non utilisés ici).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
