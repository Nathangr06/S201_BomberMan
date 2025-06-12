package bomberman;

import javafx.scene.paint.Color;

public class GameConstants {
    // Constantes de la grille
    public static final int TILE_SIZE = 40;
    public static final int GRID_WIDTH = 15;
    public static final int GRID_HEIGHT = 13;
    public static final int CANVAS_WIDTH = GRID_WIDTH * TILE_SIZE;
    public static final int CANVAS_HEIGHT = GRID_HEIGHT * TILE_SIZE + 60;
    public static final int TIMER_HEIGHT = 60;
    
    // Constantes de mouvement
    public static final double MOVEMENT_SPEED = 3.0;
    public static final double BOMB_PUSH_SPEED = 8.0;
    public static final long MOVE_COOLDOWN = 16_000_000;
    public static final long AI_MOVE_INTERVAL = 500_000_000;
    
    // Constantes de bombes
    public static final long DEFAULT_BOMB_COOLDOWN = 500_000_000;
    
    // Constantes d'invincibilité
    public static final int INVINCIBILITY_DURATION = 60;
    
    // Constantes de power-ups
    public static final int MAX_POWERUPS = 5;
    public static final double POWERUP_SPAWN_CHANCE = 0.3;
    
    // Couleurs des joueurs
    public static final Color[] PLAYER_COLORS = {
        Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW
    };
}