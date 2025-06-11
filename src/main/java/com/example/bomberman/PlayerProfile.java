package com.example.bomberman;

import java.io.Serializable;

public class PlayerProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String firstName;
    private int gamesPlayed;
    private int gamesWon;

    public PlayerProfile(String username, String firstName) {
        this.username = username;
        this.firstName = firstName;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
    }

    // Getters
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getGamesWon() { return gamesWon; }

    // Méthodes pour mettre à jour les statistiques
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    public void incrementGamesWon() {
        this.gamesWon++;
    }
}