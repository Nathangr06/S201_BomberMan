package com.example.bomberman;

public class Player {
    private Position gridPosition;
    private Position visualPosition;
    private Position targetPosition;
    private boolean isMoving;
    private long lastMoveTime;
    private int playerId;

    public Player(int x, int y, int playerId) {
        this.gridPosition = new Position(x, y);
        this.visualPosition = new Position(x * GameConstants.TILE_SIZE, y * GameConstants.TILE_SIZE);
        this.targetPosition = new Position(x, y);
        this.isMoving = false;
        this.lastMoveTime = 0;
        this.playerId = playerId;
    }

    // Getters pour la position grille
    public int getX() { return gridPosition.getX(); }
    public int getY() { return gridPosition.getY(); }
    public Position getGridPosition() { return new Position(gridPosition); }

    // Getters/setters pour la position visuelle
    public double getVisualX() { return visualPosition.getX(); }
    public double getVisualY() { return visualPosition.getY(); }
    public void setVisualPosition(double x, double y) {
        visualPosition.setPosition((int)x, (int)y);
    }

    // Getters/setters pour la position cible
    public Position getTargetPosition() { return new Position(targetPosition); }
    public void setTargetPosition(int x, int y) {
        targetPosition.setPosition(x, y);
    }

    // Gestion du mouvement
    public boolean isMoving() { return isMoving; }
    public void setMoving(boolean moving) { this.isMoving = moving; }

    public long getLastMoveTime() { return lastMoveTime; }
    public void setLastMoveTime(long time) { this.lastMoveTime = time; }

    public void setPosition(int x, int y) {
        gridPosition.setPosition(x, y);
    }

    public int getPlayerId() { return playerId; }
}