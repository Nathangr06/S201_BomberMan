package com.example.bomberman;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    public static final int CANVAS_HEIGHT = GRID_HEIGHT * TILE_SIZE + 60; // +60 pour la zone du timer

    private Canvas canvas;
    private GraphicsContext gc;
    private Timeline gameLoop;
    private Stage gameStage;

    private GameGrid grid;
    private Player player1;
    private Player player2;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private InputHandler inputHandler;
    private boolean gameRunning = false;
    private TextureManager textureManager;

    // Variables pour le timer
    private long gameStartTime;
    private long gameDuration = 0; // Durée du jeu en secondes
    private static final int TIMER_HEIGHT = 60; // Hauteur de la zone du timer

    // Variables pour le mouvement fluide des joueurs
    private boolean isPlayer1Moving = false;
    private boolean isPlayer2Moving = false;
    private int player1TargetX = 1;
    private int player1TargetY = 1;
    private int player2TargetX = 13;
    private int player2TargetY = 11;

    // Positions visuelles fluides (en pixels) - ajustées pour le timer
    private double player1VisualX = 1 * TILE_SIZE;
    private double player1VisualY = 1 * TILE_SIZE + TIMER_HEIGHT;
    private double player2VisualX = 13 * TILE_SIZE;
    private double player2VisualY = 11 * TILE_SIZE + TIMER_HEIGHT;

    // Vitesse de déplacement (pixels par frame)
    private static final double MOVEMENT_SPEED = 3.0;

    // Contrôle de la fréquence des déplacements
    private long lastPlayer1MoveTime = 0;
    private long lastPlayer2MoveTime = 0;
    private static final long MOVE_COOLDOWN = 16_000_000; // 120ms entre chaque mouvement

    public enum CellType {
        EMPTY, WALL, DESTRUCTIBLE_WALL, PLAYER_SPAWN, PLAYER2_SPAWN
    }

    public BombermanGame() {
        // L'initialisation se fait dans startGame()
    }

    public void startGame(Stage stage) {
        startGame(stage, null);
    }

    public void startGame(Stage stage, File levelFile) {
        textureManager = new TextureManager();
        this.gameStage = stage;

        // Initialiser le timer
        gameStartTime = System.currentTimeMillis();
        gameDuration = 0;

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
        gameStage.setTitle("Bomberman - Jeu 2 Joueurs");
        gameStage.setResizable(false);
        gameStage.show();

        canvas.requestFocus();

        startGameLoop();
        gameRunning = true;
    }

    private void initializeGameWithDefaultLevel() {
        grid = new GameGrid(GRID_WIDTH, GRID_HEIGHT);
        grid.generate();

        // Joueur 1 en haut à gauche
        player1 = new Player(1, 1);
        player1TargetX = 1;
        player1TargetY = 1;
        player1VisualX = 1 * TILE_SIZE;
        player1VisualY = 1 * TILE_SIZE + TIMER_HEIGHT;

        // Joueur 2 en bas à droite
        player2 = new Player(13, 11);
        player2TargetX = 13;
        player2TargetY = 11;
        player2VisualX = 13 * TILE_SIZE;
        player2VisualY = 11 * TILE_SIZE + TIMER_HEIGHT;

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

        // Vérifier que les deux joueurs sont définis
        if (player1 == null) {
            System.err.println("Aucun spawn joueur 1 trouvé, position par défaut utilisée");
            player1 = new Player(1, 1);
            player1TargetX = 1;
            player1TargetY = 1;
            player1VisualX = 1 * TILE_SIZE;
            player1VisualY = 1 * TILE_SIZE + TIMER_HEIGHT;
        }
        if (player2 == null) {
            System.err.println("Aucun spawn joueur 2 trouvé, position par défaut utilisée");
            player2 = new Player(13, 11);
            player2TargetX = 13;
            player2TargetY = 11;
            player2VisualX = 13 * TILE_SIZE;
            player2VisualY = 11 * TILE_SIZE + TIMER_HEIGHT;
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
                            player1 = new Player(j, i);
                            player1TargetX = j;
                            player1TargetY = i;
                            player1VisualX = j * TILE_SIZE;
                            player1VisualY = i * TILE_SIZE + TIMER_HEIGHT;
                        }
                        case PLAYER2_SPAWN -> {
                            grid.setEmpty(j, i);
                            player2 = new Player(j, i);
                            player2TargetX = j;
                            player2TargetY = i;
                            player2VisualX = j * TILE_SIZE;
                            player2VisualY = i * TILE_SIZE + TIMER_HEIGHT;
                        }
                    }
                }
            }
        }
    }

    private void startGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> { // ~60 FPS
            update();
            render();
        }));
        gameLoop.setCycleCount(Animation.INDEFINITE);
        gameLoop.play();
    }

    private void update() {
        if (!gameRunning) return;

        // Mettre à jour le timer
        updateTimer();

        try {
            handleInput();
            updatePlayerMovement();
            updateBombs();
            updateExplosions();
            checkCollisions();
        } catch (Exception e) {
            System.err.println("Erreur dans la boucle de jeu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTimer() {
        long currentTime = System.currentTimeMillis();
        gameDuration = (currentTime - gameStartTime) / 1000; // Convertir en secondes
    }

    private void handleInput() {
        long currentTime = System.nanoTime();

        // Contrôles Joueur 1 (flèches directionnelles)
        if (!isPlayer1Moving && (currentTime - lastPlayer1MoveTime) > MOVE_COOLDOWN) {
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
                lastPlayer1MoveTime = currentTime;
            }
        }

        // Contrôles Joueur 2 (ZQSD)
        if (!isPlayer2Moving && (currentTime - lastPlayer2MoveTime) > MOVE_COOLDOWN) {
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
                lastPlayer2MoveTime = currentTime;
            }
        }

        // Placement de bombes
        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.ENTER)) {
            placeBomb(player1);
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.ENTER);
        }

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.SPACE)) {
            placeBomb(player2);
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.SPACE);
        }

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.ESCAPE)) {
            stopGame();
            // returnToMainMenu(); // Appeler le menu ici si implémenté
        }
    }

    private void updatePlayerMovement() {
        // Mise à jour position visuelle joueur 1
        if (isPlayer1Moving) {
            double targetX = player1TargetX * TILE_SIZE;
            double targetY = player1TargetY * TILE_SIZE + TIMER_HEIGHT;

            // Calculer la direction du mouvement
            double deltaX = targetX - player1VisualX;
            double deltaY = targetY - player1VisualY;

            // Normaliser et appliquer la vitesse
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distance > MOVEMENT_SPEED && distance > 0.1) {
                player1VisualX += (deltaX / distance) * MOVEMENT_SPEED;
                player1VisualY += (deltaY / distance) * MOVEMENT_SPEED;
            } else {
                // Arrivé à destination
                player1VisualX = targetX;
                player1VisualY = targetY;
                player1.setPosition(player1TargetX, player1TargetY);
                isPlayer1Moving = false;
            }
        }

        // Mise à jour position visuelle joueur 2
        if (isPlayer2Moving) {
            double targetX = player2TargetX * TILE_SIZE;
            double targetY = player2TargetY * TILE_SIZE + TIMER_HEIGHT;

            // Calculer la direction du mouvement
            double deltaX = targetX - player2VisualX;
            double deltaY = targetY - player2VisualY;

            // Normaliser et appliquer la vitesse
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distance > MOVEMENT_SPEED && distance > 0.1) {
                player2VisualX += (deltaX / distance) * MOVEMENT_SPEED;
                player2VisualY += (deltaY / distance) * MOVEMENT_SPEED;
            } else {
                // Arrivé à destination
                player2VisualX = targetX;
                player2VisualY = targetY;
                player2.setPosition(player2TargetX, player2TargetY);
                isPlayer2Moving = false;
            }
        }
    }

    private boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    private void placeBomb(Player player) {
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
            // Vérifier collision avec joueur 1
            if (explosion.getX() == player1.getX() && explosion.getY() == player1.getY()) {
                gameOver("Joueur 2");
                return;
            }
            // Vérifier collision avec joueur 2
            if (explosion.getX() == player2.getX() && explosion.getY() == player2.getY()) {
                gameOver("Joueur 1");
                return;
            }
        }
    }

    private void gameOver(String winner) {
        gameRunning = false;
        stopGame();
        System.out.println(winner + " GAGNE!");
        // Afficher un message ou retourner au menu principal
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private void render() {
        try {
            // Effacer tout l'écran
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

            // Afficher le timer en haut
            renderTimer();

            // Arrière-plan du jeu (zone verte sous le timer)
            gc.setFill(Color.GREEN);
            gc.fillRect(0, TIMER_HEIGHT, CANVAS_WIDTH, CANVAS_HEIGHT - TIMER_HEIGHT);

            // Ajuster le rendu de la grille avec l'offset du timer
            if (grid != null) {
                gc.save();
                gc.translate(0, TIMER_HEIGHT);
                grid.render(gc);
                gc.restore();
            }

            // Explosions
            Image explosionTexture = textureManager.getTexture("explosion");
            for (Explosion explosion : explosions) {
                int x = explosion.getX() * TILE_SIZE;
                int y = explosion.getY() * TILE_SIZE + TIMER_HEIGHT;
                if (explosionTexture != null) {
                    gc.drawImage(explosionTexture, x, y, TILE_SIZE, TILE_SIZE);
                } else {
                    gc.setFill(Color.ORANGE);
                    gc.fillRect(x + 5, y + 5, TILE_SIZE - 10, TILE_SIZE - 10);
                }
            }

            // Bombes
            Image bombTexture = textureManager.getTexture("bomb");
            for (Bomb bomb : bombs) {
                int x = bomb.getX() * TILE_SIZE;
                int y = bomb.getY() * TILE_SIZE + TIMER_HEIGHT;
                if (bombTexture != null) {
                    gc.drawImage(bombTexture, x, y, TILE_SIZE, TILE_SIZE);
                } else {
                    gc.setFill(Color.BLACK);
                    gc.fillOval(x + 8, y + 8, TILE_SIZE - 16, TILE_SIZE - 16);
                }
            }

            // Joueurs
            Image playerTexture = textureManager.getTexture("player");
            Image player2Texture = textureManager.getTexture("player2");
            if (playerTexture != null) {
                gc.drawImage(playerTexture, player1VisualX, player1VisualY, TILE_SIZE, TILE_SIZE);
                gc.drawImage(player2Texture, player2VisualX, player2VisualY, TILE_SIZE, TILE_SIZE);
            } else {
                gc.setFill(Color.BLUE);
                gc.fillOval(player1VisualX + 5, player1VisualY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
                gc.setFill(Color.RED);
                gc.fillOval(player2VisualX + 5, player2VisualY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // Grille de jeu (ajustée pour le timer)
        try {
            gc.setStroke(Color.DARKGREEN);
            gc.setLineWidth(1);
            for (int x = 0; x <= GRID_WIDTH; x++) {
                gc.strokeLine(x * TILE_SIZE, TIMER_HEIGHT, x * TILE_SIZE, CANVAS_HEIGHT);
            }
            for (int y = 0; y <= GRID_HEIGHT; y++) {
                gc.strokeLine(0, y * TILE_SIZE + TIMER_HEIGHT, CANVAS_WIDTH, y * TILE_SIZE + TIMER_HEIGHT);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du rendu: " + e.getMessage());
        }
    }

    private void renderTimer() {
        // Arrière-plan orange style Bomberman
        gc.setFill(Color.web("#FF8C00")); // Orange vif
        gc.fillRect(0, 0, CANVAS_WIDTH, TIMER_HEIGHT);

        // Bordure noire
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(0, 0, CANVAS_WIDTH, TIMER_HEIGHT);

        // Dimensions pour les éléments
        double sectionWidth = CANVAS_WIDTH / 5.0; // 5 sections : J1, Score1, Timer, Score2, J2
        double iconSize = 30;
        double scoreBoxWidth = 50;
        double scoreBoxHeight = 25;
        double timerBoxWidth = 80;
        double timerBoxHeight = 30;

        // Section Joueur 1 (gauche)
        renderPlayerSection(20, iconSize, Color.BLUE, "1", 0);

        // Score Joueur 1
        renderScoreBox(sectionWidth + 10, 0);

        // Timer au centre
        renderTimerBox((CANVAS_WIDTH - timerBoxWidth) / 2, (TIMER_HEIGHT - timerBoxHeight) / 2, timerBoxWidth, timerBoxHeight);

        // Score Joueur 2
        renderScoreBox(CANVAS_WIDTH - sectionWidth - scoreBoxWidth - 10, 0);

        // Section Joueur 2 (droite)
        renderPlayerSection(CANVAS_WIDTH - 60, iconSize, Color.RED, "2", 0);
    }

    private void renderPlayerSection(double x, double iconSize, Color playerColor, String playerNum, int score) {
        double y = (TIMER_HEIGHT - iconSize) / 2;

        // Icône de bombe stylisée
        gc.setFill(Color.BLACK);
        gc.fillOval(x, y, iconSize, iconSize);

        // Mèche de la bombe
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(3);
        gc.strokeLine(x + iconSize * 0.7, y + iconSize * 0.2, x + iconSize * 0.9, y);

        // Highlight sur la bombe
        gc.setFill(Color.GRAY);
        gc.fillOval(x + 5, y + 5, iconSize * 0.3, iconSize * 0.3);

        // Couronne du joueur (icône stylisée)
        double crownX = x + iconSize + 5;
        double crownY = y;
        double crownSize = iconSize * 0.8;

        // Base de la couronne
        gc.setFill(playerColor);
        gc.fillRect(crownX, crownY + crownSize * 0.6, crownSize, crownSize * 0.4);

        // Pics de la couronne
        gc.fillPolygon(
                new double[]{crownX, crownX + crownSize * 0.2, crownX + crownSize * 0.1},
                new double[]{crownY + crownSize * 0.6, crownY, crownY + crownSize * 0.6},
                3
        );
        gc.fillPolygon(
                new double[]{crownX + crownSize * 0.2, crownX + crownSize * 0.5, crownX + crownSize * 0.35},
                new double[]{crownY + crownSize * 0.6, crownY + crownSize * 0.1, crownY + crownSize * 0.6},
                3
        );
        gc.fillPolygon(
                new double[]{crownX + crownSize * 0.5, crownX + crownSize * 0.8, crownX + crownSize * 0.65},
                new double[]{crownY + crownSize * 0.6, crownY, crownY + crownSize * 0.6},
                3
        );
        gc.fillPolygon(
                new double[]{crownX + crownSize * 0.8, crownX + crownSize, crownX + crownSize * 0.9},
                new double[]{crownY + crownSize * 0.6, crownY + crownSize * 0.1, crownY + crownSize * 0.6},
                3
        );

        // Bordure noire pour la couronne
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(crownX, crownY + crownSize * 0.6, crownSize, crownSize * 0.4);
    }

    private void renderScoreBox(double x, int score) {
        double boxY = (TIMER_HEIGHT - 25) / 2;
        double boxWidth = 50;
        double boxHeight = 25;

        // Fond noir pour le score
        gc.setFill(Color.BLACK);
        gc.fillRect(x, boxY, boxWidth, boxHeight);

        // Bordure blanche
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, boxY, boxWidth, boxHeight);

        // Texte du score
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        String scoreText = String.valueOf(score);

        // Centrer le texte dans la boîte
        double textX = x + (boxWidth - scoreText.length() * 8) / 2;
        double textY = boxY + boxHeight / 2 + 6;
        gc.fillText(scoreText, textX, textY);
    }

    private void renderTimerBox(double x, double y, double width, double height) {
        // Fond noir pour le timer
        gc.setFill(Color.BLACK);
        gc.fillRect(x, y, width, height);

        // Bordure blanche
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);

        // Texte du timer
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        String timeText = formatTime(gameDuration);

        // Centrer le texte dans la boîte
        double textX = x + (width - timeText.length() * 9) / 2;
        double textY = y + height / 2 + 6;
        gc.fillText(timeText, textX, textY);
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

        // Réinitialiser le timer
        gameStartTime = System.currentTimeMillis();
        gameDuration = 0;

        // Remettre les joueurs à leurs positions initiales
        if (player1 != null) {
            player1.setPosition(1, 1);
            player1TargetX = 1;
            player1TargetY = 1;
            player1VisualX = 1 * TILE_SIZE;
            player1VisualY = 1 * TILE_SIZE + TIMER_HEIGHT;
        }
        if (player2 != null) {
            player2.setPosition(13, 11);
            player2TargetX = 13;
            player2TargetY = 11;
            player2VisualX = 13 * TILE_SIZE;
            player2VisualY = 11 * TILE_SIZE + TIMER_HEIGHT;
        }

        isPlayer1Moving = false;
        isPlayer2Moving = false;
        lastPlayer1MoveTime = 0;
        lastPlayer2MoveTime = 0;

        gameRunning = true;
        if (gameLoop != null) {
            gameLoop.play();
        }
    }
}