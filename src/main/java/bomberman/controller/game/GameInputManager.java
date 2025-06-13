package bomberman.controller.game;

import bomberman.model.ai.AIPlayer;
import bomberman.model.game.BombSystem;
import bomberman.model.entities.GamePlayer;
import bomberman.controller.menu.InputHandler;
import javafx.scene.input.KeyCode;

/**
 * Gestionnaire des entrées du jeu Bomberman.
 * Cette classe centralise la gestion des touches clavier pour les joueurs humains et l'IA.
 * Elle coordonne les actions de mouvement et de placement de bombes selon les configurations
 * de touches assignées à chaque joueur.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class GameInputManager {

    /** Gestionnaire d'entrées pour capturer les événements clavier */
    private InputHandler inputHandler;

    /** Instance du joueur IA pour gérer les actions automatiques */
    private AIPlayer aiPlayer;

    /** Timestamp du dernier mouvement de l'IA pour contrôler la fréquence d'actions */
    private long lastAIMoveTime = 0;

    /**
     * Constructeur du gestionnaire d'entrées.
     *
     * @param inputHandler Le gestionnaire d'entrées pour capturer les événements clavier
     */
    public GameInputManager(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    /**
     * Définit l'instance du joueur IA.
     *
     * @param aiPlayer L'instance du joueur IA à associer
     */
    public void setAIPlayer(AIPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }

    /**
     * Gère les entrées d'un joueur spécifique.
     * Cette méthode traite les actions de mouvement et de placement de bombes
     * en fonction des touches pressées et des contraintes de jeu.
     *
     * @param gamePlayer Le joueur dont il faut traiter les entrées
     * @param currentTime Le timestamp actuel pour gérer les cooldowns
     * @param bombSystem Le système de bombes pour valider les placements
     */
    public void handlePlayerInput(GamePlayer gamePlayer, long currentTime, BombSystem bombSystem) {
        if (gamePlayer.getStats().isEliminated()) return;

        int playerNum = gamePlayer.getPlayerNumber();
        KeyCode[] keys = getPlayerKeys(playerNum);

        // Gestion du mouvement
        if (gamePlayer.canMoveNow(currentTime)) {
            int newX = gamePlayer.getTargetX();
            int newY = gamePlayer.getTargetY();

            if (inputHandler.isKeyPressed(keys[0])) newX--; // LEFT
            else if (inputHandler.isKeyPressed(keys[1])) newX++; // RIGHT
            else if (inputHandler.isKeyPressed(keys[2])) newY--; // UP
            else if (inputHandler.isKeyPressed(keys[3])) newY++; // DOWN

            if ((newX != gamePlayer.getTargetX() || newY != gamePlayer.getTargetY())) {
                if (canPlayerMoveTo(gamePlayer, newX, newY, bombSystem)) {
                    gamePlayer.setTarget(newX, newY);
                    gamePlayer.startMoving(currentTime);
                }
            }
        }

        // Gestion du placement de bombes
        if (inputHandler.isKeyPressed(keys[4])) { // BOMB
            if (gamePlayer.getStats().canPlaceBomb(currentTime)) {
                int x = gamePlayer.getPlayer().getX();
                int y = gamePlayer.getPlayer().getY();
                bombSystem.placeBomb(x, y, gamePlayer.getStats().getBombRange(), null);
                gamePlayer.getStats().setLastBombTime(currentTime);
                inputHandler.setKeyReleased(keys[4]);
            }
        }
    }

    /**
     * Vérifie si un joueur peut se déplacer vers une position donnée.
     * Cette méthode valide les contraintes de mouvement (murs, bombes, autres joueurs).
     *
     * @param gamePlayer Le joueur qui souhaite se déplacer
     * @param x La coordonnée X de destination
     * @param y La coordonnée Y de destination
     * @param bombSystem Le système de bombes pour vérifier les collisions
     * @return true si le mouvement est autorisé, false sinon
     * @todo Implémenter la logique complète avec accès au GameGrid
     */
    private boolean canPlayerMoveTo(GamePlayer gamePlayer, int x, int y, BombSystem bombSystem) {
        // Cette méthode nécessiterait l'accès au GameGrid et à la liste des joueurs
        // Elle devrait être implémentée dans la classe principale du jeu
        return true; // Placeholder
    }

    /**
     * Retourne la configuration de touches pour un joueur donné.
     * Chaque joueur a un mapping de touches différent pour éviter les conflits.
     *
     * @param playerNum Le numéro du joueur (1-4)
     * @return Un tableau de KeyCode dans l'ordre [LEFT, RIGHT, UP, DOWN, BOMB]
     */
    private KeyCode[] getPlayerKeys(int playerNum) {
        switch (playerNum) {
            case 1:
                return new KeyCode[]{
                        KeyCode.LEFT, KeyCode.RIGHT, KeyCode.UP, KeyCode.DOWN, KeyCode.ENTER
                };
            case 2:
                return new KeyCode[]{
                        KeyCode.Q, KeyCode.D, KeyCode.Z, KeyCode.S, KeyCode.SPACE
                };
            case 3:
                return new KeyCode[]{
                        KeyCode.J, KeyCode.L, KeyCode.I, KeyCode.K, KeyCode.U
                };
            case 4:
                return new KeyCode[]{
                        KeyCode.NUMPAD4, KeyCode.NUMPAD6, KeyCode.NUMPAD8,
                        KeyCode.NUMPAD5, KeyCode.NUMPAD0
                };
            default:
                return new KeyCode[5];
        }
    }

    /**
     * Vérifie si la touche Échap est pressée.
     *
     * @return true si la touche Échap est pressée, false sinon
     */
    public boolean isEscapePressed() {
        return inputHandler.isKeyPressed(KeyCode.ESCAPE);
    }
}