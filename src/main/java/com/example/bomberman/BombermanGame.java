package com.example.bomberman;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BombermanGame {

    public static final int TILE_SIZE = 40;
    public static final int GRID_WIDTH = 15;
    public static final int GRID_HEIGHT = 13;
    public static final int CANVAS_WIDTH = GRID_WIDTH * TILE_SIZE;
    public static final int CANVAS_HEIGHT = GRID_HEIGHT * TILE_SIZE;

    private Canvas canvas;
    private GraphicsContext gc;
    private Timeline gameLoop;
    private Stage gameStage;

    private GameGrid grid;
    private Player player;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private InputHandler inputHandler;
    private boolean gameRunning = false;

    public enum CellType {
        EMPTY, WALL, DESTRUCTIBLE_WALL, PLAYER_SPAWN
    }

    public BombermanGame() {
        // L'initialisation se fait dans startGame()
    }

    public void startGame(Stage stage) {
        startGame(stage, null);
    }

    public void startGame(Stage stage, File levelFile) {
        this.gameStage = stage;

        if (levelFile != null && levelFile.exists()) {
            initializeGameWithCustomLevel(levelFile);
        } else {
            initializeGameWithDefaultLevel();
        }

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        VBox root = new VBox(canvas);
        Scene scene = new Scene(root);

        inputHandler = new InputHandler(scene);

        gameStage.setScene(scene);
        gameStage.setTitle("Bomberman - Jeu");
        gameStage.setResizable(false);
        gameStage.show();

        canvas.requestFocus();

        startGameLoop();
        gameRunning = true;
    }

    private void initializeGameWithDefaultLevel() {
        grid = new GameGrid(GRID_WIDTH, GRID_HEIGHT);
        grid.generate();

        player = new Player(1, 1);
        bombs = new ArrayList<>();
        explosions = new ArrayList<>();
    }

    private void initializeGameWithCustomLevel(File levelFile) {
        grid = new GameGrid(GRID_WIDTH, GRID_HEIGHT);
        bombs = new ArrayList<>();
        explosions = new ArrayList<>();

        try {
            loadCustomLevel(levelFile);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du niveau: " + e.getMessage());
            initializeGameWithDefaultLevel();
        }

        if (player == null) {
            System.err.println("Aucun spawn joueur trouvé, position par défaut utilisée");
            player = new Player(1, 1);
        }
    }

    private void loadCustomLevel(File levelFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String sizeStr = reader.readLine();
            String[] sizeParts = sizeStr.split(",");
            int rows = Integer.parseInt(sizeParts[0]);
            int cols = Integer.parseInt(sizeParts[1]);

            if (rows != GRID_HEIGHT || cols != GRID_WIDTH) {
                throw new IOException("Taille de grille incompatible !");
            }

            for (int i = 0; i < GRID_HEIGHT; i++) {
                for (int j = 0; j < GRID_WIDTH; j++) {
                    grid.setEmpty(j, i);
                }
            }

            for (int i = 0; i < rows; i++) {
                String line = reader.readLine();
                if (line == null) break;

                String[] values = line.split(",");
                for (int j = 0; j < cols && j < values.length; j++) {
                    int typeIndex = Integer.parseInt(values[j]);
                    CellType cellType = CellType.values()[typeIndex];

                    switch (cellType) {
                        case EMPTY -> grid.setEmpty(j, i);
                        case WALL -> grid.setIndestructibleWall(j, i);
                        case DESTRUCTIBLE_WALL -> grid.setDestructibleWall(j, i);
                        case PLAYER_SPAWN -> {
                            grid.setEmpty(j, i);
                            player = new Player(j, i);
                        }
                    }
                }
            }
        }
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
    private static final long MOVE_INTERVAL = 200_000_000; // 200 ms

    private void update() {
        if (!gameRunning) return;

        handleInput();
        updateBombs();
        updateExplosions();
        checkCollisions();
    }

    private void handleInput() {
        long now = System.nanoTime();

        if (now - lastMoveTime < MOVE_INTERVAL) return;

        int newX = player.getX();
        int newY = player.getY();

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.LEFT)) newX--;
        else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.RIGHT)) newX++;
        else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.UP)) newY--;
        else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.DOWN)) newY++;

        if (grid.inBounds(newX, newY) && grid.isWalkable(newX, newY) && !hasBombAt(newX, newY)) {
            player.setPosition(newX, newY);
        }

        lastMoveTime = now;

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.SPACE)) {
            placeBomb();
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.SPACE);
        }

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.ESCAPE)) {
            stopGame();
            // returnToMainMenu(); // Appeler le menu ici si implémenté
        }
    }

    private boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    private void placeBomb() {
        if (!hasBombAt(player.getX(), player.getY())) {
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
        int range = 2;
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
        explosions.removeIf(Explosion::decreaseTimerAndCheck);
    }

    private void checkCollisions() {
        for (Explosion explosion : explosions) {
            if (explosion.getX() == player.getX() && explosion.getY() == player.getY()) {
                gameOver();
                return;
            }
        }
    }

    private void gameOver() {
        gameRunning = false;
        stopGame();
        System.out.println("GAME OVER!");
        // Afficher un message ou retourner au menu principal
    }

    private void render() {
        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        if (grid != null) grid.render(gc);

        // Rendu explosions
        gc.setFill(Color.ORANGE);
        for (Explosion explosion : explosions) {
            gc.fillRect(explosion.getX() * TILE_SIZE + 5, explosion.getY() * TILE_SIZE + 5,
                    TILE_SIZE - 10, TILE_SIZE - 10);
        }

        // Rendu bombes
        gc.setFill(Color.BLACK);
        for (Bomb bomb : bombs) {
            gc.fillOval(bomb.getX() * TILE_SIZE + 8, bomb.getY() * TILE_SIZE + 8,
                    TILE_SIZE - 16, TILE_SIZE - 16);
        }

        // Rendu joueur
        if (player != null) {
            gc.setFill(Color.BLUE);
            gc.fillOval(player.getX() * TILE_SIZE + 5, player.getY() * TILE_SIZE + 5,
                    TILE_SIZE - 10, TILE_SIZE - 10);
        }

        // Rendu grille
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(1);
        for (int x = 0; x <= GRID_WIDTH; x++) {
            gc.strokeLine(x * TILE_SIZE, 0, x * TILE_SIZE, CANVAS_HEIGHT);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * TILE_SIZE, CANVAS_WIDTH, y * TILE_SIZE);
        }
    }

    public void stopGame() {
        gameRunning = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void restartGame() {
        stopGame();
        bombs.clear();
        explosions.clear();
        if (player != null) {
            player.setPosition(1, 1);
        }
        gameRunning = true;
        if (gameLoop != null) {
            gameLoop.play();
        }
    }
}
