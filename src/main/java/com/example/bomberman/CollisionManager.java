package com.example.bomberman;

public class CollisionManager {
    private ExplosionManager explosionManager;

    public CollisionManager(ExplosionManager explosionManager) {
        this.explosionManager = explosionManager;
    }

    public Player checkPlayerCollisions(Player player1, Player player2) {
        // Retourne le joueur qui a été touché, null sinon
        for (Explosion explosion : explosionManager.getExplosions()) {
            if (explosion.getX() == player1.getX() && explosion.getY() == player1.getY()) {
                return player1;
            }
            if (explosion.getX() == player2.getX() && explosion.getY() == player2.getY()) {
                return player2;
            }
        }
        return null;
    }
}