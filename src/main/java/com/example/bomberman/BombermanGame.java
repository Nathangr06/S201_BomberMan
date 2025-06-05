package com.example.bomberman;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BombermanGame extends Application {

    private static final int TILE_SIZE = 40;
    private static final int GRID_WIDTH = 15;
    private static final int GRID_HEIGHT = 13;
    private static final int CANVAS_WIDTH = GRID_WIDTH * TILE_SIZE;
    private static final int CANVAS_HEIGHT = GRID_HEIGHT * TILE_SIZE;

    private Canvas canvas;
    private GraphicsContext gc;
    private Timeline gameLoop;

    // Grille de jeu (0 = vide, 1 = mur indestructible, 2 = mur destructible)
    private int[][] grid;

    // Joueur
    private Player player;

    // Bombes et explosions
    private List<Bomb> bombs;
    private List<Explosion> explosions;

    // Contrôles
    private boolean[] keys = new boolean[256];

    @Override
    public void start(Stage primaryStage) {
        initializeGame();

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        VBox root = new VBox();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root);

        // Gestion des touches
        scene.setOnKeyPressed(e -> {
            if (e.getCode().ordinal() < keys.length) {
                keys[e.getCode().ordinal()] = true;
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode().ordinal() < keys.length) {
                keys[e.getCode().ordinal()] = false;
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Bomberman");
        primaryStage.setResizable(false);
        primaryStage.show();

        canvas.requestFocus();

        startGameLoop();
    }

    private void initializeGame() {
        grid = new int[GRID_HEIGHT][GRID_WIDTH];
        bombs = new ArrayList<>();
        explosions = new ArrayList<>();

        // Créer la grille avec des murs
        Random rand = new Random();
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (x == 0 || y == 0 || x == GRID_WIDTH - 1 || y == GRID_HEIGHT - 1) {
                    grid[y][x] = 1; // Murs extérieurs
                } else if (x % 2 == 0 && y % 2 == 0) {
                    grid[y][x] = 1; // Murs intérieurs fixes
                } else if ((x > 2 || y > 2) && rand.nextDouble() < 0.3) {
                    grid[y][x] = 2; // Murs destructibles aléatoires
                }
            }
        }

        // Position initiale du joueur
        player = new Player(1, 1);
        grid[1][1] = 0; // S'assurer que la position de départ est libre
        grid[1][2] = 0;
        grid[2][1] = 0;
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
        updateBombs();
        updateExplosions();
    }

    private void handleInput() {
        int newX = player.x;
        int newY = player.y;

        if (keys[KeyCode.LEFT.ordinal()]) newX--;
        if (keys[KeyCode.RIGHT.ordinal()]) newX++;
        if (keys[KeyCode.UP.ordinal()]) newY--;
        if (keys[KeyCode.DOWN.ordinal()]) newY++;

        // Vérifier les collisions
        if (newX >= 0 && newX < GRID_WIDTH && newY >= 0 && newY < GRID_HEIGHT &&
                grid[newY][newX] == 0 && !hasBombAt(newX, newY)) {
            player.x = newX;
            player.y = newY;
        }

        // Placer une bombe
        if (keys[KeyCode.SPACE.ordinal()]) {
            placeBomb();
            keys[KeyCode.SPACE.ordinal()] = false; // Éviter le spam
        }
    }

    private boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(bomb -> bomb.x == x && bomb.y == y);
    }

    private void placeBomb() {
        if (!hasBombAt(player.x, player.y)) {
            bombs.add(new Bomb(player.x, player.y));
        }
    }

    private void updateBombs() {
        Iterator<Bomb> bombIter = bombs.iterator();
        while (bombIter.hasNext()) {
            Bomb bomb = bombIter.next();
            bomb.timer--;
            if (bomb.timer <= 0) {
                explodeBomb(bomb);
                bombIter.remove();
            }
        }
    }

    private void explodeBomb(Bomb bomb) {
        int range = 2;

        // Centre de l'explosion
        explosions.add(new Explosion(bomb.x, bomb.y, 60));

        // Explosion dans les 4 directions
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        for (int dir = 0; dir < 4; dir++) {
            for (int i = 1; i <= range; i++) {
                int x = bomb.x + dx[dir] * i;
                int y = bomb.y + dy[dir] * i;

                if (x < 0 || x >= GRID_WIDTH || y < 0 || y >= GRID_HEIGHT) break;
                if (grid[y][x] == 1) break; // Mur indestructible

                explosions.add(new Explosion(x, y, 60));

                if (grid[y][x] == 2) {
                    grid[y][x] = 0; // Détruire le mur destructible
                    break;
                }
            }
        }
    }

    private void updateExplosions() {
        explosions.removeIf(explosion -> --explosion.timer <= 0);
    }

    private void render() {
        // Effacer l'écran
        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Dessiner la grille
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                int tileType = grid[y][x];
                if (tileType == 1) {
                    // Mur indestructible
                    gc.setFill(Color.GRAY);
                    gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else if (tileType == 2) {
                    // Mur destructible
                    gc.setFill(Color.BROWN);
                    gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Dessiner les explosions
        gc.setFill(Color.ORANGE);
        for (Explosion explosion : explosions) {
            gc.fillRect(explosion.x * TILE_SIZE + 5, explosion.y * TILE_SIZE + 5,
                    TILE_SIZE - 10, TILE_SIZE - 10);
        }

        // Dessiner les bombes
        gc.setFill(Color.BLACK);
        for (Bomb bomb : bombs) {
            gc.fillOval(bomb.x * TILE_SIZE + 8, bomb.y * TILE_SIZE + 8,
                    TILE_SIZE - 16, TILE_SIZE - 16);
        }

        // Dessiner le joueur
        gc.setFill(Color.BLUE);
        gc.fillOval(player.x * TILE_SIZE + 5, player.y * TILE_SIZE + 5,
                TILE_SIZE - 10, TILE_SIZE - 10);

        // Dessiner la grille
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(1);
        for (int x = 0; x <= GRID_WIDTH; x++) {
            gc.strokeLine(x * TILE_SIZE, 0, x * TILE_SIZE, CANVAS_HEIGHT);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * TILE_SIZE, CANVAS_WIDTH, y * TILE_SIZE);
        }
    }

    // Classes internes
    private static class Player {
        int x, y;

        Player(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Bomb {
        int x, y;
        int timer;

        Bomb(int x, int y) {
            this.x = x;
            this.y = y;
            this.timer = 180; // 3 secondes à 60 FPS
        }
    }

    private static class Explosion {
        int x, y;
        int timer;

        Explosion(int x, int y, int timer) {
            this.x = x;
            this.y = y;
            this.timer = timer;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}