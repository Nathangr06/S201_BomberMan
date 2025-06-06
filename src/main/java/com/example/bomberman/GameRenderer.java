package com.example.bomberman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class GameRenderer {
    private GraphicsContext gc;
    private TextureManager textureManager;

    public GameRenderer(GraphicsContext gc, TextureManager textureManager) {
        this.gc = gc;
        this.textureManager = textureManager;
    }

    public void render(GameGrid grid, Player player1, Player player2,
                       BombManager bombManager, ExplosionManager explosionManager) {
        clearCanvas();
        renderGrid(grid);
        renderExplosions(explosionManager);
        renderBombs(bombManager);
        renderPlayers(player1, player2);
        renderGridLines();
    }

    private void clearCanvas() {
        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);
    }

    private void renderGrid(GameGrid grid) {
        if (grid != null) {
            grid.render(gc);
        }
    }

    private void renderExplosions(ExplosionManager explosionManager) {
        Image explosionTexture = textureManager.getTexture("explosion");
        for (Explosion explosion : explosionManager.getExplosions()) {
            int x = explosion.getX() * GameConstants.TILE_SIZE;
            int y = explosion.getY() * GameConstants.TILE_SIZE;

            if (explosionTexture != null) {
                gc.drawImage(explosionTexture, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
            } else {
                gc.setFill(Color.ORANGE);
                gc.fillRect(x + 5, y + 5, GameConstants.TILE_SIZE - 10, GameConstants.TILE_SIZE - 10);
            }
        }
    }

    private void renderBombs(BombManager bombManager) {
        Image bombTexture = textureManager.getTexture("bomb");
        for (Bomb bomb : bombManager.getBombs()) {
            int x = bomb.getX() * GameConstants.TILE_SIZE;
            int y = bomb.getY() * GameConstants.TILE_SIZE;

            if (bombTexture != null) {
                gc.drawImage(bombTexture, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
            } else {
                gc.setFill(Color.BLACK);
                gc.fillOval(x + 8, y + 8, GameConstants.TILE_SIZE - 16, GameConstants.TILE_SIZE - 16);
            }
        }
    }

    private void renderPlayers(Player player1, Player player2) {
        Image playerTexture = textureManager.getTexture("player");
        Image player2Texture = textureManager.getTexture("player2");

        if (playerTexture != null) {
            gc.drawImage(playerTexture, player1.getVisualX(), player1.getVisualY(),
                    GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
            gc.drawImage(player2Texture, player2.getVisualX(), player2.getVisualY(),
                    GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillOval(player1.getVisualX() + 5, player1.getVisualY() + 5,
                    GameConstants.TILE_SIZE - 10, GameConstants.TILE_SIZE - 10);
            gc.setFill(Color.RED);
            gc.fillOval(player2.getVisualX() + 5, player2.getVisualY() + 5,
                    GameConstants.TILE_SIZE - 10, GameConstants.TILE_SIZE - 10);
        }
    }

    private void renderGridLines() {
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(1);

        for (int x = 0; x <= GameConstants.GRID_WIDTH; x++) {
            gc.strokeLine(x * GameConstants.TILE_SIZE, 0,
                    x * GameConstants.TILE_SIZE, GameConstants.CANVAS_HEIGHT);
        }
        for (int y = 0; y <= GameConstants.GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * GameConstants.TILE_SIZE,
                    GameConstants.CANVAS_WIDTH, y * GameConstants.TILE_SIZE);
        }
    }
}