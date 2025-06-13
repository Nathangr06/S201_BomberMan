package bomberman.model.entities;

/**
 * Représente un drapeau dans le mode Capture the Flag.
 * Un drapeau a une position fixe et un état de capture.
 * Les joueurs doivent capturer le drapeau adverse pour gagner.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class Flag {

    /** Coordonnée X du drapeau sur la grille */
    private int x;

    /** Coordonnée Y du drapeau sur la grille */
    private int y;

    /** État de capture du drapeau */
    private boolean captured;

    /**
     * Constructeur d'un drapeau à la position spécifiée.
     * Le drapeau est initialement non capturé.
     *
     * @param x Coordonnée X sur la grille
     * @param y Coordonnée Y sur la grille
     */
    public Flag(int x, int y) {
        this.x = x;
        this.y = y;
        this.captured = false;
    }

    /**
     * Retourne la coordonnée X du drapeau.
     *
     * @return La coordonnée X
     */
    public int getX() { return x; }

    /**
     * Retourne la coordonnée Y du drapeau.
     *
     * @return La coordonnée Y
     */
    public int getY() { return y; }

    /**
     * Vérifie si le drapeau est capturé.
     *
     * @return true si le drapeau est capturé, false sinon
     */
    public boolean isCaptured() { return captured; }

    /**
     * Définit l'état de capture du drapeau.
     *
     * @param captured true pour capturer le drapeau, false pour le libérer
     */
    public void setCaptured(boolean captured) { this.captured = captured; }
}