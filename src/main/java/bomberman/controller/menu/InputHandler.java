package bomberman.controller.menu;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public InputHandler(Scene scene) {
        scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
    }

    public boolean isKeyPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    public void setKeyReleased(KeyCode key) {
        pressedKeys.remove(key);
    }
}
