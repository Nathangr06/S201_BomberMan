// BombermanMainApp.java
package bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Classe principale de l'application Bomberman.
 * Cette classe hérite de JavaFX Application et constitue le point d'entrée
 * de l'application. Elle gère l'initialisation de l'interface utilisateur
 * et l'affichage de la fenêtre principale du jeu.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class BombermanMainApp extends Application {

    /**
     * Méthode de démarrage de l'application JavaFX.
     * Configure et affiche la fenêtre principale avec le menu principal du jeu.
     * Charge le fichier FXML, applique les styles CSS et configure les propriétés
     * de la fenêtre.
     *
     * @param primaryStage la fenêtre principale de l'application
     * @throws Exception si une erreur survient lors du chargement des ressources
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainMenu.fxml"));
        Parent root = loader.load();

        // Créer la scène
        Scene scene = new Scene(root, 800, 950);

        // Ajouter une feuille de style CSS si nécessaire
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Configurer la fenêtre
        primaryStage.setTitle("Bomberman - Menu Principal");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        // Afficher la fenêtre
        primaryStage.show();
    }

    /**
     * Point d'entrée principal de l'application.
     * Lance l'application JavaFX en appelant la méthode launch() héritée
     * de la classe Application.
     *
     * @param args les arguments de ligne de commande passés à l'application
     */
    public static void main(String[] args) {
        launch(args);
    }
}