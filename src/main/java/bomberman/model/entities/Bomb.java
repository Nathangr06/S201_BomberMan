package bomberman.model.entities;

/**
 * Représente une bombe dans le jeu Bomberman.
 * Une bombe a une position, un timer d'explosion et une portée d'explosion.
 * Le timer décrémente à chaque frame jusqu'à l'explosion.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class Bomb {

    /** Coordonnée X de la bombe sur la grille */
    private int x;

    /** Coordonnée Y de la bombe sur la grille */
    private int y;

    /** Timer avant explosion (en frames) */
    private int timer;

    /** Portée de l'explosion en cases */
    private int range = 1;

    /**
     * Constructeur d'une bombe à la position spécifiée.
     * Le timer est initialisé à 180 frames (3 secondes à 60 FPS).
     *
     * @param x Coordonnée X sur la grille
     * @param y Coordonnée Y sur la grille
     */
    public Bomb(int x, int y) {
        this.x = x;
        this.y = y;
        this.timer = 180; // 3 secondes à 60 FPS
    }

    /**
     * Retourne la coordonnée X de la bombe.
     *
     * @return La coordonnée X
     */
    public int getX() { return x; }

    /**
     * Retourne la coordonnée Y de la bombe.
     *
     * @return La coordonnée Y
     */
    public int getY() { return y; }

    /**
     * Retourne la portée d'explosion de la bombe.
     *
     * @return La portée en nombre de cases
     */
    public int getRange() { return range; }

    /**
     * Définit la portée d'explosion de la bombe.
     *
     * @param range La nouvelle portée en nombre de cases
     */
    public void setRange(int range) { this.range = range; }

    /**
     * Décrémente le timer de la bombe d'une frame.
     * Appelé à chaque cycle de jeu pour faire progresser vers l'explosion.
     */
    public void decreaseTimer() {
        timer--;
    }

    /**
     * Vérifie si la bombe a explosé.
     *
     * @return true si le timer est arrivé à zéro ou moins, false sinon
     */
    public boolean isExploded() {
        return timer <= 0;
    }

    /**
     * Modifie la position de la bombe.
     * Utilisé notamment pour les bombes poussées par les joueurs.
     *
     * @param x Nouvelle coordonnée X
     * @param y Nouvelle coordonnée Y
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retourne le timer actuel de la bombe.
     *
     * @return Le nombre de frames restantes avant explosion
     */
    public int getTimer() {
        return timer;
    }
}