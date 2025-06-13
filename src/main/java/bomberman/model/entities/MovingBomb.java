package bomberman.model.entities;

import static bomberman.utils.GameConstants.*;

/**
 * Représente une bombe en mouvement dans le jeu Bomberman.
 * Cette classe gère l'animation fluide d'une bombe poussée par un joueur,
 * depuis sa position initiale jusqu'à sa destination finale.
 * L'animation est basée sur l'interpolation entre positions pour un rendu fluide.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class MovingBomb {

    /** La bombe qui se déplace */
    private Bomb bomb;

    /** Position visuelle X actuelle en pixels */
    private double visualX;

    /** Position visuelle Y actuelle en pixels */
    private double visualY;

    /** Position cible X en pixels */
    private double targetX;

    /** Position cible Y en pixels */
    private double targetY;

    /** Indique si la bombe est actuellement en mouvement */
    private boolean isMoving;

    /**
     * Constructeur d'une bombe en mouvement.
     * Initialise les positions de départ et d'arrivée en pixels,
     * en tenant compte du décalage du timer en haut de l'écran.
     *
     * @param bomb La bombe à déplacer
     * @param targetGridX Coordonnée X de destination sur la grille
     * @param targetGridY Coordonnée Y de destination sur la grille
     */
    public MovingBomb(Bomb bomb, int targetGridX, int targetGridY) {
        this.bomb = bomb;
        this.visualX = bomb.getX() * TILE_SIZE;
        this.visualY = bomb.getY() * TILE_SIZE + TIMER_HEIGHT;
        this.targetX = targetGridX * TILE_SIZE;
        this.targetY = targetGridY * TILE_SIZE + TIMER_HEIGHT;
        this.isMoving = true;
    }

    /**
     * Met à jour la position visuelle de la bombe en mouvement.
     * Calcule la nouvelle position en interpolant vers la destination
     * à la vitesse définie par BOMB_PUSH_SPEED.
     * Synchronise la position logique de la bombe quand l'animation se termine.
     *
     * @return false si l'animation est terminée, true si elle continue
     */
    public boolean updatePosition() {
        if (!isMoving) return false;

        double deltaX = targetX - visualX;
        double deltaY = targetY - visualY;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance <= BOMB_PUSH_SPEED) {
            // Arrivée à destination
            visualX = targetX;
            visualY = targetY;
            isMoving = false;

            // Mettre à jour la position logique de la bombe
            int gridX = (int) (targetX / TILE_SIZE);
            int gridY = (int) ((targetY - TIMER_HEIGHT) / TILE_SIZE);
            bomb.setPosition(gridX, gridY);

            return false; // Animation terminée
        } else {
            // Continuer le mouvement
            visualX += (deltaX / distance) * BOMB_PUSH_SPEED;
            visualY += (deltaY / distance) * BOMB_PUSH_SPEED;
            return true; // Animation continue
        }
    }

    /**
     * Retourne la position visuelle X actuelle en pixels.
     *
     * @return La position visuelle X
     */
    public double getVisualX() { return visualX; }

    /**
     * Retourne la position visuelle Y actuelle en pixels.
     *
     * @return La position visuelle Y
     */
    public double getVisualY() { return visualY; }

    /**
     * Retourne la bombe associée à ce mouvement.
     *
     * @return L'instance Bomb
     */
    public Bomb getBomb() { return bomb; }

    /**
     * Vérifie si la bombe est actuellement en mouvement.
     *
     * @return true si la bombe bouge, false si elle est arrivée à destination
     */
    public boolean isMoving() { return isMoving; }
}