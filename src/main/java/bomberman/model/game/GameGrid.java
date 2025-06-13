package bomberman.model.game;

import bomberman.utils.GameConstants;
import bomberman.controller.game.TextureManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * Représente et gère la grille de jeu du Bomberman.
 * Cette classe encapsule la logique de la grille de jeu, incluant la génération
 * procédurale de niveaux, la gestion des différents types de cellules,
 * et le rendu visuel avec support des textures. Elle constitue le terrain
 * de jeu sur lequel évoluent les joueurs et les entités.
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Génération procédurale de niveaux équilibrés</li>
 *   <li>Gestion de différents types de cellules (vide, murs, power-ups)</li>
 *   <li>Validation des déplacements et collisions</li>
 *   <li>Rendu visuel avec textures et fallbacks</li>
 *   <li>Interface pour modification dynamique du terrain</li>
 * </ul>
 *
 * <p>Types de cellules supportés :</p>
 * <ul>
 *   <li><strong>EMPTY (0)</strong> : Case vide, praticable par les joueurs</li>
 *   <li><strong>WALL_INDESTRUCTIBLE (1)</strong> : Mur permanent, bloque tout</li>
 *   <li><strong>WALL_DESTRUCTIBLE (2)</strong> : Mur destructible par les bombes</li>
 *   <li><strong>POWERUP_BOMB (3)</strong> : Power-up d'amélioration de bombes</li>
 *   <li><strong>POWERUP_FIRE (4)</strong> : Power-up d'amélioration de portée</li>
 * </ul>
 *
 * <p>Algorithme de génération :</p>
 * <ol>
 *   <li>Bordures : Murs indestructibles sur tous les bords</li>
 *   <li>Structure : Murs indestructibles en damier (positions paires)</li>
 *   <li>Obstacles : Murs destructibles aléatoires (30% de probabilité)</li>
 *   <li>Zones de spawn : Espaces libres garantis pour les joueurs</li>
 * </ol>
 *
 * <p>Système de coordonnées :</p>
 * La grille utilise un système de coordonnées (x, y) où :
 * <ul>
 *   <li>x représente la colonne (0 à width-1)</li>
 *   <li>y représente la ligne (0 à height-1)</li>
 *   <li>Origine (0,0) en haut à gauche</li>
 * </ul>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class GameGrid {

    /** Largeur de la grille en nombre de cellules */
    private final int width;

    /** Hauteur de la grille en nombre de cellules */
    private final int height;

    /** Matrice 2D stockant les types de cellules [y][x] */
    private final int[][] grid;

    /** Gestionnaire de textures pour le rendu visuel */
    private final TextureManager textureManager;

    // ==================== CONSTANTES DE TYPES DE CELLULES ====================

    /** Case vide, praticable par les joueurs et entités */
    public static final int EMPTY = 0;

    /** Mur indestructible, bloque tous les mouvements et explosions */
    public static final int WALL_INDESTRUCTIBLE = 1;

    /** Mur destructible, peut être détruit par les explosions */
    public static final int WALL_DESTRUCTIBLE = 2;

    /** Power-up d'amélioration des bombes */
    public static final int POWERUP_BOMB = 3;

    /** Power-up d'amélioration de la portée d'explosion */
    public static final int POWERUP_FIRE = 4;

    /**
     * Constructeur de la grille de jeu.
     * Initialise une grille vide avec les dimensions spécifiées
     * et configure le gestionnaire de textures.
     *
     * @param width Largeur de la grille en nombre de cellules
     * @param height Hauteur de la grille en nombre de cellules
     */
    public GameGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
        this.textureManager = TextureManager.getInstance();
    }

    /**
     * Génère un niveau de jeu procédural.
     * Crée un niveau équilibré avec des bordures indestructibles,
     * une structure en damier pour la navigation, et des obstacles
     * destructibles placés aléatoirement. Garantit des zones de spawn libres.
     *
     * <p>Algorithme de génération :</p>
     * <ol>
     *   <li><strong>Bordures</strong> : Murs indestructibles sur tout le périmètre</li>
     *   <li><strong>Structure</strong> : Murs indestructibles aux positions (x,y) paires</li>
     *   <li><strong>Obstacles</strong> : Murs destructibles aléatoires (30% de probabilité)</li>
     *   <li><strong>Spawn</strong> : Zone 3x3 en haut-gauche garantie libre</li>
     * </ol>
     *
     * <p>Contraintes de génération :</p>
     * <ul>
     *   <li>Aucun obstacle dans les 3 premières cases (spawn du joueur)</li>
     *   <li>Motif en damier pour assurer la navigabilité</li>
     *   <li>Probabilité d'obstacle calibrée pour l'équilibre gameplay</li>
     * </ul>
     */
    public void generate() {
        Random rand = new Random();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    // Bordures indestructibles
                    grid[y][x] = WALL_INDESTRUCTIBLE;
                } else if (x % 2 == 0 && y % 2 == 0) {
                    // Structure en damier pour la navigation
                    grid[y][x] = WALL_INDESTRUCTIBLE;
                } else if ((x > 2 || y > 2) && rand.nextDouble() < 0.3) {
                    // Obstacles aléatoires (30% de probabilité, hors zone de spawn)
                    grid[y][x] = WALL_DESTRUCTIBLE;
                } else {
                    // Cases vides par défaut
                    grid[y][x] = EMPTY;
                }
            }
        }

        // Garantir l'espace de spawn du joueur en haut-gauche
        grid[1][1] = EMPTY;
        grid[1][2] = EMPTY;
        grid[2][1] = EMPTY;
    }

    /**
     * Vérifie si des coordonnées sont dans les limites de la grille.
     *
     * @param x Coordonnée X (colonne) à vérifier
     * @param y Coordonnée Y (ligne) à vérifier
     * @return true si les coordonnées sont valides, false sinon
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /**
     * Vérifie si une cellule est praticable (accessible aux entités).
     * Une cellule est praticable si elle est vide et dans les limites de la grille.
     *
     * @param x Coordonnée X de la cellule
     * @param y Coordonnée Y de la cellule
     * @return true si la cellule est praticable, false sinon
     */
    public boolean isWalkable(int x, int y) {
        return inBounds(x, y) && grid[y][x] == EMPTY;
    }

    /**
     * Vérifie si une cellule contient un mur indestructible.
     *
     * @param x Coordonnée X de la cellule
     * @param y Coordonnée Y de la cellule
     * @return true si c'est un mur indestructible, false sinon
     */
    public boolean isIndestructibleWall(int x, int y) {
        return inBounds(x, y) && grid[y][x] == WALL_INDESTRUCTIBLE;
    }

    /**
     * Vérifie si une cellule contient un mur destructible.
     *
     * @param x Coordonnée X de la cellule
     * @param y Coordonnée Y de la cellule
     * @return true si c'est un mur destructible, false sinon
     */
    public boolean isDestructibleWall(int x, int y) {
        return inBounds(x, y) && grid[y][x] == WALL_DESTRUCTIBLE;
    }

    /**
     * Transforme une cellule en case vide.
     * Utilisé principalement lors de la destruction de murs par les explosions.
     * Vérifie les limites avant modification.
     *
     * @param x Coordonnée X de la cellule à vider
     * @param y Coordonnée Y de la cellule à vider
     */
    public void setEmpty(int x, int y) {
        if (inBounds(x, y)) {
            grid[y][x] = EMPTY;
        }
    }

    /**
     * Place un mur indestructible à la position spécifiée.
     * Utilisé lors du chargement de niveaux personnalisés ou de modifications dynamiques.
     *
     * @param x Coordonnée X où placer le mur
     * @param y Coordonnée Y où placer le mur
     */
    public void setIndestructibleWall(int x, int y) {
        if (inBounds(x, y)) {
            grid[y][x] = WALL_INDESTRUCTIBLE;
        }
    }

    /**
     * Place un mur destructible à la position spécifiée.
     * Utilisé lors du chargement de niveaux personnalisés ou de modifications dynamiques.
     *
     * @param x Coordonnée X où placer le mur
     * @param y Coordonnée Y où placer le mur
     */
    public void setDestructibleWall(int x, int y) {
        if (inBounds(x, y)) {
            grid[y][x] = WALL_DESTRUCTIBLE;
        }
    }

    /**
     * Retourne le type de cellule à une position donnée.
     * Permet d'inspecter le contenu d'une cellule pour la logique de jeu.
     *
     * @param x Coordonnée X de la cellule
     * @param y Coordonnée Y de la cellule
     * @return Le type de cellule (constante EMPTY, WALL_*, POWERUP_*) ou -1 si hors limites
     */
    public int getCellType(int x, int y) {
        if (inBounds(x, y)) {
            return grid[y][x];
        }
        return -1; // Valeur invalide si hors limites
    }

    /**
     * Définit le type d'une cellule à une position donnée.
     * Permet de modifier dynamiquement le contenu de la grille.
     * Valide le type et les coordonnées avant modification.
     *
     * @param x Coordonnée X de la cellule
     * @param y Coordonnée Y de la cellule
     * @param type Le nouveau type de cellule (doit être une constante valide)
     */
    public void setCellType(int x, int y, int type) {
        if (inBounds(x, y) && (type == EMPTY || type == WALL_INDESTRUCTIBLE || type == WALL_DESTRUCTIBLE)) {
            grid[y][x] = type;
        }
    }

    /**
     * Retourne la largeur de la grille.
     *
     * @return Le nombre de colonnes de la grille
     */
    public int getWidth() {
        return width;
    }

    /**
     * Retourne la hauteur de la grille.
     *
     * @return Le nombre de lignes de la grille
     */
    public int getHeight() {
        return height;
    }

    /**
     * Effectue le rendu visuel de la grille complète.
     * Dessine toutes les cellules avec leurs textures appropriées ou des fallbacks
     * colorés si les textures ne sont pas disponibles. Utilise un système de
     * rendu en couches avec le sol en arrière-plan.
     *
     * <p>Pipeline de rendu :</p>
     * <ol>
     *   <li><strong>Sol</strong> : Texture de fond pour toutes les cellules</li>
     *   <li><strong>Éléments</strong> : Murs et power-ups par-dessus le sol</li>
     *   <li><strong>Fallbacks</strong> : Couleurs simples si textures indisponibles</li>
     * </ol>
     *
     * <p>Correspondance textures :</p>
     * <ul>
     *   <li><strong>ground</strong> : Sol de base (vert clair en fallback)</li>
     *   <li><strong>wall_indestructible</strong> : Murs permanents</li>
     *   <li><strong>wall_destructible</strong> : Murs destructibles</li>
     *   <li><strong>powerup_bomb/fire</strong> : Power-ups spécialisés</li>
     * </ul>
     *
     * @param gc Le contexte graphique JavaFX pour le dessin
     */
    public void render(GraphicsContext gc) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tileType = grid[y][x];
                int pixelX = x * GameConstants.TILE_SIZE;
                int pixelY = y * GameConstants.TILE_SIZE;

                // Dessiner le sol en arrière-plan pour toutes les cases
                Image groundTexture = textureManager.getTexture("ground");
                if (groundTexture != null) {
                    gc.drawImage(groundTexture, pixelX, pixelY,
                            GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
                } else {
                    // Fallback : sol vert clair
                    gc.setFill(Color.LIGHTGREEN);
                    gc.fillRect(pixelX, pixelY, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
                }

                // Dessiner les éléments par-dessus selon le type
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
                    // EMPTY ne nécessite aucune texture supplémentaire
                }

                // Appliquer la texture si disponible
                if (texture != null) {
                    gc.drawImage(texture, pixelX, pixelY,
                            GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
                }
            }
        }
    }
}