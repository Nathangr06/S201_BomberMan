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

public class CaptureTheFlag extends Application {

    public static final int TILE_SIZE = 40;
    public static final int GRID_WIDTH = 15;
    public static final int GRID_HEIGHT = 13;
    public static final int CANVAS_WIDTH = GRID_WIDTH * TILE_SIZE;
    public static final int CANVAS_HEIGHT = GRID_HEIGHT * TILE_SIZE;

    private Canvas canvas;
    private GraphicsContext gc;
    private Timeline gameLoop;

    private GameGrid grid;
    private Player player1;
    private Player player2;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private TextureManager textureManager;

    private InputHandler inputHandler;

    private double player1PixelX, player1PixelY;
    private double player2PixelX, player2PixelY;
    private double playerSpeed = 2.0;
    private boolean isPlayer1Moving = false;
    private boolean isPlayer2Moving = false;
    private int player1TargetX, player1TargetY;
    private int player2TargetX, player2TargetY;

    private Flag flag1, flag2;
    private boolean player1HasFlag = false;
    private boolean player2HasFlag = false;
    private boolean captureTheFlagMode = true; // pour activer/désactiver le mode
    private boolean player1Alive = true;
    private boolean player2Alive = true;

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

        player1 = new Player(1, 1);
        player2 = new Player(13, 11);

        player1PixelX = player1.getX() * TILE_SIZE;
        player1PixelY = player1.getY() * TILE_SIZE;
        player1TargetX = player1.getX();
        player1TargetY = player1.getY();

        player2PixelX = player2.getX() * TILE_SIZE;
        player2PixelY = player2.getY() * TILE_SIZE;
        player2TargetX = player2.getX();
        player2TargetY = player2.getY();

        bombs = new java.util.ArrayList<>();
        explosions = new java.util.ArrayList<>();

