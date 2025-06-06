package com.example.bomberman;

public class Explosion {
    private int x, y;
    private int timer;

    public Explosion(int x, int y, int timer) {
        this.x = x;
        this.y = y;
        this.timer = timer;
    }

    public boolean decreaseTimerAndCheck() {
        timer--;
        return timer <= 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
