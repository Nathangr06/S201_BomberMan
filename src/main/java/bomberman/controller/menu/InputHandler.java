package bomberman.controller.menu;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Gestionnaire d'entrées clavier pour le jeu Bomberman.
 * Cette classe centralise la capture et la gestion des événements clavier
 * en maintenant un état temps réel des touches pressées. Elle permet
 * de détecter les appuis simultanés et offre un contrôle précis sur
 * l'état des touches pour un gameplay fluide.
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Capture automatique des événements clavier JavaFX</li>
 *   <li>Gestion d'état temps réel des touches pressées</li>
 *   <li>Support des appuis simultanés multiples</li>
 *   <li>Interface simple pour interroger l'état des touches</li>
 *   <li>Contrôle manuel de l'état des touches</li>
 * </ul>
 *
 * <p>Usage typique :</p>
 * <pre>
 * Scene scene = new Scene(root);
 * InputHandler inputHandler = new InputHandler(scene);
 *
 * // Dans la boucle de jeu
 * if (inputHandler.isKeyPressed(KeyCode.SPACE)) {
 *     // Placer une bombe
 * }
 * if (inputHandler.isKeyPressed(KeyCode.LEFT)) {
 *     // Déplacer vers la gauche
 * }
 * </pre>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class InputHandler {

    /**
     * Ensemble des touches actuellement pressées.
     * Utilise un HashSet pour une recherche en O(1) et pour gérer
     * automatiquement l'unicité des touches.
     */
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    /**
     * Constructeur du gestionnaire d'entrées.
     * Initialise les gestionnaires d'événements clavier sur la scène fournie.
     * Les événements sont automatiquement capturés et l'état des touches
     * est maintenu en temps réel.
     *
     * @param scene La scène JavaFX sur laquelle capturer les événements clavier
     * @throws NullPointerException si la scène est null
     */
    public InputHandler(Scene scene) {
        scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
    }

    /**
     * Vérifie si une touche spécifique est actuellement pressée.
     * Cette méthode permet de détecter l'état instantané d'une touche
     * et supporte la détection d'appuis simultanés multiples.
     *
     * @param key Le code de la touche à vérifier
     * @return true si la touche est actuellement pressée, false sinon
     */
    public boolean isKeyPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    /**
     * Force la libération d'une touche spécifique.
     * Cette méthode permet de contrôler manuellement l'état d'une touche,
     * utile pour éviter les répétitions d'actions ou pour implémenter
     * des mécaniques de jeu spéciales (ex: action unique par appui).
     *
     * <p>Cas d'usage typiques :</p>
     * <ul>
     *   <li>Éviter la répétition d'actions (placement de bombes)</li>
     *   <li>Implémenter des actions "press once" même avec maintien</li>
     *   <li>Réinitialiser l'état après une action spéciale</li>
     * </ul>
     *
     * @param key Le code de la touche à libérer
     */
    public void setKeyReleased(KeyCode key) {
        pressedKeys.remove(key);
    }
}