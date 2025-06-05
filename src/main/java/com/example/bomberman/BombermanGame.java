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

    private long lastMoveTime = 0;
    private long lastBombTime = 0;
    private static final long MOVE_INTERVAL = 200_000_000; // 200 ms
    private static final long BOMB_INTERVAL = 300_000_000; // 300 ms
    private static final int MAX_BOMBS = 3;

    private void update() {
        handleInput();
        updateBombs();
        updateExplosions();
        checkPowerUpCollection();
        checkPlayerExplosionCollision();
    }

    private void handleInput() {
        long now = System.nanoTime();

        // Gestion du mouvement
        if (now - lastMoveTime >= MOVE_INTERVAL) {
            int newX = player.getX();
            int newY = player.getY();

            if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.LEFT)) newX--;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.RIGHT)) newX++;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.UP)) newY--;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.DOWN)) newY++;

            if (grid.isWalkable(newX, newY) && !hasBombAt(newX, newY)) {
                player.setPosition(newX, newY);
                lastMoveTime = now;
            }
        }

        // Gestion du placement de bombes
        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.SPACE) &&
                now - lastBombTime >= BOMB_INTERVAL) {
            placeBomb();
            lastBombTime = now;
        }
    }

    private boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    private void placeBomb() {
        if (bombs.size() < MAX_BOMBS && !hasBombAt(player.getX(), player.getY())) {
            bombs.add(new Bomb(player.getX(), player.getY()));
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
        int playerX = player.getX();
        int playerY = player.getY();

        if (grid.isPowerUp(playerX, playerY)) {
            int powerUpType = grid.getTileType(playerX, playerY);

            switch (powerUpType) {
                case GameGrid.POWERUP_BOMB:
                    player.increaseBombCapacity();
                    System.out.println("Power-up Bombe récupéré ! Capacité: " + player.getBombCapacity());
                    break;
                case GameGrid.POWERUP_FIRE:
                    player.increaseFireRange();
                    System.out.println("Power-up Feu récupéré ! Portée: " + player.getFireRange());
                    break;
            }

            grid.removePowerUp(playerX, playerY);
        }
    }

    private void checkPlayerExplosionCollision() {
        int playerX = player.getX();
        int playerY = player.getY();

        for (Explosion explosion : explosions) {
            if (explosion.getX() == playerX && explosion.getY() == playerY) {
                // Le joueur est touché par une explosion
                playerDeath();
                return;
            }
        }
    }

    private void playerDeath() {
        // Arrêter le jeu ou redémarrer
        System.out.println("Game Over !");
        gameLoop.stop();

        // Optionnel: redémarrer automatiquement après quelques secondes
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
        // Effacer l'écran avec une couleur de fond
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Rendre la grille (sol, murs, power-ups)
        grid.render(gc);

        // Rendre les explosions
        Image explosionTexture = textureManager.getTexture("explosion");
        for (Explosion explosion : explosions) {
            int pixelX = explosion.getX() * TILE_SIZE;
            int pixelY = explosion.getY() * TILE_SIZE;

            if (explosionTexture != null) {
                gc.drawImage(explosionTexture, pixelX, pixelY, TILE_SIZE, TILE_SIZE);
            } else {
                // Fallback vers les rectangles colorés
                gc.setFill(Color.ORANGE);
                gc.fillRect(pixelX + 5, pixelY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
            }
        }

        // Rendre les bombes
        Image bombTexture = textureManager.getTexture("bomb");
        for (Bomb bomb : bombs) {
            int pixelX = bomb.getX() * TILE_SIZE;
            int pixelY = bomb.getY() * TILE_SIZE;

            if (bombTexture != null) {
                gc.drawImage(bombTexture, pixelX, pixelY, TILE_SIZE, TILE_SIZE);
            } else {
                // Fallback vers les cercles noirs
                gc.setFill(Color.BLACK);
                gc.fillOval(pixelX + 8, pixelY + 8, TILE_SIZE - 16, TILE_SIZE - 16);
            }
        }

        // Rendre le joueur
        Image playerTexture = textureManager.getTexture("player");
        int playerPixelX = player.getX() * TILE_SIZE;
        int playerPixelY = player.getY() * TILE_SIZE;

        if (playerTexture != null) {
            gc.drawImage(playerTexture, playerPixelX, playerPixelY, TILE_SIZE, TILE_SIZE);
        } else {
            // Fallback vers le cercle bleu
            gc.setFill(Color.BLUE);
            gc.fillOval(playerPixelX + 5, playerPixelY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        }

        // Rendre la grille (optionnel - pour le debug)
        renderGrid(gc);

        // Afficher les statistiques du joueur
        renderUI(gc);
    }

    private void renderGrid(GraphicsContext gc) {
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(0.5);
        gc.setGlobalAlpha(0.3); // Rendre les lignes semi-transparentes

        for (int x = 0; x <= GRID_WIDTH; x++) {
            gc.strokeLine(x * TILE_SIZE, 0, x * TILE_SIZE, CANVAS_HEIGHT);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * TILE_SIZE, CANVAS_WIDTH, y * TILE_SIZE);
        }

        gc.setGlobalAlpha(1.0); // Remettre l'opacité normale
    }

    private void renderUI(GraphicsContext gc) {
        // Afficher les statistiques du joueur
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(14));

        String bombInfo = "Bombes: " + bombs.size() + "/" + player.getBombCapacity();
        String fireInfo = "Portée: " + player.getFireRange();

        gc.fillText(bombInfo, 10, 20);
        gc.fillText(fireInfo, 10, 40);

        // Instructions
        gc.setFill(Color.YELLOW);
        gc.setFont(javafx.scene.text.Font.font(10));
        gc.fillText("Flèches: Bouger | Espace: Bombe", CANVAS_WIDTH - 180, CANVAS_HEIGHT - 10);
    }

    public static void main(String[] args) {
        launch(args);
    }
}