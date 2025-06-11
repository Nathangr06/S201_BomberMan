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

    // Système de vies
    private int player1Lives = 3;
    private int player2Lives = 3;
    private int player1SpawnX = 1;
    private int player1SpawnY = 1;
    private int player2SpawnX = 13;
    private int player2SpawnY = 11;

    // Système d'invincibilité
    private int player1InvincibilityTimer = 0;
    private int player2InvincibilityTimer = 0;
    private static final int INVINCIBILITY_DURATION = 60; // Durée d'une explosion de bombe

    // Variables pour le mouvement fluide des joueurs
    private boolean aiMode = false;
    private AIPlayer aiPlayer;
    private long lastAIMoveTime = 0;
    private static final long AI_MOVE_INTERVAL = 500_000_000; // 500ms entre chaque action IA
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

    // Système de power-ups
    private List<PowerUp> powerUps;
    private static final int MAX_POWERUPS = 5;
    private static final double POWERUP_SPAWN_CHANCE = 0.3; // 30% de chance

    // Stats des joueurs avec power-ups
    private int player1BombRange = 2;
    private int player2BombRange = 2;
    private double player1Speed = MOVEMENT_SPEED;
    private double player2Speed = MOVEMENT_SPEED;
    private boolean player1CanPassWalls = false;
    private boolean player2CanPassWalls = false;
    private long player1BombCooldown = 0;
    private long player2BombCooldown = 0;
    private static final long DEFAULT_BOMB_COOLDOWN = 500_000_000; // 500ms
    private long player1LastBombTime = 0;
    private long player2LastBombTime = 0;
    private boolean wallPassDropped = false; // Indique si le Wall Pass a déjà été drop
    private boolean player1CanPushBombs = false;
    private boolean player2CanPushBombs = false;

    public enum PowerUpType {
        BOMB_RANGE("range", Color.ORANGE),      // Augmente la portée des bombes
        SPEED_BOOST("speed", Color.CYAN),       // Augmente la vitesse
        WALL_PASS("wall", Color.PURPLE),        // Permet de traverser les murs destructibles
        BOMB_COOLDOWN("cooldown", Color.YELLOW), // Réduit le délai entre les bombes
        BOMB_PUSH("push", Color.MAGENTA);       // Permet de pousser les bombes

        private final String label;
        private final Color color;

        PowerUpType(String label, Color color) {
            this.label = label;
            this.color = color;
        }

        public Color getColor() { return color; }
        public String getLabel() { return label; }
    }

    public class PowerUp {
        private int x, y;
        private PowerUpType type;

        public PowerUp(int x, int y, PowerUpType type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public PowerUpType getType() { return type; }
    }


    public enum CellType {
        EMPTY, WALL, DESTRUCTIBLE_WALL, PLAYER_SPAWN, PLAYER2_SPAWN
    }

    public BombermanGame() {
        // L'initialisation se fait dans startGame()
    }

    public void setAIMode(boolean aiMode) {
        this.aiMode = aiMode;
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

        // Positions de spawn par défaut
        player1SpawnX = 1;
        player1SpawnY = 1;
        player2SpawnX = 13;
        player2SpawnY = 11;

        // Joueur 1 en haut à gauche
        player1 = new Player(player1SpawnX, player1SpawnY);
        player1TargetX = player1SpawnX;
        player1TargetY = player1SpawnY;
        player1VisualX = player1SpawnX * TILE_SIZE;
        player1VisualY = player1SpawnY * TILE_SIZE + TIMER_HEIGHT;

        // Joueur 2 en bas à droite
        player2 = new Player(player2SpawnX, player2SpawnY);
        player2TargetX = player2SpawnX;
        player2TargetY = player2SpawnY;
        player2VisualX = player2SpawnX * TILE_SIZE;
        player2VisualY = player2SpawnY * TILE_SIZE + TIMER_HEIGHT;

        // Réinitialiser les vies et invincibilité
        player1Lives = 3;
        player2Lives = 3;
        player1InvincibilityTimer = 0;
        player2InvincibilityTimer = 0;

        if (aiMode) {
            aiPlayer = new AIPlayer(grid, this);
        }

        bombs = new ArrayList<>();
        explosions = new ArrayList<>();
        powerUps = new ArrayList<>();
        resetPlayerPowerUps();
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
            player1SpawnX = 1;
            player1SpawnY = 1;
            player1 = new Player(player1SpawnX, player1SpawnY);
            player1TargetX = player1SpawnX;
            player1TargetY = player1SpawnY;
            player1VisualX = player1SpawnX * TILE_SIZE;
            player1VisualY = player1SpawnY * TILE_SIZE + TIMER_HEIGHT;
        }
        if (player2 == null) {
            System.err.println("Aucun spawn joueur 2 trouvé, position par défaut utilisée");
            player2SpawnX = 13;
            player2SpawnY = 11;
            player2 = new Player(player2SpawnX, player2SpawnY);
            player2TargetX = player2SpawnX;
            player2TargetY = player2SpawnY;
            player2VisualX = player2SpawnX * TILE_SIZE;
            player2VisualY = player2SpawnY * TILE_SIZE + TIMER_HEIGHT;
        }

        // Réinitialiser les vies et invincibilité
        player1Lives = 3;
        player2Lives = 3;
        player1InvincibilityTimer = 0;
        player2InvincibilityTimer = 0;

        if (aiMode) {
            aiPlayer = new AIPlayer(grid, this);
        }

        powerUps = new ArrayList<>();
        resetPlayerPowerUps();
    }

    private void resetPlayerPowerUps() {
        player1BombRange = 2;
        player2BombRange = 2;
        player1Speed = MOVEMENT_SPEED;
        player2Speed = MOVEMENT_SPEED;
        player1CanPassWalls = false;
        player2CanPassWalls = false;
        player1BombCooldown = 0;
        player2BombCooldown = 0;
        player1LastBombTime = 0;
        player2LastBombTime = 0;
        wallPassDropped = false;
        player1CanPushBombs = false;
        player2CanPushBombs = false;
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
                            player1SpawnX = j;
                            player1SpawnY = i;
                            player1 = new Player(j, i);
                            player1TargetX = j;
                            player1TargetY = i;
                            player1VisualX = j * TILE_SIZE;
                            player1VisualY = i * TILE_SIZE + TIMER_HEIGHT;
                        }
                        case PLAYER2_SPAWN -> {
                            grid.setEmpty(j, i);
                            player2SpawnX = j;
                            player2SpawnY = i;
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
            updateInvincibility();
            updateBombs();
            updateExplosions();
            updatePowerUps();
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

            if ((newX != player1TargetX || newY != player1TargetY) &&
                    (grid.isWalkable(newX, newY) || (player1CanPassWalls && grid.isDestructibleWall(newX, newY)))) {

                // Vérifier s'il y a une bombe à pousser
                if (hasBombAt(newX, newY) && player1CanPushBombs) {
                    int pushX = newX + (newX - player1TargetX);
                    int pushY = newY + (newY - player1TargetY);
                    if (canPushBombTo(pushX, pushY)) {
                        pushBomb(newX, newY, pushX, pushY);
                        player1TargetX = newX;
                        player1TargetY = newY;
                        isPlayer1Moving = true;
                        lastPlayer1MoveTime = currentTime;
                    }
                } else if (!hasBombAt(newX, newY)) {
                    player1TargetX = newX;
                    player1TargetY = newY;
                    isPlayer1Moving = true;
                    lastPlayer1MoveTime = currentTime;
                }
            }
        }

        // Contrôles Joueur 2 (ZQSD) ou IA
        if (aiMode) {
            // IA contrôle le joueur 2
            if (!isPlayer2Moving && (currentTime - lastAIMoveTime) > AI_MOVE_INTERVAL) {
                AIPlayer.AIAction action = aiPlayer.getNextAction(bombs, explosions);
                if (action != null) {
                    switch (action) {
                        case MOVE_LEFT:
                            if ((grid.isWalkable(player2TargetX - 1, player2TargetY) ||
                                    (player2CanPassWalls && grid.isDestructibleWall(player2TargetX - 1, player2TargetY)))) {

                                if (hasBombAt(player2TargetX - 1, player2TargetY) && player2CanPushBombs) {
                                    if (canPushBombTo(player2TargetX - 2, player2TargetY)) {
                                        pushBomb(player2TargetX - 1, player2TargetY, player2TargetX - 2, player2TargetY);
                                        player2TargetX--;
                                        isPlayer2Moving = true;
                                    }
                                } else if (!hasBombAt(player2TargetX - 1, player2TargetY)) {
                                    player2TargetX--;
                                    isPlayer2Moving = true;
                                }
                            }
                            break;

                        case MOVE_RIGHT:
                            if ((grid.isWalkable(player2TargetX + 1, player2TargetY) ||
                                    (player2CanPassWalls && grid.isDestructibleWall(player2TargetX + 1, player2TargetY)))) {

                                if (hasBombAt(player2TargetX + 1, player2TargetY) && player2CanPushBombs) {
                                    if (canPushBombTo(player2TargetX + 2, player2TargetY)) {
                                        pushBomb(player2TargetX + 1, player2TargetY, player2TargetX + 2, player2TargetY);
                                        player2TargetX++;
                                        isPlayer2Moving = true;
                                    }
                                } else if (!hasBombAt(player2TargetX + 1, player2TargetY)) {
                                    player2TargetX++;
                                    isPlayer2Moving = true;
                                }
                            }
                            break;

                        case MOVE_UP:
                            if ((grid.isWalkable(player2TargetX, player2TargetY - 1) ||
                                    (player2CanPassWalls && grid.isDestructibleWall(player2TargetX, player2TargetY - 1)))) {

                                if (hasBombAt(player2TargetX, player2TargetY - 1) && player2CanPushBombs) {
                                    if (canPushBombTo(player2TargetX, player2TargetY - 2)) {
                                        pushBomb(player2TargetX, player2TargetY - 1, player2TargetX, player2TargetY - 2);
                                        player2TargetY--;
                                        isPlayer2Moving = true;
                                    }
                                } else if (!hasBombAt(player2TargetX, player2TargetY - 1)) {
                                    player2TargetY--;
                                    isPlayer2Moving = true;
                                }
                            }
                            break;

                        case MOVE_DOWN:
                            if ((grid.isWalkable(player2TargetX, player2TargetY + 1) ||
                                    (player2CanPassWalls && grid.isDestructibleWall(player2TargetX, player2TargetY + 1)))) {

                                if (hasBombAt(player2TargetX, player2TargetY + 1) && player2CanPushBombs) {
                                    if (canPushBombTo(player2TargetX, player2TargetY + 2)) {
                                        pushBomb(player2TargetX, player2TargetY + 1, player2TargetX, player2TargetY + 2);
                                        player2TargetY++;
                                        isPlayer2Moving = true;
                                    }
                                } else if (!hasBombAt(player2TargetX, player2TargetY + 1)) {
                                    player2TargetY++;
                                    isPlayer2Moving = true;
                                }
                            }
                            break;

                        case PLACE_BOMB:
                            if (currentTime - player2LastBombTime > (DEFAULT_BOMB_COOLDOWN - player2BombCooldown)) {
                                placeBomb(player2, player2BombRange);
                                player2LastBombTime = currentTime;
                            }
                            break;
                    }
                    lastAIMoveTime = currentTime;
                }
            }
        } else {
            // Contrôles manuels pour le joueur 2
            if (!isPlayer2Moving && (currentTime - lastPlayer2MoveTime) > MOVE_COOLDOWN) {
                int newX = player2TargetX;
                int newY = player2TargetY;
                if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.Q)) newX--;
                else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.D)) newX++;
                else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.Z)) newY--;
                else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.S)) newY++;

                if ((newX != player2TargetX || newY != player2TargetY) &&
                        (grid.isWalkable(newX, newY) || (player2CanPassWalls && grid.isDestructibleWall(newX, newY)))) {

                    // Vérifier s'il y a une bombe à pousser
                    if (hasBombAt(newX, newY) && player2CanPushBombs) {
                        int pushX = newX + (newX - player2TargetX);
                        int pushY = newY + (newY - player2TargetY);
                        if (canPushBombTo(pushX, pushY)) {
                            pushBomb(newX, newY, pushX, pushY);
                            player2TargetX = newX;
                            player2TargetY = newY;
                            isPlayer2Moving = true;
                            lastPlayer2MoveTime = currentTime;
                        }
                    } else if (!hasBombAt(newX, newY)) {
                        player2TargetX = newX;
                        player2TargetY = newY;
                        isPlayer2Moving = true;
                        lastPlayer2MoveTime = currentTime;
                    }
                }
            }

            if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.SPACE)) {
                if (currentTime - player2LastBombTime > (DEFAULT_BOMB_COOLDOWN - player2BombCooldown)) {
                    placeBomb(player2, player2BombRange);
                    player2LastBombTime = currentTime;
                    inputHandler.setKeyReleased(javafx.scene.input.KeyCode.SPACE);
                }
            }
        }

        // Placement de bombes
        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.ENTER)) {
            if (currentTime - player1LastBombTime > (DEFAULT_BOMB_COOLDOWN - player1BombCooldown)) {
                placeBomb(player1, player1BombRange);
                player1LastBombTime = currentTime;
                inputHandler.setKeyReleased(javafx.scene.input.KeyCode.ENTER);
            }
        }

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.ESCAPE)) {
            stopGame();
        }
    }

    private void updateInvincibility() {
        // Décrémenter les timers d'invincibilité
        if (player1InvincibilityTimer > 0) {
            player1InvincibilityTimer--;
        }
        if (player2InvincibilityTimer > 0) {
            player2InvincibilityTimer--;
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

            // Normaliser et appliquer la vitesse personnalisée
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distance > player1Speed && distance > 0.1) {
                player1VisualX += (deltaX / distance) * player1Speed;
                player1VisualY += (deltaY / distance) * player1Speed;
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

            // Normaliser et appliquer la vitesse personnalisée
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distance > player2Speed && distance > 0.1) {
                player2VisualX += (deltaX / distance) * player2Speed;
                player2VisualY += (deltaY / distance) * player2Speed;
            } else {
                // Arrivé à destination
                player2VisualX = targetX;
                player2VisualY = targetY;
                player2.setPosition(player2TargetX, player2TargetY);
                isPlayer2Moving = false;
            }
        }
    }

    private void updatePowerUps() {
        // Vérifier les collisions avec les power-ups
        var iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();

            // Collision avec joueur 1
            if (powerUp.getX() == player1.getX() && powerUp.getY() == player1.getY()) {
                applyPowerUp(1, powerUp.getType());
                iterator.remove();
                continue;
            }

            // Collision avec joueur 2
            if (powerUp.getX() == player2.getX() && powerUp.getY() == player2.getY()) {
                applyPowerUp(2, powerUp.getType());
                iterator.remove();
            }
        }
    }

    private boolean canPushBombTo(int x, int y) {
        return grid.inBounds(x, y) && grid.isWalkable(x, y) && !hasBombAt(x, y) &&
                !hasPlayerAt(x, y);
    }

    private boolean hasPlayerAt(int x, int y) {
        return (player1.getX() == x && player1.getY() == y) ||
                (player2.getX() == x && player2.getY() == y);
    }

    private void pushBomb(int fromX, int fromY, int toX, int toY) {
        for (Bomb bomb : bombs) {
            if (bomb.getX() == fromX && bomb.getY() == fromY) {
                bomb.setPosition(toX, toY);
                break;
            }
        }
    }

    private void applyPowerUp(int playerNumber, PowerUpType type) {
        switch (type) {
            case BOMB_RANGE:
                if (playerNumber == 1) {
                    player1BombRange = Math.min(player1BombRange + 1, 5); // Max 5
                } else {
                    player2BombRange = Math.min(player2BombRange + 1, 5);
                }
                break;
            case SPEED_BOOST:
                if (playerNumber == 1) {
                    player1Speed = Math.min(player1Speed + 1.0, MOVEMENT_SPEED * 2); // Max 2x vitesse
                } else {
                    player2Speed = Math.min(player2Speed + 1.0, MOVEMENT_SPEED * 2);
                }
                break;
            case WALL_PASS:
                if (playerNumber == 1) {
                    player1CanPassWalls = true;
                } else {
                    player2CanPassWalls = true;
                }
                break;
            case BOMB_COOLDOWN:
                if (playerNumber == 1) {
                    player1BombCooldown = Math.min(player1BombCooldown + 200_000_000L, 400_000_000L); // Max -400ms
                } else {
                    player2BombCooldown = Math.min(player2BombCooldown + 200_000_000L, 400_000_000L);
                }
                break;
            case BOMB_PUSH:
                if (playerNumber == 1) {
                    player1CanPushBombs = true;
                } else {
                    player2CanPushBombs = true;
                }
                break;
        }
        System.out.println("Joueur " + playerNumber + " a ramassé un power-up: " + type.getLabel());
    }

    private boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    private void placeBomb(Player player) {
        placeBomb(player, 2); // Portée par défaut
    }

    private void placeBomb(Player player, int range) {
        if (!hasBombAt(player.getX(), player.getY())) {
            Bomb bomb = new Bomb(player.getX(), player.getY());
            bomb.setRange(range);
            bombs.add(bomb);
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
        int range = bomb.getRange();
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
                    // Chance de faire apparaître un power-up
                    if (Math.random() < POWERUP_SPAWN_CHANCE && powerUps.size() < MAX_POWERUPS) {
                        PowerUpType randomType;
                        if (wallPassDropped) {
                            // Si Wall Pass déjà drop, choisir parmi les autres types
                            PowerUpType[] availableTypes = {PowerUpType.BOMB_RANGE, PowerUpType.SPEED_BOOST,
                                    PowerUpType.BOMB_COOLDOWN, PowerUpType.BOMB_PUSH};
                            randomType = availableTypes[(int)(Math.random() * availableTypes.length)];
                        } else {
                            // Sinon, choisir parmi tous les types
                            PowerUpType[] types = PowerUpType.values();
                            randomType = types[(int)(Math.random() * types.length)];
                            if (randomType == PowerUpType.WALL_PASS) {
                                wallPassDropped = true;
                            }
                        }
                        powerUps.add(new PowerUp(x, y, randomType));
                    }
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
            // Vérifier collision avec joueur 1 (seulement s'il n'est pas invincible)
            if (player1InvincibilityTimer <= 0 && explosion.getX() == player1.getX() && explosion.getY() == player1.getY()) {
                handlePlayerDeath(1);
                return;
            }
            // Vérifier collision avec joueur 2 (seulement s'il n'est pas invincible)
            if (player2InvincibilityTimer <= 0 && explosion.getX() == player2.getX() && explosion.getY() == player2.getY()) {
                handlePlayerDeath(2);
                return;
            }
        }
    }

    private void handlePlayerDeath(int playerNumber) {
        if (playerNumber == 1) {
            player1Lives--;
            System.out.println("Joueur 1 mort! Vies restantes: " + player1Lives);

            if (player1Lives <= 0) {
                gameOver("Joueur 2");
            } else {
                // Respawn du joueur 1
                respawnPlayer1();
            }
        } else if (playerNumber == 2) {
            player2Lives--;
            System.out.println("Joueur 2 mort! Vies restantes: " + player2Lives);

            if (player2Lives <= 0) {
                gameOver("Joueur 1");
            } else {
                // Respawn du joueur 2
                respawnPlayer2();
            }
        }
    }

    private void respawnPlayer1() {
        // Remettre le joueur 1 à sa position de spawn
        player1.setPosition(player1SpawnX, player1SpawnY);
        player1TargetX = player1SpawnX;
        player1TargetY = player1SpawnY;
        player1VisualX = player1SpawnX * TILE_SIZE;
        player1VisualY = player1SpawnY * TILE_SIZE + TIMER_HEIGHT;
        isPlayer1Moving = false;

        // Activer l'invincibilité temporaire
        player1InvincibilityTimer = INVINCIBILITY_DURATION;
    }

    private void respawnPlayer2() {
        // Remettre le joueur 2 à sa position de spawn
        player2.setPosition(player2SpawnX, player2SpawnY);
        player2TargetX = player2SpawnX;
        player2TargetY = player2SpawnY;
        player2VisualX = player2SpawnX * TILE_SIZE;
        player2VisualY = player2SpawnY * TILE_SIZE + TIMER_HEIGHT;
        isPlayer2Moving = false;

        // Activer l'invincibilité temporaire
        player2InvincibilityTimer = INVINCIBILITY_DURATION;
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

            // Power-ups
            for (PowerUp powerUp : powerUps) {
                int x = powerUp.getX() * TILE_SIZE;
                int y = powerUp.getY() * TILE_SIZE + TIMER_HEIGHT;

                // Fond du power-up
                gc.setFill(Color.WHITE);
                gc.fillRect(x + 5, y + 5, TILE_SIZE - 10, TILE_SIZE - 10);

                // Couleur du power-up
                gc.setFill(powerUp.getType().getColor());
                gc.fillRect(x + 8, y + 8, TILE_SIZE - 16, TILE_SIZE - 16);

                // Texte du power-up
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                String label = powerUp.getType().getLabel().substring(0, 1).toUpperCase();
                gc.fillText(label, x + TILE_SIZE/2 - 3, y + TILE_SIZE/2 + 3);
            }

            // Joueurs avec effet visuel d'invincibilité
            Image playerTexture = textureManager.getTexture("player");
            Image player2Texture = textureManager.getTexture("player2");

            if (playerTexture != null) {
                // Joueur 1 avec effet de clignotement si invincible
                if (player1InvincibilityTimer > 0 && (player1InvincibilityTimer / 5) % 2 == 0) {
                    // Clignotement : ne pas dessiner le joueur 1 frames sur 2
                } else {
                    gc.drawImage(playerTexture, player1VisualX, player1VisualY, TILE_SIZE, TILE_SIZE);
                }

                // Joueur 2 avec effet de clignotement si invincible
                if (player2InvincibilityTimer > 0 && (player2InvincibilityTimer / 5) % 2 == 0) {
                    // Clignotement : ne pas dessiner le joueur 2 frames sur 2
                } else {
                    gc.drawImage(player2Texture, player2VisualX, player2VisualY, TILE_SIZE, TILE_SIZE);
                }
            } else {
                // Fallback avec cercles colorés
                if (player1InvincibilityTimer > 0 && (player1InvincibilityTimer / 5) % 2 == 0) {
                    // Clignotement : couleur semi-transparente
                    gc.setFill(Color.LIGHTBLUE);
                } else {
                    gc.setFill(Color.BLUE);
                }
                gc.fillOval(player1VisualX + 5, player1VisualY + 5, TILE_SIZE - 10, TILE_SIZE - 10);

                if (player2InvincibilityTimer > 0 && (player2InvincibilityTimer / 5) % 2 == 0) {
                    // Clignotement : couleur semi-transparente
                    gc.setFill(Color.LIGHTCORAL);
                } else {
                    gc.setFill(Color.RED);
                }
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

        // Timer au centre
        double timerBoxWidth = 80;
        double timerBoxHeight = 30;
        renderTimerBox((CANVAS_WIDTH - timerBoxWidth) / 2, (TIMER_HEIGHT - timerBoxHeight) / 2, timerBoxWidth, timerBoxHeight);

        // Section Joueur 1 à gauche
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("Joueur 1", 20, TIMER_HEIGHT / 2 - 5);

        // Vies du joueur 1
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Vies: " + player1Lives, 20, TIMER_HEIGHT / 2 + 15);

        // Power-ups du joueur 1
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        String p1Powers = "R:" + player1BombRange + " S:" + String.format("%.1f", player1Speed/MOVEMENT_SPEED) +
                (player1CanPushBombs ? " P" : "");
        gc.fillText(p1Powers, 20, TIMER_HEIGHT / 2 + 30);

        // Section Joueur 2 à droite
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        String player2Text = "Joueur 2";
        double textWidth = player2Text.length() * 9; // Approximation de la largeur du texte
        gc.fillText(player2Text, CANVAS_WIDTH - textWidth - 20, TIMER_HEIGHT / 2 - 5);

        // Vies du joueur 2
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        String livesText = "Vies: " + player2Lives;
        double livesWidth = livesText.length() * 8;
        gc.fillText(livesText, CANVAS_WIDTH - livesWidth - 20, TIMER_HEIGHT / 2 + 15);

        // Power-ups du joueur 2
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        String p2Powers = "R:" + player2BombRange + " S:" + String.format("%.1f", player2Speed/MOVEMENT_SPEED) +
                (player2CanPushBombs ? " P" : "");
        double p2PowersWidth = p2Powers.length() * 6;
        gc.fillText(p2Powers, CANVAS_WIDTH - p2PowersWidth - 20, TIMER_HEIGHT / 2 + 30);
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

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }

    public void restartGame() {
        stopGame();
        bombs.clear();
        explosions.clear();
        powerUps.clear();
        wallPassDropped = false;
        resetPlayerPowerUps();

        // Réinitialiser le timer
        gameStartTime = System.currentTimeMillis();
        gameDuration = 0;

        // Réinitialiser les vies et invincibilité
        player1Lives = 3;
        player2Lives = 3;
        player1InvincibilityTimer = 0;
        player2InvincibilityTimer = 0;

        // Remettre les joueurs à leurs positions initiales
        if (player1 != null) {
            player1.setPosition(player1SpawnX, player1SpawnY);
            player1TargetX = player1SpawnX;
            player1TargetY = player1SpawnY;
            player1VisualX = player1SpawnX * TILE_SIZE;
            player1VisualY = player1SpawnY * TILE_SIZE + TIMER_HEIGHT;
        }
        if (player2 != null) {
            player2.setPosition(player2SpawnX, player2SpawnY);
            player2TargetX = player2SpawnX;
            player2TargetY = player2SpawnY;
            player2VisualX = player2SpawnX * TILE_SIZE;
            player2VisualY = player2SpawnY * TILE_SIZE + TIMER_HEIGHT;
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