package bomberman.model.game;

import bomberman.utils.GameConstants;
import bomberman.model.entities.Bomb;
import bomberman.model.entities.Explosion;
import bomberman.model.entities.GamePlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Système de gestion des bombes et explosions du jeu Bomberman.
 * Cette classe centralise toute la logique liée aux bombes : placement, animations
 * de poussée, décompte des timers, déclenchement des explosions et gestion
 * des collisions. Elle coordonne également l'apparition des power-ups lors
 * de la destruction de murs.
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Placement et validation de bombes sur la grille</li>
 *   <li>Système de bombes poussables avec animation fluide</li>
 *   <li>Gestion des timers et déclenchement automatique des explosions</li>
 *   <li>Propagation des explosions avec obstacles et portée variable</li>
 *   <li>Détection de collisions entre explosions et entités</li>
 *   <li>Génération de power-ups lors de destruction de murs</li>
 * </ul>
 *
 * <p>Architecture du système :</p>
 * <pre>
 * BombSystem
 * ├── Bombes statiques (List&lt;Bomb&gt;)
 * ├── Bombes en mouvement (List&lt;MovingBomb&gt;)
 * └── Explosions actives (List&lt;Explosion&gt;)
 * </pre>
 *
 * <p>Cycle de vie d'une bombe :</p>
 * <ol>
 *   <li>Placement via {@link #placeBomb(int, int, int, GameGrid)}</li>
 *   <li>Décompte du timer automatique</li>
 *   <li>Option de poussée avec {@link #tryPushBomb(int, int, int, int, GameGrid, List)}</li>
 *   <li>Explosion et propagation directionnelle</li>
 *   <li>Destruction de murs et génération de power-ups</li>
 *   <li>Nettoyage automatique après expiration</li>
 * </ol>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class BombSystem {

    /** Liste des bombes statiques actives */
    private List<Bomb> bombs;

    /** Liste des bombes en cours d'animation de poussée */
    private List<MovingBomb> movingBombs;

    /** Liste des explosions actives */
    private List<Explosion> explosions;

    /**
     * Constructeur du système de bombes.
     * Initialise toutes les collections d'entités vides.
     */
    public BombSystem() {
        this.bombs = new ArrayList<>();
        this.movingBombs = new ArrayList<>();
        this.explosions = new ArrayList<>();
    }

    /**
     * Classe interne représentant une bombe en mouvement (poussée).
     * Gère l'animation fluide d'une bombe qui se déplace d'une position
     * à une autre suite à une poussée par un joueur. L'animation utilise
     * une interpolation linéaire pour un mouvement visuel fluide.
     *
     * <p>Fonctionnement :</p>
     * <ul>
     *   <li>Position de départ basée sur la bombe existante</li>
     *   <li>Position cible calculée selon la direction de poussée</li>
     *   <li>Interpolation pixel par pixel à vitesse constante</li>
     *   <li>Synchronisation position logique/visuelle à l'arrivée</li>
     * </ul>
     */
    public static class MovingBomb {

        /** La bombe qui se déplace */
        private Bomb bomb;

        /** Position visuelle X actuelle en pixels */
        private double visualX, visualY;

        /** Position cible X en pixels */
        private double targetX, targetY;

        /** Indique si la bombe est actuellement en mouvement */
        private boolean isMoving;

        /**
         * Constructeur d'une bombe en mouvement.
         * Initialise les positions de départ et d'arrivée en tenant compte
         * du décalage du timer dans l'interface.
         *
         * @param bomb La bombe à animer
         * @param targetGridX Coordonnée X de destination sur la grille
         * @param targetGridY Coordonnée Y de destination sur la grille
         */
        public MovingBomb(Bomb bomb, int targetGridX, int targetGridY) {
            this.bomb = bomb;
            this.visualX = bomb.getX() * GameConstants.TILE_SIZE;
            this.visualY = bomb.getY() * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;
            this.targetX = targetGridX * GameConstants.TILE_SIZE;
            this.targetY = targetGridY * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;
            this.isMoving = true;
        }

        /**
         * Met à jour la position visuelle de la bombe en mouvement.
         * Calcule la nouvelle position en interpolant vers la destination
         * à la vitesse définie. Synchronise la position logique quand
         * l'animation se termine.
         *
         * @return false si l'animation est terminée, true si elle continue
         */
        public boolean updatePosition() {
            if (!isMoving) return false;

            double deltaX = targetX - visualX;
            double deltaY = targetY - visualY;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (distance <= GameConstants.BOMB_PUSH_SPEED) {
                // Arrivée à destination
                visualX = targetX;
                visualY = targetY;
                isMoving = false;

                // Synchroniser la position logique
                int gridX = (int) (targetX / GameConstants.TILE_SIZE);
                int gridY = (int) ((targetY - GameConstants.TIMER_HEIGHT) / GameConstants.TILE_SIZE);
                bomb.setPosition(gridX, gridY);
                return false; // Animation terminée
            } else {
                // Continuer le mouvement
                visualX += (deltaX / distance) * GameConstants.BOMB_PUSH_SPEED;
                visualY += (deltaY / distance) * GameConstants.BOMB_PUSH_SPEED;
                return true; // Animation continue
            }
        }

        /**
         * Retourne la position visuelle X actuelle.
         *
         * @return La position X en pixels
         */
        public double getVisualX() { return visualX; }

        /**
         * Retourne la position visuelle Y actuelle.
         *
         * @return La position Y en pixels
         */
        public double getVisualY() { return visualY; }

        /**
         * Retourne la bombe associée à ce mouvement.
         *
         * @return L'instance Bomb
         */
        public Bomb getBomb() { return bomb; }

        /**
         * Vérifie si la bombe est actuellement en mouvement.
         *
         * @return true si la bombe bouge, false si elle est arrivée
         */
        public boolean isMoving() { return isMoving; }
    }

    /**
     * Place une nouvelle bombe sur la grille.
     * Vérifie que la position est valide (case vide et accessible)
     * et qu'aucune autre bombe n'est déjà présente.
     *
     * @param x Coordonnée X sur la grille
     * @param y Coordonnée Y sur la grille
     * @param range Portée d'explosion de la bombe
     * @param grid La grille de jeu pour validation
     */
    public void placeBomb(int x, int y, int range, GameGrid grid) {
        if (grid.isWalkable(x, y) && !hasBombAt(x, y)) {
            Bomb bomb = new Bomb(x, y);
            bomb.setRange(range);
            bombs.add(bomb);
        }
    }

    /**
     * Vérifie s'il y a une bombe à la position spécifiée.
     *
     * @param x Coordonnée X à vérifier
     * @param y Coordonnée Y à vérifier
     * @return true s'il y a une bombe à cette position
     */
    public boolean hasBombAt(int x, int y) {
        return bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y);
    }

    /**
     * Vérifie s'il y a une bombe en mouvement à la position spécifiée.
     *
     * @param x Coordonnée X à vérifier
     * @param y Coordonnée Y à vérifier
     * @return true s'il y a une bombe en mouvement à cette position
     */
    public boolean isBombMovingAt(int x, int y) {
        return movingBombs.stream().anyMatch(mb ->
                mb.getBomb().getX() == x && mb.getBomb().getY() == y && mb.isMoving());
    }

    /**
     * Tente de pousser une bombe dans une direction donnée.
     * Calcule la distance maximale de poussée en vérifiant les obstacles,
     * autres bombes et joueurs. Démarre l'animation si le mouvement est possible.
     *
     * <p>Contraintes de poussée :</p>
     * <ul>
     *   <li>Distance maximale : 3 cases</li>
     *   <li>Arrêt sur mur ou bombe existante</li>
     *   <li>Arrêt sur position occupée par un joueur</li>
     *   <li>Respect des limites de la grille</li>
     * </ul>
     *
     * @param bombX Coordonnée X de la bombe à pousser
     * @param bombY Coordonnée Y de la bombe à pousser
     * @param dirX Direction X de poussée (-1, 0, ou 1)
     * @param dirY Direction Y de poussée (-1, 0, ou 1)
     * @param grid La grille pour vérifier les obstacles
     * @param players La liste des joueurs pour éviter les collisions
     * @return true si la poussée a été initiée, false sinon
     */
    public boolean tryPushBomb(int bombX, int bombY, int dirX, int dirY, GameGrid grid,
                               List<GamePlayer> players) {
        int maxDistance = 3;
        int actualDistance = 0;
        int finalX = bombX;
        int finalY = bombY;

        // Calculer la distance maximale possible
        for (int i = 1; i <= maxDistance; i++) {
            int checkX = bombX + (dirX * i);
            int checkY = bombY + (dirY * i);

            // Vérifications des obstacles
            if (!grid.inBounds(checkX, checkY)) break;
            if (!grid.isWalkable(checkX, checkY)) break;
            if (hasBombAt(checkX, checkY)) break;
            if (hasPlayerAt(checkX, checkY, players)) break;

            finalX = checkX;
            finalY = checkY;
            actualDistance = i;
        }

        // Démarrer l'animation si possible
        if (actualDistance == 0) {
            return false;
        }

        startBombPushAnimation(bombX, bombY, finalX, finalY);
        return true;
    }

    /**
     * Vérifie s'il y a un joueur actif à la position spécifiée.
     * Exclut les joueurs éliminés de la vérification.
     *
     * @param x Coordonnée X à vérifier
     * @param y Coordonnée Y à vérifier
     * @param players La liste des joueurs à vérifier
     * @return true s'il y a un joueur actif à cette position
     */
    private boolean hasPlayerAt(int x, int y, List<GamePlayer> players) {
        return players.stream().anyMatch(p ->
                !p.getStats().isEliminated() &&
                        p.getPlayer().getX() == x &&
                        p.getPlayer().getY() == y);
    }

    /**
     * Démarre l'animation de poussée d'une bombe.
     * Trouve la bombe correspondante et crée une MovingBomb
     * pour gérer l'animation jusqu'à la destination.
     *
     * @param fromX Coordonnée X de départ
     * @param fromY Coordonnée Y de départ
     * @param toX Coordonnée X de destination
     * @param toY Coordonnée Y de destination
     */
    private void startBombPushAnimation(int fromX, int fromY, int toX, int toY) {
        for (Bomb bomb : bombs) {
            if (bomb.getX() == fromX && bomb.getY() == fromY) {
                MovingBomb movingBomb = new MovingBomb(bomb, toX, toY);
                movingBombs.add(movingBomb);
                break;
            }
        }
    }

    /**
     * Met à jour tous les éléments du système pour une frame.
     * Traite les timers de bombes, les animations de mouvement
     * et la durée des explosions dans l'ordre approprié.
     *
     * @param grid La grille de jeu pour les explosions
     * @param powerUpSystem Le système de power-ups pour la génération
     */
    public void update(GameGrid grid, PowerUpSystem powerUpSystem) {
        updateBombs(grid, powerUpSystem);
        updateMovingBombs();
        updateExplosions();
    }

    /**
     * Met à jour les timers des bombes et déclenche les explosions.
     * Utilise un Iterator pour permettre la suppression sécurisée
     * pendant l'itération.
     *
     * @param grid La grille pour la propagation des explosions
     * @param powerUpSystem Le système pour générer des power-ups
     */
    private void updateBombs(GameGrid grid, PowerUpSystem powerUpSystem) {
        Iterator<Bomb> iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next();
            bomb.decreaseTimer();
            if (bomb.isExploded()) {
                explodeBomb(bomb, grid, powerUpSystem);
                iterator.remove();
            }
        }
    }

    /**
     * Met à jour les animations de bombes en mouvement.
     * Supprime les animations terminées de la liste.
     */
    private void updateMovingBombs() {
        Iterator<MovingBomb> iterator = movingBombs.iterator();
        while (iterator.hasNext()) {
            MovingBomb movingBomb = iterator.next();
            if (!movingBomb.updatePosition()) {
                iterator.remove();
            }
        }
    }

    /**
     * Met à jour les explosions actives.
     * Supprime automatiquement les explosions expirées.
     */
    private void updateExplosions() {
        explosions.removeIf(Explosion::decreaseTimerAndCheck);
    }

    /**
     * Déclenche l'explosion d'une bombe.
     * Crée une explosion centrale puis propage dans les 4 directions
     * selon la portée de la bombe. Détruit les murs destructibles
     * et génère des power-ups selon la probabilité configurée.
     *
     * <p>Mécanisme de propagation :</p>
     * <ul>
     *   <li>Explosion centrale à la position de la bombe</li>
     *   <li>Propagation dans 4 directions (haut, bas, gauche, droite)</li>
     *   <li>Arrêt sur mur indestructible ou limite de portée</li>
     *   <li>Destruction des murs destructibles avec power-up aléatoire</li>
     * </ul>
     *
     * @param bomb La bombe qui explose
     * @param grid La grille pour modifier les murs
     * @param powerUpSystem Le système pour générer des power-ups
     */
    private void explodeBomb(Bomb bomb, GameGrid grid, PowerUpSystem powerUpSystem) {
        int range = bomb.getRange();
        explosions.add(new Explosion(bomb.getX(), bomb.getY(), 60));

        // Directions : haut, droite, bas, gauche
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        for (int dir = 0; dir < 4; dir++) {
            for (int i = 1; i <= range; i++) {
                int x = bomb.getX() + dx[dir] * i;
                int y = bomb.getY() + dy[dir] * i;

                // Vérifications des limites et obstacles
                if (!grid.inBounds(x, y)) break;
                if (grid.isIndestructibleWall(x, y)) break;

                explosions.add(new Explosion(x, y, 60));

                // Gestion des murs destructibles
                if (grid.isDestructibleWall(x, y)) {
                    grid.setEmpty(x, y);
                    // Génération aléatoire de power-up
                    if (Math.random() < GameConstants.POWERUP_SPAWN_CHANCE) {
                        powerUpSystem.spawnPowerUp(x, y);
                    }
                    break; // L'explosion s'arrête sur un mur destructible
                }
            }
        }
    }

    /**
     * Vérifie s'il y a une collision avec une explosion à une position.
     * Utilisé pour détecter si un joueur est touché par une explosion.
     *
     * @param x Coordonnée X à vérifier
     * @param y Coordonnée Y à vérifier
     * @return true s'il y a une explosion à cette position
     */
    public boolean checkExplosionCollision(int x, int y) {
        return explosions.stream().anyMatch(e -> e.getX() == x && e.getY() == y);
    }

    /**
     * Supprime toutes les bombes, animations et explosions.
     * Utilisé pour réinitialiser le système entre les parties
     * ou lors d'un redémarrage de niveau.
     */
    public void clear() {
        bombs.clear();
        movingBombs.clear();
        explosions.clear();
    }

    // ==================== GETTERS ====================

    /**
     * Retourne la liste des bombes statiques.
     *
     * @return La liste des bombes actives
     */
    public List<Bomb> getBombs() { return bombs; }

    /**
     * Retourne la liste des bombes en mouvement.
     *
     * @return La liste des animations de bombes
     */
    public List<MovingBomb> getMovingBombs() { return movingBombs; }

    /**
     * Retourne la liste des explosions actives.
     *
     * @return La liste des explosions en cours
     */
    public List<Explosion> getExplosions() { return explosions; }
}