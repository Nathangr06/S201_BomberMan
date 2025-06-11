package com.example.bomberman;

public class Flag {
    private int x, y;
    private boolean captured;

    public Flag(int x, int y) {
        this.x = x;
        this.y = y;
        this.captured = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isCaptured() { return captured; }
    public void setCaptured(boolean captured) { this.captured = captured; }
}