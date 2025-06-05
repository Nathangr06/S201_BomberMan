package com.example.bomberman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Random;

public class GameGrid {
    private final int width;
    private final int height;
    private final int[][] grid;
    private final TextureManager textureManager;

    public static final int EMPTY = 0;
    public static final int WALL_INDESTRUCTIBLE = 1;
    public static final int WALL_DESTRUCTIBLE = 2;
    public static final int POWERUP_BOMB = 3;
    public static final int POWERUP_FIRE = 4;

    public GameGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
        this.textureManager = TextureManager.getInstance();
    }

    public void generate() {
        Random rand = new Random();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    grid[y][x] = WALL_INDESTRUCTIBLE;
                } else if (x % 2 == 0 && y % 2 == 0) {
                    grid[y][x] = WALL_INDESTRUCTIBLE;
                } else if ((x > 2 || y > 2) && rand.nextDouble() < 0.3) {
                    grid[y][x] = WALL_DESTRUCTIBLE;
                } else {
                    grid[y][x] = EMPTY;
                }
            }
        }

        // Clear starting area
        grid[1][1] = EMPTY;
        grid[1][2] = EMPTY;
        grid[2][1] = EMPTY;
    }

    public boolean isWalkable(int x, int y) {
        return inBounds(x, y) && (grid[y][x] == EMPTY || isPowerUp(x, y));
    }

    public boolean isIndestructibleWall(int x, int y) {
        return inBounds(x, y) && grid[y][x] == WALL_INDESTRUCTIBLE;
    }

    public boolean isDestructibleWall(int x, int y) {
        return inBounds(x, y) && grid[y][x] == WALL_DESTRUCTIBLE;
    }

    public boolean isPowerUp(int x, int y) {
        return inBounds(x, y) && (grid[y][x] == POWERUP_BOMB || grid[y][x] == POWERUP_FIRE);
    }

    public void setEmpty(int x, int y) {
        if (inBounds(x, y)) {
            // Chance de spawner un power-up quand un mur destructible est détruit
            Random rand = new Random();
            if (grid[y][x] == WALL_DESTRUCTIBLE && rand.nextDouble() < 0.3) {
                grid[y][x] = rand.nextBoolean() ? POWERUP_BOMB : POWERUP_FIRE;
            } else {
                grid[y][x] = EMPTY;
            }
        }
    }

    public int getTileType(int x, int y) {
        if (inBounds(x, y)) {
            return grid[y][x];
        }
        return -1;
    }

    public void removePowerUp(int x, int y) {
        if (inBounds(x, y) && isPowerUp(x, y)) {
            grid[y][x] = EMPTY;
        }
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public void render(GraphicsContext gc) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tileType = grid[y][x];
                int pixelX = x * BombermanGame.TILE_SIZE;
                int pixelY = y * BombermanGame.TILE_SIZE;

                // Dessiner le sol en arrière-plan pour toutes les cases
                Image groundTexture = textureManager.getTexture("ground");
                if (groundTexture != null) {
                    gc.drawImage(groundTexture, pixelX, pixelY,
                            BombermanGame.TILE_SIZE, BombermanGame.TILE_SIZE);
                } else {
                    gc.setFill(Color.LIGHTGREEN);
                    gc.fillRect(pixelX, pixelY, BombermanGame.TILE_SIZE, BombermanGame.TILE_SIZE);
                }

                // Dessiner les éléments par-dessus
                Image texture = null;
                switch (tileType) {
                    case WALL_INDESTRUCTIBLE:
                        texture = textureManager.getTexture("wall_indestructible");
                        break;
                    case WALL_DESTRUCTIBLE:
                        texture = textureManager.getTexture("wall_destructible");
                        break;
                    case POWERUP_BOMB:
                        texture = textureManager.getTexture("powerup_bomb");
                        break;
                    case POWERUP_FIRE:
                        texture = textureManager.getTexture("powerup_fire");
                        break;
                }

                if (texture != null) {
                    gc.drawImage(texture, pixelX, pixelY,
                            BombermanGame.TILE_SIZE, BombermanGame.TILE_SIZE);
                }
            }
        }
    }
}