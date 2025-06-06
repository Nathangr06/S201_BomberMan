package com.example.bomberman;

import java.util.ArrayList;
import java.util.List;

public class ExplosionManager {
    private List<Explosion> explosions;

    public ExplosionManager() {
        this.explosions = new ArrayList<>();
    }

    public void createExplosion(int x, int y) {
        explosions.add(new Explosion(x, y, GameConstants.EXPLOSION_DURATION));
    }

    public void update() {
        explosions.removeIf(Explosion::decreaseTimerAndCheck);
    }

    public List<Explosion> getExplosions() {
        return new ArrayList<>(explosions);
    }

    public boolean hasExplosionAt(int x, int y) {
        return explosions.stream().anyMatch(e -> e.getX() == x && e.getY() == y);
    }

    public void clear() {
        explosions.clear();
    }
}