package bomberman.model.game;

import bomberman.model.game.GameManager;
import bomberman.utils.GameConstants;
import bomberman.controller.game.GameInputManager;
import bomberman.controller.game.GameRenderer;
import bomberman.controller.game.TextureManager;
import bomberman.controller.menu.InputHandler;
import bomberman.model.entities.GamePlayer;
import bomberman.model.entities.Player;
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

/**
 * Classe principale du jeu Bomberman.
 * Cette classe orchestre le fonctionnement complet du jeu en coordonnant
 * tous les systèmes (rendu, entrées, logique de jeu) et en gérant le cycle
 * de vie de la partie. Elle sert de point d'entrée pour lancer une session
 * de jeu avec support de 2 à 4 joueurs et de cartes personnalisées.
 *
 * <p>Responsabilités principales :</p>
 * <ul>
 *   <li>Orchestration de tous les systèmes de jeu</li>
 *   <li>Gestion de la boucle de jeu à 60 FPS</li>
 *   <li>Interface entre l'interface utilisateur et la logique de jeu</li>
 *   <li>Chargement et validation des niveaux personnalisés</li>
 *   <li>Configuration multi-joueurs (2-4 joueurs)</li>
 *   <li>Gestion des entrées clavier pour tous les joueurs</li>
 * </ul>
 *
 * <p>Architecture du système :</p>
 * <pre>
 * BombermanGame (Chef d'orchestre)
 * ├── GameManager (Logique de jeu)
 * ├── GameRenderer (Affichage)
 * ├── GameInputManager (Entrées)
 * ├── TextureManager (Ressources visuelles)
 * └── InputHandler (Capture clavier)
 * </pre>
 *
 * <p>Cycle de vie d'une partie :</p>
 * <ol>
 *   <li>Configuration (nombre de joueurs, niveau)</li>
 *   <li>Initialisation des systèmes</li>
 *   <li>Démarrage de la boucle de jeu</li>
 *   <li>Exécution jusqu'à condition de fin</li>
 *   <li>Affichage du résultat</li>
 * </ol>
 *
 * <p>Contrôles multi-joueurs :</p>
 * <ul>
 *   <li><strong>Joueur 1</strong> : Flèches + Entrée</li>
 *   <li><strong>Joueur 2</strong> : ZQSD + Espace</li>
 *   <li><strong>Joueur 3</strong> : IJKL + U</li>
 *   <li><strong>Joueur 4</strong> : Pavé numérique</li>
 * </ul>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class BombermanGame {

    // ==================== COMPOSANTS JAVAFX ====================

    /** Canvas de rendu du jeu */
    private Canvas canvas;

    /** Contexte graphique pour le dessin */
    private GraphicsContext gc;

    /** Timeline pour la boucle de jeu principale */
    private Timeline gameLoop;

    /** Fenêtre de jeu JavaFX */
    private Stage gameStage;

    // ==================== SYSTÈMES PRINCIPAUX ====================

    /** Gestionnaire de la logique de jeu */
    private GameManager gameManager;

    /** Moteur de rendu pour l'affichage */
    private GameRenderer gameRenderer;

    /** Gestionnaire des entrées de jeu */
    private GameInputManager inputManager;

    /** Gestionnaire des textures et sprites */
    private TextureManager textureManager;

    /** Gestionnaire des événements clavier */
    private InputHandler inputHandler;

    // ==================== CONFIGURATION ====================

    /** Nombre de joueurs dans la partie (2-4) */
    private int playerCount = 2;

    /**
     * Constructeur par défaut du jeu Bomberman.
     * Initialise une instance de jeu avec la configuration par défaut.
     * Le nombre de joueurs est initialisé à 2 et peut être modifié
     * avant le démarrage avec {@link #setPlayerCount(int)}.
     */
    public BombermanGame() {}

    /**
     * Définit le nombre de joueurs pour la partie.
     * Valide et ajuste automatiquement la valeur entre 2 et 4 joueurs.
     * Cette méthode doit être appelée avant {@link #startGame(Stage)}
     * pour prendre effet.
     *
     * @param count Le nombre de joueurs souhaité (sera contraint entre 2 et 4)
     */
    public void setPlayerCount(int count) {
        this.playerCount = Math.max(2, Math.min(4, count));
    }

    /**
     * Retourne le nombre de joueurs configuré pour la partie.
     *
     * @return Le nombre de joueurs (entre 2 et 4)
     */
    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * Démarre une nouvelle partie avec un niveau par défaut.
     * Surcharge de convénience qui appelle {@link #startGame(Stage, File)}
     * avec un niveau null pour utiliser la génération automatique.
     *
     * @param stage La fenêtre JavaFX pour afficher le jeu
     */
    public void startGame(Stage stage) {
        startGame(stage, null);
    }

    /**
     * Démarre une nouvelle partie avec un niveau optionnel.
     * Initialise tous les systèmes de jeu, charge le niveau spécifié
     * (ou génère un niveau par défaut), configure l'interface utilisateur
     * et lance la boucle de jeu principale.
     *
     * @param stage La fenêtre JavaFX pour afficher le jeu
     * @param levelFile Le fichier de niveau personnalisé (.bmn) ou null pour le niveau par défaut
     */
    public void startGame(Stage stage, File levelFile) {
        this.gameStage = stage;

        // Initialiser les systèmes
        textureManager = new TextureManager();
        gameManager = new GameManager(playerCount);

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

    /**
     * Crée un niveau par défaut avec génération procédurale.
     * Utilise l'algorithme de génération intégré pour créer
     * un niveau équilibré avec des murs destructibles aléatoires.
     *
     * @return Une nouvelle grille de jeu générée procéduralement
     */
    private GameGrid createDefaultLevel() {
        GameGrid grid = new GameGrid(GameConstants.GRID_WIDTH, GameConstants.GRID_HEIGHT);
        grid.generate();
        return grid;
    }

    /**
     * Charge un niveau personnalisé depuis un fichier.
     * Parse un fichier .bmn créé par l'éditeur de niveaux et reconstruit
     * la grille selon les spécifications. En cas d'erreur, utilise un niveau par défaut.
     *
     * <p>Format du fichier attendu :</p>
     * <pre>
     * Ligne 1: hauteur,largeur
     * Lignes suivantes: valeurs séparées par virgules (un type par case)
     * </pre>
     *
     * @param levelFile Le fichier .bmn à charger
     * @return La grille chargée ou un niveau par défaut en cas d'erreur
     */
    private GameGrid loadCustomLevel(File levelFile) {
        GameGrid grid = new GameGrid(GameConstants.GRID_WIDTH, GameConstants.GRID_HEIGHT);

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String sizeStr = reader.readLine();
            String[] sizeParts = sizeStr.split(",");
            int rows = Integer.parseInt(sizeParts[0]);
            int cols = Integer.parseInt(sizeParts[1]);

            // Validation des dimensions
            if (rows != GameConstants.GRID_HEIGHT || cols != GameConstants.GRID_WIDTH) {
                throw new IOException("Taille de grille incompatible !");
            }

            // Initialiser la grille vide
            for (int i = 0; i < GameConstants.GRID_HEIGHT; i++) {
                for (int j = 0; j < GameConstants.GRID_WIDTH; j++) {
                    grid.setEmpty(j, i);
                }
            }

            // Charger le niveau ligne par ligne
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

    /**
     * Énumération des types de cellules pour le chargement de niveaux.
     * Correspondance avec les valeurs sauvegardées par l'éditeur de niveaux.
     */
    public enum CellType {
        /** Case vide (praticable) */
        EMPTY,

        /** Mur indestructible */
        WALL,

        /** Mur destructible */
        DESTRUCTIBLE_WALL,

        /** Position de spawn du joueur 1 */
        PLAYER_SPAWN,

        /** Position de spawn du joueur 2 */
        PLAYER2_SPAWN
    }

    /**
     * Configure l'interface utilisateur JavaFX.
     * Initialise le canvas, le renderer, les gestionnaires d'entrées
     * et configure la fenêtre de jeu avec le titre approprié.
     *
     * @param stage La fenêtre JavaFX à configurer
     */
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

        gameStage.setScene(scene);

        // Configurer le titre selon le nombre de joueurs
        String title = "Bomberman - ";
        if (playerCount == 4) {
            title += "4 Joueurs";
        } else {
            title += "2 Joueurs";
        }
        gameStage.setTitle(title);

        gameStage.setResizable(false);
        gameStage.show();
        canvas.requestFocus();
    }

    /**
     * Démarre la boucle de jeu principale.
     * Configure une Timeline JavaFX qui s'exécute à 60 FPS (16ms par frame)
     * pour assurer un gameplay fluide et responsive.
     */
    private void startGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            update();
            render();
        }));
        gameLoop.setCycleCount(Animation.INDEFINITE);
        gameLoop.play();
    }

    /**
     * Met à jour l'état du jeu pour une frame.
     * Traite les entrées utilisateur et met à jour la logique de jeu
     * si la partie est en cours. Gère les exceptions pour éviter les crashes.
     */
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

    /**
     * Gère toutes les entrées utilisateur pour une frame.
     * Traite l'échappement pour quitter le jeu et délègue la gestion
     * des entrées spécifiques à chaque joueur actif.
     */
    private void handleInput() {
        long currentTime = System.nanoTime();

        // Gérer l'échappement pour quitter
        if (inputManager.isEscapePressed()) {
            stopGame();
            return;
        }

        // Gérer les entrées de tous les joueurs actifs
        for (GamePlayer player : gameManager.getPlayers()) {
            if (!player.getStats().isEliminated()) {
                handlePlayerInput(player, currentTime);
            }
        }
    }

    /**
     * Gère les entrées spécifiques à un joueur.
     * Traite les commandes de mouvement et de placement de bombes
     * selon le mapping de touches du joueur et les contraintes de jeu.
     *
     * @param player Le joueur dont il faut traiter les entrées
     * @param currentTime Le timestamp actuel pour les cooldowns
     */
    private void handlePlayerInput(GamePlayer player, long currentTime) {
        // Gestion du mouvement
        if (player.canMoveNow(currentTime)) {
            int newX = player.getTargetX();
            int newY = player.getTargetY();

            int playerNum = player.getPlayerNumber();

            // Vérifier les touches directionnelles
            if (isPlayerKeyPressed(playerNum, "LEFT")) newX--;
            else if (isPlayerKeyPressed(playerNum, "RIGHT")) newX++;
            else if (isPlayerKeyPressed(playerNum, "UP")) newY--;
            else if (isPlayerKeyPressed(playerNum, "DOWN")) newY++;

            // Appliquer le mouvement si valide
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

    /**
     * Vérifie si une touche spécifique d'un joueur est pressée.
     *
     * @param playerNum Le numéro du joueur (1-4)
     * @param action L'action à vérifier ("LEFT", "RIGHT", "UP", "DOWN", "BOMB")
     * @return true si la touche correspondante est pressée
     */
    private boolean isPlayerKeyPressed(int playerNum, String action) {
        javafx.scene.input.KeyCode key = getPlayerKey(playerNum, action);
        return inputHandler.isKeyPressed(key);
    }

    /**
     * Force la libération d'une touche spécifique d'un joueur.
     * Utilisé pour éviter les répétitions d'actions comme le placement de bombes.
     *
     * @param playerNum Le numéro du joueur (1-4)
     * @param action L'action à libérer ("BOMB" principalement)
     */
    private void releasePlayerKey(int playerNum, String action) {
        javafx.scene.input.KeyCode key = getPlayerKey(playerNum, action);
        inputHandler.setKeyReleased(key);
    }

    /**
     * Retourne la touche clavier associée à une action pour un joueur donné.
     * Implémente le mapping de touches multi-joueurs avec des schémas
     * de contrôle distincts pour éviter les conflits.
     *
     * <p>Mappings des joueurs :</p>
     * <ul>
     *   <li><strong>Joueur 1</strong> : Flèches directionnelles + Entrée</li>
     *   <li><strong>Joueur 2</strong> : ZQSD + Espace</li>
     *   <li><strong>Joueur 3</strong> : IJKL + U</li>
     *   <li><strong>Joueur 4</strong> : Pavé numérique 8456 + 0</li>
     * </ul>
     *
     * @param playerNum Le numéro du joueur (1-4)
     * @param action L'action demandée ("LEFT", "RIGHT", "UP", "DOWN", "BOMB")
     * @return Le KeyCode correspondant ou null si non trouvé
     */
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

    /**
     * Effectue le rendu d'une frame du jeu.
     * Délègue au GameRenderer l'affichage du jeu en cours ou
     * de l'écran de fin de partie selon l'état actuel.
     */
    private void render() {
        try {
            if (gameManager.isGameRunning()) {
                gameRenderer.renderGame(
                        gameManager.getGrid(),
                        gameManager.getPlayers(),
                        gameManager.getBombSystem(),
                        gameManager.getPowerUpSystem(),
                        gameManager.getGameTimer(),
                        playerCount
                );
            } else {
                // Afficher l'écran de fin de partie
                String winner = gameManager.getWinnerText();
                gameRenderer.renderGameOver(winner);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Arrête la partie en cours.
     * Marque le jeu comme non actif et interrompt la boucle de jeu.
     * Utilisé notamment pour la touche Échap ou la fermeture de fenêtre.
     */
    public void stopGame() {
        gameManager.setGameRunning(false);
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /**
     * Vérifie si une partie est actuellement en cours.
     *
     * @return true si le jeu est en cours d'exécution
     */
    public boolean isGameRunning() {
        return gameManager != null && gameManager.isGameRunning();
    }

    /**
     * Redémarre la partie actuelle.
     * Réinitialise l'état du jeu et relance la boucle si elle était arrêtée.
     */
    public void restartGame() {
        if (gameManager != null) {
            gameManager.restartGame();
            if (gameLoop != null) {
                gameLoop.play();
            }
        }
    }

    // ==================== GETTERS POUR COMPATIBILITÉ ====================

    /**
     * Retourne le joueur 1 pour compatibilité avec l'ancien code.
     *
     * @return L'instance Player du joueur 1 ou null s'il n'existe pas
     */
    public Player getPlayer1() {
        return gameManager.getPlayers().size() > 0 ?
                gameManager.getPlayers().get(0).getPlayer() : null;
    }

    /**
     * Retourne le joueur 2 pour compatibilité avec l'ancien code.
     *
     * @return L'instance Player du joueur 2 ou null s'il n'existe pas
     */
    public Player getPlayer2() {
        return gameManager.getPlayers().size() > 1 ?
                gameManager.getPlayers().get(1).getPlayer() : null;
    }

    /**
     * Retourne le joueur 3 pour compatibilité avec l'ancien code.
     *
     * @return L'instance Player du joueur 3 ou null s'il n'existe pas
     */
    public Player getPlayer3() {
        return gameManager.getPlayers().size() > 2 ?
                gameManager.getPlayers().get(2).getPlayer() : null;
    }

    /**
     * Retourne le joueur 4 pour compatibilité avec l'ancien code.
     *
     * @return L'instance Player du joueur 4 ou null s'il n'existe pas
     */
    public Player getPlayer4() {
        return gameManager.getPlayers().size() > 3 ?
                gameManager.getPlayers().get(3).getPlayer() : null;
    }
}