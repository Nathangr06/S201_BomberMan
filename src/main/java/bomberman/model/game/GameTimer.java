package bomberman.model.game;

public class GameTimer {
    private long gameStartTime;
    private long gameDuration;
    
    public GameTimer() {
        reset();
    }
    
    public void reset() {
        this.gameStartTime = System.currentTimeMillis();
        this.gameDuration = 0;
    }
    
    public void update() {
        long currentTime = System.currentTimeMillis();
        gameDuration = (currentTime - gameStartTime) / 1000;
    }
    
    public long getDuration() {
        return gameDuration;
    }
    
    public String getFormattedTime() {
        long minutes = gameDuration / 60;
        long seconds = gameDuration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}