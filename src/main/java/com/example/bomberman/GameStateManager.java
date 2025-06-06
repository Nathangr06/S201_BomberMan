package com.example.bomberman;

public class GameStateManager {
    public enum GameState {
        PLAYING, PAUSED, GAME_OVER, MENU
    }

    private GameState currentState;
    private String winner;

    public GameStateManager() {
        this.currentState = GameState.MENU;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setState(GameState state) {
        this.currentState = state;
    }

    public void gameOver(String winner) {
        this.winner = winner;
        this.currentState = GameState.GAME_OVER;
    }

    public String getWinner() {
        return winner;
    }

    public boolean isPlaying() {
        return currentState == GameState.PLAYING;
    }

    public void startGame() {
        currentState = GameState.PLAYING;
        winner = null;
    }

    public void pauseGame() {
        if (currentState == GameState.PLAYING) {
            currentState = GameState.PAUSED;
        }
    }

    public void resumeGame() {
        if (currentState == GameState.PAUSED) {
            currentState = GameState.PLAYING;
        }
    }
}