        if (captureTheFlagMode) {
            flag1 = new Flag(player1.getX(), player1.getY());
            flag2 = new Flag(player2.getX(), player2.getY());
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

    private void update() {
        handleInput();
        updatePlayerMovement();
        updateBombs();
        updateExplosions();
        checkPlayerExplosionCollision();

        if (captureTheFlagMode) {
            if (!flag2.isCaptured() && player1.getX() == flag2.getX() && player1.getY() == flag2.getY()) {
                player1HasFlag = true;
                flag2.setCaptured(true);
            }
            if (!flag1.isCaptured() && player2.getX() == flag1.getX() && player2.getY() == flag1.getY()) {
                player2HasFlag = true;
                flag1.setCaptured(true);
            }

            if (player1HasFlag && player1.getX() == flag1.getX() && player1.getY() == flag1.getY() && player1Alive) {
                endGame("Joueur 1 a capturé le drapeau et a gagné !");
            }
            if (player2HasFlag && player2.getX() == flag2.getX() && player2.getY() == flag2.getY() && player2Alive) {
                endGame("Joueur 2 a capturé le drapeau et a gagné !");
            }
        }
    }

    private void handleInput() {
        if (player1Alive && !isPlayer1Moving) {
            int newX = player1TargetX;
            int newY = player1TargetY;
            if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.LEFT)) newX--;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.RIGHT)) newX++;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.UP)) newY--;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.DOWN)) newY++;

            if ((newX != player1TargetX || newY != player1TargetY) && grid.isWalkable(newX, newY) && !hasBombAt(newX, newY)) {
                player1TargetX = newX;
                player1TargetY = newY;
                isPlayer1Moving = true;
            }
        }

        if (player2Alive && !isPlayer2Moving) {
            int newX = player2TargetX;
            int newY = player2TargetY;
            if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.Q)) newX--;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.D)) newX++;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.Z)) newY--;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.S)) newY++;

            if ((newX != player2TargetX || newY != player2TargetY) && grid.isWalkable(newX, newY) && !hasBombAt(newX, newY)) {
                player2TargetX = newX;
                player2TargetY = newY;
                isPlayer2Moving = true;
            }
        }

        // Les joueurs peuvent poser des bombes même morts (mode CTF)
        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.ENTER)) {
            placeBomb(player1);
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.ENTER);
        }

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.B)) {
            placeBomb(player2);
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.B);
        }
    }

    private void updatePlayerMovement() {
        if (isPlayer1Moving) {
            double targetX = player1TargetX * TILE_SIZE;
            double targetY = player1TargetY * TILE_SIZE;
            double dx = targetX - player1PixelX;
            double dy = targetY - player1PixelY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance <= playerSpeed) {
                player1PixelX = targetX;
                player1PixelY = targetY;
                player1.setPosition(player1TargetX, player1TargetY);
                isPlayer1Moving = false;
            } else {
                player1PixelX += (dx / distance) * playerSpeed;
                player1PixelY += (dy / distance) * playerSpeed;
            }
        }

        if (isPlayer2Moving) {
            double targetX = player2TargetX * TILE_SIZE;
            double targetY = player2TargetY * TILE_SIZE;
            double dx = targetX - player2PixelX;
            double dy = targetY - player2PixelY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance <= playerSpeed) {
                player2PixelX = targetX;
                player2PixelY = targetY;
                player2.setPosition(player2TargetX, player2TargetY);
                isPlayer2Moving = false;
            } else {
                player2PixelX += (dx / distance) * playerSpeed;
                player2PixelY += (dy / distance) * playerSpeed;
            }
        }
    }

    private void placeBomb(Player player) {
        int x = player.getX();
        int y = player.getY();
        if (!hasBombAt(x, y)) {
            bombs.add(new Bomb(x, y));
        }
    }

    private boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
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

    private void checkPlayerExplosionCollision() {
        int p1X = player1.getX();
        int p1Y = player1.getY();
        int p2X = player2.getX();
        int p2Y = player2.getY();

        for (Explosion explosion : explosions) {
            int ex = explosion.getX();
            int ey = explosion.getY();

            if (player1Alive && ex == p1X && ey == p1Y) {
                player1Alive = false;
                if (player1HasFlag) endGame("Joueur 1 est mort avec le drapeau !");
            }

            if (player2Alive && ex == p2X && ey == p2Y) {
                player2Alive = false;
                if (player2HasFlag) endGame("Joueur 2 est mort avec le drapeau !");
            }
        }
    }

    private void endGame(String message) {
        gameLoop.stop();
        System.out.println("Fin de partie : " + message);
    }

    private void render() {
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        grid.render(gc);

        // Affichage des drapeaux si mode CTF activé
        if (captureTheFlagMode) {
            Image flagTextureJ1 = textureManager.getTexture("Flag_J1");
            Image flagTextureJ2 = textureManager.getTexture("Flag_J2");

            if (!flag1.isCaptured()) {
                if (flagTextureJ1 != null) {
                    gc.drawImage(flagTextureJ1, flag1.getX() * TILE_SIZE, flag1.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else {
                    gc.setFill(Color.YELLOW);
                    gc.fillRect(flag1.getX() * TILE_SIZE + 10, flag1.getY() * TILE_SIZE + 10, 20, 20);
                }
            }

            if (!flag2.isCaptured()) {
                if (flagTextureJ2 != null) {
                    gc.drawImage(flagTextureJ2, flag2.getX() * TILE_SIZE, flag2.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else {
                    gc.setFill(Color.YELLOW);
                    gc.fillRect(flag2.getX() * TILE_SIZE + 10, flag2.getY() * TILE_SIZE + 10, 20, 20);
                }
            }
        }

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
        Image player2Texture = textureManager.getTexture("player2");
        if (playerTexture != null) {
            gc.drawImage(playerTexture, player1PixelX, player1PixelY, TILE_SIZE, TILE_SIZE);
            gc.drawImage(player2Texture, player2PixelX, player2PixelY, TILE_SIZE, TILE_SIZE);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillOval(player1PixelX + 5, player1PixelY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
            gc.setFill(Color.RED);
            gc.fillOval(player2PixelX + 5, player2PixelY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        }

        // Indiquer si un joueur a le drapeau (petit carré sur le joueur)
        if (player1HasFlag && player1Alive) {
            gc.setFill(Color.YELLOW);
            gc.fillRect(player1PixelX + TILE_SIZE / 3, player1PixelY + TILE_SIZE / 3, TILE_SIZE / 3, TILE_SIZE / 3);
        }
        if (player2HasFlag && player2Alive) {
            gc.setFill(Color.YELLOW);
            gc.fillRect(player2PixelX + TILE_SIZE / 3, player2PixelY + TILE_SIZE / 3, TILE_SIZE / 3, TILE_SIZE / 3);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}