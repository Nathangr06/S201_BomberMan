/**
 * Module principal du jeu Bomberman.
 * Ce module définit toutes les dépendances nécessaires pour l'exécution du jeu,
 * incluant JavaFX pour l'interface graphique et JUnit pour les tests.
 * Il expose également tous les packages nécessaires à l'application.
 *
 * <p>Dépendances requises :</p>
 * <ul>
 *   <li>javafx.controls - Composants d'interface utilisateur JavaFX</li>
 *   <li>javafx.fxml - Support FXML pour les interfaces déclaratives</li>
 *   <li>javafx.graphics - API graphiques de base de JavaFX</li>
 *   <li>org.junit.jupiter.api - Framework de tests unitaires</li>
 *   <li>org.junit.platform.commons - Plateforme commune JUnit</li>
 * </ul>
 *
 * <p>Packages exportés :</p>
 * <ul>
 *   <li>bomberman - Package principal de l'application</li>
 *   <li>bomberman.controller.game - Contrôleurs de jeu</li>
 *   <li>bomberman.controller.menu - Contrôleurs de menus</li>
 *   <li>bomberman.model.ai - Intelligence artificielle</li>
 *   <li>bomberman.model.entities - Entités du jeu</li>
 *   <li>bomberman.model.game - Logique de jeu</li>
 *   <li>bomberman.model.profile - Gestion des profils</li>
 *   <li>bomberman.utils - Utilitaires</li>
 * </ul>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
module bomberman {
    // Dépendances JavaFX pour l'interface graphique
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Dépendances JUnit pour les tests
    requires org.junit.jupiter.api;
    requires org.junit.platform.commons;

    // Package principal - ouvert à JavaFX FXML et exporté
    opens bomberman to javafx.fxml;
    exports bomberman;

    // Contrôleurs de jeu - exportés et ouverts pour FXML
    exports bomberman.controller.game;
    opens bomberman.controller.game to javafx.fxml;

    // Contrôleurs de menu - exportés et ouverts pour FXML
    exports bomberman.controller.menu;
    opens bomberman.controller.menu to javafx.fxml;

    // Intelligence artificielle - exportée et ouverte pour FXML
    exports bomberman.model.ai;
    opens bomberman.model.ai to javafx.fxml;

    // Entités du jeu - exportées et ouvertes pour FXML
    exports bomberman.model.entities;
    opens bomberman.model.entities to javafx.fxml;

    // Logique de jeu - exportée et ouverte pour FXML
    exports bomberman.model.game;
    opens bomberman.model.game to javafx.fxml;

    // Gestion des profils - exportée et ouverte pour FXML
    exports bomberman.model.profile;
    opens bomberman.model.profile to javafx.fxml;

    // Utilitaires - exportés et ouverts pour FXML
    exports bomberman.utils;
    opens bomberman.utils to javafx.fxml;
}