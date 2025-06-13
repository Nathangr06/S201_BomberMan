package bomberman.model.game;

import bomberman.utils.GameConstants;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Système de gestion des power-ups dans le jeu Bomberman.
 * Cette classe gère la génération, la distribution et la collecte des power-ups
 * qui améliorent les capacités des joueurs. Elle implémente un système équilibré
 * avec différents types de bonus et des mécaniques de rareté pour maintenir
 * l'équilibre du gameplay.
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Génération aléatoire de power-ups lors de destruction de murs</li>
 *   <li>Gestion de différents types d'améliorations</li>
 *   <li>Système de rareté pour power-ups spéciaux</li>
 *   <li>Détection et gestion de la collecte par les joueurs</li>
 *   <li>Limitation du nombre total de power-ups actifs</li>
 * </ul>
 *
 * <p>Types de power-ups disponibles :</p>
 * <ul>
 *   <li><strong>BOMB_RANGE</strong> : Augmente la portée des explosions</li>
 *   <li><strong>SPEED_BOOST</strong> : Accélère la vitesse de déplacement</li>
 *   <li><strong>WALL_PASS</strong> : Permet de traverser les murs destructibles</li>
 *   <li><strong>BOMB_COOLDOWN</strong> : Réduit le temps entre les bombes</li>
 *   <li><strong>BOMB_PUSH</strong> : Permet de pousser les bombes</li>
 * </ul>
 *
 * <p>Mécaniques d'équilibrage :</p>
 * <ul>
 *   <li>Limitation globale via GameConstants.MAX_POWERUPS</li>
 *   <li>Power-up WALL_PASS unique par partie (équilibrage)</li>
 *   <li>Distribution aléatoire pour maintenir l'imprévisibilité</li>
 *   <li>Collecte instantanée sans durée d'effet</li>
 * </ul>
 *
 * <p>Cycle de vie d'un power-up :</p>
 * <ol>
 *   <li>Génération via {@link #spawnPowerUp(int, int)} lors de destruction de mur</li>
 *   <li>Affichage sur la grille avec couleur distinctive</li>
 *   <li>Collecte via {@link #checkPowerUpCollection(int, int)} par collision joueur</li>
 *   <li>Application immédiate de l'effet sur les statistiques du joueur</li>
 *   <li>Suppression automatique de la liste des power-ups actifs</li>
 * </ol>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class PowerUpSystem {

    /** Liste des power-ups actuellement présents sur la grille */
    private List<PowerUp> powerUps;

    /**
     * Indicateur de génération du power-up WALL_PASS.
     * Utilisé pour limiter ce power-up à une seule occurrence par partie
     * afin de maintenir l'équilibre du gameplay.
     */
    private boolean wallPassDropped = false;

    /**
     * Constructeur du système de power-ups.
     * Initialise la liste des power-ups vide et les variables d'état.
     */
    public PowerUpSystem() {
        this.powerUps = new ArrayList<>();
    }

    /**
     * Énumération des types de power-ups disponibles.
     * Chaque type a un label d'identification et une couleur distinctive
     * pour la représentation visuelle dans le jeu.
     */
    public enum PowerUpType {
        /** Augmente la portée d'explosion des bombes (couleur orange) */
        BOMB_RANGE("range", Color.ORANGE),

        /** Augmente la vitesse de déplacement du joueur (couleur cyan) */
        SPEED_BOOST("speed", Color.CYAN),

        /** Permet de traverser les murs destructibles (couleur violette) */
        WALL_PASS("wall", Color.PURPLE),

        /** Réduit le délai entre les placements de bombes (couleur jaune) */
        BOMB_COOLDOWN("cooldown", Color.YELLOW),

        /** Permet de pousser les bombes lors de déplacements (couleur magenta) */
        BOMB_PUSH("push", Color.MAGENTA);

        /** Label textuel du power-up pour l'identification */
        private final String label;

        /** Couleur distinctive pour l'affichage visuel */
        private final Color color;

        /**
         * Constructeur d'un type de power-up.
         *
         * @param label Le label textuel d'identification
         * @param color La couleur distinctive pour l'affichage
         */
        PowerUpType(String label, Color color) {
            this.label = label;
            this.color = color;
        }

        /**
         * Retourne la couleur du power-up.
         *
         * @return La couleur JavaFX pour l'affichage
         */
        public Color getColor() { return color; }

        /**
         * Retourne le label du power-up.
         *
         * @return Le label textuel d'identification
         */
        public String getLabel() { return label; }
    }

    /**
     * Classe représentant un power-up individuel sur la grille.
     * Encapsule la position et le type d'un power-up spécifique
     * présent sur le terrain de jeu.
     */
    public static class PowerUp {
        /** Coordonnée X du power-up sur la grille */
        private int x, y;

        /** Type de power-up déterminant ses effets */
        private PowerUpType type;

        /**
         * Constructeur d'un power-up.
         *
         * @param x Coordonnée X sur la grille
         * @param y Coordonnée Y sur la grille
         * @param type Le type de power-up et ses effets
         */
        public PowerUp(int x, int y, PowerUpType type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        /**
         * Retourne la coordonnée X du power-up.
         *
         * @return La position X sur la grille
         */
        public int getX() { return x; }

        /**
         * Retourne la coordonnée Y du power-up.
         *
         * @return La position Y sur la grille
         */
        public int getY() { return y; }

        /**
         * Retourne le type du power-up.
         *
         * @return Le type déterminant les effets
         */
        public PowerUpType getType() { return type; }
    }

    /**
     * Génère un nouveau power-up à la position spécifiée.
     * Vérifie d'abord que la limite maximale de power-ups n'est pas atteinte,
     * puis sélectionne un type aléatoire selon les règles d'équilibrage
     * et crée un nouveau power-up à la position donnée.
     *
     * <p>Conditions de génération :</p>
     * <ul>
     *   <li>Nombre actuel &lt; GameConstants.MAX_POWERUPS</li>
     *   <li>Type sélectionné selon algorithme de rareté</li>
     *   <li>Position libre (non vérifiée, responsabilité de l'appelant)</li>
     * </ul>
     *
     * @param x Coordonnée X où générer le power-up
     * @param y Coordonnée Y où générer le power-up
     */
    public void spawnPowerUp(int x, int y) {
        if (powerUps.size() < GameConstants.MAX_POWERUPS) {
            PowerUpType randomType = getRandomPowerUpType();
            powerUps.add(new PowerUp(x, y, randomType));
        }
    }

    /**
     * Sélectionne un type de power-up aléatoire avec gestion de la rareté.
     * Implémente un système d'équilibrage où certains power-ups (notamment WALL_PASS)
     * sont limités à une seule occurrence par partie pour maintenir l'équilibre.
     *
     * <p>Algorithme de sélection :</p>
     * <ul>
     *   <li><strong>Si WALL_PASS déjà généré</strong> : Sélection parmi les 4 autres types</li>
     *   <li><strong>Si WALL_PASS pas encore généré</strong> : Sélection parmi tous les types</li>
     *   <li><strong>Marquage automatique</strong> : wallPassDropped = true si WALL_PASS sélectionné</li>
     * </ul>
     *
     * <p>Cette approche garantit :</p>
     * <ul>
     *   <li>Distribution équitable des power-ups communs</li>
     *   <li>Rareté contrôlée pour les capacités puissantes</li>
     *   <li>Maintien de l'équilibre compétitif</li>
     * </ul>
     *
     * @return Le type de power-up sélectionné selon les règles d'équilibrage
     */
    private PowerUpType getRandomPowerUpType() {
        if (wallPassDropped) {
            // WALL_PASS déjà généré, sélectionner parmi les autres types
            PowerUpType[] availableTypes = {
                    PowerUpType.BOMB_RANGE,
                    PowerUpType.SPEED_BOOST,
                    PowerUpType.BOMB_COOLDOWN,
                    PowerUpType.BOMB_PUSH
            };
            return availableTypes[(int)(Math.random() * availableTypes.length)];
        } else {
            // WALL_PASS pas encore généré, tous les types disponibles
            PowerUpType[] types = PowerUpType.values();
            PowerUpType randomType = types[(int)(Math.random() * types.length)];
            if (randomType == PowerUpType.WALL_PASS) {
                wallPassDropped = true;
            }
            return randomType;
        }
    }

    /**
     * Vérifie et gère la collecte d'un power-up à une position donnée.
     * Parcourt la liste des power-ups actifs pour détecter une collision
     * à la position spécifiée. Si un power-up est trouvé, il est automatiquement
     * supprimé de la liste et retourné pour application de ses effets.
     *
     * <p>Mécanisme de collecte :</p>
     * <ol>
     *   <li>Parcours de tous les power-ups actifs</li>
     *   <li>Comparaison de position exacte (x, y)</li>
     *   <li>Suppression immédiate du power-up de la liste</li>
     *   <li>Retour du power-up pour application des effets</li>
     * </ol>
     *
     * <p>Note d'implémentation :</p>
     * Utilise une boucle for classique avec index pour permettre
     * la suppression sécurisée pendant l'itération.
     *
     * @param x Coordonnée X de la position à vérifier
     * @param y Coordonnée Y de la position à vérifier
     * @return Le power-up collecté ou null si aucun power-up à cette position
     */
    public PowerUp checkPowerUpCollection(int x, int y) {
        for (int i = 0; i < powerUps.size(); i++) {
            PowerUp powerUp = powerUps.get(i);
            if (powerUp.getX() == x && powerUp.getY() == y) {
                powerUps.remove(i);
                return powerUp;
            }
        }
        return null;
    }

    /**
     * Remet à zéro le système de power-ups.
     * Supprime tous les power-ups actifs et réinitialise les variables
     * d'état pour une nouvelle partie. Utilisé lors du redémarrage
     * ou de l'initialisation d'une nouvelle partie.
     *
     * <p>Éléments réinitialisés :</p>
     * <ul>
     *   <li>Liste des power-ups (vidée complètement)</li>
     *   <li>Flag wallPassDropped (remis à false)</li>
     *   <li>État prêt pour nouvelle partie</li>
     * </ul>
     */
    public void clear() {
        powerUps.clear();
        wallPassDropped = false;
    }

    /**
     * Retourne la liste des power-ups actuellement actifs.
     * Fournit l'accès en lecture à la liste des power-ups présents
     * sur la grille pour l'affichage et le rendu visuel.
     *
     * @return La liste des power-ups actifs
     */
    public List<PowerUp> getPowerUps() {
        return powerUps;
    }
}