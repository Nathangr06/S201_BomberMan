package bomberman.model.entities;

import bomberman.utils.GameConstants;
import bomberman.model.game.GameGrid;
import bomberman.model.profile.PlayerStats;

/**
 * Représente un joueur dans le jeu Bomberman.
 * Cette classe encapsule les données du joueur (Player), ses statistiques (PlayerStats),
 * et gère la logique de mouvement fluide avec interpolation visuelle.
 * Elle coordonne la position logique sur la grille et la position visuelle à l'écran.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class GamePlayer {

    /** Instance du joueur contenant la position logique */
    private Player player;

    /** Statistiques et capacités du joueur */
    private PlayerStats stats;

    /** Coordonnée X de spawn du joueur */
    private int spawnX;

    /** Coordonnée Y de spawn du joueur */
    private int spawnY;

    /** Position cible X vers laquelle le joueur se déplace */
    private int targetX;

    /** Position cible Y vers laquelle le joueur se déplace */
    private int targetY;

    /** Position visuelle X en pixels pour le rendu fluide */
    private double visualX;

    /** Position visuelle Y en pixels pour le rendu fluide */
    private double visualY;

    /** Indique si le joueur est actuellement en mouvement */
    private boolean isMoving;

    /** Timestamp du dernier mouvement pour gérer les cooldowns */
    private long lastMoveTime;

    /** Numéro d'identification du joueur (1-4) */
    private int playerNumber;

    /**
     * Constructeur d'un joueur de jeu.
     * Initialise le joueur à sa position de spawn avec des statistiques par défaut.
     *
     * @param playerNumber Numéro d'identification du joueur (1-4)
     * @param spawnX Coordonnée X de spawn sur la grille
     * @param spawnY Coordonnée Y de spawn sur la grille
     */
    public GamePlayer(int playerNumber, int spawnX, int spawnY) {
        this.playerNumber = playerNumber;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.player = new Player(spawnX, spawnY);
        this.stats = new PlayerStats();

        initializePosition();
    }

    /**
     * Initialise la position du joueur aux coordonnées de spawn.
     * Remet à zéro l'état de mouvement et synchronise les positions logique et visuelle.
     */
    private void initializePosition() {
        this.targetX = spawnX;
        this.targetY = spawnY;
        this.visualX = spawnX * GameConstants.TILE_SIZE;
        this.visualY = spawnY * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;
        this.isMoving = false;
        this.lastMoveTime = 0;
    }

    /**
     * Fait réapparaître le joueur à sa position de spawn.
     * Remet à zéro sa position et active l'invincibilité temporaire.
     */
    public void respawn() {
        player.setPosition(spawnX, spawnY);
        initializePosition();
        stats.setInvincibilityTimer(GameConstants.INVINCIBILITY_DURATION);
    }

    /**
     * Définit la position cible vers laquelle le joueur doit se déplacer.
     *
     * @param x Coordonnée X cible
     * @param y Coordonnée Y cible
     */
    public void setTarget(int x, int y) {
        this.targetX = x;
        this.targetY = y;
    }

    /**
     * Met à jour la position visuelle du joueur pour un mouvement fluide.
     * Interpole entre la position actuelle et la position cible en fonction de la vitesse.
     * Synchronise la position logique quand la destination est atteinte.
     */
    public void updateVisualPosition() {
        if (isMoving) {
            double targetVisualX = targetX * GameConstants.TILE_SIZE;
            double targetVisualY = targetY * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;

            double deltaX = targetVisualX - visualX;
            double deltaY = targetVisualY - visualY;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (distance > stats.getSpeed() && distance > 0.1) {
                visualX += (deltaX / distance) * stats.getSpeed();
                visualY += (deltaY / distance) * stats.getSpeed();
            } else {
                visualX = targetVisualX;
                visualY = targetVisualY;
                player.setPosition(targetX, targetY);
                isMoving = false;
            }
        }
    }

    /**
     * Vérifie si le joueur peut se déplacer vers une position donnée.
     * Prend en compte les murs et les capacités spéciales du joueur.
     *
     * @param x Coordonnée X de destination
     * @param y Coordonnée Y de destination
     * @param grid La grille de jeu pour vérifier les obstacles
     * @return true si le mouvement est possible, false sinon
     */
    public boolean canMoveTo(int x, int y, GameGrid grid) {
        return grid.isWalkable(x, y) || (stats.canPassWalls() && grid.isDestructibleWall(x, y));
    }

    /**
     * Vérifie si le joueur peut effectuer un mouvement maintenant.
     * Vérifie qu'il n'est pas déjà en mouvement et que le cooldown est écoulé.
     *
     * @param currentTime Timestamp actuel
     * @return true si le joueur peut bouger, false sinon
     */
    public boolean canMoveNow(long currentTime) {
        return !isMoving && (currentTime - lastMoveTime) > GameConstants.MOVE_COOLDOWN;
    }

    /**
     * Démarre un mouvement en enregistrant le timestamp.
     *
     * @param currentTime Timestamp du début du mouvement
     */
    public void startMoving(long currentTime) {
        this.isMoving = true;
        this.lastMoveTime = currentTime;
    }

    /**
     * Met à jour l'état du joueur à chaque frame.
     * Gère l'invincibilité et met à jour la position visuelle.
     */
    public void update() {
        stats.updateInvincibility();
        updateVisualPosition();
    }

    /**
     * Remet à zéro l'état du joueur.
     * Réinitialise les statistiques et fait réapparaître le joueur.
     */
    public void reset() {
        stats.reset();
        respawn();
    }

    // Getters

    /**
     * Retourne l'instance Player associée.
     *
     * @return L'instance Player
     */
    public Player getPlayer() { return player; }

    /**
     * Retourne les statistiques du joueur.
     *
     * @return L'instance PlayerStats
     */
    public PlayerStats getStats() { return stats; }

    /**
     * Retourne la coordonnée X de spawn.
     *
     * @return La coordonnée X de spawn
     */
    public int getSpawnX() { return spawnX; }

    /**
     * Retourne la coordonnée Y de spawn.
     *
     * @return La coordonnée Y de spawn
     */
    public int getSpawnY() { return spawnY; }

    /**
     * Retourne la coordonnée X cible.
     *
     * @return La coordonnée X cible
     */
    public int getTargetX() { return targetX; }

    /**
     * Retourne la coordonnée Y cible.
     *
     * @return La coordonnée Y cible
     */
    public int getTargetY() { return targetY; }

    /**
     * Retourne la position visuelle X en pixels.
     *
     * @return La position visuelle X
     */
    public double getVisualX() { return visualX; }

    /**
     * Retourne la position visuelle Y en pixels.
     *
     * @return La position visuelle Y
     */
    public double getVisualY() { return visualY; }

    /**
     * Vérifie si le joueur est en mouvement.
     *
     * @return true si le joueur bouge, false sinon
     */
    public boolean isMoving() { return isMoving; }

    /**
     * Retourne le timestamp du dernier mouvement.
     *
     * @return Le timestamp du dernier mouvement
     */
    public long getLastMoveTime() { return lastMoveTime; }

    /**
     * Retourne le numéro du joueur.
     *
     * @return Le numéro du joueur (1-4)
     */
    public int getPlayerNumber() { return playerNumber; }

    // Setters

    /**
     * Définit l'état de mouvement du joueur.
     *
     * @param moving true si le joueur est en mouvement, false sinon
     */
    public void setMoving(boolean moving) { this.isMoving = moving; }

    /**
     * Définit le timestamp du dernier mouvement.
     *
     * @param time Le nouveau timestamp
     */
    public void setLastMoveTime(long time) { this.lastMoveTime = time; }
}