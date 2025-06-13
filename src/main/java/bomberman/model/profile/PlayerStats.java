package bomberman.model.profile;

import bomberman.utils.GameConstants;
import bomberman.model.game.PowerUpSystem;

public class PlayerStats {
    private int lives;
    private int bombRange;
    private double speed;
    private boolean canPassWalls;
    private boolean canPushBombs;
    private long bombCooldown;
    private long lastBombTime;
    private int invincibilityTimer;
    private boolean eliminated;
    
    public PlayerStats() {
        reset();
    }
    
    public void reset() {
        this.lives = 3;
        this.bombRange = 1;
        this.speed = GameConstants.MOVEMENT_SPEED;
        this.canPassWalls = false;
        this.canPushBombs = false;
        this.bombCooldown = 0;
        this.lastBombTime = 0;
        this.invincibilityTimer = 0;
        this.eliminated = false;
    }
    
    public void applyPowerUp(PowerUpSystem.PowerUpType type) {
        switch (type) {
            case BOMB_RANGE:
                bombRange = Math.min(bombRange + 1, 5);
                break;
            case SPEED_BOOST:
                speed = Math.min(speed + 1.0, GameConstants.MOVEMENT_SPEED * 2);
                break;
            case WALL_PASS:
                canPassWalls = true;
                break;
            case BOMB_COOLDOWN:
                bombCooldown = Math.min(bombCooldown + 200_000_000L, 400_000_000L);
                break;
            case BOMB_PUSH:
                canPushBombs = true;
                break;
        }
    }
    
    public void takeDamage() {
        lives--;
        if (lives <= 0) {
            eliminated = true;
        } else {
            invincibilityTimer = GameConstants.INVINCIBILITY_DURATION;
        }
    }
    
    public void updateInvincibility() {
        if (invincibilityTimer > 0) {
            invincibilityTimer--;
        }
    }
    
    public boolean canPlaceBomb(long currentTime) {
        return currentTime - lastBombTime > (GameConstants.DEFAULT_BOMB_COOLDOWN - bombCooldown);
    }
    
    public void setLastBombTime(long time) {
        this.lastBombTime = time;
    }
    
    public void setInvincibilityTimer(int timer) {
        this.invincibilityTimer = timer;
    }
    
    // Getters
    public int getLives() { return lives; }
    public int getBombRange() { return bombRange; }
    public double getSpeed() { return speed; }
    public boolean canPassWalls() { return canPassWalls; }
    public boolean canPushBombs() { return canPushBombs; }
    public long getBombCooldown() { return bombCooldown; }
    public long getLastBombTime() { return lastBombTime; }
    public int getInvincibilityTimer() { return invincibilityTimer; }
    public boolean isEliminated() { return eliminated; }
    public boolean isInvincible() { return invincibilityTimer > 0; }
}