package com.example.bomberman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * Représente une grille de jeu pour le Bomberman.
 * Gère les types de cellules (murs, cases vides, power-ups) et leur affichage.
 */
public class GameGrid {
    /**
     * Largeur de la grille en nombre de cellules.
     */
    private final int width;

    /**
     * Hauteur de la grille en nombre de cellules.
     */
    private final int height;

    /**
     * Tableau 2D représentant les types de chaque cellule.
     */
    private final int[][] grid;

    /**
     * Gestionnaire des textures utilisées pour le rendu.
     */
    private final TextureManager textureManager;

    /**
     * Constante représentant une cellule vide.
     */
    public static final int EMPTY = 0;

    /**
     * Constante représentant un mur indestructible.
     */
    public static final int WALL_INDESTRUCTIBLE = 1;

    /**
     * Constante représentant un mur destructible.
     */
    public static final int WALL_DESTRUCTIBLE = 2;

    /**
     * Constante représentant un power-up bombe.
     */
    public static final int POWERUP_BOMB = 3;

    /**
     * Constante représentant un power-up feu.
     */
    public static final int POWERUP_FIRE = 4;

    /**
     * Initialise une nouvelle grille de jeu de dimensions données.
     *
     * @param width largeur en nombre de cellules.
     * @param height hauteur en nombre de cellules.
     */
    public GameGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
        this.textureManager = TextureManager.getInstance();
    }

    /**
     * Génère la grille avec des murs indestructibles aux bords,
     * des murs destructibles aléatoires, et des cases vides.
     * Vide également la zone de départ.
     */
    public void generate() {
        Random rand = new Random();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    grid[y][x] = WALL_INDESTRUCTIBLE;
                } else if (x % 2 == 0 && y % 2 == 0) {
                    grid[y][x] = WALL_INDESTRUCTIBLE;
                } else if ((x > 2 || y > 2) && rand.nextDouble() < 0.3) {
                    grid[y][x] = WALL_DESTRUCTIBLE;
                } else {
                    grid[y][x] = EMPTY;
                }
            }
        }

        // Libérer la zone de départ pour le joueur
        grid[1][1] = EMPTY;
        grid[1][2] = EMPTY;
        grid[2][1] = EMPTY;
    }

    /**
     * Vérifie si une position donnée est dans les limites de la grille.
     *
     * @param x coordonnée x (colonne).
     * @param y coordonnée y (ligne).
     * @return true si la position est valide dans la grille.
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /**
     * Vérifie si la cellule à une position donnée est franchissable (vide).
     *
     * @param x coordonnée x.
     * @param y coordonnée y.
     * @return true si la cellule est vide et dans la grille.
     */
    public boolean isWalkable(int x, int y) {
        return inBounds(x, y) && grid[y][x] == EMPTY;
    }

    /**
     * Vérifie si la cellule à une position donnée est un mur indestructible.
     *
     * @param x coordonnée x.
     * @param y coordonnée y.
     * @return true si la cellule est un mur indestructible.
     */
    public boolean isIndestructibleWall(int x, int y) {
        return inBounds(x, y) && grid[y][x] == WALL_INDESTRUCTIBLE;
    }

    /**
     * Vérifie si la cellule à une position donnée est un mur destructible.
     *
     * @param x coordonnée x.
     * @param y coordonnée y.
     * @return true si la cellule est un mur destructible.
     */
    public boolean isDestructibleWall(int x, int y) {
        return inBounds(x, y) && grid[y][x] == WALL_DESTRUCTIBLE;
    }

    /**
     * Définit la cellule à la position donnée comme vide.
     *
     * @param x coordonnée x.
     * @param y coordonnée y.
     */
    public void setEmpty(int x, int y) {
        if (inBounds(x, y)) {
            grid[y][x] = EMPTY;
        }
    }

    /**
     * Définit la cellule à la position donnée comme mur indestructible.
     *
     * @param x coordonnée x.
     * @param y coordonnée y.
     */
    public void setIndestructibleWall(int x, int y) {
        if (inBounds(x, y)) {
            grid[y][x] = WALL_INDESTRUCTIBLE;
        }
    }

    /**
     * Définit la cellule à la position donnée comme mur destructible.
     *
     * @param x coordonnée x.
     * @param y coordonnée y.
     */
    public void setDestructibleWall(int x, int y) {
        if (inBounds(x, y)) {
            grid[y][x] = WALL_DESTRUCTIBLE;
        }
    }

    /**
     * Récupère le type de la cellule à la position donnée.
     *
     * @param x coordonnée x.
     * @param y coordonnée y.
     * @return type de la cellule ou -1 si hors limites.
     */
    public int getCellType(int x, int y) {
        if (inBounds(x, y)) {
            return grid[y][x];
        }
        return -1; // Valeur invalide si hors limites
    }

    /**
     * Définit le type de la cellule à la position donnée.
     * Seuls les types vides et murs sont autorisés.
     *
     * @param x coordonnée x.
     * @param y coordonnée y.
     * @param type type à appliquer (EMPTY, WALL_INDESTRUCTIBLE, WALL_DESTRUCTIBLE).
     */
    public void setCellType(int x, int y, int type) {
        if (inBounds(x, y) && (type == EMPTY || type == WALL_INDESTRUCTIBLE || type == WALL_DESTRUCTIBLE)) {
            grid[y][x] = type;
        }
    }

    /**
     * Récupère la largeur de la grille en nombre de cellules.
     *
     * @return largeur.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Récupère la hauteur de la grille en nombre de cellules.
     *
     * @return hauteur.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Rend la grille à l'écran en dessinant chaque cellule avec sa texture.
     * Affiche également le sol en arrière-plan.
     *
     * @param gc contexte graphique JavaFX pour dessiner.
     */
    public void render(GraphicsContext gc) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tileType = grid[y][x];
                int pixelX = x * BombermanGame.TILE_SIZE;
                int pixelY = y * BombermanGame.TILE_SIZE;

                // Dessiner le sol en arrière-plan pour toutes les cases
                Image groundTexture = textureManager.getTexture("ground");
                if (groundTexture != null) {
                    gc.drawImage(groundTexture, pixelX, pixelY,
                            BombermanGame.TILE_SIZE, BombermanGame.TILE_SIZE);
                } else {
                    gc.setFill(Color.LIGHTGREEN);
                    gc.fillRect(pixelX, pixelY, BombermanGame.TILE_SIZE, BombermanGame.TILE_SIZE);
                }

                // Dessiner les éléments par-dessus
                Image texture = null;
                switch (tileType) {
                    case WALL_INDESTRUCTIBLE:
                        texture = textureManager.getTexture("wall_indestructible");
                        break;
                    case WALL_DESTRUCTIBLE:
                        texture = textureManager.getTexture("wall_destructible");
                        break;
                    case POWERUP_BOMB:
                        texture = textureManager.getTexture("powerup_bomb");
                        break;
                    case POWERUP_FIRE:
                        texture = textureManager.getTexture("powerup_fire");
                        break;
                }

                if (texture != null) {
                    gc.drawImage(texture, pixelX, pixelY,
                            BombermanGame.TILE_SIZE, BombermanGame.TILE_SIZE);
                }
            }
        }
    }
}
