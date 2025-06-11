package com.example.bomberman;

/**
 * Représente une bombe dans le jeu Bomberman.
 * La bombe a une position, un timer avant explosion, et une portée d'explosion.
 */
public class Bomb {
    private int x, y;
    private int timer;
    private int range = 2;

    /**
     * Crée une nouvelle bombe à la position donnée avec un timer par défaut.
     *
     * @param x La position horizontale de la bombe.
     * @param y La position verticale de la bombe.
     */
    public Bomb(int x, int y) {
        this.x = x;
        this.y = y;
        this.timer = 180; // 3 secondes à 60 FPS
    }

    /**
     * Retourne la position horizontale de la bombe.
     *
     * @return La coordonnée x.
     */
    public int getX() {
        return x;
    }

    /**
     * Retourne la position verticale de la bombe.
     *
     * @return La coordonnée y.
     */
    public int getY() {
        return y;
    }

    /**
     * Retourne la portée d'explosion de la bombe.
     *
     * @return La portée d'explosion.
     */
    public int getRange() {
        return range;
    }

    /**
     * Définit la portée d'explosion de la bombe.
     *
     * @param range La nouvelle portée d'explosion.
     */
    public void setRange(int range) {
        this.range = range;
    }

    /**
     * Décrémente le timer de la bombe d'une unité.
     * Utilisé pour gérer le compte à rebours avant l'explosion.
     */
    public void decreaseTimer() {
        timer--;
    }

    /**
     * Indique si la bombe a explosé.
     *
     * @return true si le timer est écoulé (<= 0), sinon false.
     */
    public boolean isExploded() {
        return timer <= 0;
    }

    /**
     * Modifie la position de la bombe.
     *
     * @param x Nouvelle position horizontale.
     * @param y Nouvelle position verticale.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retourne le temps restant avant l'explosion.
     *
     * @return Le timer actuel.
     */
    public int getTimer() {
        return timer;
    }
}
