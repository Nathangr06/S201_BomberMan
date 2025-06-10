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
    public static final int LEGEND_WIDTH = 300; // Largeur réservée pour la légende
    public static final int CANVAS_WIDTH = GRID_WIDTH * TILE_SIZE + LEGEND_WIDTH;
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
    private List<PowerUp> powerUps;
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
    private static final int MAX_LIVES = 3;

    // Positions de spawn initiales
    private int player1SpawnX = 1;
    private int player1SpawnY = 1;
    private int player2SpawnX = 13;
    private int player2SpawnY = 11;

    // Variables pour la respawn temporaire
    private boolean player1IsRespawning = false;
    private boolean player2IsRespawning = false;
    private long player1RespawnTime = 0;
    private long player2RespawnTime = 0;
    private static final long RESPAWN_DURATION = 2000; // 2 secondes d'invincibilité

    // Stats des joueurs pour les power-ups
    private PlayerStats player1Stats = new PlayerStats();
    private PlayerStats player2Stats = new PlayerStats();

    // Vitesse de déplacement dynamique basée sur les power-ups
    private double currentMovementSpeed1 = MOVEMENT_SPEED;
    private double currentMovementSpeed2 = MOVEMENT_SPEED;

    // Variables pour le mouvement fluide des joueurs
    private boolean isPlayer1Moving = false;
    private boolean isPlayer2Moving = false;
    private int player1TargetX = 1;
    private int player1TargetY = 1;
    private int player2TargetX = 13;
    private int player2TargetY = 11;


    //Variables pour le bot
    private boolean aiMode = false;
    private AIPlayer aiPlayer;

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

    // Variables pour la légende
    private boolean showLegend = true;
    private long legendToggleTime = 0;
    private static final long LEGEND_TOGGLE_COOLDOWN = 500_000_000; // 500ms

    public enum CellType {
        EMPTY, WALL, DESTRUCTIBLE_WALL, PLAYER_SPAWN, PLAYER2_SPAWN
    }

    public enum PowerUpType {
        FIRE_UP,        // 🔺 Augmente la portée des bombes
        BOMB_UP,        // 💣 Plus de bombes simultanées
        SPEED_UP,       // 🏃‍♂️ Vitesse de déplacement
        REMOTE_CONTROL, // 🎮 Détonateur manuel
        POWER_GLOVE,    // 🤾‍♂️ Lancer des bombes
        KICK,           // 🦵 Pousser des bombes
        BOMB_PASS,      // ➕ Traverser les bombes
        WALL_PASS       // ⬜ Traverser les murs destructibles
    }

    public static class PowerUp {
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

    public static class PlayerStats {
        public int bombRange = 2;      // Portée des bombes
        public int maxBombs = 1;       // Nombre max de bombes
        public double speed = 3.0;     // Vitesse de déplacement
        public boolean hasRemoteControl = false;
        public boolean hasPowerGlove = false;
        public boolean hasKick = false;
        public boolean hasBombPass = false;
        public boolean hasWallPass = false;

        public void reset() {
            bombRange = 2;
            maxBombs = 1;
            speed = 3.0;
            hasRemoteControl = false;
            hasPowerGlove = false;
            hasKick = false;
            hasBombPass = false;
            hasWallPass = false;
        }
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



    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }

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

        // Réinitialiser les vies
        player1Lives = MAX_LIVES;
        player2Lives = MAX_LIVES;
        player1IsRespawning = false;
        player2IsRespawning = false;

        // Réinitialiser les stats des joueurs
        player1Stats.reset();
        player2Stats.reset();
        currentMovementSpeed1 = MOVEMENT_SPEED;
        currentMovementSpeed2 = MOVEMENT_SPEED;

        // Réinitialiser les stats des joueurs
        player1Stats.reset();
        player2Stats.reset();
        currentMovementSpeed1 = MOVEMENT_SPEED;
        currentMovementSpeed2 = MOVEMENT_SPEED;

        bombs = new ArrayList<>();
        explosions = new ArrayList<>();
        powerUps = new ArrayList<>();
        powerUps = new ArrayList<>();
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

        // Réinitialiser les vies
        player1Lives = MAX_LIVES;
        player2Lives = MAX_LIVES;
        player1IsRespawning = false;
        player2IsRespawning = false;
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

        // Mettre à jour les états de respawn
        updateRespawnStates();

        try {
            handleInput();
            updatePlayerMovement();
            updateBombs();
            updateExplosions();
            checkCollisions();
            checkPowerUpCollisions();
        } catch (Exception e) {
            System.err.println("Erreur dans la boucle de jeu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateRespawnStates() {
        long currentTime = System.currentTimeMillis();

        if (player1IsRespawning && currentTime - player1RespawnTime > RESPAWN_DURATION) {
            player1IsRespawning = false;
        }

        if (player2IsRespawning && currentTime - player2RespawnTime > RESPAWN_DURATION) {
            player2IsRespawning = false;
        }
    }

    private void updateTimer() {
        long currentTime = System.currentTimeMillis();
        gameDuration = (currentTime - gameStartTime) / 1000; // Convertir en secondes
    }

    private void handleInput() {
        long currentTime = System.nanoTime();

        // Toggle de la légende avec F1 (supprimé car la légende est maintenant permanente)
        // La légende est maintenant toujours affichée dans l'espace dédié

        // Contrôles Joueur 1 (flèches directionnelles)
        if (!isPlayer1Moving && (currentTime - lastPlayer1MoveTime) > MOVE_COOLDOWN) {
            int newX = player1TargetX;
            int newY = player1TargetY;
            if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.LEFT)) newX--;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.RIGHT)) newX++;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.UP)) newY--;
            else if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.DOWN)) newY++;

            if ((newX != player1TargetX || newY != player1TargetY) &&
                    canPlayerMoveTo(newX, newY, player1Stats) &&
                    !hasBombAt(newX, newY, player1Stats.hasBombPass)) {
                player1TargetX = newX;
                player1TargetY = newY;
                isPlayer1Moving = true;
                lastPlayer1MoveTime = currentTime;
                currentMovementSpeed1 = player1Stats.speed;
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

            if ((newX != player2TargetX || newY != player2TargetY) &&
                    canPlayerMoveTo(newX, newY, player2Stats) &&
                    !hasBombAt(newX, newY, player2Stats.hasBombPass)) {
                player2TargetX = newX;
                player2TargetY = newY;
                isPlayer2Moving = true;
                lastPlayer2MoveTime = currentTime;
                currentMovementSpeed2 = player2Stats.speed;
            }
        }

        // Placement de bombes
        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.ENTER)) {
            placeBomb(player1, player1Stats);
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.ENTER);
        }

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.SPACE)) {
            placeBomb(player2, player2Stats);
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.SPACE);
        }

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.ESCAPE)) {
            stopGame();
            // returnToMainMenu(); // Appeler le menu ici si implémenté
        }
    }

    private boolean canPlayerMoveTo(int x, int y, PlayerStats stats) {
        if (!grid.inBounds(x, y)) return false;

        if (grid.isIndestructibleWall(x, y)) return false;

        if (grid.isDestructibleWall(x, y) && !stats.hasWallPass) return false;

        return true;
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
            if (distance > currentMovementSpeed1 && distance > 0.1) {
                player1VisualX += (deltaX / distance) * currentMovementSpeed1;
                player1VisualY += (deltaY / distance) * currentMovementSpeed1;
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
            if (distance > currentMovementSpeed2 && distance > 0.1) {
                player2VisualX += (deltaX / distance) * currentMovementSpeed2;
                player2VisualY += (deltaY / distance) * currentMovementSpeed2;
            } else {
                // Arrivé à destination
                player2VisualX = targetX;
                player2VisualY = targetY;
                player2.setPosition(player2TargetX, player2TargetY);
                isPlayer2Moving = false;
            }
        }
    }

    private boolean hasBombAt(int x, int y, boolean hasBombPass) {
        if (hasBombPass) return false; // Peut traverser les bombes
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    private boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    private void placeBomb(Player player, PlayerStats stats) {
        // Compter les bombes actuelles du joueur
        long playerBombs = bombs.stream().count(); // Simplification - dans un vrai jeu, on trackrait par joueur

        if (playerBombs < stats.maxBombs && !hasBombAt(player.getX(), player.getY())) {
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
        // Utiliser la portée par défaut (on pourrait améliorer en trackant qui a posé quelle bombe)
        int range = Math.max(player1Stats.bombRange, player2Stats.bombRange);
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
                    if (Math.random() < 0.3) { // 30% de chance
                        spawnRandomPowerUp(x, y);
                    }
                    break;
                }
            }
        }
    }

    private void spawnRandomPowerUp(int x, int y) {
        PowerUpType[] types = PowerUpType.values();
        PowerUpType randomType = types[(int)(Math.random() * types.length)];
        powerUps.add(new PowerUp(x, y, randomType));
    }

    private void checkPowerUpCollisions() {
        // Vérifier collision joueur 1 avec power-ups
        powerUps.removeIf(powerUp -> {
            if (powerUp.getX() == player1.getX() && powerUp.getY() == player1.getY()) {
                applyPowerUp(1, powerUp.getType());
                return true;
            }
            if (powerUp.getX() == player2.getX() && powerUp.getY() == player2.getY()) {
                applyPowerUp(2, powerUp.getType());
                return true;
            }
            return false;
        });
    }

    private void applyPowerUp(int playerNumber, PowerUpType type) {
        PlayerStats stats = (playerNumber == 1) ? player1Stats : player2Stats;
        String playerName = "Joueur " + playerNumber;

        switch (type) {
            case FIRE_UP -> {
                stats.bombRange = Math.min(stats.bombRange + 1, 8); // Max 8
                System.out.println(playerName + " a récupéré Fire Up! Portée: " + stats.bombRange);
            }
            case BOMB_UP -> {
                stats.maxBombs = Math.min(stats.maxBombs + 1, 6); // Max 6
                System.out.println(playerName + " a récupéré Bomb Up! Max bombes: " + stats.maxBombs);
            }
            case SPEED_UP -> {
                stats.speed = Math.min(stats.speed + 1.0, 8.0); // Max 8
                System.out.println(playerName + " a récupéré Speed Up! Vitesse: " + stats.speed);
            }
            case REMOTE_CONTROL -> {
                stats.hasRemoteControl = true;
                System.out.println(playerName + " a récupéré Remote Control!");
            }
            case POWER_GLOVE -> {
                stats.hasPowerGlove = true;
                System.out.println(playerName + " a récupéré Power Glove!");
            }
            case KICK -> {
                stats.hasKick = true;
                System.out.println(playerName + " a récupéré Kick!");
            }
            case BOMB_PASS -> {
                stats.hasBombPass = true;
                System.out.println(playerName + " a récupéré Bomb Pass!");
            }
            case WALL_PASS -> {
                stats.hasWallPass = true;
                System.out.println(playerName + " a récupéré Wall Pass!");
            }
        }
    }

    private void updateExplosions() {
        explosions.removeIf(Explosion::decreaseTimerAndCheck);
    }

    private void checkCollisions() {
        for (Explosion explosion : explosions) {
            // Vérifier collision avec joueur 1 (seulement s'il n'est pas en train de respawn)
            if (!player1IsRespawning && explosion.getX() == player1.getX() && explosion.getY() == player1.getY()) {
                playerHit(1);
                return;
            }
            // Vérifier collision avec joueur 2 (seulement s'il n'est pas en train de respawn)
            if (!player2IsRespawning && explosion.getX() == player2.getX() && explosion.getY() == player2.getY()) {
                playerHit(2);
                return;
            }
        }
    }

    private void playerHit(int playerNumber) {
        if (playerNumber == 1) {
            player1Lives--;
            System.out.println("Joueur 1 touché ! Vies restantes: " + player1Lives);

            if (player1Lives <= 0) {
                gameOver("Joueur 2");
            } else {
                respawnPlayer(1);
            }
        } else {
            player2Lives--;
            System.out.println("Joueur 2 touché ! Vies restantes: " + player2Lives);

            if (player2Lives <= 0) {
                gameOver("Joueur 1");
            } else {
                respawnPlayer(2);
            }
        }
    }

    private void respawnPlayer(int playerNumber) {
        if (playerNumber == 1) {
            // Remettre le joueur 1 à sa position de spawn
            player1.setPosition(player1SpawnX, player1SpawnY);
            player1TargetX = player1SpawnX;
            player1TargetY = player1SpawnY;
            player1VisualX = player1SpawnX * TILE_SIZE;
            player1VisualY = player1SpawnY * TILE_SIZE + TIMER_HEIGHT;
            isPlayer1Moving = false;

            // Activer l'invincibilité temporaire
            player1IsRespawning = true;
            player1RespawnTime = System.currentTimeMillis();

            System.out.println("Joueur 1 respawn à la position (" + player1SpawnX + ", " + player1SpawnY + ")");
        } else {
            // Remettre le joueur 2 à sa position de spawn
            player2.setPosition(player2SpawnX, player2SpawnY);
            player2TargetX = player2SpawnX;
            player2TargetY = player2SpawnY;
            player2VisualX = player2SpawnX * TILE_SIZE;
            player2VisualY = player2SpawnY * TILE_SIZE + TIMER_HEIGHT;
            isPlayer2Moving = false;

            // Activer l'invincibilité temporaire
            player2IsRespawning = true;
            player2RespawnTime = System.currentTimeMillis();

            System.out.println("Joueur 2 respawn à la position (" + player2SpawnX + ", " + player2SpawnY + ")");
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

            // Afficher le timer en haut (seulement sur la zone de jeu)
            renderTimer();

            // Arrière-plan du jeu (zone verte pour le terrain de jeu uniquement)
            gc.setFill(Color.GREEN);
            gc.fillRect(0, TIMER_HEIGHT, GRID_WIDTH * TILE_SIZE, CANVAS_HEIGHT - TIMER_HEIGHT);

            // Arrière-plan de la zone légende (gris foncé)
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(GRID_WIDTH * TILE_SIZE, 0, LEGEND_WIDTH, CANVAS_HEIGHT);

            // Ajuster le rendu de la grille avec l'offset du timer
            if (grid != null) {
                gc.save();
                gc.translate(0, TIMER_HEIGHT);
                grid.render(gc);
                gc.restore();
            }

            // Power-ups
            for (PowerUp powerUp : powerUps) {
                int x = powerUp.getX() * TILE_SIZE;
                int y = powerUp.getY() * TILE_SIZE + TIMER_HEIGHT;

                // Couleur selon le type de power-up
                Color powerUpColor = getPowerUpColor(powerUp.getType());
                gc.setFill(powerUpColor);
                gc.fillRect(x + 5, y + 5, TILE_SIZE - 10, TILE_SIZE - 10);

                // Bordure noire
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2);
                gc.strokeRect(x + 5, y + 5, TILE_SIZE - 10, TILE_SIZE - 10);

                // Icône/symbole du power-up
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                String symbol = getPowerUpSymbol(powerUp.getType());
                gc.fillText(symbol, x + TILE_SIZE/2 - 6, y + TILE_SIZE/2 + 4);
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

            // Joueurs avec effet de clignotement si en respawn
            Image playerTexture = textureManager.getTexture("player");
            Image player2Texture = textureManager.getTexture("player2");
            if (playerTexture != null) {
                // Joueur 1 avec clignotement si en respawn
                if (!player1IsRespawning || (System.currentTimeMillis() / 200) % 2 == 0) {
                    gc.drawImage(playerTexture, player1VisualX, player1VisualY, TILE_SIZE, TILE_SIZE);
                }
                // Joueur 2 avec clignotement si en respawn
                if (!player2IsRespawning || (System.currentTimeMillis() / 200) % 2 == 0) {
                    gc.drawImage(player2Texture, player2VisualX, player2VisualY, TILE_SIZE, TILE_SIZE);
                }
            } else {
                // Joueur 1 avec clignotement si en respawn
                if (!player1IsRespawning || (System.currentTimeMillis() / 200) % 2 == 0) {
                    gc.setFill(Color.BLUE);
                    gc.fillOval(player1VisualX + 5, player1VisualY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
                }
                // Joueur 2 avec clignotement si en respawn
                if (!player2IsRespawning || (System.currentTimeMillis() / 200) % 2 == 0) {
                    gc.setFill(Color.RED);
                    gc.fillOval(player2VisualX + 5, player2VisualY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
                }
            }

            // Grille de jeu (ajustée pour le timer et limitée à la zone de jeu)
            gc.setStroke(Color.DARKGREEN);
            gc.setLineWidth(1);
            for (int x = 0; x <= GRID_WIDTH; x++) {
                gc.strokeLine(x * TILE_SIZE, TIMER_HEIGHT, x * TILE_SIZE, CANVAS_HEIGHT);
            }
            for (int y = 0; y <= GRID_HEIGHT; y++) {
                gc.strokeLine(0, y * TILE_SIZE + TIMER_HEIGHT, GRID_WIDTH * TILE_SIZE, y * TILE_SIZE + TIMER_HEIGHT);
            }

            // Bordure séparant le jeu de la légende
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);
            gc.strokeLine(GRID_WIDTH * TILE_SIZE, 0, GRID_WIDTH * TILE_SIZE, CANVAS_HEIGHT);

            // Afficher la légende (maintenant dans l'espace dédié)
            renderLegend();

        } catch (Exception e) {
            System.err.println("Erreur lors du rendu: " + e.getMessage());
        }
    }

    private void renderLegend() {
        // Position et dimensions de la légende (dans l'espace dédié à droite)
        double legendX = GRID_WIDTH * TILE_SIZE + 10;
        double legendY = 10;
        double legendWidth = LEGEND_WIDTH - 20;
        double legendHeight = CANVAS_HEIGHT - 20;

        // Fond semi-transparent noir
        gc.setFill(Color.rgb(0, 0, 0, 0.85));
        gc.fillRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);

        // Bordure dorée
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.strokeRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);

        // Titre de la légende
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("GUIDE DE JEU", legendX + 10, legendY + 25);

        double currentY = legendY + 50;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Section Contrôles
        gc.setFill(Color.CYAN);
        gc.fillText("CONTRÔLES:", legendX + 10, currentY);
        currentY += 20;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        gc.fillText("Joueur 1 (Bleu):", legendX + 15, currentY);
        currentY += 15;
        gc.fillText("  Déplacement: ↑ ↓ ← →", legendX + 20, currentY);
        currentY += 15;
        gc.fillText("  Bombe: ENTRÉE", legendX + 20, currentY);
        currentY += 20;

        gc.fillText("Joueur 2 (Rouge):", legendX + 15, currentY);
        currentY += 15;
        gc.fillText("  Déplacement: Z Q S D", legendX + 20, currentY);
        currentY += 15;
        gc.fillText("  Bombe: ESPACE", legendX + 20, currentY);
        currentY += 20;

        gc.fillText("Autres:", legendX + 15, currentY);
        currentY += 15;
        gc.fillText("  F1: Masquer cette aide", legendX + 20, currentY);
        currentY += 15;
        gc.fillText("  ESC: Quitter le jeu", legendX + 20, currentY);
        currentY += 25;

        // Section Power-ups
        gc.setFill(Color.CYAN);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("POWER-UPS:", legendX + 10, currentY);
        currentY += 20;

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));

        // Fire Up
        gc.setFill(Color.ORANGE);
        gc.fillRect(legendX + 15, currentY - 12, 12, 12);
        gc.setFill(Color.WHITE);
        gc.fillText("🔥", legendX + 17, currentY - 2);
        gc.fillText("Fire Up - Portée +1", legendX + 35, currentY);
        currentY += 15;

        // Bomb Up
        gc.setFill(Color.DARKRED);
        gc.fillRect(legendX + 15, currentY - 12, 12, 12);
        gc.setFill(Color.WHITE);
        gc.fillText("💣", legendX + 17, currentY - 2);
        gc.fillText("Bomb Up - +1 bombe", legendX + 35, currentY);
        currentY += 15;

        // Speed Up
        gc.setFill(Color.YELLOW);
        gc.fillRect(legendX + 15, currentY - 12, 12, 12);
        gc.setFill(Color.BLACK);
        gc.fillText("⚡", legendX + 17, currentY - 2);
        gc.setFill(Color.WHITE);
        gc.fillText("Speed Up - Vitesse +1", legendX + 35, currentY);
        currentY += 15;

        // Remote Control
        gc.setFill(Color.PURPLE);
        gc.fillRect(legendX + 15, currentY - 12, 12, 12);
        gc.setFill(Color.WHITE);
        gc.fillText("📱", legendX + 17, currentY - 2);
        gc.fillText("Remote - Détonateur", legendX + 35, currentY);
        currentY += 15;

        // Power Glove
        gc.setFill(Color.BROWN);
        gc.fillRect(legendX + 15, currentY - 12, 12, 12);
        gc.setFill(Color.WHITE);
        gc.fillText("🧤", legendX + 17, currentY - 2);
        gc.fillText("Power Glove - Lancer", legendX + 35, currentY);
        currentY += 15;

        // Kick
        gc.setFill(Color.GREEN);
        gc.fillRect(legendX + 15, currentY - 12, 12, 12);
        gc.setFill(Color.WHITE);
        gc.fillText("🦵", legendX + 17, currentY - 2);
        gc.fillText("Kick - Pousser bombes", legendX + 35, currentY);
        currentY += 15;

        // Bomb Pass
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(legendX + 15, currentY - 12, 12, 12);
        gc.setFill(Color.BLACK);
        gc.fillText("➕", legendX + 17, currentY - 2);
        gc.setFill(Color.WHITE);
        gc.fillText("Bomb Pass - Traverser", legendX + 35, currentY);
        currentY += 15;

        // Wall Pass
        gc.setFill(Color.PINK);
        gc.fillRect(legendX + 15, currentY - 12, 12, 12);
        gc.setFill(Color.BLACK);
        gc.fillText("⬜", legendX + 17, currentY - 2);
        gc.setFill(Color.WHITE);
        gc.fillText("Wall Pass - Murs OK", legendX + 35, currentY);
        currentY += 25;

        // Section Règles
        gc.setFill(Color.CYAN);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("RÈGLES:", legendX + 10, currentY);
        currentY += 18;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        gc.fillText("• Détruisez les murs pour", legendX + 15, currentY);
        currentY += 12;
        gc.fillText("  révéler des power-ups", legendX + 17, currentY);
        currentY += 15;
        gc.fillText("• Évitez vos explosions", legendX + 15, currentY);
        currentY += 15;
        gc.fillText("• 3 vies par joueur", legendX + 15, currentY);
        currentY += 15;
        gc.fillText("• Éliminez l'adversaire", legendX + 15, currentY);
        currentY += 15;
        gc.fillText("• 2 sec d'invincibilité", legendX + 15, currentY);
        currentY += 12;
        gc.fillText("  après respawn", legendX + 17, currentY);
        gc.fillText("➕", legendX + 17, currentY - 2);
        gc.setFill(Color.WHITE);
        gc.fillText("Bomb Pass - Traverser bombes", legendX + 35, currentY);
    currentY += 15;

    // Wall Pass
        gc.setFill(Color.PINK);
        gc.fillRect(legendX + 15, currentY - 12, 12, 12);
        gc.setFill(Color.BLACK);
        gc.fillText("⬜", legendX + 17, currentY - 2);
        gc.setFill(Color.WHITE);
        gc.fillText("Wall Pass - Traverser murs", legendX + 35, currentY);
}

