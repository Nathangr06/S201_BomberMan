package com.example.bomberman;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class BombermanGame extends Application {

    public static final int TILE_SIZE = 40;
    public static final int GRID_WIDTH = 15;
    public static final int GRID_HEIGHT = 13;
    public static final int CANVAS_WIDTH = GRID_WIDTH * TILE_SIZE;
    public static final int CANVAS_HEIGHT = GRID_HEIGHT * TILE_SIZE;

    private Canvas canvas;
    private GraphicsContext gc;
    private Timeline gameLoop;

    private GameGrid grid;
    private Player player;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private TextureManager textureManager;

    private InputHandler inputHandler;

    private double playerPixelX, playerPixelY;
    private double playerSpeed = 2.0;
    private boolean isMoving = false;
    private int targetGridX, targetGridY;

    @Override
    public void start(Stage primaryStage) {
        initializeGame();

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        VBox root = new VBox(canvas);
        Scene scene = new Scene(root);

        inputHandler = new InputHandler(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Bomberman");
        primaryStage.setResizable(false);
        primaryStage.show();

        canvas.requestFocus();

        startGameLoop();
    }

    private void initializeGame() {
        textureManager = TextureManager.getInstance();

        grid = new GameGrid(GRID_WIDTH, GRID_HEIGHT);
        grid.generate();

        player = new Player(1, 1);
        playerPixelX = player.getX() * TILE_SIZE;
        playerPixelY = player.getY() * TILE_SIZE;
        targetGridX = player.getX();
        targetGridY = player.getY();

        bombs = new java.util.ArrayList<>();
        explosions = new java.util.ArrayList<>();
    }

    private void startGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            update();
            render();
        }));
        gameLoop.setCycleCount(Animation.INDEFINITE);
        gameLoop.play();
    }

    private void update() {
        handleInput();
        updatePlayerMovement();
        updateBombs();
        updateExplosions();
        checkPowerUpCollection();
        checkPlayerExplosionCollision();
    }

    private void handleInput() {
        if (!isMoving) {
            int newX = targetGridX;
            int newY = targetGridY;

            if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.LEFT)) newX--;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.RIGHT)) newX++;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.UP)) newY--;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.DOWN)) newY++;

            if ((newX != targetGridX || newY != targetGridY) &&
                    grid.isWalkable(newX, newY) && !hasBombAt(newX, newY)) {
                targetGridX = newX;
                targetGridY = newY;
                isMoving = true;
            }
        }

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.SPACE)) {
            placeBomb();
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.SPACE);
        }
    }

    private void updatePlayerMovement() {
        if (isMoving) {
            double targetPixelX = targetGridX * TILE_SIZE;
            double targetPixelY = targetGridY * TILE_SIZE;

            double dx = targetPixelX - playerPixelX;
            double dy = targetPixelY - playerPixelY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= playerSpeed) {
                playerPixelX = targetPixelX;
                playerPixelY = targetPixelY;
                player.setPosition(targetGridX, targetGridY);
                isMoving = false;
            } else {
                double moveX = (dx / distance) * playerSpeed;
                double moveY = (dy / distance) * playerSpeed;
                playerPixelX += moveX;
                playerPixelY += moveY;
            }
        }
    }

    private boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    private void placeBomb() {
        int x = (int)(playerPixelX / TILE_SIZE + 0.5);
        int y = (int)(playerPixelY / TILE_SIZE + 0.5);
        if (bombs.size() < player.getBombCapacity() && !hasBombAt(x, y)) {
            bombs.add(new Bomb(x, y));
        }
    }

    private void updateBombs() {
        var iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next();
            bomb.decreaseTimer();
            if (bomb.isExploded()) {
                explodeBomb(bomb);
                iterator.remove();
            }
        }
    }

    private void explodeBomb(Bomb bomb) {
        int range = player.getFireRange();
        explosions.add(new Explosion(bomb.getX(), bomb.getY(), 60));

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        for (int dir = 0; dir < 4; dir++) {
            for (int i = 1; i <= range; i++) {
                int x = bomb.getX() + dx[dir] * i;
                int y = bomb.getY() + dy[dir] * i;

                if (!grid.inBounds(x, y)) break;
                if (grid.isIndestructibleWall(x, y)) break;

                explosions.add(new Explosion(x, y, 60));

                if (grid.isDestructibleWall(x, y)) {
                    grid.setEmpty(x, y);
                    break;
                }
            }
        }
    }

    private void updateExplosions() {
        explosions.removeIf(explosion -> explosion.decreaseTimerAndCheck());
    }

    private void checkPowerUpCollection() {
        int x = targetGridX;
        int y = targetGridY;

        if (grid.isPowerUp(x, y)) {
            int type = grid.getTileType(x, y);
            switch (type) {
                case GameGrid.POWERUP_BOMB -> {
                    player.increaseBombCapacity();
                    System.out.println("Power-up Bombe !");
                }
                case GameGrid.POWERUP_FIRE -> {
                    player.increaseFireRange();
                    System.out.println("Power-up Feu !");
                }
            }
            grid.removePowerUp(x, y);
        }
    }

    private void checkPlayerExplosionCollision() {
        int px = targetGridX;
        int py = targetGridY;

        for (Explosion explosion : explosions) {
            if (explosion.getX() == px && explosion.getY() == py) {
                playerDeath();
                return;
            }
        }
    }

    private void playerDeath() {
        System.out.println("Game Over !");
        gameLoop.stop();
        Timeline restartTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> restartGame()));
        restartTimer.play();
    }

    private void restartGame() {
        gameLoop.stop();
        initializeGame();
        startGameLoop();
        System.out.println("Jeu redémarré !");
    }

    private void render() {
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        grid.render(gc);

        Image explosionTexture = textureManager.getTexture("explosion");
        for (Explosion explosion : explosions) {
            int x = explosion.getX() * TILE_SIZE;
            int y = explosion.getY() * TILE_SIZE;
            if (explosionTexture != null) {
                gc.drawImage(explosionTexture, x, y, TILE_SIZE, TILE_SIZE);
            } else {
                gc.setFill(Color.ORANGE);
                gc.fillRect(x + 5, y + 5, TILE_SIZE - 10, TILE_SIZE - 10);
            }
        }

        Image bombTexture = textureManager.getTexture("bomb");
        for (Bomb bomb : bombs) {
            int x = bomb.getX() * TILE_SIZE;
            int y = bomb.getY() * TILE_SIZE;
            if (bombTexture != null) {
                gc.drawImage(bombTexture, x, y, TILE_SIZE, TILE_SIZE);
            } else {
                gc.setFill(Color.BLACK);
                gc.fillOval(x + 8, y + 8, TILE_SIZE - 16, TILE_SIZE - 16);
            }
        }

        Image playerTexture = textureManager.getTexture("player");
        if (playerTexture != null) {
            gc.drawImage(playerTexture, playerPixelX, playerPixelY, TILE_SIZE, TILE_SIZE);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillOval(playerPixelX + 5, playerPixelY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        }

        renderGrid(gc);
        renderUI(gc);
    }

    private void renderGrid(GraphicsContext gc) {
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(0.5);
        gc.setGlobalAlpha(0.3);
        for (int x = 0; x <= GRID_WIDTH; x++) {
            gc.strokeLine(x * TILE_SIZE, 0, x * TILE_SIZE, CANVAS_HEIGHT);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * TILE_SIZE, CANVAS_WIDTH, y * TILE_SIZE);
        }
        gc.setGlobalAlpha(1.0);
    }

    private void renderUI(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(14));
        gc.fillText("Bombes: " + bombs.size() + "/" + player.getBombCapacity(), 10, 20);
        gc.fillText("Portée: " + player.getFireRange(), 10, 40);

        gc.setFill(Color.YELLOW);
        gc.setFont(javafx.scene.text.Font.font(10));
        gc.fillText("Flèches: Bouger | Espace: Bombe", CANVAS_WIDTH - 180, CANVAS_HEIGHT - 10);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
