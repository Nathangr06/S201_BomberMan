package bomberman.model.game;

import bomberman.model.ai.AIPlayer;
import bomberman.model.entities.GamePlayer;
import bomberman.model.profile.PlayerProfile;
import bomberman.model.profile.PlayerProfileManager;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private List<GamePlayer> players;
    private BombSystem bombSystem;
    private PowerUpSystem powerUpSystem;
    private GameGrid grid;
    private GameTimer gameTimer;
    private AIPlayer aiPlayer;
    private final BombermanGame game;

    private int playerCount;
    private boolean aiMode;
    private boolean gameRunning;
    
    public GameManager(int playerCount, boolean aiMode, BombermanGame game) {
        this.game = game;
        this.playerCount = Math.max(2, Math.min(4, playerCount));
        this.aiMode = aiMode;
        this.players = new ArrayList<>();
        this.bombSystem = new BombSystem();
        this.powerUpSystem = new PowerUpSystem();
        this.gameTimer = new GameTimer();
        this.gameRunning = false;
    }
    
    public void initializeGame(GameGrid grid) {
        this.grid = grid;
        initializePlayers();
        
        if (aiMode && players.size() > 1) {
            aiPlayer = new AIPlayer(grid, game); // Référence au jeu principal à ajuster
        }
        
        bombSystem.clear();
        powerUpSystem.clear();
        gameTimer.reset();
        gameRunning = true;
    }
    
    private void initializePlayers() {
        players.clear();
        
        // Positions de spawn par défaut
        int[][] spawnPositions = {
            {1, 1},     // Joueur 1
            {13, 11},   // Joueur 2
            {13, 1},    // Joueur 3
            {1, 11}     // Joueur 4
        };
        
        for (int i = 0; i < playerCount; i++) {
            int spawnX = spawnPositions[i][0];
            int spawnY = spawnPositions[i][1];
            players.add(new GamePlayer(i + 1, spawnX, spawnY));
        }
    }
    
    public void update() {
        if (!gameRunning) return;
        
        gameTimer.update();
        
        // Mise à jour des joueurs
        for (GamePlayer player : players) {
            if (!player.getStats().isEliminated()) {
                player.update();
            }
        }
        
        // Mise à jour des systèmes
        bombSystem.update(grid, powerUpSystem);
        updatePowerUpCollections();
        checkExplosionCollisions();
        checkGameEnd();
    }
    
    private void updatePowerUpCollections() {
        for (GamePlayer player : players) {
            if (!player.getStats().isEliminated()) {
                int x = player.getPlayer().getX();
                int y = player.getPlayer().getY();
                PowerUpSystem.PowerUp powerUp = powerUpSystem.checkPowerUpCollection(x, y);
                if (powerUp != null) {
                    player.getStats().applyPowerUp(powerUp.getType());
                    System.out.println("Joueur " + player.getPlayerNumber() + 
                                     " a ramassé un power-up: " + powerUp.getType().getLabel());
                }
            }
        }
    }
    
    private void checkExplosionCollisions() {
        for (GamePlayer player : players) {
            if (!player.getStats().isEliminated() && !player.getStats().isInvincible()) {
                int x = player.getPlayer().getX();
                int y = player.getPlayer().getY();
                if (bombSystem.checkExplosionCollision(x, y)) {
                    handlePlayerDeath(player);
                    return; // Sortir après la première mort pour éviter les problèmes de concurrence
                }
            }
        }
    }
    
    private void handlePlayerDeath(GamePlayer player) {
        player.getStats().takeDamage();
        System.out.println("Joueur " + player.getPlayerNumber() + " mort! Vies restantes: " + 
                          player.getStats().getLives());
        
        if (player.getStats().isEliminated()) {
            System.out.println("🔥 Joueur " + player.getPlayerNumber() + " ÉLIMINÉ!");
        } else {
            player.respawn();
        }
    }
    
    private void checkGameEnd() {
        List<GamePlayer> alivePlayers = new ArrayList<>();
        
        for (GamePlayer player : players) {
            if (!player.getStats().isEliminated()) {
                alivePlayers.add(player);
            }
        }
        
        if (alivePlayers.size() <= 1) {
            gameRunning = false;
            if (alivePlayers.size() == 1) {
                handleGameWin(alivePlayers.get(0));
            } else {
                handleGameDraw();
            }
        }
    }
    
    private void handleGameWin(GamePlayer winner) {
        // Mettre à jour les profils de joueur si nécessaire
        PlayerProfileManager profileManager = PlayerProfileManager.getInstance();
        PlayerProfile profile = profileManager.getCurrentProfile();
        
        if (profile != null) {
            profile.incrementGamesPlayed();
            if (winner.getPlayerNumber() == 1) {
                profile.incrementGamesWon();
            }
            profileManager.saveProfiles();
        }
        
        System.out.println("Joueur " + winner.getPlayerNumber() + " gagne!");
    }
    
    private void handleGameDraw() {
        PlayerProfileManager profileManager = PlayerProfileManager.getInstance();
        PlayerProfile profile = profileManager.getCurrentProfile();
        
        if (profile != null) {
            profile.incrementGamesPlayed();
            profileManager.saveProfiles();
        }
        
        System.out.println("Match nul!");
    }
    
    public boolean canPlayerMoveTo(GamePlayer gamePlayer, int x, int y) {
        if (gamePlayer.getStats().isEliminated()) return false;
        
        // Vérifier si la position est valide
        if (!grid.inBounds(x, y)) return false;
        
        // Vérifier si le joueur peut traverser les murs
        boolean canPassWalls = gamePlayer.getStats().canPassWalls();
        if (!grid.isWalkable(x, y) && !(canPassWalls && grid.isDestructibleWall(x, y))) {
            return false;
        }
        
        // Vérifier les bombes
        if (bombSystem.hasBombAt(x, y)) {
            if (gamePlayer.getStats().canPushBombs() && !bombSystem.isBombMovingAt(x, y)) {
                // Calculer la direction du push
                int currentX = gamePlayer.getPlayer().getX();
                int currentY = gamePlayer.getPlayer().getY();
                int dirX = x - currentX;
                int dirY = y - currentY;
                
                return bombSystem.tryPushBomb(x, y, dirX, dirY, grid, players);
            }
            return false;
        }
        
        // Vérifier s'il y a une bombe en mouvement
        if (bombSystem.isBombMovingAt(x, y)) {
            return false;
        }
        
        return true;
    }
    
    public void placeBombForPlayer(GamePlayer gamePlayer) {
        if (gamePlayer.getStats().isEliminated()) return;
        
        int x = gamePlayer.getPlayer().getX();
        int y = gamePlayer.getPlayer().getY();
        int range = gamePlayer.getStats().getBombRange();
        
        bombSystem.placeBomb(x, y, range, grid);
    }
    
    public void restartGame() {
        gameRunning = false;
        
        // Réinitialiser tous les systèmes
        bombSystem.clear();
        powerUpSystem.clear();
        gameTimer.reset();
        
        // Réinitialiser les joueurs
        for (GamePlayer player : players) {
            player.reset();
        }
        
        gameRunning = true;
    }
    
    public String getWinnerText() {
        List<GamePlayer> alivePlayers = new ArrayList<>();
        
        for (GamePlayer player : players) {
            if (!player.getStats().isEliminated()) {
                alivePlayers.add(player);
            }
        }
        
        if (alivePlayers.size() == 1) {
            return "Joueur " + alivePlayers.get(0).getPlayerNumber();
        } else {
            return "Match nul";
        }
    }
    
    // Getters
    public List<GamePlayer> getPlayers() { return players; }
    public BombSystem getBombSystem() { return bombSystem; }
    public PowerUpSystem getPowerUpSystem() { return powerUpSystem; }
    public GameGrid getGrid() { return grid; }
    public GameTimer getGameTimer() { return gameTimer; }
    public AIPlayer getAiPlayer() { return aiPlayer; }
    public int getPlayerCount() { return playerCount; }
    public boolean isAiMode() { return aiMode; }
    public boolean isGameRunning() { return gameRunning; }
    
    // Setters
    public void setGameRunning(boolean running) { this.gameRunning = running; }
}