private void renderTimer() {
    // Arrière-plan orange style Bomberman (seulement sur la zone de jeu)
    gc.setFill(Color.web("#FF8C00")); // Orange vif
    gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, TIMER_HEIGHT);

    // Bordure noire
    gc.setStroke(Color.BLACK);
    gc.setLineWidth(2);
    gc.strokeRect(0, 0, GRID_WIDTH * TILE_SIZE, TIMER_HEIGHT);

    // Dimensions pour les éléments (ajustées à la nouvelle largeur)
    double gameAreaWidth = GRID_WIDTH * TILE_SIZE;
    double sectionWidth = gameAreaWidth / 5.0; // 5 sections : J1, Score1, Timer, Score2, J2

    // Section Joueur 1 (gauche)
    renderPlayerText(20, "JOUEUR 1", Color.BLUE);

    // Score Joueur 1 (maintenant les vies)
    renderLifeBox(sectionWidth + 10, player1Lives);

    // Timer au centre
    double timerBoxWidth = 80;
    double timerBoxHeight = 30;
    renderTimerBox((gameAreaWidth - timerBoxWidth) / 2, (TIMER_HEIGHT - timerBoxHeight) / 2, timerBoxWidth, timerBoxHeight);

    // Score Joueur 2 (maintenant les vies)
    double scoreBoxWidth = 50;
    renderLifeBox(gameAreaWidth - sectionWidth - scoreBoxWidth - 10, player2Lives);

    // Section Joueur 2 (droite)
    renderPlayerText(gameAreaWidth - 120, "JOUEUR 2", Color.RED);
}

