package bomberman;

import javafx.scene.input.KeyCode;

public class GameInputManager {
    private InputHandler inputHandler;
    private AIPlayer aiPlayer;
    private long lastAIMoveTime = 0;
    
    public GameInputManager(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }
    
    public void setAIPlayer(AIPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }
    
    public void handlePlayerInput(GamePlayer gamePlayer, long currentTime, BombSystem bombSystem) {
        if (gamePlayer.getStats().isEliminated()) return;
        
        int playerNum = gamePlayer.getPlayerNumber();
        KeyCode[] keys = getPlayerKeys(playerNum);
        
        // Gestion du mouvement
        if (gamePlayer.canMoveNow(currentTime)) {
            int newX = gamePlayer.getTargetX();
            int newY = gamePlayer.getTargetY();
            
            if (inputHandler.isKeyPressed(keys[0])) newX--; // LEFT
            else if (inputHandler.isKeyPressed(keys[1])) newX++; // RIGHT
            else if (inputHandler.isKeyPressed(keys[2])) newY--; // UP
            else if (inputHandler.isKeyPressed(keys[3])) newY++; // DOWN
            
            if ((newX != gamePlayer.getTargetX() || newY != gamePlayer.getTargetY())) {
                if (canPlayerMoveTo(gamePlayer, newX, newY, bombSystem)) {
                    gamePlayer.setTarget(newX, newY);
                    gamePlayer.startMoving(currentTime);
                }
            }
        }
        
        // Gestion du placement de bombes
        if (inputHandler.isKeyPressed(keys[4])) { // BOMB
            if (gamePlayer.getStats().canPlaceBomb(currentTime)) {
                int x = gamePlayer.getPlayer().getX();
                int y = gamePlayer.getPlayer().getY();
                bombSystem.placeBomb(x, y, gamePlayer.getStats().getBombRange(), null);
                gamePlayer.getStats().setLastBombTime(currentTime);
                inputHandler.setKeyReleased(keys[4]);
            }
        }
    }
    
    public void handleAIInput(GamePlayer aiGamePlayer, long currentTime, BombSystem bombSystem) {
        if (aiGamePlayer.getStats().isEliminated() || aiPlayer == null) return;
        
        if (!aiGamePlayer.isMoving() && (currentTime - lastAIMoveTime) > GameConstants.AI_MOVE_INTERVAL) {
            AIPlayer.AIAction action = aiPlayer.getNextAction(
                bombSystem.getBombs(), 
                bombSystem.getExplosions()
            );
            
            if (action != null) {
                executeAIAction(aiGamePlayer, action, currentTime, bombSystem);
                lastAIMoveTime = currentTime;
            }
        }
    }
    
    private void executeAIAction(GamePlayer gamePlayer, AIPlayer.AIAction action, 
                                long currentTime, BombSystem bombSystem) {
        int newX = gamePlayer.getTargetX();
        int newY = gamePlayer.getTargetY();
        
        switch (action) {
            case MOVE_LEFT:
                newX--;
                break;
            case MOVE_RIGHT:
                newX++;
                break;
            case MOVE_UP:
                newY--;
                break;
            case MOVE_DOWN:
                newY++;
                break;
            case PLACE_BOMB:
                if (gamePlayer.getStats().canPlaceBomb(currentTime)) {
                    int x = gamePlayer.getPlayer().getX();
                    int y = gamePlayer.getPlayer().getY();
                    bombSystem.placeBomb(x, y, gamePlayer.getStats().getBombRange(), null);
                    gamePlayer.getStats().setLastBombTime(currentTime);
                }
                return;
        }
        
        if (canPlayerMoveTo(gamePlayer, newX, newY, bombSystem)) {
            gamePlayer.setTarget(newX, newY);
            gamePlayer.startMoving(currentTime);
        }
    }
    
    private boolean canPlayerMoveTo(GamePlayer gamePlayer, int x, int y, BombSystem bombSystem) {
        // Cette méthode nécessiterait l'accès au GameGrid et à la liste des joueurs
        // Elle devrait être implémentée dans la classe principale du jeu
        return true; // Placeholder
    }
    
    private KeyCode[] getPlayerKeys(int playerNum) {
        switch (playerNum) {
            case 1:
                return new KeyCode[]{
                    KeyCode.LEFT, KeyCode.RIGHT, KeyCode.UP, KeyCode.DOWN, KeyCode.ENTER
                };
            case 2:
                return new KeyCode[]{
                    KeyCode.Q, KeyCode.D, KeyCode.Z, KeyCode.S, KeyCode.SPACE
                };
            case 3:
                return new KeyCode[]{
                    KeyCode.J, KeyCode.L, KeyCode.I, KeyCode.K, KeyCode.U
                };
            case 4:
                return new KeyCode[]{
                    KeyCode.NUMPAD4, KeyCode.NUMPAD6, KeyCode.NUMPAD8, 
                    KeyCode.NUMPAD5, KeyCode.NUMPAD0
                };
            default:
                return new KeyCode[5];
        }
    }
    
    public boolean isEscapePressed() {
        return inputHandler.isKeyPressed(KeyCode.ESCAPE);
    }
}