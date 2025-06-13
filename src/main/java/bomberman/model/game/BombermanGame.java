package bomberman.model.game;

import bomberman.controller.game.GameInputManager;
import bomberman.controller.game.GameRenderer;
import bomberman.controller.menu.InputHandler;
import bomberman.controller.game.TextureManager;
import bomberman.model.entities.GamePlayer;
import bomberman.model.entities.Player;
import bomberman.utils.GameConstants;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;

public class BombermanGame {

    private Canvas canvas;
    private GraphicsContext gc;
    private Timeline gameLoop;
    private Stage gameStage;

    // Systèmes principaux
    private GameManager gameManager;
    private GameRenderer gameRenderer;
    private GameInputManager inputManager;
    private TextureManager textureManager;
    private InputHandler inputHandler;

    // Configuration
    private int playerCount = 2;
    private boolean aiMode = false;

    public BombermanGame() {}

    public void setPlayerCount(int count) {
        this.playerCount = Math.max(2, Math.min(4, count));
    }

    public void setAIMode(boolean aiMode) {
        this.aiMode = aiMode;
    }

    public int getPlayerCount() { return playerCount; }
    public boolean isAIMode() { return aiMode; }

    public void startGame(Stage stage) {
        startGame(stage, null);
    }

    public void startGame(Stage stage, File levelFile) {
        this.gameStage = stage;

        // Initialiser les systèmes
        textureManager = new TextureManager();
        gameManager = new GameManager(playerCount, aiMode, this);

        // Initialiser la grille
        GameGrid grid;
        if (levelFile != null && levelFile.exists()) {
            grid = loadCustomLevel(levelFile);
        } else {
            grid = createDefaultLevel();
        }

        // Initialiser le jeu
        gameManager.initializeGame(grid);

        // Initialiser l'interface
        setupUI(stage);

        // Démarrer la boucle de jeu
        startGameLoop();
    }

    private GameGrid createDefaultLevel() {
        GameGrid grid = new GameGrid(GameConstants.GRID_WIDTH, GameConstants.GRID_HEIGHT);
        grid.generate();
        return grid;
    }

    private GameGrid loadCustomLevel(File levelFile) {
        GameGrid grid = new GameGrid(GameConstants.GRID_WIDTH, GameConstants.GRID_HEIGHT);

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String sizeStr = reader.readLine();
            String[] sizeParts = sizeStr.split(",");
            int rows = Integer.parseInt(sizeParts[0]);
            int cols = Integer.parseInt(sizeParts[1]);

            if (rows != GameConstants.GRID_HEIGHT || cols != GameConstants.GRID_WIDTH) {
                throw new IOException("Taille de grille incompatible !");
            }

            // Initialiser la grille vide
            for (int i = 0; i < GameConstants.GRID_HEIGHT; i++) {
                for (int j = 0; j < GameConstants.GRID_WIDTH; j++) {
                    grid.setEmpty(j, i);
                }
            }

            // Charger le niveau
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
                            // Les positions de spawn sont gérées par GameManager
                        }
                        case PLAYER2_SPAWN -> {
                            grid.setEmpty(j, i);
                            // Les positions de spawn sont gérées par GameManager
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du niveau: " + e.getMessage());
            return createDefaultLevel();
        }