private void renderPlayerText(double x, String playerText, Color playerColor) {
    double y = TIMER_HEIGHT / 2;

    // Texte du joueur
    gc.setFill(playerColor);
    gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    gc.fillText(playerText, x, y + 4);

    // Contour noir pour le texte (effet rétro)
    gc.setStroke(Color.BLACK);
    gc.setLineWidth(1);
    gc.strokeText(playerText, x, y + 4);
}

private void renderLifeBox(double x, int lives) {
    double boxY = (TIMER_HEIGHT - 25) / 2;
    double boxWidth = 50;
    double boxHeight = 25;

    // Fond noir pour les vies
    gc.setFill(Color.BLACK);
    gc.fillRect(x, boxY, boxWidth, boxHeight);

    // Bordure blanche
    gc.setStroke(Color.WHITE);
    gc.setLineWidth(2);
    gc.strokeRect(x, boxY, boxWidth, boxHeight);

    // Texte des vies
    gc.setFill(Color.WHITE);
    gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    String livesText = String.valueOf(lives);

    // Centrer le texte dans la boîte
    double textX = x + (boxWidth - livesText.length() * 8) / 2;
    double textY = boxY + boxHeight / 2 + 6;
    gc.fillText(livesText, textX, textY);
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
    powerUps.clear();

    // Réinitialiser le timer
    gameStartTime = System.currentTimeMillis();
    gameDuration = 0;

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

    // Réinitialiser les vies et états de respawn
    player1Lives = MAX_LIVES;
    player2Lives = MAX_LIVES;
    player1IsRespawning = false;
    player2IsRespawning = false;

    // Réinitialiser les stats des joueurs
    player1Stats.reset();
    player2Stats.reset();

    isPlayer1Moving = false;
    isPlayer2Moving = false;
    lastPlayer1MoveTime = 0;
    lastPlayer2MoveTime = 0;

    currentMovementSpeed1 = player1Stats.speed;
    currentMovementSpeed2 = player2Stats.speed;

    gameRunning = true;
    if (gameLoop != null) {
        gameLoop.play();
    }
}

private Color getPowerUpColor(PowerUpType type) {
    return switch (type) {
        case FIRE_UP -> Color.ORANGE;
        case BOMB_UP -> Color.DARKRED;
        case SPEED_UP -> Color.YELLOW;
        case REMOTE_CONTROL -> Color.PURPLE;
        case POWER_GLOVE -> Color.BROWN;
        case KICK -> Color.GREEN;
        case BOMB_PASS -> Color.LIGHTBLUE;
        case WALL_PASS -> Color.PINK;
    };
}

private String getPowerUpSymbol(PowerUpType type) {
    return switch (type) {
        case FIRE_UP -> "🔥";
        case BOMB_UP -> "💣";
        case SPEED_UP -> "⚡";
        case REMOTE_CONTROL -> "📱";
        case POWER_GLOVE -> "🧤";
        case KICK -> "🦵";
        case BOMB_PASS -> "➕";
        case WALL_PASS -> "⬜";
    };
}
}