package bomberman.model.entities;

public class Bomb {
    private int x, y;
    private int timer;
    private int range = 1;

    public Bomb(int x, int y) {
        this.x = x;
        this.y = y;
        this.timer = 180; // 3 secondes à 60 FPS
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }

    public void decreaseTimer() {
        timer--;
    }

    public boolean isExploded() {
        return timer <= 0;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getTimer() {
        return timer;
    }
}
