package com.example.bomberman;

public class Bomb {
    private int x, y;
    private int timer;

    public Bomb(int x, int y) {
        this.x = x;
        this.y = y;
        this.timer = 180; // 3 secondes à 60 FPS
    }

    public void decreaseTimer() {
        timer--;
    }

    public boolean isExploded() {
        return timer <= 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
