package bomberman.model.game;

import bomberman.controller.game.TextureManager;
import bomberman.controller.menu.InputHandler;
import bomberman.model.entities.Bomb;
import bomberman.model.entities.Explosion;
import bomberman.model.entities.Flag;
import bomberman.model.entities.Player;
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

/**
 * Mode de jeu Capture the Flag pour Bomberman.
 * Cette application JavaFX implémente une variante du jeu Bomberman où l'objectif
 * n'est plus d'éliminer l'adversaire mais de capturer son drapeau et de le ramener
 * à sa base. Ce mode introduit de nouvelles mécaniques tactiques et stratégiques
 * tout en conservant les éléments de base du gameplay Bomberman.
 *
 * <p>Règles du jeu :</p>
 * <ul>
 *   <li>Chaque joueur a un drapeau à sa position de spawn</li>
 *   <li>Pour gagner : capturer le drapeau adverse ET le ramener à sa base</li>
 *   <li>Les joueurs peuvent toujours placer des bombes même après être morts</li>
 *   <li>Mourir avec le drapeau adverse fait perdre la partie</li>
 *   <li>Les bombes servent à créer des passages et gêner l'adversaire</li>
 * </ul>
 *
 * <p>Mécaniques spéciales :</p>
 * <ul>
 *   <li><strong>Capture</strong> : Se déplacer sur le drapeau adverse</li>
 *   <li><strong>Victoire</strong> : Ramener le drapeau à sa base d'origine</li>
 *   <li><strong>Défaite</strong> : Mourir en portant le drapeau adverse</li>
 *   <li><strong>Indication visuelle</strong> : Carré jaune sur le porteur du drapeau</li>
 * </ul>
 *
 * <p>Contrôles :</p>
 * <ul>
 *   <li><strong>Joueur 1</strong> : Flèches directionnelles + Entrée (bombe)</li>
 *   <li><strong>Joueur 2</strong> : ZQSD + B (bombe)</li>
 * </ul>
 *
 * <p>Différences avec le mode classique :</p>
 * <ul>
 *   <li>Objectif de capture au lieu d'élimination</li>
 *   <li>Possibilité de jouer après la mort (placement de bombes)</li>
 *   <li>Système de drapeaux avec positions fixes</li>
 *   <li>Conditions de victoire alternatives</li>
 * </ul>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class CaptureTheFlag extends Application {

    // ==================== CONSTANTES DE JEU ====================

    /** Taille d'une tuile en pixels */
    public static final int TILE_SIZE = 40;

    /** Largeur de la grille en nombre de tuiles */
    public static final int GRID_WIDTH = 15;

    /** Hauteur de la grille en nombre de tuiles */
    public static final int GRID_HEIGHT = 13;

    /** Largeur totale du canvas en pixels */
    public static final int CANVAS_WIDTH = GRID_WIDTH * TILE_SIZE;

    /** Hauteur totale du canvas en pixels */
    public static final int CANVAS_HEIGHT = GRID_HEIGHT * TILE_SIZE;

    // ==================== COMPOSANTS JAVAFX ====================

    /** Canvas de rendu du jeu */
    private Canvas canvas;

    /** Contexte graphique pour le dessin */
    private GraphicsContext gc;

    /** Boucle de jeu principale */
    private Timeline gameLoop;

    // ==================== ENTITÉS DE JEU ====================

    /** Grille de jeu avec murs et obstacles */
    private GameGrid grid;

    /** Joueur 1 (bleu) */
    private Player player1;

    /** Joueur 2 (rouge) */
    private Player player2;

    /** Liste des bombes actives */
    private List<Bomb> bombs;

    /** Liste des explosions en cours */
    private List<Explosion> explosions;

    /** Gestionnaire de textures pour les sprites */
    private TextureManager textureManager;

    /** Gestionnaire des entrées clavier */
    private InputHandler inputHandler;

    // ==================== SYSTÈME DE MOUVEMENT FLUIDE ====================

    /** Position pixel du joueur 1 pour le rendu fluide */
    private double player1PixelX, player1PixelY;

    /** Position pixel du joueur 2 pour le rendu fluide */
    private double player2PixelX, player2PixelY;

    /** Vitesse de déplacement en pixels par frame */
    private double playerSpeed = 3.0;

    /** État de mouvement des joueurs */
    private boolean isPlayer1Moving = false;
    private boolean isPlayer2Moving = false;

    /** Positions cibles sur la grille */
    private int player1TargetX, player1TargetY;
    private int player2TargetX, player2TargetY;

    // ==================== MÉCANIQUES CAPTURE THE FLAG ====================

    /** Drapeau du joueur 1 (à capturer par le joueur 2) */
    private Flag flag1;

    /** Drapeau du joueur 2 (à capturer par le joueur 1) */
    private Flag flag2;

    /** Indique si le joueur 1 porte le drapeau adverse */
    private boolean player1HasFlag = false;

    /** Indique si le joueur 2 porte le drapeau adverse */
    private boolean player2HasFlag = false;

    /** Active/désactive le mode Capture the Flag */
    private boolean captureTheFlagMode = true;

    /** État de vie des joueurs */
    private boolean player1Alive = true;
    private boolean player2Alive = true;

    /**
     * Point d'entrée principal de l'application JavaFX.
     * Initialise le jeu, configure l'interface utilisateur et démarre la boucle de jeu.
     *
     * @param primaryStage La fenêtre principale de l'application
     */
    @Override
    public void start(Stage primaryStage) {
        initializeGame();

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        VBox root = new VBox(canvas);
        Scene scene = new Scene(root);

        inputHandler = new InputHandler(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Bomberman - Capture the Flag");
        primaryStage.setResizable(false);
        primaryStage.show();

        canvas.requestFocus();

        startGameLoop();
    }

    /**
     * Initialise l'état du jeu pour une nouvelle partie.
     * Configure la grille, positionne les joueurs aux coins opposés,
     * initialise les collections d'entités et place les drapeaux si le mode CTF est activé.
     */
    private void initializeGame() {
        textureManager = TextureManager.getInstance();

        grid = new GameGrid(GRID_WIDTH, GRID_HEIGHT);
        grid.generate();

        // Positionnement initial aux coins opposés
        player1 = new Player(1, 1);
        player2 = new Player(13, 11);

        // Synchronisation des positions pixel et grille
        player1PixelX = player1.getX() * TILE_SIZE;
        player1PixelY = player1.getY() * TILE_SIZE;
        player1TargetX = player1.getX();
        player1TargetY = player1.getY();

        player2PixelX = player2.getX() * TILE_SIZE;
        player2PixelY = player2.getY() * TILE_SIZE;
        player2TargetX = player2.getX();
        player2TargetY = player2.getY();

        // Initialisation des collections d'entités
        bombs = new java.util.ArrayList<>();
        explosions = new java.util.ArrayList<>();

        // Placement des drapeaux en mode CTF
        if (captureTheFlagMode) {
            flag1 = new Flag(player1.getX(), player1.getY());
            flag2 = new Flag(player2.getX(), player2.getY());
        }
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
     * Met à jour l'état complet du jeu pour une frame.
     * Traite les entrées, met à jour les entités et vérifie les conditions
     * spéciales du mode Capture the Flag.
     */
    private void update() {
        handleInput();
        updatePlayerMovement();
        updateBombs();
        updateExplosions();
        checkPlayerExplosionCollision();

        // Mécaniques spécifiques au mode CTF
        if (captureTheFlagMode) {
            // Capture du drapeau adverse
            if (!flag2.isCaptured() && player1.getX() == flag2.getX() && player1.getY() == flag2.getY()) {
                player1HasFlag = true;
                flag2.setCaptured(true);
            }
            if (!flag1.isCaptured() && player2.getX() == flag1.getX() && player2.getY() == flag1.getY()) {
                player2HasFlag = true;
                flag1.setCaptured(true);
            }

            // Conditions de victoire : ramener le drapeau à sa base
            if (player1HasFlag && player1.getX() == flag1.getX() && player1.getY() == flag1.getY() && player1Alive) {
                endGame("Joueur 1 a capturé le drapeau et a gagné !");
            }
            if (player2HasFlag && player2.getX() == flag2.getX() && player2.getY() == flag2.getY() && player2Alive) {
                endGame("Joueur 2 a capturé le drapeau et a gagné !");
            }
        }
    }

    /**
     * Gère les entrées des deux joueurs.
     * Traite les commandes de mouvement pour les joueurs vivants et
     * les commandes de placement de bombes (disponibles même après la mort en mode CTF).
     */
    private void handleInput() {
        // Gestion du mouvement du joueur 1 (si vivant et pas en mouvement)
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

        // Gestion du mouvement du joueur 2 (si vivant et pas en mouvement)
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

        // Placement de bombes (disponible même après la mort en mode CTF)
        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.ENTER)) {
            placeBomb(player1);
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.ENTER);
        }

        if (inputHandler.isKeyPressed(javafx.scene.input.KeyCode.B)) {
            placeBomb(player2);
            inputHandler.setKeyReleased(javafx.scene.input.KeyCode.B);
        }
    }

    /**
     * Met à jour le mouvement fluide des joueurs.
     * Interpole entre la position actuelle et la position cible pour créer
     * un déplacement visuel fluide. Synchronise les positions logiques
     * quand la destination est atteinte.
     */
    private void updatePlayerMovement() {
        // Mouvement du joueur 1
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

        // Mouvement du joueur 2
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

    /**
     * Place une bombe à la position du joueur spécifié.
     * Vérifie qu'aucune bombe n'est déjà présente à cette position
     * avant d'en créer une nouvelle.
     *
     * @param player Le joueur qui place la bombe
     */
    private void placeBomb(Player player) {
        int x = player.getX();
        int y = player.getY();
        if (!hasBombAt(x, y)) {
            bombs.add(new Bomb(x, y));
        }
    }

    /**
     * Vérifie s'il y a une bombe à la position spécifiée.
     *
     * @param x Coordonnée X à vérifier
     * @param y Coordonnée Y à vérifier
     * @return true s'il y a une bombe à cette position
     */
    private boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    /**
     * Met à jour toutes les bombes actives.
     * Décrémente les timers et déclenche les explosions
     * quand le timer atteint zéro.
     */
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

    /**
     * Déclenche l'explosion d'une bombe.
     * Crée une explosion centrale et propage dans les 4 directions
     * avec une portée de 2 cases. Détruit les murs destructibles rencontrés.
     *
     * @param bomb La bombe qui explose
     */
    private void explodeBomb(Bomb bomb) {
        int range = 2;
        explosions.add(new Explosion(bomb.getX(), bomb.getY(), 60));

        // Directions : haut, droite, bas, gauche
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

    /**
     * Met à jour toutes les explosions actives.
     * Supprime automatiquement les explosions dont le timer a expiré.
     */
    private void updateExplosions() {
        explosions.removeIf(Explosion::decreaseTimerAndCheck);
    }

    /**
     * Vérifie les collisions entre joueurs et explosions.
     * Marque un joueur comme mort s'il se trouve sur une explosion.
     * En mode CTF, terminer la partie si un joueur meurt en portant un drapeau.
     */
    private void checkPlayerExplosionCollision() {
        int p1X = player1.getX();
        int p1Y = player1.getY();
        int p2X = player2.getX();
        int p2Y = player2.getY();

        for (Explosion explosion : explosions) {
            int ex = explosion.getX();
            int ey = explosion.getY();

            // Collision joueur 1
            if (player1Alive && ex == p1X && ey == p1Y) {
                player1Alive = false;
                if (player1HasFlag) endGame("Joueur 1 est mort avec le drapeau !");
            }

            // Collision joueur 2
            if (player2Alive && ex == p2X && ey == p2Y) {
                player2Alive = false;
                if (player2HasFlag) endGame("Joueur 2 est mort avec le drapeau !");
            }
        }
    }

    /**
     * Termine la partie avec un message spécifique.
     * Arrête la boucle de jeu et affiche le résultat dans la console.
     *
     * @param message Le message de fin de partie à afficher
     */
    private void endGame(String message) {
        gameLoop.stop();
        System.out.println("Fin de partie : " + message);
    }

    /**
     * Effectue le rendu complet d'une frame du jeu.
     * Dessine tous les éléments visuels dans l'ordre approprié :
     * fond, grille, drapeaux, explosions, bombes, joueurs et indicateurs.
     */
    private void render() {
        // Fond vert foncé
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Rendu de la grille
        grid.render(gc);

        // Affichage des drapeaux si mode CTF activé
        if (captureTheFlagMode) {
            Image flagTextureJ1 = textureManager.getTexture("Flag_J1");
            Image flagTextureJ2 = textureManager.getTexture("Flag_J2");

            // Drapeau du joueur 1 (si pas capturé)
            if (!flag1.isCaptured()) {
                if (flagTextureJ1 != null) {
                    gc.drawImage(flagTextureJ1, flag1.getX() * TILE_SIZE, flag1.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else {
                    gc.setFill(Color.YELLOW);
                    gc.fillRect(flag1.getX() * TILE_SIZE + 10, flag1.getY() * TILE_SIZE + 10, 20, 20);
                }
            }

            // Drapeau du joueur 2 (si pas capturé)
            if (!flag2.isCaptured()) {
                if (flagTextureJ2 != null) {
                    gc.drawImage(flagTextureJ2, flag2.getX() * TILE_SIZE, flag2.getY() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else {
                    gc.setFill(Color.YELLOW);
                    gc.fillRect(flag2.getX() * TILE_SIZE + 10, flag2.getY() * TILE_SIZE + 10, 20, 20);
                }
            }
        }

        // Rendu des explosions
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

        // Rendu des bombes
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

        // Rendu des joueurs
        Image playerTexture = textureManager.getTexture("player");
        Image player2Texture = textureManager.getTexture("player2");
        if (playerTexture != null && player2Texture != null) {
            gc.drawImage(playerTexture, player1PixelX, player1PixelY, TILE_SIZE, TILE_SIZE);
            gc.drawImage(player2Texture, player2PixelX, player2PixelY, TILE_SIZE, TILE_SIZE);
        } else {
            // Fallback sans textures
            gc.setFill(Color.BLUE);
            gc.fillOval(player1PixelX + 5, player1PixelY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
            gc.setFill(Color.RED);
            gc.fillOval(player2PixelX + 5, player2PixelY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        }

        // Indicateurs visuels : carré jaune sur le porteur du drapeau
        if (player1HasFlag && player1Alive) {
            gc.setFill(Color.YELLOW);
            gc.fillRect(player1PixelX + TILE_SIZE / 3, player1PixelY + TILE_SIZE / 3, TILE_SIZE / 3, TILE_SIZE / 3);
        }
        if (player2HasFlag && player2Alive) {
            gc.setFill(Color.YELLOW);
            gc.fillRect(player2PixelX + TILE_SIZE / 3, player2PixelY + TILE_SIZE / 3, TILE_SIZE / 3, TILE_SIZE / 3);
        }
    }

    /**
     * Point d'entrée principal de l'application.
     * Lance l'interface graphique JavaFX.
     *
     * @param args Arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        launch(args);
    }
}