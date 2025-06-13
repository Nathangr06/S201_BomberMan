package bomberman.model.profile;

import bomberman.utils.GameConstants;
import bomberman.model.game.PowerUpSystem;

/**
 * Gestion des statistiques et capacités d'un joueur dans le jeu Bomberman.
 * Cette classe contient toutes les propriétés qui définissent l'état d'un joueur,
 * incluant ses points de vie, ses capacités, ses power-ups et son état de jeu.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class PlayerStats {

    /** Nombre de vies restantes du joueur */
    private int lives;

    /** Portée d'explosion des bombes du joueur */
    private int bombRange;

    /** Vitesse de déplacement du joueur */
    private double speed;

    /** Indique si le joueur peut traverser les murs */
    private boolean canPassWalls;

    /** Indique si le joueur peut pousser les bombes */
    private boolean canPushBombs;

    /** Réduction du temps de recharge des bombes (en nanosecondes) */
    private long bombCooldown;

    /** Timestamp de la dernière bombe placée */
    private long lastBombTime;

    /** Temps d'invincibilité restant après avoir pris des dégâts */
    private int invincibilityTimer;

    /** Indique si le joueur a été éliminé de la partie */
    private boolean eliminated;

    /**
     * Constructeur par défaut.
     * Initialise les statistiques du joueur avec les valeurs par défaut.
     */
    public PlayerStats() {
        reset();
    }

    /**
     * Remet toutes les statistiques du joueur à leurs valeurs par défaut.
     * Utilisé en début de partie ou pour réinitialiser un joueur.
     */
    public void reset() {
        this.lives = 3;
        this.bombRange = 1;
        this.speed = GameConstants.MOVEMENT_SPEED;
        this.canPassWalls = false;
        this.canPushBombs = false;
        this.bombCooldown = 0;
        this.lastBombTime = 0;
        this.invincibilityTimer = 0;
        this.eliminated = false;
    }

    /**
     * Applique l'effet d'un power-up sur les statistiques du joueur.
     * Chaque type de power-up améliore une capacité spécifique avec des limites maximales.
     *
     * @param type le type de power-up à appliquer
     */
    public void applyPowerUp(PowerUpSystem.PowerUpType type) {
        switch (type) {
            case BOMB_RANGE:
                bombRange = Math.min(bombRange + 1, 5);
                break;
            case SPEED_BOOST:
                speed = Math.min(speed + 1.0, GameConstants.MOVEMENT_SPEED * 2);
                break;
            case WALL_PASS:
                canPassWalls = true;
                break;
            case BOMB_COOLDOWN:
                bombCooldown = Math.min(bombCooldown + 200_000_000L, 400_000_000L);
                break;
            case BOMB_PUSH:
                canPushBombs = true;
                break;
        }
    }

    /**
     * Fait subir des dégâts au joueur.
     * Diminue le nombre de vies et déclenche l'invincibilité temporaire.
     * Si le joueur n'a plus de vies, il est éliminé de la partie.
     */
    public void takeDamage() {
        lives--;
        if (lives <= 0) {
            eliminated = true;
        } else {
            invincibilityTimer = GameConstants.INVINCIBILITY_DURATION;
        }
    }

    /**
     * Met à jour le timer d'invincibilité du joueur.
     * Appelé à chaque frame pour décrémenter le temps d'invincibilité restant.
     */
    public void updateInvincibility() {
        if (invincibilityTimer > 0) {
            invincibilityTimer--;
        }
    }

    /**
     * Vérifie si le joueur peut placer une bombe à l'instant donné.
     * Prend en compte le temps de recharge des bombes et les améliorations.
     *
     * @param currentTime le timestamp actuel du jeu
     * @return true si le joueur peut placer une bombe, false sinon
     */
    public boolean canPlaceBomb(long currentTime) {
        return currentTime - lastBombTime > (GameConstants.DEFAULT_BOMB_COOLDOWN - bombCooldown);
    }

    /**
     * Enregistre le moment où le joueur a placé sa dernière bombe.
     *
     * @param time le timestamp de placement de la bombe
     */
    public void setLastBombTime(long time) {
        this.lastBombTime = time;
    }

    /**
     * Définit manuellement le timer d'invincibilité du joueur.
     *
     * @param timer la durée d'invincibilité en frames
     */
    public void setInvincibilityTimer(int timer) {
        this.invincibilityTimer = timer;
    }

    /**
     * Récupère le nombre de vies restantes du joueur.
     *
     * @return le nombre de vies
     */
    public int getLives() {
        return lives;
    }

    /**
     * Récupère la portée d'explosion des bombes du joueur.
     *
     * @return la portée des bombes
     */
    public int getBombRange() {
        return bombRange;
    }

    /**
     * Récupère la vitesse de déplacement du joueur.
     *
     * @return la vitesse de déplacement
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Indique si le joueur peut traverser les murs.
     *
     * @return true si le joueur peut traverser les murs, false sinon
     */
    public boolean canPassWalls() {
        return canPassWalls;
    }

    /**
     * Indique si le joueur peut pousser les bombes.
     *
     * @return true si le joueur peut pousser les bombes, false sinon
     */
    public boolean canPushBombs() {
        return canPushBombs;
    }

    /**
     * Récupère la réduction du temps de recharge des bombes.
     *
     * @return la réduction du cooldown en nanosecondes
     */
    public long getBombCooldown() {
        return bombCooldown;
    }

    /**
     * Récupère le timestamp de la dernière bombe placée.
     *
     * @return le timestamp de la dernière bombe
     */
    public long getLastBombTime() {
        return lastBombTime;
    }

    /**
     * Récupère le temps d'invincibilité restant.
     *
     * @return le temps d'invincibilité en frames
     */
    public int getInvincibilityTimer() {
        return invincibilityTimer;
    }

    /**
     * Indique si le joueur a été éliminé de la partie.
     *
     * @return true si le joueur est éliminé, false sinon
     */
    public boolean isEliminated() {
        return eliminated;
    }

    /**
     * Indique si le joueur est actuellement invincible.
     *
     * @return true si le joueur est invincible, false sinon
     */
    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }
}