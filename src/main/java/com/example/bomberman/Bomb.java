package com.example.bomberman;

public class Bomb {
    private int x, y;
    private int timer;
    private int range; // Nouvelle propriété pour la portée
    private static final int EXPLOSION_TIME = 180; // 3 secondes à 60 FPS

    // Constructeur avec portée par défaut (pour compatibilité)
    public Bomb(int x, int y) {
        this(x, y, 2); // Portée par défaut de 2
    }

    // Nouveau constructeur avec portée personnalisée
    public Bomb(int x, int y, int range) {
        this.x = x;
        this.y = y;
        this.range = range;
        this.timer = EXPLOSION_TIME;
    }

    public void decreaseTimer() {
        timer--;
    }

    public boolean isExploded() {
        return timer <= 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRange() {
        return range;
    }

    public int getTimer() {
        return timer;
    }

    // Méthode pour obtenir le pourcentage de temps restant (pour effets visuels)
    public double getTimeProgress() {
        return (double) timer / EXPLOSION_TIME;
    }

    // Méthode pour savoir si la bombe est sur le point d'exploser (pour effet visuel)
    public boolean isAboutToExplode() {
        return timer <= 30; // Dernière demi-seconde
    }
}