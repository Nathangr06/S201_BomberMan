package bomberman.model.game;

import bomberman.model.profile.PlayerProfile;
import bomberman.model.profile.PlayerProfileManager;
import bomberman.model.ai.AIPlayer;
import bomberman.model.entities.GamePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire principal de la logique de jeu Bomberman.
 * Cette classe centralise et orchestre tous les aspects du gameplay :
 * gestion des joueurs, systèmes de bombes et power-ups, détection des collisions,
 * conditions de victoire et intégration avec les profils joueurs. Elle constitue
 * le cœur logique du jeu en coordonnant tous les sous-systèmes.
 *
 * <p>Responsabilités principales :</p>
 * <ul>
 *   <li>Coordination de tous les systèmes de jeu (bombes, power-ups, timer)</li>
 *   <li>Gestion du cycle de vie des joueurs (spawn, mort, élimination)</li>
 *   <li>Validation des mouvements avec mécaniques avancées (poussée de bombes)</li>
 *   <li>Détection des collisions et application des dégâts</li>
 *   <li>Vérification des conditions de victoire/défaite</li>
 *   <li>Intégration avec le système de profils et statistiques</li>
 * </ul>
 *
 * <p>Architecture des systèmes :</p>
 * <pre>
 * GameManager (Orchestrateur)
 * ├── List&lt;GamePlayer&gt; (Joueurs actifs)
 * ├── BombSystem (Bombes et explosions)
 * ├── PowerUpSystem (Bonus et collectibles)
 * ├── GameGrid (Terrain de jeu)
 * ├── GameTimer (Horloge de partie)
 * └── PlayerProfileManager (Statistiques)
 * </pre>
 *
 * <p>Cycle de mise à jour :</p>
 * <ol>
 *   <li>Mise à jour du timer de partie</li>
 *   <li>Mise à jour des états des joueurs</li>
 *   <li>Mise à jour du système de bombes</li>
 *   <li>Collecte des power-ups</li>
 *   <li>Vérification des collisions d'explosion</li>
 *   <li>Contrôle des conditions de fin de partie</li>
 * </ol>
 *
 * <p>Positions de spawn par défaut :</p>
 * <ul>
 *   <li><strong>Joueur 1</strong> : (1, 1) - Coin haut-gauche</li>
 *   <li><strong>Joueur 2</strong> : (13, 11) - Coin bas-droite</li>
 *   <li><strong>Joueur 3</strong> : (13, 1) - Coin haut-droite</li>
 *   <li><strong>Joueur 4</strong> : (1, 11) - Coin bas-gauche</li>
 * </ul>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class GameManager {

    /** Liste des joueurs actifs dans la partie */
    private List<GamePlayer> players;

    /** Système de gestion des bombes et explosions */
    private BombSystem bombSystem;

    /** Système de gestion des power-ups */
    private PowerUpSystem powerUpSystem;

    /** Grille de jeu représentant le terrain */
    private GameGrid grid;

    /** Timer de partie pour la gestion du temps */
    private GameTimer gameTimer;

    /** Instance du joueur IA (si mode IA activé) */
    private AIPlayer aiPlayer;

    /** Nombre de joueurs dans la partie (2-4) */
    private int playerCount;

    /** Indique si le mode IA est activé */
    private boolean aiMode;

    /** État de fonctionnement de la partie */
    private boolean gameRunning;

    /**
     * Constructeur du gestionnaire de jeu.
     * Initialise tous les sous-systèmes et configure le nombre de joueurs.
     * Le nombre de joueurs est automatiquement contraint entre 2 et 4.
     *
     * @param playerCount Le nombre de joueurs souhaité (sera contraint entre 2 et 4)
     */
    public GameManager(int playerCount) {
        this.playerCount = Math.max(2, Math.min(4, playerCount));
        this.aiMode = aiMode;
        this.players = new ArrayList<>();
        this.bombSystem = new BombSystem();
        this.powerUpSystem = new PowerUpSystem();
        this.gameTimer = new GameTimer();
        this.gameRunning = false;
    }

    /**
     * Initialise une nouvelle partie avec la grille spécifiée.
     * Configure tous les systèmes, place les joueurs aux positions de spawn
     * et démarre le timer de partie.
     *
     * @param grid La grille de jeu à utiliser pour cette partie
     */
    public void initializeGame(GameGrid grid) {
        this.grid = grid;
        initializePlayers();

        bombSystem.clear();
        powerUpSystem.clear();
        gameTimer.reset();
        gameRunning = true;
    }

    /**
     * Initialise les joueurs aux positions de spawn prédéfinies.
     * Place les joueurs aux quatre coins de la carte selon un schéma équilibré
     * qui garantit une distance maximale entre les adversaires.
     *
     * <p>Algorithme de placement :</p>
     * <ul>
     *   <li>Joueurs disposés en diagonale pour équilibrage</li>
     *   <li>Positions éloignées pour éviter les confrontations immédiates</li>
     *   <li>Zones de spawn garanties libres par la génération de grille</li>
     * </ul>
     */
    private void initializePlayers() {
        players.clear();

        // Positions de spawn optimisées pour l'équilibrage
        int[][] spawnPositions = {
                {1, 1},     // Joueur 1 - Coin haut-gauche
                {13, 11},   // Joueur 2 - Coin bas-droite (diagonale)
                {13, 1},    // Joueur 3 - Coin haut-droite
                {1, 11}     // Joueur 4 - Coin bas-gauche (diagonale)
        };

        for (int i = 0; i < playerCount; i++) {
            int spawnX = spawnPositions[i][0];
            int spawnY = spawnPositions[i][1];
            players.add(new GamePlayer(i + 1, spawnX, spawnY));
        }
    }

    /**
     * Met à jour l'état complet du jeu pour une frame.
     * Orchestre la mise à jour de tous les sous-systèmes dans l'ordre approprié
     * pour maintenir la cohérence du gameplay et détecter les événements importants.
     *
     * <p>Séquence de mise à jour :</p>
     * <ol>
     *   <li>Timer de partie (temps restant)</li>
     *   <li>États des joueurs (mouvement, invincibilité)</li>
     *   <li>Système de bombes (timers, explosions)</li>
     *   <li>Collecte de power-ups</li>
     *   <li>Collisions avec explosions</li>
     *   <li>Conditions de fin de partie</li>
     * </ol>
     */
    public void update() {
        if (!gameRunning) return;

        gameTimer.update();

        // Mise à jour des joueurs actifs
        for (GamePlayer player : players) {
            if (!player.getStats().isEliminated()) {
                player.update();
            }
        }

        // Mise à jour des systèmes de jeu
        bombSystem.update(grid, powerUpSystem);
        updatePowerUpCollections();
        checkExplosionCollisions();
        checkGameEnd();
    }

    /**
     * Gère la collecte des power-ups par les joueurs.
     * Vérifie pour chaque joueur actif s'il se trouve sur un power-up
     * et applique les effets correspondants tout en supprimant le power-up collecté.
     */
    private void updatePowerUpCollections() {
        for (GamePlayer player : players) {
            if (!player.getStats().isEliminated()) {
                int x = player.getPlayer().getX();
                int y = player.getPlayer().getY();
                PowerUpSystem.PowerUp powerUp = powerUpSystem.checkPowerUpCollection(x, y);
                if (powerUp != null) {
                    player.getStats().applyPowerUp(powerUp.getType());
                    System.out.println("Joueur " + player.getPlayerNumber() +
                            " a ramassé un power-up: " + powerUp.getType().getLabel());
                }
            }
        }
    }

    /**
     * Vérifie les collisions entre joueurs et explosions.
     * Contrôle si des joueurs actifs et vulnérables se trouvent sur des cases
     * d'explosion et déclenche la gestion des dégâts appropriée.
     *
     * <p>Conditions de collision :</p>
     * <ul>
     *   <li>Joueur non éliminé</li>
     *   <li>Joueur non invincible (pas de period d'invincibilité)</li>
     *   <li>Position exactement sur une explosion active</li>
     * </ul>
     */
    private void checkExplosionCollisions() {
        for (GamePlayer player : players) {
            if (!player.getStats().isEliminated() && !player.getStats().isInvincible()) {
                int x = player.getPlayer().getX();
                int y = player.getPlayer().getY();
                if (bombSystem.checkExplosionCollision(x, y)) {
                    handlePlayerDeath(player);
                    return; // Sortir après la première mort pour éviter les problèmes de concurrence
                }
            }
        }
    }

    /**
     * Gère la mort d'un joueur suite à une explosion.
     * Applique les dégâts, vérifie l'élimination et gère le respawn
     * avec periode d'invincibilité appropriée.
     *
     * @param player Le joueur qui a subi des dégâts
     */
    private void handlePlayerDeath(GamePlayer player) {
        player.getStats().takeDamage();
        System.out.println("Joueur " + player.getPlayerNumber() + " mort! Vies restantes: " +
                player.getStats().getLives());

        if (player.getStats().isEliminated()) {
            System.out.println("🔥 Joueur " + player.getPlayerNumber() + " ÉLIMINÉ!");
        } else {
            player.respawn();
        }
    }

    /**
     * Vérifie les conditions de fin de partie.
     * Détermine s'il reste suffisamment de joueurs pour continuer
     * et déclenche les procédures de fin appropriées (victoire ou match nul).
     *
     * <p>Conditions de fin :</p>
     * <ul>
     *   <li><strong>Victoire</strong> : Un seul joueur survivant</li>
     *   <li><strong>Match nul</strong> : Aucun joueur survivant</li>
     *   <li><strong>Continuation</strong> : 2+ joueurs survivants</li>
     * </ul>
     */
    private void checkGameEnd() {
        List<GamePlayer> alivePlayers = new ArrayList<>();

        for (GamePlayer player : players) {
            if (!player.getStats().isEliminated()) {
                alivePlayers.add(player);
            }
        }

        if (alivePlayers.size() <= 1) {
            gameRunning = false;
            if (alivePlayers.size() == 1) {
                handleGameWin(alivePlayers.get(0));
            } else {
                handleGameDraw();
            }
        }
    }

    /**
     * Gère la victoire d'un joueur.
     * Met à jour les statistiques du profil actuel si configuré
     * et enregistre la partie gagnée pour le joueur 1.
     *
     * @param winner Le joueur gagnant
     */
    private void handleGameWin(GamePlayer winner) {
        // Mettre à jour les profils de joueur si nécessaire
        PlayerProfileManager profileManager = PlayerProfileManager.getInstance();
        PlayerProfile profile = profileManager.getCurrentProfile();

        if (profile != null) {
            profile.incrementGamesPlayed();
            if (winner.getPlayerNumber() == 1) {
                profile.incrementGamesWon();
            }
            profileManager.saveProfiles();
        }

        System.out.println("Joueur " + winner.getPlayerNumber() + " gagne!");
    }

    /**
     * Gère un match nul (aucun survivant).
     * Met à jour les statistiques du profil avec une partie jouée
     * mais aucune victoire enregistrée.
     */
    private void handleGameDraw() {
        PlayerProfileManager profileManager = PlayerProfileManager.getInstance();
        PlayerProfile profile = profileManager.getCurrentProfile();

        if (profile != null) {
            profile.incrementGamesPlayed();
            profileManager.saveProfiles();
        }

        System.out.println("Match nul!");
    }

    /**
     * Valide si un joueur peut se déplacer vers une position donnée.
     * Effectue une validation complète incluant les limites de grille,
     * les obstacles, les bombes et les mécaniques avancées comme la poussée.
     *
     * <p>Validations effectuées :</p>
     * <ol>
     *   <li><strong>État joueur</strong> : Vérification non-éliminé</li>
     *   <li><strong>Limites</strong> : Position dans les bornes de la grille</li>
     *   <li><strong>Terrain</strong> : Case praticable ou traversable avec capacités</li>
     *   <li><strong>Bombes</strong> : Gestion poussée ou blocage</li>
     *   <li><strong>Animations</strong> : Évitement bombes en mouvement</li>
     * </ol>
     *
     * @param gamePlayer Le joueur qui souhaite se déplacer
     * @param x Coordonnée X de destination
     * @param y Coordonnée Y de destination
     * @return true si le mouvement est autorisé, false sinon
     */
    public boolean canPlayerMoveTo(GamePlayer gamePlayer, int x, int y) {
        if (gamePlayer.getStats().isEliminated()) return false;

        // Vérifier les limites de la grille
        if (!grid.inBounds(x, y)) return false;

        // Vérifier la praticabilité du terrain avec capacités spéciales
        boolean canPassWalls = gamePlayer.getStats().canPassWalls();
        if (!grid.isWalkable(x, y) && !(canPassWalls && grid.isDestructibleWall(x, y))) {
            return false;
        }

        // Gestion des bombes avec mécaniques de poussée
        if (bombSystem.hasBombAt(x, y)) {
            if (gamePlayer.getStats().canPushBombs() && !bombSystem.isBombMovingAt(x, y)) {
                // Calculer la direction de poussée
                int currentX = gamePlayer.getPlayer().getX();
                int currentY = gamePlayer.getPlayer().getY();
                int dirX = x - currentX;
                int dirY = y - currentY;

                return bombSystem.tryPushBomb(x, y, dirX, dirY, grid, players);
            }
            return false;
        }

        // Éviter les bombes en cours d'animation
        if (bombSystem.isBombMovingAt(x, y)) {
            return false;
        }

        return true;
    }

    /**
     * Place une bombe pour un joueur à sa position actuelle.
     * Vérifie que le joueur est actif et utilise ses statistiques
     * pour déterminer la portée de la bombe.
     *
     * @param gamePlayer Le joueur qui place la bombe
     */
    public void placeBombForPlayer(GamePlayer gamePlayer) {
        if (gamePlayer.getStats().isEliminated()) return;

        int x = gamePlayer.getPlayer().getX();
        int y = gamePlayer.getPlayer().getY();
        int range = gamePlayer.getStats().getBombRange();

        bombSystem.placeBomb(x, y, range, grid);
    }

    /**
     * Redémarre la partie actuelle.
     * Remet à zéro tous les systèmes de jeu et replace les joueurs
     * à leurs positions de spawn avec leurs statistiques initiales.
     */
    public void restartGame() {
        gameRunning = false;

        // Réinitialiser tous les systèmes
        bombSystem.clear();
        powerUpSystem.clear();
        gameTimer.reset();

        // Réinitialiser les joueurs
        for (GamePlayer player : players) {
            player.reset();
        }

        gameRunning = true;
    }

    /**
     * Retourne le texte du gagnant pour l'affichage.
     * Analyse l'état actuel des joueurs pour déterminer
     * le résultat de la partie.
     *
     * @return Le nom du joueur gagnant ou "Match nul"
     */
    public String getWinnerText() {
        List<GamePlayer> alivePlayers = new ArrayList<>();

        for (GamePlayer player : players) {
            if (!player.getStats().isEliminated()) {
                alivePlayers.add(player);
            }
        }

        if (alivePlayers.size() == 1) {
            return "Joueur " + alivePlayers.get(0).getPlayerNumber();
        } else {
            return "Match nul";
        }
    }

    // ==================== ACCESSEURS ====================

    /**
     * Retourne la liste des joueurs actifs.
     *
     * @return La liste des GamePlayer
     */
    public List<GamePlayer> getPlayers() { return players; }

    /**
     * Retourne le système de bombes.
     *
     * @return L'instance BombSystem
     */
    public BombSystem getBombSystem() { return bombSystem; }

    /**
     * Retourne le système de power-ups.
     *
     * @return L'instance PowerUpSystem
     */
    public PowerUpSystem getPowerUpSystem() { return powerUpSystem; }

    /**
     * Retourne la grille de jeu.
     *
     * @return L'instance GameGrid
     */
    public GameGrid getGrid() { return grid; }

    /**
     * Retourne le timer de partie.
     *
     * @return L'instance GameTimer
     */
    public GameTimer getGameTimer() { return gameTimer; }

    /**
     * Retourne l'instance du joueur IA.
     *
     * @return L'instance AIPlayer ou null si mode IA non activé
     */
    public AIPlayer getAiPlayer() { return aiPlayer; }

    /**
     * Retourne le nombre de joueurs dans la partie.
     *
     * @return Le nombre de joueurs (2-4)
     */
    public int getPlayerCount() { return playerCount; }

    /**
     * Vérifie si le mode IA est activé.
     *
     * @return true si mode IA, false sinon
     */
    public boolean isAiMode() { return aiMode; }

    /**
     * Vérifie si la partie est en cours.
     *
     * @return true si le jeu est actif, false sinon
     */
    public boolean isGameRunning() { return gameRunning; }

    // ==================== MODIFICATEURS ====================

    /**
     * Définit l'état de fonctionnement de la partie.
     *
     * @param running true pour activer le jeu, false pour l'arrêter
     */
    public void setGameRunning(boolean running) { this.gameRunning = running; }
}