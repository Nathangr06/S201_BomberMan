package com.example.bomberman;

import java.util.Random;

public class AIController {
    private Random random;
    private long lastActionTime;
    private long lastBombTime;
    private static final long ACTION_COOLDOWN = 300_000_000L; // 300ms en nanosecondes
    private static final long BOMB_COOLDOWN = 2_000_000_000L; // 2 secondes en nanosecondes

    public AIController() {
        this.random = new Random();
        this.lastActionTime = 0;
        this.lastBombTime = 0;
    }

    public void updateAI(Player aiPlayer, MovementController movementController, BombManager bombManager) {
        long currentTime = System.nanoTime();

        // Cooldown pour les actions
        if (currentTime - lastActionTime < ACTION_COOLDOWN) {
            return;
        }

        // Si l'IA est en mouvement, ne pas essayer de nouvelles actions
        if (aiPlayer.isMoving()) {
            return;
        }

        // Décision aléatoire : 85% mouvement, 15% bombe
        if (random.nextDouble() < 0.85) {
            tryRandomMovement(aiPlayer, movementController);
        } else {
            tryPlaceBomb(aiPlayer, bombManager, currentTime);
        }

        lastActionTime = currentTime;
    }

    private void tryRandomMovement(Player aiPlayer, MovementController movementController) {
        // Directions possibles : haut, bas, gauche, droite
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        // Essayer une direction aléatoire
        int direction = random.nextInt(4);
        movementController.tryMovePlayer(aiPlayer, dx[direction], dy[direction]);
    }

    private void tryPlaceBomb(Player aiPlayer, BombManager bombManager, long currentTime) {
        // Cooldown pour les bombes
        if (currentTime - lastBombTime < BOMB_COOLDOWN) {
            return;
        }

        // Placer une bombe avec une probabilité
        if (random.nextDouble() < 0.15) { // 15% de chance de placer une bombe
            bombManager.placeBomb(aiPlayer);
            lastBombTime = currentTime;
        }
    }
}