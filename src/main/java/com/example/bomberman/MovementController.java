package com.example.bomberman;

public class MovementController {
    private GameGrid grid;
    private BombManager bombManager;

    public MovementController(GameGrid grid, BombManager bombManager) {
        this.grid = grid;
        this.bombManager = bombManager;
    }

    public boolean canMoveToPosition(int x, int y) {
        return grid.isWalkable(x, y) && !bombManager.hasBombAt(x, y);
    }

    public void updatePlayerMovement(Player player) {
        if (!player.isMoving()) return;

        Position target = player.getTargetPosition();
        double targetX = target.getX() * GameConstants.TILE_SIZE;
        double targetY = target.getY() * GameConstants.TILE_SIZE;

        double deltaX = targetX - player.getVisualX();
        double deltaY = targetY - player.getVisualY();

        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance > GameConstants.MOVEMENT_SPEED && distance > 0.1) {
            double newX = player.getVisualX() + (deltaX / distance) * GameConstants.MOVEMENT_SPEED;
            double newY = player.getVisualY() + (deltaY / distance) * GameConstants.MOVEMENT_SPEED;
            player.setVisualPosition(newX, newY);
        } else {
            // Arrivé à destination
            player.setVisualPosition(targetX, targetY);
            player.setPosition(target.getX(), target.getY());
            player.setMoving(false);
        }
    }

    public boolean tryMovePlayer(Player player, int deltaX, int deltaY) {
        long currentTime = System.nanoTime();

        if (player.isMoving() || (currentTime - player.getLastMoveTime()) <= GameConstants.MOVE_COOLDOWN) {
            return false;
        }

        Position target = player.getTargetPosition();
        int newX = target.getX() + deltaX;
        int newY = target.getY() + deltaY;

        if (canMoveToPosition(newX, newY)) {
            player.setTargetPosition(newX, newY);
            player.setMoving(true);
            player.setLastMoveTime(currentTime);
            return true;
        }

        return false;
    }
}