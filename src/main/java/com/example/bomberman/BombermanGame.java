package com.example.bomberman;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

public class BombermanGame {
    private Canvas canvas;
    private GraphicsContext gc;
    private Timeline gameLoop;
    private Stage gameStage;

    // Managers
    private GameStateManager gameStateManager;
    private BombManager bombManager;
    private ExplosionManager explosionManager;
    private MovementController movementController;
    private CollisionManager collisionManager;
    private InputController inputController;
    private GameRenderer gameRenderer;

    // Game objects
    private GameGrid grid;
    private Player player1;
    private Player player2;
    private InputHandler inputHandler;
    private TextureManager textureManager;

    public BombermanGame() {
        gameStateManager = new GameStateManager();
    }

    public void startGame(Stage stage) {
        startGame(stage, null);
    }

    public void startGame(Stage stage, File levelFile) {
        this.gameStage = stage;
        textureManager = new TextureManager();

        initializeGame(levelFile);
        initializeUI(stage);
        initializeControllers();

        startGameLoop();
        gameStateManager.startGame();
    }

    private void initializeGame(File levelFile) {
        try {
            LevelLoader.LevelData levelData;
            if (levelFile != null && levelFile.exists()) {
                levelData = LevelLoader.loadLevel(levelFile);
            } else {
                levelData = LevelLoader.createDefaultLevel();
            }

            grid = levelData.grid;
            player1 = new Player(levelData.player1Spawn.getX(), levelData.player1Spawn.getY(), 1);
            player2 = new Player(levelData.player2Spawn.getX(), levelData.player2Spawn.getY(), 2);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du niveau: " + e.getMessage());
            LevelLoader.LevelData defaultLevel = LevelLoader.createDefaultLevel();
            grid = defaultLevel.grid;
            player1 = new Player(1, 1, 1);
            player2 = new Player(13, 11, 2);
        }
    }

    private void initializeUI(Stage stage) {
        canvas = new Canvas(GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        VBox root = new VBox(canvas);
        Scene scene = new Scene(root);

        inputHandler = new InputHandler(scene);

        stage.setScene(scene);
        stage.setTitle("Bomberman - Jeu 2 Joueurs");
        stage.setResizable(false);
        stage.show();

        canvas.requestFocus();
    }

    private void initializeControllers() {
        explosionManager = new ExplosionManager();
        bombManager = new BombManager(grid, explosionManager);
        movementController = new MovementController(grid, bombManager);
        collisionManager = new CollisionManager(explosionManager);
        inputController = new InputController(inputHandler, movementController, bombManager);
        gameRenderer = new GameRenderer(gc, textureManager);
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
        if (!gameStateManager.isPlaying()) return;

        try {
            inputController.handlePlayerInput(player1, player2);

            if (inputController.isEscapePressed()) {
                stopGame();
                return;
            }

            movementController.updatePlayerMovement(player1);
            movementController.updatePlayerMovement(player2);

            bombManager.update();
            explosionManager.update();

            Player hitPlayer = collisionManager.checkPlayerCollisions(player1, player2);
            if (hitPlayer != null) {
                String winner = (hitPlayer == player1) ? "Joueur 2" : "Joueur 1";
                gameStateManager.gameOver(winner);
                System.out.println(winner + " GAGNE!");
            }

        } catch (Exception e) {
            System.err.println("Erreur dans la boucle de jeu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void render() {
        try {
            gameRenderer.render(grid, player1, player2, bombManager, explosionManager);
        } catch (Exception e) {
            System.err.println("Erreur lors du rendu: " + e.getMessage());
        }
    }

    public void stopGame() {
        gameStateManager.setState(GameStateManager.GameState.MENU);
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public boolean isGameRunning() {
        return gameStateManager.isPlaying();
    }

    public void restartGame() {
        stopGame();

        // Réinitialiser les managers
        bombManager.clear();
        explosionManager.clear();

        // Remettre les joueurs à leurs positions initiales
        player1.setPosition(1, 1);
        player1.setTargetPosition(1, 1);
        player1.setVisualPosition(1 * GameConstants.TILE_SIZE, 1 * GameConstants.TILE_SIZE);
        player1.setMoving(false);

        player2.setPosition(13, 11);
        player2.setTargetPosition(13, 11);
        player2.setVisualPosition(13 * GameConstants.TILE_SIZE, 11 * GameConstants.TILE_SIZE);
        player2.setMoving(false);

        gameStateManager.startGame();
        if (gameLoop != null) {
            gameLoop.play();
        }
    }
}