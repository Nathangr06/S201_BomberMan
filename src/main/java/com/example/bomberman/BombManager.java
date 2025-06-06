package com.example.bomberman;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BombManager {
    private List<Bomb> bombs;
    private ExplosionManager explosionManager;
    private GameGrid grid;

    public BombManager(GameGrid grid, ExplosionManager explosionManager) {
        this.bombs = new ArrayList<>();
        this.grid = grid;
        this.explosionManager = explosionManager;
    }

    public boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    public void placeBomb(Player player) {
        if (!hasBombAt(player.getX(), player.getY())) {
            bombs.add(new Bomb(player.getX(), player.getY()));
        }
    }

    public void update() {
        Iterator<Bomb> iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next();
            bomb.decreaseTimer();
            if (bomb.isExploded()) {
                explodeBomb(bomb);
                iterator.remove();
            }
        }
    }

    private void explodeBomb(Bomb bomb) {
        explosionManager.createExplosion(bomb.getX(), bomb.getY());

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        for (int dir = 0; dir < 4; dir++) {
            for (int i = 1; i <= GameConstants.EXPLOSION_RANGE; i++) {
                int x = bomb.getX() + dx[dir] * i;
                int y = bomb.getY() + dy[dir] * i;

                if (!grid.inBounds(x, y) || grid.isIndestructibleWall(x, y)) {
                    break;
                }

                explosionManager.createExplosion(x, y);

                if (grid.isDestructibleWall(x, y)) {
                    grid.setEmpty(x, y);
                    break;
                }
            }
        }
    }

    public List<Bomb> getBombs() {
        return new ArrayList<>(bombs);
    }

    public void clear() {
        bombs.clear();
    }
}