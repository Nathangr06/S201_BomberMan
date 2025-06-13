package bomberman.model.ai;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

/**
 * Mode de jeu Bomberman avec intelligence artificielle.
 * Cette application JavaFX implémente un mode de jeu où un joueur humain affronte
 * une IA dans une partie de Bomberman. L'IA utilise des algorithmes de prise de décision
 * basés sur l'analyse de la situation de jeu, la gestion des dangers et l'optimisation
 * des mouvements tactiques.
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Jeu complet Bomberman avec joueur humain vs IA</li>
 *   <li>Intelligence artificielle avec stratégies multiples</li>
 *   <li>Mouvement fluide avec interpolation pixel-perfect</li>
 *   <li>Génération procédurale de niveau</li>
 *   <li>Gestion complète des collisions et explosions</li>
 *   <li>Interface de jeu responsive à 60 FPS</li>
 * </ul>
 *
 * <p>Architecture de l'IA :</p>
 * L'IA fonctionne selon un système de priorités :
 * <ol>
 *   <li><strong>Survie</strong> : Échapper aux explosions imminentes</li>
 *   <li><strong>Stratégie</strong> : Placer des bombes tactiques</li>
 *   <li><strong>Mouvement</strong> : Se positionner intelligemment</li>
 * </ol>
 *
 * <p>Contrôles :</p>
 * <ul>
 *   <li>Flèches directionnelles : Déplacement du joueur</li>
 *   <li>ESPACE : Placer une bombe</li>
 *   <li>R : Redémarrer après game over</li>
 * </ul>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class AIPlayer extends Application {

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

    /** Scène JavaFX pour la gestion des événements */
    private Scene scene;

    // ==================== ÉTAT DU JEU ====================

    /** Grille de jeu contenant les murs et obstacles */
    private GameGrid grid;

    /** Joueur humain */
    private Player humanPlayer;

    /** Joueur IA */
    private Player aiPlayer;

    /** Liste des bombes actives */
    private List<Bomb> bombs;

    /** Liste des explosions en cours */
    private List<Explosion> explosions;

    /** Ensemble des touches actuellement pressées */
    private Set<KeyCode> pressedKeys;

    // ==================== SYSTÈME DE MOUVEMENT FLUIDE ====================

    /** Position pixel X du joueur humain pour le rendu fluide */
    private double humanPixelX, humanPixelY;

    /** Position pixel X de l'IA pour le rendu fluide */
    private double aiPixelX, aiPixelY;

    /** Vitesse de déplacement en pixels par frame */
    private double playerSpeed = 3.0;

    /** Indique si le joueur humain est en mouvement */
    private boolean isHumanMoving = false;

    /** Indique si l'IA est en mouvement */
    private boolean isAIMoving = false;

    /** Position cible du joueur humain sur la grille */
    private int humanTargetX, humanTargetY;

    /** Position cible de l'IA sur la grille */
    private int aiTargetX, aiTargetY;

    // ==================== ÉTAT DES JOUEURS ====================

    /** Indique si le joueur humain est vivant */
    private boolean humanAlive = true;

    /** Indique si l'IA est vivante */
    private boolean aiAlive = true;

    /** Indique si la partie est terminée */
    private boolean gameOver = false;

    /** Résultat de la partie */
    private String gameResult = "";

    // ==================== INTELLIGENCE ARTIFICIELLE ====================

    /** Générateur de nombres aléatoires pour l'IA */
    private Random random = new Random();

    /** Timestamp de la dernière action de l'IA */
    private long lastAIActionTime = 0;

    /** Cooldown entre les actions de l'IA en nanosecondes (200ms) */
    private static final long AI_ACTION_COOLDOWN = 200_000_000;

    /** Indique si l'IA vient de placer une bombe */
    private boolean aiJustPlacedBomb = false;

    /** Position de la dernière bombe placée par l'IA */
    private int lastAIBombX = -1, lastAIBombY = -1;

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
        scene = new Scene(root);

        // Configuration de la gestion des touches
        pressedKeys = new HashSet<>();
        scene.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));

        primaryStage.setScene(scene);
        primaryStage.setTitle("Bomberman - Joueur vs IA");
        primaryStage.setResizable(false);
        primaryStage.show();

        canvas.requestFocus();
        startGameLoop();
    }

    /**
     * Initialise l'état initial du jeu.
     * Crée la grille, positionne les joueurs aux coins opposés,
     * initialise les collections d'entités et remet à zéro l'état de jeu.
     */
    private void initializeGame() {
        grid = new GameGrid();

        // Positionnement initial des joueurs aux coins opposés
        humanPlayer = new Player(1, 1);
        aiPlayer = new Player(13, 11);

        // Synchronisation des positions pixel et grille
        humanPixelX = humanPlayer.x * TILE_SIZE;
        humanPixelY = humanPlayer.y * TILE_SIZE;
        humanTargetX = humanPlayer.x;
        humanTargetY = humanPlayer.y;

        aiPixelX = aiPlayer.x * TILE_SIZE;
        aiPixelY = aiPlayer.y * TILE_SIZE;
        aiTargetX = aiPlayer.x;
        aiTargetY = aiPlayer.y;

        // Initialisation des collections d'entités
        bombs = new ArrayList<>();
        explosions = new ArrayList<>();

        // Remise à zéro de l'état de jeu
        humanAlive = true;
        aiAlive = true;
        gameOver = false;
        gameResult = "";
    }

    /**
     * Démarre la boucle de jeu principale.
     * Configure une Timeline JavaFX qui s'exécute à 60 FPS (16ms par frame)
     * pour mettre à jour et rendre le jeu de manière fluide.
     */
    private void startGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (!gameOver) {
                update();
            }
            render();
        }));
        gameLoop.setCycleCount(Animation.INDEFINITE);
        gameLoop.play();
    }

    /**
     * Met à jour l'état complet du jeu pour une frame.
     * Traite les entrées, l'IA, les mouvements, les bombes, explosions
     * et vérifie les conditions de victoire dans l'ordre approprié.
     */
    private void update() {
        handleHumanInput();
        handleAI();
        updateMovement();
        updateBombs();
        updateExplosions();
        checkCollisions();
        checkWinCondition();
    }

    /**
     * Gère les entrées du joueur humain.
     * Traite les commandes de mouvement (flèches), placement de bombes (ESPACE)
     * et redémarrage (R). Implémente un système de mouvement basé sur des cibles
     * pour un déplacement fluide.
     */
    private void handleHumanInput() {
        if (humanAlive && !isHumanMoving) {
            int newX = humanTargetX;
            int newY = humanTargetY;

            // Détection des touches directionnelles
            if (pressedKeys.contains(KeyCode.LEFT)) newX--;
            else if (pressedKeys.contains(KeyCode.RIGHT)) newX++;
            else if (pressedKeys.contains(KeyCode.UP)) newY--;
            else if (pressedKeys.contains(KeyCode.DOWN)) newY++;

            // Validation et application du mouvement
            if ((newX != humanTargetX || newY != humanTargetY) &&
                    canMoveTo(newX, newY)) {
                humanTargetX = newX;
                humanTargetY = newY;
                isHumanMoving = true;
            }
        }

        // Placement de bombes
        if (pressedKeys.contains(KeyCode.SPACE) && humanAlive) {
            placeBomb(humanPlayer);
            pressedKeys.remove(KeyCode.SPACE);
        }

        // Redémarrage du jeu
        if (pressedKeys.contains(KeyCode.R) && gameOver) {
            restartGame();
            pressedKeys.remove(KeyCode.R);
        }
    }

    /**
     * Gère le comportement de l'intelligence artificielle.
     * Détermine la prochaine action de l'IA basée sur l'analyse de la situation
     * et exécute l'action appropriée (mouvement ou placement de bombe).
     */
    private void handleAI() {
        if (aiAlive && !isAIMoving) {
            AIAction action = getNextAIAction();

            if (action != null) {
                switch (action) {
                    case MOVE_LEFT:
                        if (canMoveTo(aiTargetX - 1, aiTargetY)) {
                            aiTargetX--;
                            isAIMoving = true;
                        }
                        break;
                    case MOVE_RIGHT:
                        if (canMoveTo(aiTargetX + 1, aiTargetY)) {
                            aiTargetX++;
                            isAIMoving = true;
                        }
                        break;
                    case MOVE_UP:
                        if (canMoveTo(aiTargetX, aiTargetY - 1)) {
                            aiTargetY--;
                            isAIMoving = true;
                        }
                        break;
                    case MOVE_DOWN:
                        if (canMoveTo(aiTargetX, aiTargetY + 1)) {
                            aiTargetY++;
                            isAIMoving = true;
                        }
                        break;
                    case PLACE_BOMB:
                        placeBomb(aiPlayer);
                        break;
                }
            }
        }
    }

    /**
     * Détermine la prochaine action de l'IA selon un système de priorités.
     * Analyse la situation actuelle et choisit l'action optimale basée sur
     * la survie, la stratégie et le positionnement tactique.
     *
     * <p>Algorithme de décision par priorité :</p>
     * <ol>
     *   <li>Échapper au danger immédiat (bombes sur le point d'exploser)</li>
     *   <li>Fuir après avoir placé une bombe</li>
     *   <li>Placer une bombe si c'est sûr et utile</li>
     *   <li>Se déplacer intelligemment vers/loin du joueur</li>
     *   <li>Attendre si aucune action n'est possible</li>
     * </ol>
     *
     * @return L'action à exécuter ou null si aucune action n'est possible
     */
    private AIAction getNextAIAction() {
        long currentTime = System.nanoTime();

        // Respecter le cooldown entre les actions
        if (currentTime - lastAIActionTime < AI_ACTION_COOLDOWN) {
            return AIAction.WAIT;
        }

        int x = aiPlayer.x;
        int y = aiPlayer.y;

        // Priorité 1: Échapper si on vient de placer une bombe
        if (aiJustPlacedBomb && hasBombAt(lastAIBombX, lastAIBombY)) {
            AIAction escape = findEscapeRoute(x, y);
            if (escape != null) {
                lastAIActionTime = currentTime;
                return escape;
            }
        } else {
            aiJustPlacedBomb = false;
        }

        // Priorité 2: Échapper au danger immédiat
        if (isInDanger(x, y)) {
            AIAction escape = findEscapeRoute(x, y);
            if (escape != null) {
                lastAIActionTime = currentTime;
                return escape;
            }
        }

        // Priorité 3: Placer une bombe si c'est sûr et utile
        if (canSafelyPlaceBomb(x, y)) {
            aiJustPlacedBomb = true;
            lastAIBombX = x;
            lastAIBombY = y;
            lastAIActionTime = currentTime;
            return AIAction.PLACE_BOMB;
        }

        // Priorité 4: Se déplacer intelligemment
        AIAction move = chooseSmartMove(x, y);
        if (move != null) {
            lastAIActionTime = currentTime;
            return move;
        }

        return AIAction.WAIT;
    }

    /**
     * Détermine si une position est dangereuse pour l'IA.
     * Vérifie la présence d'explosions actuelles et de bombes sur le point
     * d'exploser dans la zone d'effet.
     *
     * @param x Coordonnée X à vérifier
     * @param y Coordonnée Y à vérifier
     * @return true si la position est dangereuse, false sinon
     */
    private boolean isInDanger(int x, int y) {
        // Vérifier les explosions actuelles
        for (Explosion exp : explosions) {
            if (exp.x == x && exp.y == y) return true;
        }

        // Vérifier les bombes dangereuses (timer <= 60 = 1 seconde)
        for (Bomb bomb : bombs) {
            if (bomb.timer <= 60 && isInBlastRange(x, y, bomb.x, bomb.y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si une position est dans la zone d'explosion d'une bombe.
     * Calcule si la position est sur les axes horizontal ou vertical
     * de la bombe dans un rayon de 2 cases.
     *
     * @param x Coordonnée X à vérifier
     * @param y Coordonnée Y à vérifier
     * @param bombX Coordonnée X de la bombe
     * @param bombY Coordonnée Y de la bombe
     * @return true si la position est dans la zone d'explosion
     */
    private boolean isInBlastRange(int x, int y, int bombX, int bombY) {
        if (y == bombY && Math.abs(x - bombX) <= 2) return true;
        if (x == bombX && Math.abs(y - bombY) <= 2) return true;
        return false;
    }

    /**
     * Trouve une route d'évasion sûre depuis une position donnée.
     * Analyse les 4 directions possibles et retourne un mouvement
     * vers une case sûre, ou null si aucune évasion n'est possible.
     *
     * @param x Coordonnée X de départ
     * @param y Coordonnée Y de départ
     * @return Une action de mouvement vers la sécurité ou null
     */
    private AIAction findEscapeRoute(int x, int y) {
        AIAction[] actions = {AIAction.MOVE_LEFT, AIAction.MOVE_RIGHT, AIAction.MOVE_UP, AIAction.MOVE_DOWN};
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        List<AIAction> safeMoves = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (canMoveTo(newX, newY) && !isInDanger(newX, newY)) {
                safeMoves.add(actions[i]);
            }
        }

        return safeMoves.isEmpty() ? null : safeMoves.get(random.nextInt(safeMoves.size()));
    }

    /**
     * Détermine si l'IA peut placer une bombe en toute sécurité.
     * Vérifie qu'il n'y a pas déjà de bombe, que la position n'est pas dangereuse,
     * qu'une route d'évasion existe et que la bombe serait utile.
     *
     * @param x Coordonnée X où placer la bombe
     * @param y Coordonnée Y où placer la bombe
     * @return true si le placement est sûr et utile
     */
    private boolean canSafelyPlaceBomb(int x, int y) {
        if (hasBombAt(x, y) || isInDanger(x, y)) return false;

        // Simuler la bombe et vérifier qu'on peut s'échapper
        List<Bomb> futureBombs = new ArrayList<>(bombs);
        futureBombs.add(new Bomb(x, y));

        return findEscapeRoute(x, y) != null && isBombUseful(x, y);
    }

    /**
     * Évalue l'utilité de placer une bombe à une position donnée.
     * Vérifie la présence de murs destructibles à proximité ou
     * la possibilité d'attaquer le joueur humain.
     *
     * @param x Coordonnée X de la bombe potentielle
     * @param y Coordonnée Y de la bombe potentielle
     * @return true si la bombe serait utile
     */
    private boolean isBombUseful(int x, int y) {
        // Vérifier les murs destructibles dans un rayon de 2 cases
        int[] dx = {-1, 1, 0, 0, -2, 2, 0, 0};
        int[] dy = {0, 0, -1, 1, 0, 0, -2, 2};

        for (int i = 0; i < dx.length; i++) {
            int checkX = x + dx[i];
            int checkY = y + dy[i];
            if (grid.inBounds(checkX, checkY) && grid.isDestructibleWall(checkX, checkY)) {
                return true;
            }
        }

        // Vérifier si le joueur humain est à portée (30% de chance d'attaque)
        if (random.nextDouble() < 0.3) {
            int distance = Math.abs(humanPlayer.x - x) + Math.abs(humanPlayer.y - y);
            if (distance >= 2 && distance <= 4) {
                return isInBlastRange(humanPlayer.x, humanPlayer.y, x, y);
            }
        }
        return false;
    }

    /**
     * Choisit un mouvement intelligent basé sur la stratégie tactique.
     * Implémente une logique adaptative qui fait fuir l'IA quand elle est
     * trop proche du joueur et l'attire quand elle est trop loin.
     *
     * @param x Position X actuelle de l'IA
     * @param y Position Y actuelle de l'IA
     * @return L'action de mouvement choisie ou null
     */
    private AIAction chooseSmartMove(int x, int y) {
        List<AIAction> possibleMoves = getPossibleMoves(x, y);
        if (possibleMoves.isEmpty()) return null;

        // Stratégie basée sur la distance au joueur (60% du temps)
        if (random.nextDouble() < 0.6) {
            int distance = Math.abs(humanPlayer.x - x) + Math.abs(humanPlayer.y - y);

            if (distance <= 2) {
                // Trop proche : fuir
                return moveAwayFromPlayer(x, y, possibleMoves);
            } else if (distance > 5) {
                // Trop loin : se rapprocher
                return moveTowardsPlayer(x, y, possibleMoves);
            }
        }

        // Mouvement aléatoire (40% du temps ou distance moyenne)
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    /**
     * Trouve le mouvement qui rapproche l'IA du joueur humain.
     * Calcule la distance Manhattan pour chaque mouvement possible
     * et sélectionne celui qui minimise la distance.
     *
     * @param x Position X actuelle
     * @param y Position Y actuelle
     * @param moves Liste des mouvements possibles
     * @return Le mouvement qui rapproche le plus du joueur
     */
    private AIAction moveTowardsPlayer(int x, int y, List<AIAction> moves) {
        AIAction best = null;
        int bestDistance = Integer.MAX_VALUE;

        for (AIAction move : moves) {
            int[] newPos = getNewPosition(x, y, move);
            int distance = Math.abs(humanPlayer.x - newPos[0]) + Math.abs(humanPlayer.y - newPos[1]);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = move;
            }
        }
        return best;
    }

    /**
     * Trouve le mouvement qui éloigne l'IA du joueur humain.
     * Calcule la distance Manhattan pour chaque mouvement possible
     * et sélectionne celui qui maximise la distance.
     *
     * @param x Position X actuelle
     * @param y Position Y actuelle
     * @param moves Liste des mouvements possibles
     * @return Le mouvement qui éloigne le plus du joueur
     */
    private AIAction moveAwayFromPlayer(int x, int y, List<AIAction> moves) {
        AIAction best = null;
        int bestDistance = -1;

        for (AIAction move : moves) {
            int[] newPos = getNewPosition(x, y, move);
            int distance = Math.abs(humanPlayer.x - newPos[0]) + Math.abs(humanPlayer.y - newPos[1]);
            if (distance > bestDistance) {
                bestDistance = distance;
                best = move;
            }
        }
        return best;
    }

    /**
     * Calcule la nouvelle position résultant d'une action de mouvement.
     *
     * @param x Coordonnée X actuelle
     * @param y Coordonnée Y actuelle
     * @param action L'action de mouvement à simuler
     * @return Un tableau [newX, newY] avec la nouvelle position
     */
    private int[] getNewPosition(int x, int y, AIAction action) {
        switch (action) {
            case MOVE_LEFT: return new int[]{x - 1, y};
            case MOVE_RIGHT: return new int[]{x + 1, y};
            case MOVE_UP: return new int[]{x, y - 1};
            case MOVE_DOWN: return new int[]{x, y + 1};
            default: return new int[]{x, y};
        }
    }

    /**
     * Retourne la liste des mouvements possibles depuis une position.
     * Filtre les mouvements en vérifiant que la destination est praticable
     * et sûre (pas de danger immédiat).
     *
     * @param x Coordonnée X de départ
     * @param y Coordonnée Y de départ
     * @return Liste des actions de mouvement possibles
     */
    private List<AIAction> getPossibleMoves(int x, int y) {
        List<AIAction> moves = new ArrayList<>();
        AIAction[] actions = {AIAction.MOVE_LEFT, AIAction.MOVE_RIGHT, AIAction.MOVE_UP, AIAction.MOVE_DOWN};
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];
            if (canMoveTo(newX, newY) && !isInDanger(newX, newY)) {
                moves.add(actions[i]);
            }
        }
        return moves;
    }

    /**
     * Met à jour le mouvement fluide des joueurs.
     * Interpole entre la position actuelle et la position cible pour
     * créer un mouvement visuel fluide. Synchronise les positions
     * logiques quand la destination est atteinte.
     */
    private void updateMovement() {
        // Mouvement du joueur humain
        if (isHumanMoving) {
            double targetX = humanTargetX * TILE_SIZE;
            double targetY = humanTargetY * TILE_SIZE;
            double dx = targetX - humanPixelX;
            double dy = targetY - humanPixelY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= playerSpeed) {
                humanPixelX = targetX;
                humanPixelY = targetY;
                humanPlayer.x = humanTargetX;
                humanPlayer.y = humanTargetY;
                isHumanMoving = false;
            } else {
                humanPixelX += (dx / distance) * playerSpeed;
                humanPixelY += (dy / distance) * playerSpeed;
            }
        }

        // Mouvement de l'IA
        if (isAIMoving) {
            double targetX = aiTargetX * TILE_SIZE;
            double targetY = aiTargetY * TILE_SIZE;
            double dx = targetX - aiPixelX;
            double dy = targetY - aiPixelY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= playerSpeed) {
                aiPixelX = targetX;
                aiPixelY = targetY;
                aiPlayer.x = aiTargetX;
                aiPlayer.y = aiTargetY;
                isAIMoving = false;
            } else {
                aiPixelX += (dx / distance) * playerSpeed;
                aiPixelY += (dy / distance) * playerSpeed;
            }
        }
    }

    /**
     * Place une bombe à la position du joueur spécifié.
     * Vérifie qu'il n'y a pas déjà de bombe à cette position
     * avant d'en créer une nouvelle.
     *
     * @param player Le joueur qui place la bombe
     */
    private void placeBomb(Player player) {
        if (!hasBombAt(player.x, player.y)) {
            bombs.add(new Bomb(player.x, player.y));
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
        return bombs.stream().anyMatch(b -> b.x == x && b.y == y);
    }

    /**
     * Vérifie si un déplacement vers une position est possible.
     * Contrôle les limites de la grille, les obstacles et la présence de bombes.
     *
     * @param x Coordonnée X de destination
     * @param y Coordonnée Y de destination
     * @return true si le mouvement est possible
     */
    private boolean canMoveTo(int x, int y) {
        return grid.inBounds(x, y) && grid.isWalkable(x, y) && !hasBombAt(x, y);
    }

    /**
     * Met à jour toutes les bombes actives.
     * Décrémente les timers et déclenche les explosions
     * quand le timer atteint zéro.
     */
    private void updateBombs() {
        bombs.removeIf(bomb -> {
            bomb.timer--;
            if (bomb.timer <= 0) {
                explodeBomb(bomb);
                return true;
            }
            return false;
        });
    }

    /**
     * Déclenche l'explosion d'une bombe.
     * Crée une explosion centrale et propage l'explosion dans les 4 directions
     * jusqu'à une portée de 2 cases ou jusqu'à rencontrer un obstacle.
     * Détruit les murs destructibles sur le passage.
     *
     * @param bomb La bombe qui explose
     */
    private void explodeBomb(Bomb bomb) {
        explosions.add(new Explosion(bomb.x, bomb.y, 60));

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        for (int dir = 0; dir < 4; dir++) {
            for (int i = 1; i <= 2; i++) {
                int x = bomb.x + dx[dir] * i;
                int y = bomb.y + dy[dir] * i;

                if (!grid.inBounds(x, y) || grid.isIndestructibleWall(x, y)) break;

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
     * Décrémente les timers d'explosion et supprime celles qui sont terminées.
     */
    private void updateExplosions() {
        explosions.removeIf(exp -> --exp.timer <= 0);
    }

    /**
     * Vérifie les collisions entre joueurs et explosions.
     * Marque un joueur comme éliminé s'il se trouve sur une case d'explosion.
     */
    private void checkCollisions() {
        for (Explosion exp : explosions) {
            if (humanAlive && exp.x == humanPlayer.x && exp.y == humanPlayer.y) {
                humanAlive = false;
            }
            if (aiAlive && exp.x == aiPlayer.x && exp.y == aiPlayer.y) {
                aiAlive = false;
            }
        }
    }

    /**
     * Vérifie les conditions de victoire et de fin de partie.
     * Détermine le gagnant selon l'état des joueurs et met à jour
     * les variables de fin de partie.
     */
    private void checkWinCondition() {
        if (!gameOver) {
            if (!humanAlive && !aiAlive) {
                gameResult = "Égalité !";
                gameOver = true;
            } else if (!humanAlive) {
                gameResult = "L'IA a gagné !";
                gameOver = true;
            } else if (!aiAlive) {
                gameResult = "Vous avez gagné !";
                gameOver = true;
            }
        }
    }

    /**
     * Redémarre une nouvelle partie.
     * Réinitialise complètement l'état du jeu pour permettre
     * de rejouer sans relancer l'application.
     */
    private void restartGame() {
        initializeGame();
    }

    /**
     * Effectue le rendu complet d'une frame du jeu.
     * Dessine tous les éléments visuels dans l'ordre approprié :
     * fond, grille, explosions, bombes, puis joueurs.
     */
    private void render() {
        // Fond vert foncé
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Rendu de la grille (murs et cases vides)
        grid.render(gc);

        // Explosions (rectangles orange)
        gc.setFill(Color.ORANGE);
        for (Explosion exp : explosions) {
            gc.fillRect(exp.x * TILE_SIZE + 5, exp.y * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        }

        // Bombes (cercles noirs)
        gc.setFill(Color.BLACK);
        for (Bomb bomb : bombs) {
            gc.fillOval(bomb.x * TILE_SIZE + 8, bomb.y * TILE_SIZE + 8, TILE_SIZE - 16, TILE_SIZE - 16);
        }

        // Joueurs (cercles colorés)
        if (humanAlive) {
            gc.setFill(Color.BLUE);
            gc.fillOval(humanPixelX + 5, humanPixelY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        }

        if (aiAlive) {
            gc.setFill(Color.RED);
            gc.fillOval(aiPixelX + 5, aiPixelY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
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

    // ==================== CLASSES INTERNES ====================

    /**
     * Énumération des actions possibles pour l'IA.
     * Définit toutes les actions que l'intelligence artificielle
     * peut décider d'exécuter pendant son tour.
     */
    public enum AIAction {
        /** Se déplacer vers la gauche */
        MOVE_LEFT,

        /** Se déplacer vers la droite */
        MOVE_RIGHT,

        /** Se déplacer vers le haut */
        MOVE_UP,

        /** Se déplacer vers le bas */
        MOVE_DOWN,

        /** Placer une bombe à la position actuelle */
        PLACE_BOMB,

        /** Attendre sans effectuer d'action */
        WAIT
    }

    /**
     * Représente un joueur dans le jeu.
     * Stocke la position logique du joueur sur la grille de jeu.
     */
    public static class Player {
        /** Coordonnée X du joueur sur la grille */
        int x;

        /** Coordonnée Y du joueur sur la grille */
        int y;

        /**
         * Constructeur d'un joueur à une position donnée.
         *
         * @param x Coordonnée X initiale
         * @param y Coordonnée Y initiale
         */
        public Player(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Représente une bombe placée sur le terrain.
     * Une bombe a une position fixe et un timer qui décompte jusqu'à l'explosion.
     */
    public static class Bomb {
        /** Coordonnée X de la bombe */
        int x;

        /** Coordonnée Y de la bombe */
        int y;

        /** Timer avant explosion (en frames) */
        int timer;

        /**
         * Constructeur d'une bombe à une position donnée.
         * Le timer est initialisé à 120 frames (2 secondes à 60 FPS).
         *
         * @param x Coordonnée X de la bombe
         * @param y Coordonnée Y de la bombe
         */
        public Bomb(int x, int y) {
            this.x = x;
            this.y = y;
            this.timer = 120; // 2 secondes à 60 FPS
        }
    }

    /**
     * Représente une explosion temporaire sur le terrain.
     * Une explosion a une position et une durée limitée avant de disparaître.
     */
    public static class Explosion {
        /** Coordonnée X de l'explosion */
        int x;

        /** Coordonnée Y de l'explosion */
        int y;

        /** Timer de durée de l'explosion (en frames) */
        int timer;

        /**
         * Constructeur d'une explosion à une position donnée.
         *
         * @param x Coordonnée X de l'explosion
         * @param y Coordonnée Y de l'explosion
         * @param timer Durée de l'explosion en frames
         */
        public Explosion(int x, int y, int timer) {
            this.x = x;
            this.y = y;
            this.timer = timer;
        }
    }

    /**
     * Représente la grille de jeu avec les murs et obstacles.
     * Gère la génération procédurale du niveau et le rendu visuel de la grille.
     *
     * <p>Types de cases :</p>
     * <ul>
     *   <li>0 = Case vide (praticable)</li>
     *   <li>1 = Mur destructible (peut être détruit par les explosions)</li>
     *   <li>2 = Mur indestructible (bloque les mouvements et explosions)</li>
     * </ul>
     */
    public static class GameGrid {
        /** Matrice représentant les types de cases de la grille */
        private int[][] grid;

        /** Largeur de la grille */
        private final int width = GRID_WIDTH;

        /** Hauteur de la grille */
        private final int height = GRID_HEIGHT;

        /**
         * Constructeur de la grille de jeu.
         * Initialise la matrice et génère un niveau procédural.
         */
        public GameGrid() {
            grid = new int[width][height];
            generate();
        }

        /**
         * Génère un niveau de jeu procédural.
         * Crée une structure avec des murs indestructibles en bordure et
         * dans un motif régulier, puis ajoute des murs destructibles aléatoirement
         * tout en préservant les zones de spawn des joueurs.
         */
        public void generate() {
            Random rand = new Random();

            // Remplir avec des murs indestructibles sur les bords et en motif
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                        grid[x][y] = 2; // Mur indestructible
                    } else if (x % 2 == 0 && y % 2 == 0) {
                        grid[x][y] = 2; // Mur indestructible
                    } else {
                        grid[x][y] = 0; // Vide
                    }
                }
            }

            // Ajouter des murs destructibles aléatoirement (30% de probabilité)
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    if (grid[x][y] == 0 && rand.nextDouble() < 0.3) {
                        // Ne pas bloquer les zones de spawn (coins)
                        if (!((x <= 2 && y <= 2) || (x >= width - 3 && y >= height - 3))) {
                            grid[x][y] = 1; // Mur destructible
                        }
                    }
                }
            }
        }

        /**
         * Vérifie si une position est dans les limites de la grille.
         *
         * @param x Coordonnée X à vérifier
         * @param y Coordonnée Y à vérifier
         * @return true si la position est valide
         */
        public boolean inBounds(int x, int y) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }

        /**
         * Vérifie si une case est praticable (accessible aux joueurs).
         *
         * @param x Coordonnée X à vérifier
         * @param y Coordonnée Y à vérifier
         * @return true si la case est vide et praticable
         */
        public boolean isWalkable(int x, int y) {
            return inBounds(x, y) && grid[x][y] == 0;
        }

        /**
         * Vérifie si une case contient un mur destructible.
         *
         * @param x Coordonnée X à vérifier
         * @param y Coordonnée Y à vérifier
         * @return true si c'est un mur destructible
         */
        public boolean isDestructibleWall(int x, int y) {
            return inBounds(x, y) && grid[x][y] == 1;
        }

        /**
         * Vérifie si une case contient un mur indestructible.
         *
         * @param x Coordonnée X à vérifier
         * @param y Coordonnée Y à vérifier
         * @return true si c'est un mur indestructible
         */
        public boolean isIndestructibleWall(int x, int y) {
            return inBounds(x, y) && grid[x][y] == 2;
        }

        /**
         * Transforme une case en case vide.
         * Utilisé quand un mur destructible est détruit par une explosion.
         *
         * @param x Coordonnée X de la case à vider
         * @param y Coordonnée Y de la case à vider
         */
        public void setEmpty(int x, int y) {
            if (inBounds(x, y)) {
                grid[x][y] = 0;
            }
        }

        /**
         * Effectue le rendu visuel de la grille.
         * Dessine chaque case avec sa couleur appropriée selon son type
         * et ajoute des bordures pour délimiter les cases.
         *
         * @param gc Le contexte graphique pour le dessin
         */
        public void render(GraphicsContext gc) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixelX = x * TILE_SIZE;
                    int pixelY = y * TILE_SIZE;

                    switch (grid[x][y]) {
                        case 1: // Mur destructible (marron)
                            gc.setFill(Color.BROWN);
                            gc.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                            break;
                        case 2: // Mur indestructible (gris)
                            gc.setFill(Color.GRAY);
                            gc.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                            break;
                        default: // Case vide (vert clair)
                            gc.setFill(Color.LIGHTGREEN);
                            gc.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                            break;
                    }

                    // Bordure gris foncé pour délimiter les cases
                    gc.setStroke(Color.DARKGRAY);
                    gc.strokeRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }
}