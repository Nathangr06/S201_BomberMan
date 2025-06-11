package com.example.bomberman;

/**
 * Représente une explosion dans le jeu avec une position et une durée de vie.
 */
public class Explosion {
    /**
     * Coordonnée x de l'explosion.
     */
    private int x;

    /**
     * Coordonnée y de l'explosion.
     */
    private int y;

    /**
     * Durée restante de l'explosion en "ticks" ou unités de temps.
     */
    private int timer;

    /**
     * Crée une explosion à une position donnée avec une durée spécifiée.
     *
     * @param x        la coordonnée x de l'explosion.
     * @param y        la coordonnée y de l'explosion.
     * @param duration la durée de vie de l'explosion.
     */
    public Explosion(int x, int y, int duration) {
        this.x = x;
        this.y = y;
        this.timer = duration;
    }

    /**
     * Obtient la coordonnée x de l'explosion.
     *
     * @return la coordonnée x.
     */
    public int getX() {
        return x;
    }

    /**
     * Obtient la coordonnée y de l'explosion.
     *
     * @return la coordonnée y.
     */
    public int getY() {
        return y;
    }

    /**
     * Décrémente le timer de l'explosion et vérifie si elle est terminée.
     *
     * @return true si la durée est écoulée (timer <= 0), false sinon.
     */
    public boolean decreaseTimerAndCheck() {
        timer--;
        return timer <= 0;
    }
}
