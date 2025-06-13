package bomberman.model.game;

import bomberman.utils.GameConstants;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class PowerUpSystem {
    private List<PowerUp> powerUps;
    private boolean wallPassDropped = false;
    
    public PowerUpSystem() {
        this.powerUps = new ArrayList<>();
    }
    
    public enum PowerUpType {
        BOMB_RANGE("range", Color.ORANGE),
        SPEED_BOOST("speed", Color.CYAN),
        WALL_PASS("wall", Color.PURPLE),
        BOMB_COOLDOWN("cooldown", Color.YELLOW),
        BOMB_PUSH("push", Color.MAGENTA);

        private final String label;
        private final Color color;

        PowerUpType(String label, Color color) {
            this.label = label;
            this.color = color;
        }

        public Color getColor() { return color; }
        public String getLabel() { return label; }
    }

    public static class PowerUp {
        private int x, y;
        private PowerUpType type;

        public PowerUp(int x, int y, PowerUpType type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public PowerUpType getType() { return type; }
    }
    
    public void spawnPowerUp(int x, int y) {
        if (powerUps.size() < GameConstants.MAX_POWERUPS) {
            PowerUpType randomType = getRandomPowerUpType();
            powerUps.add(new PowerUp(x, y, randomType));
        }
    }
    
    private PowerUpType getRandomPowerUpType() {
        if (wallPassDropped) {
            PowerUpType[] availableTypes = {
                PowerUpType.BOMB_RANGE, 
                PowerUpType.SPEED_BOOST,
                PowerUpType.BOMB_COOLDOWN, 
                PowerUpType.BOMB_PUSH
            };
            return availableTypes[(int)(Math.random() * availableTypes.length)];
        } else {
            PowerUpType[] types = PowerUpType.values();
            PowerUpType randomType = types[(int)(Math.random() * types.length)];
            if (randomType == PowerUpType.WALL_PASS) {
                wallPassDropped = true;
            }
            return randomType;
        }
    }
    
    public PowerUp checkPowerUpCollection(int x, int y) {
        for (int i = 0; i < powerUps.size(); i++) {
            PowerUp powerUp = powerUps.get(i);
            if (powerUp.getX() == x && powerUp.getY() == y) {
                powerUps.remove(i);
                return powerUp;
            }
        }
        return null;
    }
    
    public void clear() {
        powerUps.clear();
        wallPassDropped = false;
    }
    
    public List<PowerUp> getPowerUps() {
        return powerUps;
    }
}