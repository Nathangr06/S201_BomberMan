package com.example.bomberman;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
        grid = new GameGrid(GRID_WIDTH, GRID_HEIGHT);
        grid.generate();

        player = new Player(1,1);

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
    private static final long MOVE_INTERVAL = 200_000_000; // 200 ms

    private void update() {
        handleInput();
        updateBombs();
        updateExplosions();
    }

    private void handleInput() {
        long now = System.nanoTime();

        if (now - lastMoveTime < MOVE_INTERVAL) {
            return;
        }

        int newX = player.getX();
        int newY = player.getY();

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.LEFT)) newX--;
        else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.RIGHT)) newX++;
        else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.UP)) newY--;
        else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.DOWN)) newY++;

        if (grid.isWalkable(newX, newY) && !hasBombAt(newX, newY)) {
            player.setPosition(newX, newY);
        }

        lastMoveTime = now;

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.SPACE)) {
            placeBomb();
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.SPACE);
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
        explosions.removeIf(explosion -> explosion.decreaseTimerAndCheck());
    }

    private void render() {
        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        grid.render(gc);

        gc.setFill(Color.ORANGE);
        for (Explosion explosion : explosions) {
            gc.fillRect(explosion.getX() * TILE_SIZE + 5, explosion.getY() * TILE_SIZE + 5,
                    TILE_SIZE - 10, TILE_SIZE - 10);
        }

        gc.setFill(Color.BLACK);
        for (Bomb bomb : bombs) {
            gc.fillOval(bomb.getX() * TILE_SIZE + 8, bomb.getY() * TILE_SIZE + 8,
                    TILE_SIZE - 16, TILE_SIZE - 16);
        }

        gc.setFill(Color.BLUE);
        gc.fillOval(player.getX() * TILE_SIZE + 5, player.getY() * TILE_SIZE + 5,
                TILE_SIZE - 10, TILE_SIZE - 10);

        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(1);
        for (int x = 0; x <= GRID_WIDTH; x++) {
            gc.strokeLine(x * TILE_SIZE, 0, x * TILE_SIZE, CANVAS_HEIGHT);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * TILE_SIZE, CANVAS_WIDTH, y * TILE_SIZE);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
