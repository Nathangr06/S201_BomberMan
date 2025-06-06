package com.example.bomberman;

public class GameConstants {
    // Constantes de jeu
    public static final int TILE_SIZE = 40;
    public static final int GRID_WIDTH = 15;
    public static final int GRID_HEIGHT = 13;
    public static final int CANVAS_WIDTH = GRID_WIDTH * TILE_SIZE;
    public static final int CANVAS_HEIGHT = GRID_HEIGHT * TILE_SIZE;

    // Constantes de mouvement
    public static final double MOVEMENT_SPEED = 3.0;
    public static final long MOVE_COOLDOWN = 16;
    public static final int EXPLOSION_RANGE = 4;
    public static final int EXPLOSION_DURATION = 3;
}