package com.example.bomberman;

import java.io.*;

public class LevelLoader {

    public static class LevelData {
        public GameGrid grid;
        public Position player1Spawn;
        public Position player2Spawn;

        public LevelData(GameGrid grid, Position player1Spawn, Position player2Spawn) {
            this.grid = grid;
            this.player1Spawn = player1Spawn;
            this.player2Spawn = player2Spawn;
        }
    }

    public static LevelData loadLevel(File levelFile) throws IOException {
        GameGrid grid = new GameGrid(GameConstants.GRID_WIDTH, GameConstants.GRID_HEIGHT);
        Position player1Spawn = new Position(1, 1); // Valeurs par défaut
        Position player2Spawn = new Position(13, 11);

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String sizeStr = reader.readLine();
            String[] sizeParts = sizeStr.split(",");
            int rows = Integer.parseInt(sizeParts[0]);
            int cols = Integer.parseInt(sizeParts[1]);

            if (rows != GameConstants.GRID_HEIGHT || cols != GameConstants.GRID_WIDTH) {
                throw new IOException("Taille de grille incompatible !");
            }

            // Initialiser toutes les cases comme vides
            for (int i = 0; i < GameConstants.GRID_HEIGHT; i++) {
                for (int j = 0; j < GameConstants.GRID_WIDTH; j++) {
                    grid.setEmpty(j, i);
                }
            }

            // Lire les données du niveau
            for (int i = 0; i < rows; i++) {
                String line = reader.readLine();
                if (line == null) break;

                String[] values = line.split(",");
                for (int j = 0; j < cols && j < values.length; j++) {
                    int typeIndex = Integer.parseInt(values[j]);
                    CellType cellType = CellType.fromValue(typeIndex);

                    switch (cellType) {
                        case EMPTY -> grid.setEmpty(j, i);
                        case WALL -> grid.setIndestructibleWall(j, i);
                        case DESTRUCTIBLE_WALL -> grid.setDestructibleWall(j, i);
                        case PLAYER_SPAWN -> {
                            grid.setEmpty(j, i);
                            player1Spawn = new Position(j, i);
                        }
                        case PLAYER2_SPAWN -> {
                            grid.setEmpty(j, i);
                            player2Spawn = new Position(j, i);
                        }
                    }
                }
            }
        }

        return new LevelData(grid, player1Spawn, player2Spawn);
    }

    public static LevelData createDefaultLevel() {
        GameGrid grid = new GameGrid(GameConstants.GRID_WIDTH, GameConstants.GRID_HEIGHT);
        grid.generate();

        Position player1Spawn = new Position(1, 1);
        Position player2Spawn = new Position(13, 11);

        return new LevelData(grid, player1Spawn, player2Spawn);
    }
}