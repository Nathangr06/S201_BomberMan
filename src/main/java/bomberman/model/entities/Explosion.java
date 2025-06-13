package bomberman.model.entities;

/**
 * Représente une explosion dans le jeu Bomberman.
 * Une explosion a une position et une durée limitée avant de disparaître.
 * Les explosions causent des dégâts aux joueurs et détruisent les murs destructibles.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class Explosion {

    /** Coordonnée X de l'explosion sur la grille */
    private int x;

    /** Coordonnée Y de l'explosion sur la grille */
    private int y;

    /** Timer de durée de l'explosion (en frames) */
    private int timer;

    /**
     * Constructeur d'une explosion à la position spécifiée.
     *
     * @param x Coordonnée X sur la grille
     * @param y Coordonnée Y sur la grille
     * @param duration Durée de l'explosion en frames
     */
    public Explosion(int x, int y, int duration) {
        this.x = x;
        this.y = y;
        this.timer = duration;
    }

    /**
     * Retourne la coordonnée X de l'explosion.
     *
     * @return La coordonnée X
     */
    public int getX() { return x; }

    /**
     * Retourne la coordonnée Y de l'explosion.
     *
     * @return La coordonnée Y
     */
    public int getY() { return y; }

    /**
     * Décrémente le timer de l'explosion et vérifie si elle doit disparaître.
     *
     * @return true si l'explosion doit être supprimée (timer <= 0), false sinon
     */
    public boolean decreaseTimerAndCheck() {
        timer--;
        return timer <= 0;
    }
}