package bomberman;

public class Explosion {
    private int x, y;
    private int timer;

    public Explosion(int x, int y, int duration) {
        this.x = x;
        this.y = y;
        this.timer = duration;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public boolean decreaseTimerAndCheck() {
        timer--;
        return timer <= 0;
    }
}
