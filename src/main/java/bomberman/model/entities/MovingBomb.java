package bomberman.model.entities;

import static bomberman.utils.GameConstants.*;

public class MovingBomb {
    private Bomb bomb;
    private double visualX, visualY;
    private double targetX, targetY;
    private boolean isMoving;

    public MovingBomb(Bomb bomb, int targetGridX, int targetGridY) {
        this.bomb = bomb;
        this.visualX = bomb.getX() * TILE_SIZE;
        this.visualY = bomb.getY() * TILE_SIZE + TIMER_HEIGHT;
        this.targetX = targetGridX * TILE_SIZE;
        this.targetY = targetGridY * TILE_SIZE + TIMER_HEIGHT;
        this.isMoving = true;
    }

    public boolean updatePosition() {
        if (!isMoving) return false;

        double deltaX = targetX - visualX;
        double deltaY = targetY - visualY;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance <= BOMB_PUSH_SPEED) {
            // Arrivée à destination
            visualX = targetX;
            visualY = targetY;
            isMoving = false;

            // Mettre à jour la position logique de la bombe
            int gridX = (int) (targetX / TILE_SIZE);
            int gridY = (int) ((targetY - TIMER_HEIGHT) / TILE_SIZE);
            bomb.setPosition(gridX, gridY);

            return false; // Animation terminée
        } else {
            // Continuer le mouvement
            visualX += (deltaX / distance) * BOMB_PUSH_SPEED;
            visualY += (deltaY / distance) * BOMB_PUSH_SPEED;
            return true; // Animation continue
        }
    }

    public double getVisualX() { return visualX; }
    public double getVisualY() { return visualY; }
    public Bomb getBomb() { return bomb; }
    public boolean isMoving() { return isMoving; }
}