        return grid;
    }

    public enum CellType {
        EMPTY, WALL, DESTRUCTIBLE_WALL, PLAYER_SPAWN, PLAYER2_SPAWN
    }

    private void setupUI(Stage stage) {
        canvas = new Canvas(GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        // Initialiser le renderer
        gameRenderer = new GameRenderer(gc, textureManager);

        // Créer la scène
        VBox root = new VBox(canvas);
        Scene scene = new Scene(root);

        // Initialiser l'input manager
        inputHandler = new InputHandler(scene);
        inputManager = new GameInputManager(inputHandler);

        if (aiMode && gameManager.getAiPlayer() != null) {
            inputManager.setAIPlayer(gameManager.getAiPlayer());
        }

        gameStage.setScene(scene);

        // Configurer le titre
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
        if (!gameManager.isGameRunning()) return;

        try {
            handleInput();
            gameManager.update();
        } catch (Exception e) {
            System.err.println("Erreur dans la boucle de jeu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleInput() {
        long currentTime = System.nanoTime();

        // Gérer l'échappement
        if (inputManager.isEscapePressed()) {
            stopGame();
            return;
        }

        // Gérer les entrées des joueurs
        for (GamePlayer player : gameManager.getPlayers()) {
            if (!player.getStats().isEliminated()) {
                if (player.getPlayerNumber() == 2 && aiMode) {
                    // Gérer l'IA pour le joueur 2
                    inputManager.handleAIInput(player, currentTime, gameManager.getBombSystem());
                } else {
                    // Gérer l'input humain
                    handlePlayerInput(player, currentTime);
                }
            }
        }
    }

    private void handlePlayerInput(GamePlayer player, long currentTime) {
        // Gestion du mouvement
        if (player.canMoveNow(currentTime)) {
            int newX = player.getTargetX();
            int newY = player.getTargetY();

            int playerNum = player.getPlayerNumber();

            if (isPlayerKeyPressed(playerNum, "LEFT")) newX--;
            else if (isPlayerKeyPressed(playerNum, "RIGHT")) newX++;
            else if (isPlayerKeyPressed(playerNum, "UP")) newY--;
            else if (isPlayerKeyPressed(playerNum, "DOWN")) newY++;

            if ((newX != player.getTargetX() || newY != player.getTargetY())) {
                if (gameManager.canPlayerMoveTo(player, newX, newY)) {
                    player.setTarget(newX, newY);
                    player.startMoving(currentTime);
                }
            }
        }

        // Gestion du placement de bombes
        if (isPlayerKeyPressed(player.getPlayerNumber(), "BOMB")) {
            if (player.getStats().canPlaceBomb(currentTime)) {
                gameManager.placeBombForPlayer(player);
                player.getStats().setLastBombTime(currentTime);
                releasePlayerKey(player.getPlayerNumber(), "BOMB");
            }
        }
    }

    private boolean isPlayerKeyPressed(int playerNum, String action) {
        javafx.scene.input.KeyCode key = getPlayerKey(playerNum, action);
        return inputHandler.isKeyPressed(key);
    }

    private void releasePlayerKey(int playerNum, String action) {
        javafx.scene.input.KeyCode key = getPlayerKey(playerNum, action);
        inputHandler.setKeyReleased(key);
    }

    private javafx.scene.input.KeyCode getPlayerKey(int playerNum, String action) {
        switch (playerNum) {
            case 1:
                switch (action) {
                    case "LEFT": return javafx.scene.input.KeyCode.LEFT;
                    case "RIGHT": return javafx.scene.input.KeyCode.RIGHT;
                    case "UP": return javafx.scene.input.KeyCode.UP;
                    case "DOWN": return javafx.scene.input.KeyCode.DOWN;
                    case "BOMB": return javafx.scene.input.KeyCode.ENTER;
                }
                break;
            case 2:
                switch (action) {
                    case "LEFT": return javafx.scene.input.KeyCode.Q;
                    case "RIGHT": return javafx.scene.input.KeyCode.D;
                    case "UP": return javafx.scene.input.KeyCode.Z;
                    case "DOWN": return javafx.scene.input.KeyCode.S;
                    case "BOMB": return javafx.scene.input.KeyCode.SPACE;
                }
                break;
            case 3:
                switch (action) {
                    case "LEFT": return javafx.scene.input.KeyCode.J;
                    case "RIGHT": return javafx.scene.input.KeyCode.L;
                    case "UP": return javafx.scene.input.KeyCode.I;
                    case "DOWN": return javafx.scene.input.KeyCode.K;
                    case "BOMB": return javafx.scene.input.KeyCode.U;
                }
                break;
            case 4:
                switch (action) {
                    case "LEFT": return javafx.scene.input.KeyCode.NUMPAD4;
                    case "RIGHT": return javafx.scene.input.KeyCode.NUMPAD6;
                    case "UP": return javafx.scene.input.KeyCode.NUMPAD8;
                    case "DOWN": return javafx.scene.input.KeyCode.NUMPAD5;
                    case "BOMB": return javafx.scene.input.KeyCode.NUMPAD0;
                }
                break;
        }
        return null;
    }

    private void render() {
        try {
            if (gameManager.isGameRunning()) {
                gameRenderer.renderGame(
                        gameManager.getGrid(),
                        gameManager.getPlayers(),
                        gameManager.getBombSystem(),
                        gameManager.getPowerUpSystem(),
                        gameManager.getGameTimer(),
                        playerCount,
                        aiMode
                );
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void stopGame() {
        gameManager.setGameRunning(false);
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public boolean isGameRunning() {
        return gameManager != null && gameManager.isGameRunning();
    }

    public void restartGame() {
        if (gameManager != null) {
            gameManager.restartGame();
            if (gameLoop != null) {
                gameLoop.play();
            }
        }
    }

    // Getters pour compatibilité avec l'ancien code si nécessaire
    public Player getPlayer1() {
        return gameManager.getPlayers().size() > 0 ?
                gameManager.getPlayers().get(0).getPlayer() : null;
    }

    public Player getPlayer2() {
        return gameManager.getPlayers().size() > 1 ?
                gameManager.getPlayers().get(1).getPlayer() : null;
    }

    public Player getPlayer3() {
        return gameManager.getPlayers().size() > 2 ?
                gameManager.getPlayers().get(2).getPlayer() : null;
    }

    public Player getPlayer4() {
        return gameManager.getPlayers().size() > 3 ?
                gameManager.getPlayers().get(3).getPlayer() : null;
    }
}