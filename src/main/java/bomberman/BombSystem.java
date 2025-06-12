package bomberman;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BombSystem {
    private List<Bomb> bombs;
    private List<MovingBomb> movingBombs;
    private List<Explosion> explosions;
    
    public BombSystem() {
        this.bombs = new ArrayList<>();
        this.movingBombs = new ArrayList<>();
        this.explosions = new ArrayList<>();
    }
    
    public static class MovingBomb {
        private Bomb bomb;
        private double visualX, visualY;
        private double targetX, targetY;
        private boolean isMoving;

        public MovingBomb(Bomb bomb, int targetGridX, int targetGridY) {
            this.bomb = bomb;
            this.visualX = bomb.getX() * GameConstants.TILE_SIZE;
            this.visualY = bomb.getY() * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;
            this.targetX = targetGridX * GameConstants.TILE_SIZE;
            this.targetY = targetGridY * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;
            this.isMoving = true;
        }

        public boolean updatePosition() {
            if (!isMoving) return false;

            double deltaX = targetX - visualX;
            double deltaY = targetY - visualY;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (distance <= GameConstants.BOMB_PUSH_SPEED) {
                visualX = targetX;
                visualY = targetY;
                isMoving = false;

                int gridX = (int) (targetX / GameConstants.TILE_SIZE);
                int gridY = (int) ((targetY - GameConstants.TIMER_HEIGHT) / GameConstants.TILE_SIZE);
                bomb.setPosition(gridX, gridY);
                return false;
            } else {
                visualX += (deltaX / distance) * GameConstants.BOMB_PUSH_SPEED;
                visualY += (deltaY / distance) * GameConstants.BOMB_PUSH_SPEED;
                return true;
            }
        }

        public double getVisualX() { return visualX; }
        public double getVisualY() { return visualY; }
        public Bomb getBomb() { return bomb; }
        public boolean isMoving() { return isMoving; }
    }
    
    public void placeBomb(int x, int y, int range, GameGrid grid) {
        if (grid.isWalkable(x, y) && !hasBombAt(x, y)) {
            Bomb bomb = new Bomb(x, y);
            bomb.setRange(range);
            bombs.add(bomb);
        }
    }
    
    public boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }
    
    public boolean isBombMovingAt(int x, int y) {
        return movingBombs.stream().anyMatch(mb -> 
            mb.getBomb().getX() == x && mb.getBomb().getY() == y && mb.isMoving());
    }
    
    public boolean tryPushBomb(int bombX, int bombY, int dirX, int dirY, GameGrid grid, 
                              List<GamePlayer> players) {
        int maxDistance = 3;
        int actualDistance = 0;
        int finalX = bombX;
        int finalY = bombY;

        for (int i = 1; i <= maxDistance; i++) {
            int checkX = bombX + (dirX * i);
            int checkY = bombY + (dirY * i);

            if (!grid.inBounds(checkX, checkY)) break;
            if (!grid.isWalkable(checkX, checkY)) break;
            if (hasBombAt(checkX, checkY)) break;
            if (hasPlayerAt(checkX, checkY, players)) break;

            finalX = checkX;
            finalY = checkY;
            actualDistance = i;
        }

        if (actualDistance == 0) {
            return false;
        }

        startBombPushAnimation(bombX, bombY, finalX, finalY);
        return true;
    }
    
    private boolean hasPlayerAt(int x, int y, List<GamePlayer> players) {
        return players.stream().anyMatch(p -> 
            !p.getStats().isEliminated() && 
            p.getPlayer().getX() == x && 
            p.getPlayer().getY() == y);
    }
    
    private void startBombPushAnimation(int fromX, int fromY, int toX, int toY) {
        for (Bomb bomb : bombs) {
            if (bomb.getX() == fromX && bomb.getY() == fromY) {
                MovingBomb movingBomb = new MovingBomb(bomb, toX, toY);
                movingBombs.add(movingBomb);
                break;
            }
        }
    }
    
    public void update(GameGrid grid, PowerUpSystem powerUpSystem) {
        updateBombs(grid, powerUpSystem);
        updateMovingBombs();
        updateExplosions();
    }
    
    private void updateBombs(GameGrid grid, PowerUpSystem powerUpSystem) {
        Iterator<Bomb> iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next();
            bomb.decreaseTimer();
            if (bomb.isExploded()) {
                explodeBomb(bomb, grid, powerUpSystem);
                iterator.remove();
            }
        }
    }
    
    private void updateMovingBombs() {
        Iterator<MovingBomb> iterator = movingBombs.iterator();
        while (iterator.hasNext()) {
            MovingBomb movingBomb = iterator.next();
            if (!movingBomb.updatePosition()) {
                iterator.remove();
            }
        }
    }
    
    private void updateExplosions() {
        explosions.removeIf(Explosion::decreaseTimerAndCheck);
    }
    
    private void explodeBomb(Bomb bomb, GameGrid grid, PowerUpSystem powerUpSystem) {
        int range = bomb.getRange();
        explosions.add(new Explosion(bomb.getX(), bomb.getY(), 60));

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        for (int dir = 0; dir < 4; dir++) {
            for (int i = 1; i <= range; i++) {
                int x = bomb.getX() + dx[dir] * i;
                int y = bomb.getY() + dy[dir] * i;

                if (!grid.inBounds(x, y)) break;
                if (grid.isIndestructibleWall(x, y)) break;

                explosions.add(new Explosion(x, y, 60));

                if (grid.isDestructibleWall(x, y)) {
                    grid.setEmpty(x, y);
                    if (Math.random() < GameConstants.POWERUP_SPAWN_CHANCE) {
                        powerUpSystem.spawnPowerUp(x, y);
                    }
                    break;
                }
            }
        }
    }
    
    public boolean checkExplosionCollision(int x, int y) {
        return explosions.stream().anyMatch(e -> e.getX() == x && e.getY() == y);
    }
    
    public void clear() {
        bombs.clear();
        movingBombs.clear();
        explosions.clear();
    }
    
    // Getters
    public List<Bomb> getBombs() { return bombs; }
    public List<MovingBomb> getMovingBombs() { return movingBombs; }
    public List<Explosion> getExplosions() { return explosions; }
}