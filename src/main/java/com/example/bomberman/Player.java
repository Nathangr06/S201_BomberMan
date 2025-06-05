package com.example.bomberman;

public class Player {
    private int x;
    private int y;
    private int bombCapacity;
    private int fireRange;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.bombCapacity = 1; // Nombre maximum de bombes simultanées
        this.fireRange = 2;    // Portée des explosions
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getBombCapacity() {
        return bombCapacity;
    }

    public void increaseBombCapacity() {
        bombCapacity++;
    }

    public int getFireRange() {
        return fireRange;
    }

    public void increaseFireRange() {
        fireRange++;
    }

    // Méthode pour réinitialiser le joueur (utile pour restart)
    public void reset(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.bombCapacity = 1;
        this.fireRange = 2;
    }
}