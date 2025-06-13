package bomberman.model.entities;

/**
 * Représente un joueur de base dans le jeu Bomberman.
 * Cette classe encapsule les données fondamentales d'un joueur, notamment
 * sa position logique sur la grille de jeu. Elle sert de classe de base
 * pour les entités joueur et peut être étendue pour ajouter des fonctionnalités
 * plus complexes comme les statistiques, les power-ups ou l'état de jeu.
 *
 * <p>Responsabilités :</p>
 * <ul>
 *   <li>Stockage de la position logique du joueur sur la grille</li>
 *   <li>Gestion des coordonnées en nombres entiers (cases discrètes)</li>
 *   <li>Interface simple pour la lecture et modification de position</li>
 * </ul>
 *
 * <p>Usage typique :</p>
 * <pre>
 * // Création d'un joueur à la position (1, 1)
 * Player player = new Player(1, 1);
 *
 * // Déplacement du joueur
 * player.setPosition(player.getX() + 1, player.getY());
 *
 * // Vérification de position
 * if (player.getX() == targetX && player.getY() == targetY) {
 *     // Joueur arrivé à destination
 * }
 * </pre>
 *
 * <p>Note d'architecture :</p>
 * Cette classe représente uniquement la position logique sur la grille.
 * Pour le rendu visuel avec mouvement fluide, voir {@link bomberman.model.entities.GamePlayer}
 * qui gère les positions pixel et les animations.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 * @see bomberman.model.entities.GamePlayer
 */
public class Player {

    /**
     * Coordonnée X du joueur sur la grille de jeu.
     * Représente la colonne où se trouve le joueur, indexée à partir de 0.
     */
    private int x;

    /**
     * Coordonnée Y du joueur sur la grille de jeu.
     * Représente la ligne où se trouve le joueur, indexée à partir de 0.
     */
    private int y;

    /**
     * Constructeur d'un joueur à une position spécifiée.
     * Initialise le joueur avec les coordonnées données sur la grille de jeu.
     *
     * @param x La coordonnée X initiale (colonne) sur la grille
     * @param y La coordonnée Y initiale (ligne) sur la grille
     */
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retourne la coordonnée X actuelle du joueur.
     *
     * @return La position X (colonne) du joueur sur la grille
     */
    public int getX() {
        return x;
    }

    /**
     * Retourne la coordonnée Y actuelle du joueur.
     *
     * @return La position Y (ligne) du joueur sur la grille
     */
    public int getY() {
        return y;
    }

    /**
     * Modifie la position du joueur sur la grille.
     * Met à jour simultanément les coordonnées X et Y du joueur.
     * Cette méthode est utilisée pour les téléportations ou les
     * déplacements instantanés sans animation.
     *
     * @param x La nouvelle coordonnée X (colonne)
     * @param y La nouvelle coordonnée Y (ligne)
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}