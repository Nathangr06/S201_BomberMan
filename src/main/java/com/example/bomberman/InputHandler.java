package com.example.bomberman;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Gère l'état des touches du clavier pour une scène JavaFX.
 * Permet de détecter quelles touches sont actuellement pressées.
 */
public class InputHandler {

    /**
     * Ensemble des touches actuellement pressées.
     */
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    /**
     * Initialise un gestionnaire d'entrées clavier pour la scène donnée.
     * Configure les écouteurs pour ajouter ou retirer les touches pressées.
     *
     * @param scene scène JavaFX sur laquelle écouter les événements clavier.
     */
    public InputHandler(Scene scene) {
        scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
    }

    /**
     * Indique si une touche spécifique est actuellement pressée.
     *
     * @param key touche clavier à vérifier.
     * @return true si la touche est pressée, false sinon.
     */
    public boolean isKeyPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    /**
     * Marque une touche comme relâchée (non pressée).
     *
     * @param key touche clavier à libérer.
     */
    public void setKeyReleased(KeyCode key) {
        pressedKeys.remove(key);
    }
}
