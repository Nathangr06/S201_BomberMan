package com.example.bomberman;

/**
 * Représente un joueur dans le jeu avec une position en coordonnées (x, y).
 */
public class Player {

    /** Coordonnée X du joueur */
    private int x;

    /** Coordonnée Y du joueur */
    private int y;

    /**
     * Constructeur de Player initialisant la position.
     *
     * @param x position horizontale initiale
     * @param y position verticale initiale
     */
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retourne la position horizontale (coordonnée X) du joueur.
     *
     * @return position X actuelle
     */
    public int getX() {
        return x;
    }

    /**
     * Retourne la position verticale (coordonnée Y) du joueur.
     *
     * @return position Y actuelle
     */
    public int getY() {
        return y;
    }

    /**
     * Modifie la position du joueur.
     *
     * @param x nouvelle position horizontale
     * @param y nouvelle position verticale
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}