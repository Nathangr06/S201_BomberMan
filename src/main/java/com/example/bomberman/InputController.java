package com.example.bomberman;

import javafx.scene.input.KeyCode;

public class InputController {
    private InputHandler inputHandler;
    private MovementController movementController;
    private BombManager bombManager;

    public InputController(InputHandler inputHandler, MovementController movementController, BombManager bombManager) {
        this.inputHandler = inputHandler;
        this.movementController = movementController;
        this.bombManager = bombManager;
    }

    public void handlePlayerInput(Player player1, Player player2) {
        handlePlayer1Input(player1);
        handlePlayer2Input(player2);
        handleGameInput();
    }

    private void handlePlayer1Input(Player player) {
        // Mouvement joueur 1 (flèches)
        if (inputHandler.isKeyPressed(KeyCode.LEFT)) {
            movementController.tryMovePlayer(player, -1, 0);
        } else if (inputHandler.isKeyPressed(KeyCode.RIGHT)) {
            movementController.tryMovePlayer(player, 1, 0);
        } else if (inputHandler.isKeyPressed(KeyCode.UP)) {
            movementController.tryMovePlayer(player, 0, -1);
        } else if (inputHandler.isKeyPressed(KeyCode.DOWN)) {
            movementController.tryMovePlayer(player, 0, 1);
        }

        // Bombe joueur 1
        if (inputHandler.isKeyPressed(KeyCode.ENTER)) {
            bombManager.placeBomb(player);
            inputHandler.setKeyReleased(KeyCode.ENTER);
        }
    }

    private void handlePlayer2Input(Player player) {
        // Mouvement joueur 2 (ZQSD)
        if (inputHandler.isKeyPressed(KeyCode.Q)) {
            movementController.tryMovePlayer(player, -1, 0);
        } else if (inputHandler.isKeyPressed(KeyCode.D)) {
            movementController.tryMovePlayer(player, 1, 0);
        } else if (inputHandler.isKeyPressed(KeyCode.Z)) {
            movementController.tryMovePlayer(player, 0, -1);
        } else if (inputHandler.isKeyPressed(KeyCode.S)) {
            movementController.tryMovePlayer(player, 0, 1);
        }

        // Bombe joueur 2
        if (inputHandler.isKeyPressed(KeyCode.SPACE)) {
            bombManager.placeBomb(player);
            inputHandler.setKeyReleased(KeyCode.SPACE);
        }
    }

    private void handleGameInput() {
        // Gestion des touches de jeu (pause, quit, etc.)
    }

    public boolean isEscapePressed() {
        return inputHandler.isKeyPressed(KeyCode.ESCAPE);
    }
}