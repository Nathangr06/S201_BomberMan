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
    private Player player3; // Pour le mode 4 joueurs
    private Player player4; // Pour le mode 4 joueurs
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private InputHandler inputHandler;
    private boolean gameRunning = false;
    private TextureManager textureManager;

    // Configuration des modes de jeu
    private int playerCount = 2; // Par défaut 2 joueurs
    private boolean aiMode = false; // Mode IA activé ou non

    // Variables pour le timer
    private long gameStartTime;
    private long gameDuration = 0; // Durée du jeu en secondes
    private static final int TIMER_HEIGHT = 60; // Hauteur de la zone du timer

    // Système de vies pour tous les joueurs
    private int player1Lives = 3;
    private int player2Lives = 3;
    private int player3Lives = 3; // Pour le mode 4 joueurs
    private int player4Lives = 3; // Pour le mode 4 joueurs

    // États d'élimination des joueurs
    private boolean player1Eliminated = false;
    private boolean player2Eliminated = false;
    private boolean player3Eliminated = false;
    private boolean player4Eliminated = false;

    // Positions de spawn pour tous les joueurs
    private int player1SpawnX = 1;
    private int player1SpawnY = 1;
    private int player2SpawnX = 13;
    private int player2SpawnY = 11;
    private int player3SpawnX = 13; // Coin haut-droite pour joueur 3
    private int player3SpawnY = 1;
    private int player4SpawnX = 1;  // Coin bas-gauche pour joueur 4
    private int player4SpawnY = 11;

    // Système d'invincibilité pour tous les joueurs
    private int player1InvincibilityTimer = 0;
    private int player2InvincibilityTimer = 0;
    private int player3InvincibilityTimer = 0;
    private int player4InvincibilityTimer = 0;
    private static final int INVINCIBILITY_DURATION = 60; // Durée d'une explosion de bombe

    // Variables pour le mouvement fluide des joueurs
    private AIPlayer aiPlayer;
    private long lastAIMoveTime = 0;
    private static final long AI_MOVE_INTERVAL = 500_000_000; // 500ms entre chaque action IA

    // États de mouvement pour tous les joueurs
    private boolean isPlayer1Moving = false;
    private boolean isPlayer2Moving = false;
    private boolean isPlayer3Moving = false;
    private boolean isPlayer4Moving = false;

    // Positions cibles pour tous les joueurs
    private int player1TargetX = 1;
    private int player1TargetY = 1;
    private int player2TargetX = 13;
    private int player2TargetY = 11;
    private int player3TargetX = 13;
    private int player3TargetY = 1;
    private int player4TargetX = 1;
    private int player4TargetY = 11;

    // Positions visuelles fluides (en pixels) - ajustées pour le timer
    private double player1VisualX = 1 * TILE_SIZE;
    private double player1VisualY = 1 * TILE_SIZE + TIMER_HEIGHT;
    private double player2VisualX = 13 * TILE_SIZE;
    private double player2VisualY = 11 * TILE_SIZE + TIMER_HEIGHT;
    private double player3VisualX = 13 * TILE_SIZE;
    private double player3VisualY = 1 * TILE_SIZE + TIMER_HEIGHT;
    private double player4VisualX = 1 * TILE_SIZE;
    private double player4VisualY = 11 * TILE_SIZE + TIMER_HEIGHT;

    // Vitesse de déplacement (pixels par frame)
    private static final double MOVEMENT_SPEED = 3.0;

    // Contrôle de la fréquence des déplacements
    private long lastPlayer1MoveTime = 0;
    private long lastPlayer2MoveTime = 0;
    private long lastPlayer3MoveTime = 0;
    private long lastPlayer4MoveTime = 0;
    private static final long MOVE_COOLDOWN = 16_000_000; // 120ms entre chaque mouvement

    // Système de power-ups
    private List<PowerUp> powerUps;
    private static final int MAX_POWERUPS = 5;
    private static final double POWERUP_SPAWN_CHANCE = 0.3; // 30% de chance

    // Stats des joueurs avec power-ups
    private int player1BombRange = 1;
    private int player2BombRange = 1;
    private int player3BombRange = 1;
    private int player4BombRange = 1;
    private double player1Speed = MOVEMENT_SPEED;
    private double player2Speed = MOVEMENT_SPEED;
    private double player3Speed = MOVEMENT_SPEED;
    private double player4Speed = MOVEMENT_SPEED;
    private boolean player1CanPassWalls = false;
    private boolean player2CanPassWalls = false;
    private boolean player3CanPassWalls = false;
    private boolean player4CanPassWalls = false;
    private long player1BombCooldown = 0;
    private long player2BombCooldown = 0;
    private long player3BombCooldown = 0;
    private long player4BombCooldown = 0;
    private static final long DEFAULT_BOMB_COOLDOWN = 500_000_000; // 500ms
    private long player1LastBombTime = 0;
    private long player2LastBombTime = 0;
    private long player3LastBombTime = 0;
    private long player4LastBombTime = 0;
    private boolean wallPassDropped = false; // Indique si le Wall Pass a déjà été drop
    private boolean player1CanPushBombs = false;
    private boolean player2CanPushBombs = false;
    private boolean player3CanPushBombs = false;
    private boolean player4CanPushBombs = false;

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

    // **NOUVELLES MÉTHODES POUR CONFIGURER LES MODES**
    public void setPlayerCount(int count) {
        this.playerCount = Math.max(2, Math.min(4, count)); // Entre 2 et 4 joueurs
    }

    public void setAIMode(boolean aiMode) {
        this.aiMode = aiMode;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public boolean isAIMode() {
        return aiMode;
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

        // Titre adapté au mode de jeu
        String title = "Bomberman - ";
        if (aiMode) {
            title += "Mode IA";
        } else if (playerCount == 4) {
            title += "4 Joueurs";
        } else {
            title += "2 Joueurs";
        }
        gameStage.setTitle(title);

        gameStage.setResizable(false);
        gameStage.show();

        canvas.requestFocus();

        startGameLoop();
        gameRunning = true;
    }

    private void initializeGameWithDefaultLevel() {
        grid = new GameGrid(GRID_WIDTH, GRID_HEIGHT);
        grid.generate();

        // Initialiser les joueurs selon le nombre sélectionné
        initializePlayers();

        if (aiMode) {
            aiPlayer = new AIPlayer(grid, this);
        }

        bombs = new ArrayList<>();
        explosions = new ArrayList<>();
        powerUps = new ArrayList<>();
        resetPlayerPowerUps();
    }

    private void initializePlayers() {
        // Réinitialiser les états d'élimination
        player1Eliminated = false;
        player2Eliminated = false;
        player3Eliminated = false;
        player4Eliminated = false;

        // Joueur 1 (toujours présent)
        player1SpawnX = 1;
        player1SpawnY = 1;
        player1 = new Player(player1SpawnX, player1SpawnY);
        player1TargetX = player1SpawnX;
        player1TargetY = player1SpawnY;
        player1VisualX = player1SpawnX * TILE_SIZE;
        player1VisualY = player1SpawnY * TILE_SIZE + TIMER_HEIGHT;
        player1Lives = 3;
        player1InvincibilityTimer = 0;

        // Joueur 2 (toujours présent)
        player2SpawnX = 13;
        player2SpawnY = 11;
        player2 = new Player(player2SpawnX, player2SpawnY);
        player2TargetX = player2SpawnX;
        player2TargetY = player2SpawnY;
        player2VisualX = player2SpawnX * TILE_SIZE;
        player2VisualY = player2SpawnY * TILE_SIZE + TIMER_HEIGHT;
        player2Lives = 3;
        player2InvincibilityTimer = 0;

        // Joueurs 3 et 4 (seulement en mode 4 joueurs)
        if (playerCount >= 3) {
            player3SpawnX = 13;
            player3SpawnY = 1;
            player3 = new Player(player3SpawnX, player3SpawnY);
            player3TargetX = player3SpawnX;
            player3TargetY = player3SpawnY;
            player3VisualX = player3SpawnX * TILE_SIZE;
            player3VisualY = player3SpawnY * TILE_SIZE + TIMER_HEIGHT;
            player3Lives = 3;
            player3InvincibilityTimer = 0;
        }

        if (playerCount >= 4) {
            player4SpawnX = 1;
            player4SpawnY = 11;
            player4 = new Player(player4SpawnX, player4SpawnY);
            player4TargetX = player4SpawnX;
            player4TargetY = player4SpawnY;
            player4VisualX = player4SpawnX * TILE_SIZE;
            player4VisualY = player4SpawnY * TILE_SIZE + TIMER_HEIGHT;
            player4Lives = 3;
            player4InvincibilityTimer = 0;
        }
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
            return;
        }

        // Vérifier que les joueurs nécessaires sont définis
        ensurePlayersExist();

        if (aiMode) {
            aiPlayer = new AIPlayer(grid, this);
        }

        powerUps = new ArrayList<>();
        resetPlayerPowerUps();
    }

    private void ensurePlayersExist() {
        // Réinitialiser les états d'élimination
        player1Eliminated = false;
        player2Eliminated = false;
        player3Eliminated = false;
        player4Eliminated = false;

        // Vérifier et créer le joueur 1 si nécessaire
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
        player1Lives = 3;
        player1InvincibilityTimer = 0;

        // Vérifier et créer le joueur 2 si nécessaire
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
        player2Lives = 3;
        player2InvincibilityTimer = 0;

        // Créer les joueurs 3 et 4 si mode 4 joueurs
        if (playerCount >= 3) {
            if (player3 == null) {
                player3SpawnX = 13;
                player3SpawnY = 1;
                player3 = new Player(player3SpawnX, player3SpawnY);
                player3TargetX = player3SpawnX;
                player3TargetY = player3SpawnY;
                player3VisualX = player3SpawnX * TILE_SIZE;
                player3VisualY = player3SpawnY * TILE_SIZE + TIMER_HEIGHT;
            }
            player3Lives = 3;
            player3InvincibilityTimer = 0;
        }

        if (playerCount >= 4) {
            if (player4 == null) {
                player4SpawnX = 1;
                player4SpawnY = 11;
                player4 = new Player(player4SpawnX, player4SpawnY);
                player4TargetX = player4SpawnX;
                player4TargetY = player4SpawnY;
                player4VisualX = player4SpawnX * TILE_SIZE;
                player4VisualY = player4SpawnY * TILE_SIZE + TIMER_HEIGHT;
            }
            player4Lives = 3;
            player4InvincibilityTimer = 0;
        }
    }

    private void resetPlayerPowerUps() {
        player1BombRange = 1;
        player2BombRange = 1;
        player3BombRange = 1;
        player4BombRange = 1;
        player1Speed = MOVEMENT_SPEED;
        player2Speed = MOVEMENT_SPEED;
        player3Speed = MOVEMENT_SPEED;
        player4Speed = MOVEMENT_SPEED;
        player1CanPassWalls = false;
        player2CanPassWalls = false;
        player3CanPassWalls = false;
        player4CanPassWalls = false;
        player1BombCooldown = 0;
        player2BombCooldown = 0;
        player3BombCooldown = 0;
        player4BombCooldown = 0;
        player1LastBombTime = 0;
        player2LastBombTime = 0;
        player3LastBombTime = 0;
        player4LastBombTime = 0;
        wallPassDropped = false;
        player1CanPushBombs = false;
        player2CanPushBombs = false;
        player3CanPushBombs = false;
        player4CanPushBombs = false;
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

        // Contrôles Joueur 1 (flèches directionnelles) - seulement s'il n'est pas éliminé
        if (!player1Eliminated) {
            handlePlayerInput(1, currentTime);
        }

        // Contrôles Joueur 2 (ZQSD) ou IA - seulement s'il n'est pas éliminé
        if (!player2Eliminated) {
            if (aiMode) {
                handleAIInput(currentTime);
            } else {
                handlePlayerInput(2, currentTime);
            }
        }

        // Contrôles additionnels pour les joueurs 3 et 4 en mode 4 joueurs
        if (playerCount >= 3 && !player3Eliminated) {
            handlePlayerInput(3, currentTime);
        }
        if (playerCount >= 4 && !player4Eliminated) {
            handlePlayerInput(4, currentTime);
        }

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.ESCAPE)) {
            stopGame();
        }
    }

    private void handlePlayerInput(int playerNum, long currentTime) {
        Player player;
        boolean isMoving;
        long lastMoveTime;
        int targetX, targetY;
        javafx.scene.input.KeyCode leftKey, rightKey, upKey, downKey, bombKey;

        // Configuration des touches et variables selon le joueur
        switch (playerNum) {
            case 1:
                if (player1Eliminated) return;
                player = player1;
                isMoving = isPlayer1Moving;
                lastMoveTime = lastPlayer1MoveTime;
                targetX = player1TargetX;
                targetY = player1TargetY;
                leftKey = javafx.scene.input.KeyCode.LEFT;
                rightKey = javafx.scene.input.KeyCode.RIGHT;
                upKey = javafx.scene.input.KeyCode.UP;
                downKey = javafx.scene.input.KeyCode.DOWN;
                bombKey = javafx.scene.input.KeyCode.ENTER;
                break;
            case 2:
                if (player2Eliminated) return;
                player = player2;
                isMoving = isPlayer2Moving;
                lastMoveTime = lastPlayer2MoveTime;
                targetX = player2TargetX;
                targetY = player2TargetY;
                leftKey = javafx.scene.input.KeyCode.Q;
                rightKey = javafx.scene.input.KeyCode.D;
                upKey = javafx.scene.input.KeyCode.Z;
                downKey = javafx.scene.input.KeyCode.S;
                bombKey = javafx.scene.input.KeyCode.SPACE;
                break;
            case 3:
                if (player3 == null || player3Eliminated) return;
                player = player3;
                isMoving = isPlayer3Moving;
                lastMoveTime = lastPlayer3MoveTime;
                targetX = player3TargetX;
                targetY = player3TargetY;
                leftKey = javafx.scene.input.KeyCode.J;
                rightKey = javafx.scene.input.KeyCode.L;
                upKey = javafx.scene.input.KeyCode.I;
                downKey = javafx.scene.input.KeyCode.K;
                bombKey = javafx.scene.input.KeyCode.U;
                break;
            case 4:
                if (player4 == null || player4Eliminated) return;
                player = player4;
                isMoving = isPlayer4Moving;
                lastMoveTime = lastPlayer4MoveTime;
                targetX = player4TargetX;
                targetY = player4TargetY;
                leftKey = javafx.scene.input.KeyCode.NUMPAD4;
                rightKey = javafx.scene.input.KeyCode.NUMPAD6;
                upKey = javafx.scene.input.KeyCode.NUMPAD8;
                downKey = javafx.scene.input.KeyCode.NUMPAD5;
                bombKey = javafx.scene.input.KeyCode.NUMPAD0;
                break;
            default:
                return;
        }

        // Gestion du mouvement
        if (!isMoving && (currentTime - lastMoveTime) > MOVE_COOLDOWN) {
            int newX = targetX;
            int newY = targetY;

            if (inputHandler.isKeyPressed(leftKey)) newX--;
            else if (inputHandler.isKeyPressed(rightKey)) newX++;
            else if (inputHandler.isKeyPressed(upKey)) newY--;
            else if (inputHandler.isKeyPressed(downKey)) newY++;

            if ((newX != targetX || newY != targetY) && canPlayerMoveTo(playerNum, newX, newY)) {
                setPlayerTarget(playerNum, newX, newY);
                setPlayerMoving(playerNum, true);
                setPlayerLastMoveTime(playerNum, currentTime);
            }
        }

        // Gestion du placement de bombes
        if (inputHandler.isKeyPressed(bombKey)) {
            if (canPlayerPlaceBomb(playerNum, currentTime)) {
                placeBombForPlayer(playerNum);
                setPlayerLastBombTime(playerNum, currentTime);
                inputHandler.setKeyReleased(bombKey);
            }
        }
    }

    private void handleAIInput(long currentTime) {
        if (player2Eliminated) return; // Ne pas contrôler l'IA si le joueur 2 est éliminé

        if (!isPlayer2Moving && (currentTime - lastAIMoveTime) > AI_MOVE_INTERVAL) {
            AIPlayer.AIAction action = aiPlayer.getNextAction(bombs, explosions);
            if (action != null) {
                switch (action) {
                    case MOVE_LEFT:
                        if (canPlayerMoveTo(2, player2TargetX - 1, player2TargetY)) {
                            player2TargetX--;
                            isPlayer2Moving = true;
                        }
                        break;
                    case MOVE_RIGHT:
                        if (canPlayerMoveTo(2, player2TargetX + 1, player2TargetY)) {
                            player2TargetX++;
                            isPlayer2Moving = true;
                        }
                        break;
                    case MOVE_UP:
                        if (canPlayerMoveTo(2, player2TargetX, player2TargetY - 1)) {
                            player2TargetY--;
                            isPlayer2Moving = true;
                        }
                        break;
                    case MOVE_DOWN:
                        if (canPlayerMoveTo(2, player2TargetX, player2TargetY + 1)) {
                            player2TargetY++;
                            isPlayer2Moving = true;
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
    }

    // Méthodes utilitaires pour gérer les joueurs de manière uniforme
    private boolean canPlayerMoveTo(int playerNum, int x, int y) {
        boolean canPassWalls = false;
        boolean canPush = false;

        switch (playerNum) {
            case 1:
                if (player1Eliminated) return false;
                canPassWalls = player1CanPassWalls;
                canPush = player1CanPushBombs;
                break;
            case 2:
                if (player2Eliminated) return false;
                canPassWalls = player2CanPassWalls;
                canPush = player2CanPushBombs;
                break;
            case 3:
                if (player3 == null || player3Eliminated) return false;
                canPassWalls = player3CanPassWalls;
                canPush = player3CanPushBombs;
                break;
            case 4:
                if (player4 == null || player4Eliminated) return false;
                canPassWalls = player4CanPassWalls;
                canPush = player4CanPushBombs;
                break;
            default:
                return false;
        }

        if (grid.isWalkable(x, y) || (canPassWalls && grid.isDestructibleWall(x, y))) {
            if (hasBombAt(x, y) && canPush) {
                // Logique de poussée de bombe (simplifiée pour l'exemple)
                return true; // À implémenter complètement
            } else if (!hasBombAt(x, y)) {
                return true;
            }
        }
        return false;
    }

    private void setPlayerTarget(int playerNum, int x, int y) {
        switch (playerNum) {
            case 1:
                if (!player1Eliminated) {
                    player1TargetX = x;
                    player1TargetY = y;
                }
                break;
            case 2:
                if (!player2Eliminated) {
                    player2TargetX = x;
                    player2TargetY = y;
                }
                break;
            case 3:
                if (player3 != null && !player3Eliminated) {
                    player3TargetX = x;
                    player3TargetY = y;
                }
                break;
            case 4:
                if (player4 != null && !player4Eliminated) {
                    player4TargetX = x;
                    player4TargetY = y;
                }
                break;
        }
    }

    private void setPlayerMoving(int playerNum, boolean moving) {
        switch (playerNum) {
            case 1:
                if (!player1Eliminated) isPlayer1Moving = moving;
                break;
            case 2:
                if (!player2Eliminated) isPlayer2Moving = moving;
                break;
            case 3:
                if (player3 != null && !player3Eliminated) isPlayer3Moving = moving;
                break;
            case 4:
                if (player4 != null && !player4Eliminated) isPlayer4Moving = moving;
                break;
        }
    }

    private void setPlayerLastMoveTime(int playerNum, long time) {
        switch (playerNum) {
            case 1: if (!player1Eliminated) lastPlayer1MoveTime = time; break;
            case 2: if (!player2Eliminated) lastPlayer2MoveTime = time; break;
            case 3: if (player3 != null && !player3Eliminated) lastPlayer3MoveTime = time; break;
            case 4: if (player4 != null && !player4Eliminated) lastPlayer4MoveTime = time; break;
        }
    }

    private void setPlayerLastBombTime(int playerNum, long time) {
        switch (playerNum) {
            case 1: if (!player1Eliminated) player1LastBombTime = time; break;
            case 2: if (!player2Eliminated) player2LastBombTime = time; break;
            case 3: if (player3 != null && !player3Eliminated) player3LastBombTime = time; break;
            case 4: if (player4 != null && !player4Eliminated) player4LastBombTime = time; break;
        }
    }

    private boolean canPlayerPlaceBomb(int playerNum, long currentTime) {
        long lastBombTime;
        long bombCooldown;

        switch (playerNum) {
            case 1:
                if (player1Eliminated) return false;
                lastBombTime = player1LastBombTime;
                bombCooldown = player1BombCooldown;
                break;
            case 2:
                if (player2Eliminated) return false;
                lastBombTime = player2LastBombTime;
                bombCooldown = player2BombCooldown;
                break;
            case 3:
                if (player3 == null || player3Eliminated) return false;
                lastBombTime = player3LastBombTime;
                bombCooldown = player3BombCooldown;
                break;
            case 4:
                if (player4 == null || player4Eliminated) return false;
                lastBombTime = player4LastBombTime;
                bombCooldown = player4BombCooldown;
                break;
            default: return false;
        }

        return currentTime - lastBombTime > (DEFAULT_BOMB_COOLDOWN - bombCooldown);
    }

    private void placeBombForPlayer(int playerNum) {
        Player player;
        int range;

        switch (playerNum) {
            case 1:
                if (player1Eliminated) return;
                player = player1;
                range = player1BombRange;
                break;
            case 2:
                if (player2Eliminated) return;
                player = player2;
                range = player2BombRange;
                break;
            case 3:
                if (player3 == null || player3Eliminated) return;
                player = player3;
                range = player3BombRange;
                break;
            case 4:
                if (player4 == null || player4Eliminated) return;
                player = player4;
                range = player4BombRange;
                break;
            default: return;
        }

        placeBomb(player, range);
    }

    private void updateInvincibility() {
        // Décrémenter les timers d'invincibilité seulement pour les joueurs non éliminés
        if (!player1Eliminated && player1InvincibilityTimer > 0) {
            player1InvincibilityTimer--;
        }
        if (!player2Eliminated && player2InvincibilityTimer > 0) {
            player2InvincibilityTimer--;
        }
        if (playerCount >= 3 && !player3Eliminated && player3InvincibilityTimer > 0) {
            player3InvincibilityTimer--;
        }
        if (playerCount >= 4 && !player4Eliminated && player4InvincibilityTimer > 0) {
            player4InvincibilityTimer--;
        }
    }

    private void updatePlayerMovement() {
        // Mise à jour position visuelle joueur 1
        if (!player1Eliminated) {
            updatePlayerVisualPosition(1);
        }

        // Mise à jour position visuelle joueur 2
        if (!player2Eliminated) {
            updatePlayerVisualPosition(2);
        }

        // Mise à jour positions visuelles joueurs 3 et 4 si actifs et non éliminés
        if (playerCount >= 3 && !player3Eliminated) {
            updatePlayerVisualPosition(3);
        }
        if (playerCount >= 4 && !player4Eliminated) {
            updatePlayerVisualPosition(4);
        }
    }

    private void updatePlayerVisualPosition(int playerNum) {
        Player player;
        boolean isMoving;
        int targetX, targetY;
        double speed;
        double currentVisualX, currentVisualY;

        switch (playerNum) {
            case 1:
                if (player1Eliminated) return;
                player = player1;
                isMoving = isPlayer1Moving;
                targetX = player1TargetX;
                targetY = player1TargetY;
                speed = player1Speed;
                currentVisualX = player1VisualX;
                currentVisualY = player1VisualY;
                break;
            case 2:
                if (player2Eliminated) return;
                player = player2;
                isMoving = isPlayer2Moving;
                targetX = player2TargetX;
                targetY = player2TargetY;
                speed = player2Speed;
                currentVisualX = player2VisualX;
                currentVisualY = player2VisualY;
                break;
            case 3:
                if (player3 == null || player3Eliminated) return;
                player = player3;
                isMoving = isPlayer3Moving;
                targetX = player3TargetX;
                targetY = player3TargetY;
                speed = player3Speed;
                currentVisualX = player3VisualX;
                currentVisualY = player3VisualY;
                break;
            case 4:
                if (player4 == null || player4Eliminated) return;
                player = player4;
                isMoving = isPlayer4Moving;
                targetX = player4TargetX;
                targetY = player4TargetY;
                speed = player4Speed;
                currentVisualX = player4VisualX;
                currentVisualY = player4VisualY;
                break;
            default:
                return;
        }

        if (isMoving) {
            double targetVisualX = targetX * TILE_SIZE;
            double targetVisualY = targetY * TILE_SIZE + TIMER_HEIGHT;

            // Calculer la direction du mouvement
            double deltaX = targetVisualX - currentVisualX;
            double deltaY = targetVisualY - currentVisualY;

            // Normaliser et appliquer la vitesse personnalisée
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (distance > speed && distance > 0.1) {
                currentVisualX += (deltaX / distance) * speed;
                currentVisualY += (deltaY / distance) * speed;
            } else {
                // Arrivé à destination
                currentVisualX = targetVisualX;
                currentVisualY = targetVisualY;
                player.setPosition(targetX, targetY);
                setPlayerMoving(playerNum, false);
            }

            // Mettre à jour


            // Mettre à jour les variables de position visuelle
            switch (playerNum) {
                case 1: player1VisualX = currentVisualX; player1VisualY = currentVisualY; break;
                case 2: player2VisualX = currentVisualX; player2VisualY = currentVisualY; break;
                case 3: player3VisualX = currentVisualX; player3VisualY = currentVisualY; break;
                case 4: player4VisualX = currentVisualX; player4VisualY = currentVisualY; break;
            }
        }
    }

    private void updatePowerUps() {
        // Vérifier les collisions avec les power-ups seulement pour les joueurs non éliminés
        var iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();

            // Collision avec joueur 1
            if (!player1Eliminated && powerUp.getX() == player1.getX() && powerUp.getY() == player1.getY()) {
                applyPowerUp(1, powerUp.getType());
                iterator.remove();
                continue;
            }

            // Collision avec joueur 2
            if (!player2Eliminated && powerUp.getX() == player2.getX() && powerUp.getY() == player2.getY()) {
                applyPowerUp(2, powerUp.getType());
                iterator.remove();
                continue;
            }

            // Collision avec joueur 3 si actif et non éliminé
            if (playerCount >= 3 && player3 != null && !player3Eliminated &&
                    powerUp.getX() == player3.getX() && powerUp.getY() == player3.getY()) {
                applyPowerUp(3, powerUp.getType());
                iterator.remove();
                continue;
            }

            // Collision avec joueur 4 si actif et non éliminé
            if (playerCount >= 4 && player4 != null && !player4Eliminated &&
                    powerUp.getX() == player4.getX() && powerUp.getY() == player4.getY()) {
                applyPowerUp(4, powerUp.getType());
                iterator.remove();
            }
        }
    }

    private boolean canPushBombTo(int x, int y) {
        return grid.inBounds(x, y) && grid.isWalkable(x, y) && !hasBombAt(x, y) &&
                !hasPlayerAt(x, y);
    }

    private boolean hasPlayerAt(int x, int y) {
        boolean hasPlayer = false;

        if (!player1Eliminated && player1.getX() == x && player1.getY() == y) {
            hasPlayer = true;
        }
        if (!player2Eliminated && player2.getX() == x && player2.getY() == y) {
            hasPlayer = true;
        }
        if (playerCount >= 3 && player3 != null && !player3Eliminated &&
                player3.getX() == x && player3.getY() == y) {
            hasPlayer = true;
        }
        if (playerCount >= 4 && player4 != null && !player4Eliminated &&
                player4.getX() == x && player4.getY() == y) {
            hasPlayer = true;
        }

        return hasPlayer;
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
        // Ne pas appliquer de power-ups aux joueurs éliminés
        switch (playerNumber) {
            case 1: if (player1Eliminated) return; break;
            case 2: if (player2Eliminated) return; break;
            case 3: if (player3 == null || player3Eliminated) return; break;
            case 4: if (player4 == null || player4Eliminated) return; break;
        }

        switch (type) {
            case BOMB_RANGE:
                switch (playerNumber) {
                    case 1: player1BombRange = Math.min(player1BombRange + 1, 5); break;
                    case 2: player2BombRange = Math.min(player2BombRange + 1, 5); break;
                    case 3: player3BombRange = Math.min(player3BombRange + 1, 5); break;
                    case 4: player4BombRange = Math.min(player4BombRange + 1, 5); break;
                }
                break;
            case SPEED_BOOST:
                switch (playerNumber) {
                    case 1: player1Speed = Math.min(player1Speed + 1.0, MOVEMENT_SPEED * 2); break;
                    case 2: player2Speed = Math.min(player2Speed + 1.0, MOVEMENT_SPEED * 2); break;
                    case 3: player3Speed = Math.min(player3Speed + 1.0, MOVEMENT_SPEED * 2); break;
                    case 4: player4Speed = Math.min(player4Speed + 1.0, MOVEMENT_SPEED * 2); break;
                }
                break;
            case WALL_PASS:
                switch (playerNumber) {
                    case 1: player1CanPassWalls = true; break;
                    case 2: player2CanPassWalls = true; break;
                    case 3: player3CanPassWalls = true; break;
                    case 4: player4CanPassWalls = true; break;
                }
                break;
            case BOMB_COOLDOWN:
                switch (playerNumber) {
                    case 1: player1BombCooldown = Math.min(player1BombCooldown + 200_000_000L, 400_000_000L); break;
                    case 2: player2BombCooldown = Math.min(player2BombCooldown + 200_000_000L, 400_000_000L); break;
                    case 3: player3BombCooldown = Math.min(player3BombCooldown + 200_000_000L, 400_000_000L); break;
                    case 4: player4BombCooldown = Math.min(player4BombCooldown + 200_000_000L, 400_000_000L); break;
                }
                break;
            case BOMB_PUSH:
                switch (playerNumber) {
                    case 1: player1CanPushBombs = true; break;
                    case 2: player2CanPushBombs = true; break;
                    case 3: player3CanPushBombs = true; break;
                    case 4: player4CanPushBombs = true; break;
                }
                break;
        }
        System.out.println("Joueur " + playerNumber + " a ramassé un power-up: " + type.getLabel());
    }

    private boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    private void placeBomb(Player player, int range) {
        int playerX = player.getX();
        int playerY = player.getY();

        // Vérifier que la position est walkable (pas un mur) ET qu'il n'y a pas déjà une bombe
        if (grid.isWalkable(playerX, playerY) && !hasBombAt(playerX, playerY)) {
            Bomb bomb = new Bomb(playerX, playerY);
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
            // Vérifier collision avec joueur 1 (seulement s'il n'est pas invincible et non éliminé)
            if (!player1Eliminated && player1InvincibilityTimer <= 0 &&
                    explosion.getX() == player1.getX() && explosion.getY() == player1.getY()) {
                handlePlayerDeath(1);
                return;
            }
            // Vérifier collision avec joueur 2 (seulement s'il n'est pas invincible et non éliminé)
            if (!player2Eliminated && player2InvincibilityTimer <= 0 &&
                    explosion.getX() == player2.getX() && explosion.getY() == player2.getY()) {
                handlePlayerDeath(2);
                return;
            }
            // Vérifier collision avec joueur 3 si actif et non éliminé
            if (playerCount >= 3 && player3 != null && !player3Eliminated && player3InvincibilityTimer <= 0 &&
                    explosion.getX() == player3.getX() && explosion.getY() == player3.getY()) {
                handlePlayerDeath(3);
                return;
            }
            // Vérifier collision avec joueur 4 si actif et non éliminé
            if (playerCount >= 4 && player4 != null && !player4Eliminated && player4InvincibilityTimer <= 0 &&
                    explosion.getX() == player4.getX() && explosion.getY() == player4.getY()) {
                handlePlayerDeath(4);
                return;
            }
        }
    }

    private void handlePlayerDeath(int playerNumber) {
        switch (playerNumber) {
            case 1:
                if (player1Eliminated) return; // Éviter la double élimination
                player1Lives--;
                System.out.println("Joueur 1 mort! Vies restantes: " + player1Lives);
                if (player1Lives <= 0) {
                    eliminatePlayer(1);
                    checkGameEnd();
                } else {
                    respawnPlayer(1);
                }
                break;
            case 2:
                if (player2Eliminated) return; // Éviter la double élimination
                player2Lives--;
                System.out.println("Joueur 2 mort! Vies restantes: " + player2Lives);
                if (player2Lives <= 0) {
                    eliminatePlayer(2);
                    checkGameEnd();
                } else {
                    respawnPlayer(2);
                }
                break;
            case 3:
                if (player3Eliminated) return; // Éviter la double élimination
                player3Lives--;
                System.out.println("Joueur 3 mort! Vies restantes: " + player3Lives);
                if (player3Lives <= 0) {
                    eliminatePlayer(3);
                    checkGameEnd();
                } else {
                    respawnPlayer(3);
                }
                break;
            case 4:
                if (player4Eliminated) return; // Éviter la double élimination
                player4Lives--;
                System.out.println("Joueur 4 mort! Vies restantes: " + player4Lives);
                if (player4Lives <= 0) {
                    eliminatePlayer(4);
                    checkGameEnd();
                } else {
                    respawnPlayer(4);
                }
                break;
        }
    }

    private void eliminatePlayer(int playerNumber) {
        switch (playerNumber) {
            case 1:
                player1Eliminated = true;
                isPlayer1Moving = false;
                System.out.println("🔥 Joueur 1 ÉLIMINÉ!");
                break;
            case 2:
                player2Eliminated = true;
                isPlayer2Moving = false;
                System.out.println("🔥 Joueur 2 ÉLIMINÉ!");
                break;
            case 3:
                player3Eliminated = true;
                isPlayer3Moving = false;
                System.out.println("🔥 Joueur 3 ÉLIMINÉ!");
                break;
            case 4:
                player4Eliminated = true;
                isPlayer4Moving = false;
                System.out.println("🔥 Joueur 4 ÉLIMINÉ!");
                break;
        }
    }

    private void checkGameEnd() {
        List<Integer> alivePlayers = new ArrayList<>();

        if (!player1Eliminated && player1Lives > 0) alivePlayers.add(1);
        if (!player2Eliminated && player2Lives > 0) alivePlayers.add(2);
        if (playerCount >= 3 && !player3Eliminated && player3Lives > 0) alivePlayers.add(3);
        if (playerCount >= 4 && !player4Eliminated && player4Lives > 0) alivePlayers.add(4);

        if (alivePlayers.size() <= 1) {
            if (alivePlayers.size() == 1) {
                gameOver("Joueur " + alivePlayers.get(0));
            } else {
                gameOver("Match nul");
            }
        }
    }

    private void respawnPlayer(int playerNumber) {
        Player player;
        int spawnX, spawnY;

        switch (playerNumber) {
            case 1:
                if (player1Eliminated) return;
                player = player1;
                spawnX = player1SpawnX;
                spawnY = player1SpawnY;
                player1InvincibilityTimer = INVINCIBILITY_DURATION;
                break;
            case 2:
                if (player2Eliminated) return;
                player = player2;
                spawnX = player2SpawnX;
                spawnY = player2SpawnY;
                player2InvincibilityTimer = INVINCIBILITY_DURATION;
                break;
            case 3:
                if (player3 == null || player3Eliminated) return;
                player = player3;
                spawnX = player3SpawnX;
                spawnY = player3SpawnY;
                player3InvincibilityTimer = INVINCIBILITY_DURATION;
                break;
            case 4:
                if (player4 == null || player4Eliminated) return;
                player = player4;
                spawnX = player4SpawnX;
                spawnY = player4SpawnY;
                player4InvincibilityTimer = INVINCIBILITY_DURATION;
                break;
            default:
                return;
        }

        // Remettre le joueur à sa position de spawn
        player.setPosition(spawnX, spawnY);
        setPlayerTarget(playerNumber, spawnX, spawnY);
        setPlayerMoving(playerNumber, false);

        // Mettre à jour position visuelle
        switch (playerNumber) {
            case 1:
                player1VisualX = spawnX * TILE_SIZE;
                player1VisualY = spawnY * TILE_SIZE + TIMER_HEIGHT;
                break;
            case 2:
                player2VisualX = spawnX * TILE_SIZE;
                player2VisualY = spawnY * TILE_SIZE + TIMER_HEIGHT;
                break;
            case 3:
                player3VisualX = spawnX * TILE_SIZE;
                player3VisualY = spawnY * TILE_SIZE + TIMER_HEIGHT;
                break;
            case 4:
                player4VisualX = spawnX * TILE_SIZE;
                player4VisualY = spawnY * TILE_SIZE + TIMER_HEIGHT;
                break;
        }
    }

    private void gameOver(String winner) {
        gameRunning = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // Mise à jour des statistiques
        PlayerProfileManager profileManager = PlayerProfileManager.getInstance();
        PlayerProfile profile1 = profileManager.getCurrentProfile();

        if (profile1 != null) {
            profile1.incrementGamesPlayed();
            if ("Joueur 1".equals(winner)) {
                profile1.incrementGamesWon();
            }
            // Sauvegarder les modifications
            profileManager.saveProfiles();
        }

        // Afficher le message de fin
        gc.setFill(new Color(0, 0, 0, 0.8));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.fillText(winner + " gagne !",
                CANVAS_WIDTH / 2 - 100,
                CANVAS_HEIGHT / 2);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        gc.fillText("Appuyez sur ESPACE pour rejouer",
                CANVAS_WIDTH / 2 - 140,
                CANVAS_HEIGHT / 2 + 40);
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

            // Rendu des joueurs (seulement ceux qui ne sont pas éliminés)
            renderPlayers();

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

    private void renderPlayers() {
        Image playerTexture = textureManager.getTexture("player");
        Image player2Texture = textureManager.getTexture("player2");
        Image player3Texture = textureManager.getTexture("player3");
        Image player4Texture = textureManager.getTexture("player4");

        // Couleurs de fallback pour chaque joueur
        Color[] playerColors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};

        // Rendu joueur 1 seulement s'il n'est pas éliminé
        if (!player1Eliminated) {
            renderPlayer(1, player1VisualX, player1VisualY, player1InvincibilityTimer,
                    playerTexture, playerColors[0]);
        }

        // Rendu joueur 2 seulement s'il n'est pas éliminé
        if (!player2Eliminated) {
            renderPlayer(2, player2VisualX, player2VisualY, player2InvincibilityTimer,
                    player2Texture != null ? player2Texture : playerTexture, playerColors[1]);
        }

        // Rendu joueur 3 si actif et non éliminé
        if (playerCount >= 3 && player3 != null && !player3Eliminated) {
            renderPlayer(3, player3VisualX, player3VisualY, player3InvincibilityTimer,
                    player3Texture != null ? player3Texture : playerTexture, playerColors[2]);
        }

        // Rendu joueur 4 si actif et non éliminé
        if (playerCount >= 4 && player4 != null && !player4Eliminated) {
            renderPlayer(4, player4VisualX, player4VisualY, player4InvincibilityTimer,
                    player4Texture != null ? player4Texture : playerTexture, playerColors[3]);
        }
    }

    private void renderPlayer(int playerNum, double visualX, double visualY, int invincibilityTimer,
                              Image texture, Color fallbackColor) {
        // Effet de clignotement si invincible
        boolean shouldRender = invincibilityTimer <= 0 || (invincibilityTimer / 5) % 2 != 0;

        if (shouldRender) {
            if (texture != null) {
                gc.drawImage(texture, visualX, visualY, TILE_SIZE, TILE_SIZE);
            } else {
                // Fallback avec cercle coloré
                if (invincibilityTimer > 0) {
                    // Couleur semi-transparente pendant l'invincibilité
                    gc.setFill(Color.color(fallbackColor.getRed(), fallbackColor.getGreen(),
                            fallbackColor.getBlue(), 0.5));
                } else {
                    gc.setFill(fallbackColor);
                }
                gc.fillOval(visualX + 5, visualY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
            }
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

        // Rendu des informations de joueurs selon le mode
        if (playerCount == 2) {
            renderTwoPlayersInfo();
        } else {
            renderFourPlayersInfo();
        }
    }

    private void renderTwoPlayersInfo() {
        // Section Joueur 1 à gauche
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        if (!player1Eliminated) {
            gc.fillText("Joueur 1", 20, TIMER_HEIGHT / 2 - 5);
            // Vies du joueur 1
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("Vies: " + player1Lives, 20, TIMER_HEIGHT / 2 + 15);
            // Power-ups du joueur 1
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            String p1Powers = "R:" + player1BombRange + " S:" + String.format("%.1f", player1Speed/MOVEMENT_SPEED) +
                    (player1CanPushBombs ? " P" : "");
            gc.fillText(p1Powers, 20, TIMER_HEIGHT / 2 + 30);
        } else {
            gc.setFill(Color.RED);
            gc.fillText("J1: ÉLIMINÉ", 20, TIMER_HEIGHT / 2 + 5);
        }

        // Section Joueur 2 à droite
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        String player2Text = aiMode ? "IA" : "Joueur 2";
        double textWidth = player2Text.length() * 9; // Approximation de la largeur du texte

        if (!player2Eliminated) {
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
        } else {
            gc.setFill(Color.RED);
            gc.fillText("J2: ÉLIMINÉ", CANVAS_WIDTH - 100, TIMER_HEIGHT / 2 + 5);
        }
    }

    private void renderFourPlayersInfo() {
        // Interface compacte pour 4 joueurs
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Joueur 1 (coin gauche)
        if (!player1Eliminated) {
            gc.fillText("J1:" + player1Lives, 10, 20);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 8));
            gc.fillText("R:" + player1BombRange, 10, 35);
        } else {
            gc.setFill(Color.RED);
            gc.fillText("J1: ÉLIMINÉ", 10, 25);
            gc.setFill(Color.WHITE);
        }

        // Joueur 2 (coin droit)
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        if (!player2Eliminated) {
            gc.fillText("J2:" + player2Lives, CANVAS_WIDTH - 50, 20);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 8));
            gc.fillText("R:" + player2BombRange, CANVAS_WIDTH - 50, 35);
        } else {
            gc.setFill(Color.RED);
            gc.fillText("J2: ÉLIMINÉ", CANVAS_WIDTH - 80, 25);
            gc.setFill(Color.WHITE);
        }

        // Joueur 3 si actif
        if (playerCount >= 3 && player3 != null) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            if (!player3Eliminated) {
                gc.fillText("J3:" + player3Lives, 10, 50);
                gc.setFont(Font.font("Arial", FontWeight.NORMAL, 8));
                gc.fillText("R:" + player3BombRange, 10, 65);
            } else {
                gc.setFill(Color.RED);
                gc.fillText("J3: ÉLIMINÉ", 10, 55);
                gc.setFill(Color.WHITE);
            }
        }

        // Joueur 4 si actif
        if (playerCount >= 4 && player4 != null) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            if (!player4Eliminated) {
                gc.fillText("J4:" + player4Lives, CANVAS_WIDTH - 50, 50);
                gc.setFont(Font.font("Arial", FontWeight.NORMAL, 8));
                gc.fillText("R:" + player4BombRange, CANVAS_WIDTH - 50, 65);
            } else {
                gc.setFill(Color.RED);
                gc.fillText("J4: ÉLIMINÉ", CANVAS_WIDTH - 80, 55);
                gc.setFill(Color.WHITE);
            }
        }
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
    public Player getPlayer3() { return player3; }
    public Player getPlayer4() { return player4; }

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

        // Réinitialiser les états d'élimination
        player1Eliminated = false;
        player2Eliminated = false;
        player3Eliminated = false;
        player4Eliminated = false;

        // Réinitialiser les vies et invincibilité de tous les joueurs
        player1Lives = 3;
        player2Lives = 3;
        player1InvincibilityTimer = 0;
        player2InvincibilityTimer = 0;

        if (playerCount >= 3) {
            player3Lives = 3;
            player3InvincibilityTimer = 0;
        }
        if (playerCount >= 4) {
            player4Lives = 3;
            player4InvincibilityTimer = 0;
        }

        // Remettre tous les joueurs à leurs positions initiales
        respawnAllPlayers();

        // Réinitialiser les états de mouvement
        isPlayer1Moving = false;
        isPlayer2Moving = false;
        isPlayer3Moving = false;
        isPlayer4Moving = false;
        lastPlayer1MoveTime = 0;
        lastPlayer2MoveTime = 0;
        lastPlayer3MoveTime = 0;
        lastPlayer4MoveTime = 0;

        gameRunning = true;
        if (gameLoop != null) {
            gameLoop.play();
        }
    }

    private void respawnAllPlayers() {
        // Joueur 1
        if (player1 != null && !player1Eliminated) {
            player1.setPosition(player1SpawnX, player1SpawnY);
            player1TargetX = player1SpawnX;
            player1TargetY = player1SpawnY;
            player1VisualX = player1SpawnX * TILE_SIZE;
            player1VisualY = player1SpawnY * TILE_SIZE + TIMER_HEIGHT;
        }

        // Joueur 2
        if (player2 != null && !player2Eliminated) {
            player2.setPosition(player2SpawnX, player2SpawnY);
            player2TargetX = player2SpawnX;
            player2TargetY = player2SpawnY;
            player2VisualX = player2SpawnX * TILE_SIZE;
            player2VisualY = player2SpawnY * TILE_SIZE + TIMER_HEIGHT;
        }

        // Joueur 3
        if (playerCount >= 3 && player3 != null && !player3Eliminated) {
            player3.setPosition(player3SpawnX, player3SpawnY);
            player3TargetX = player3SpawnX;
            player3TargetY = player3SpawnY;
            player3VisualX = player3SpawnX * TILE_SIZE;
            player3VisualY = player3SpawnY * TILE_SIZE + TIMER_HEIGHT;
        }

        // Joueur 4
        if (playerCount >= 4 && player4 != null && !player4Eliminated) {
            player4.setPosition(player4SpawnX, player4SpawnY);
            player4TargetX = player4SpawnX;
            player4TargetY = player4SpawnY;
            player4VisualX = player4SpawnX * TILE_SIZE;
            player4VisualY = player4SpawnY * TILE_SIZE + TIMER_HEIGHT;
        }
    }
}