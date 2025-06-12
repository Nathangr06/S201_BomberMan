package bomberman;

public class GamePlayer {
    private Player player;
    private PlayerStats stats;
    private int spawnX, spawnY;
    private int targetX, targetY;
    private double visualX, visualY;
    private boolean isMoving;
    private long lastMoveTime;
    private int playerNumber;
    
    public GamePlayer(int playerNumber, int spawnX, int spawnY) {
        this.playerNumber = playerNumber;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.player = new Player(spawnX, spawnY);
        this.stats = new PlayerStats();
        
        initializePosition();
    }
    
    private void initializePosition() {
        this.targetX = spawnX;
        this.targetY = spawnY;
        this.visualX = spawnX * GameConstants.TILE_SIZE;
        this.visualY = spawnY * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;
        this.isMoving = false;
        this.lastMoveTime = 0;
    }
    
    public void respawn() {
        player.setPosition(spawnX, spawnY);
        initializePosition();
        stats.setInvincibilityTimer(GameConstants.INVINCIBILITY_DURATION);
    }
    
    public void setTarget(int x, int y) {
        this.targetX = x;
        this.targetY = y;
    }
    
    public void updateVisualPosition() {
        if (isMoving) {
            double targetVisualX = targetX * GameConstants.TILE_SIZE;
            double targetVisualY = targetY * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;
            
            double deltaX = targetVisualX - visualX;
            double deltaY = targetVisualY - visualY;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            
            if (distance > stats.getSpeed() && distance > 0.1) {
                visualX += (deltaX / distance) * stats.getSpeed();
                visualY += (deltaY / distance) * stats.getSpeed();
            } else {
                visualX = targetVisualX;
                visualY = targetVisualY;
                player.setPosition(targetX, targetY);
                isMoving = false;
            }
        }
    }
    
    public boolean canMoveTo(int x, int y, GameGrid grid) {
        return grid.isWalkable(x, y) || (stats.canPassWalls() && grid.isDestructibleWall(x, y));
    }
    
    public boolean canMoveNow(long currentTime) {
        return !isMoving && (currentTime - lastMoveTime) > GameConstants.MOVE_COOLDOWN;
    }
    
    public void startMoving(long currentTime) {
        this.isMoving = true;
        this.lastMoveTime = currentTime;
    }
    
    public void update() {
        stats.updateInvincibility();
        updateVisualPosition();
    }
    
    public void reset() {
        stats.reset();
        respawn();
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public PlayerStats getStats() { return stats; }
    public int getSpawnX() { return spawnX; }
    public int getSpawnY() { return spawnY; }
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
    public double getVisualX() { return visualX; }
    public double getVisualY() { return visualY; }
    public boolean isMoving() { return isMoving; }
    public long getLastMoveTime() { return lastMoveTime; }
    public int getPlayerNumber() { return playerNumber; }
    
    // Setters
    public void setMoving(boolean moving) { this.isMoving = moving; }
    public void setLastMoveTime(long time) { this.lastMoveTime = time; }
}