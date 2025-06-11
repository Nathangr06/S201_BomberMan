package com.example.bomberman;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe représentant un joueur contrôlé par une intelligence artificielle dans le jeu Bomberman.
 * L'IA décide des actions à effectuer en fonction de l'état du jeu, des bombes et des explosions.
 */
public class AIPlayer {

    /**
     * Enumération des actions possibles que l'IA peut effectuer.
     */
    public enum AIAction {
        MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN, PLACE_BOMB, WAIT
    }

    private final GameGrid grid;
    private final BombermanGame game;
    private final Random random;
    private long lastActionTime = 0;
    private static final long ACTION_COOLDOWN = 200_000_000; // 200ms entre les actions
    private boolean justPlacedBomb = false;
    private int lastBombX = -1, lastBombY = -1;

    /**
     * Constructeur de l'IA.
     *
     * @param grid La grille du jeu.
     * @param game L'instance du jeu Bomberman.
     */
    public AIPlayer(GameGrid grid, BombermanGame game) {
        this.grid = grid;
        this.game = game;
        this.random = new Random();
    }

    /**
     * Calcule la prochaine action que doit effectuer l'IA en fonction des bombes et explosions actuelles.
     *
     * @param bombs La liste des bombes présentes sur la grille.
     * @param explosions La liste des explosions en cours.
     * @return L'action que l'IA doit effectuer.
     */
    public AIAction getNextAction(List<Bomb> bombs, List<Explosion> explosions) {
        long currentTime = System.nanoTime();

        // Cooldown entre les actions
        if (currentTime - lastActionTime < ACTION_COOLDOWN) {
            return AIAction.WAIT;
        }

        Player player2 = game.getPlayer2();
        Player player1 = game.getPlayer1();

        if (player2 == null) return AIAction.WAIT;

        int x = player2.getX();
        int y = player2.getY();

        // Vérifier si on vient de placer une bombe
        if (justPlacedBomb && hasBombAt(lastBombX, lastBombY, bombs)) {
            // On est encore sur notre bombe, il faut absolument s'enfuir
            AIAction escapeAction = findBestEscapeRoute(x, y, bombs, explosions);
            if (escapeAction != null) {
                lastActionTime = currentTime;
                return escapeAction;
            }
        } else {
            // Reset du flag si la bombe a explosé ou n'existe plus
            justPlacedBomb = false;
        }

        // PRIORITÉ 1: Échapper au danger immédiat
        if (isInDanger(x, y, bombs, explosions)) {
            AIAction escapeAction = findBestEscapeRoute(x, y, bombs, explosions);
            if (escapeAction != null) {
                lastActionTime = currentTime;
                return escapeAction;
            }
        }

        // PRIORITÉ 2: Placer une bombe si c'est sûr et utile
        if (canSafelyPlaceBomb(x, y, player1, bombs, explosions)) {
            justPlacedBomb = true;
            lastBombX = x;
            lastBombY = y;
            lastActionTime = currentTime;
            return AIAction.PLACE_BOMB;
        }

        // PRIORITÉ 3: Se déplacer intelligemment
        AIAction moveAction = chooseSmartMove(x, y, player1, bombs, explosions);
        if (moveAction != null) {
            lastActionTime = currentTime;
            return moveAction;
        }

        return AIAction.WAIT;
    }

    /**
     * Vérifie si la position donnée est en danger à cause des bombes ou explosions.
     *
     * @param x Coordonnée x.
     * @param y Coordonnée y.
     * @param bombs Liste des bombes présentes.
     * @param explosions Liste des explosions présentes.
     * @return true si la position est dangereuse, false sinon.
     */
    private boolean isInDanger(int x, int y, List<Bomb> bombs, List<Explosion> explosions) {
        // Danger immédiat des explosions
        for (Explosion explosion : explosions) {
            if (explosion.getX() == x && explosion.getY() == y) {
                return true;
            }
        }

        // Danger des bombes qui vont exploser
        for (Bomb bomb : bombs) {
            if (bomb.getTimer() <= 60) { // Plus de marge
                if (isInBlastRange(x, y, bomb.getX(), bomb.getY())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Vérifie si une position est dans le rayon d'explosion d'une bombe.
     *
     * @param x Position à vérifier.
     * @param y Position à vérifier.
     * @param bombX Position x de la bombe.
     * @param bombY Position y de la bombe.
     * @return true si dans le rayon, false sinon.
     */
    private boolean isInBlastRange(int x, int y, int bombX, int bombY) {
        // Même ligne horizontale
        if (y == bombY && Math.abs(x - bombX) <= 2) {
            return true;
        }
        // Même ligne verticale
        if (x == bombX && Math.abs(y - bombY) <= 2) {
            return true;
        }
        return false;
    }

    /**
     * Cherche la meilleure route d'évasion à partir d'une position donnée.
     *
     * @param x Position actuelle x.
     * @param y Position actuelle y.
     * @param bombs Liste des bombes.
     * @param explosions Liste des explosions.
     * @return L'action d'évasion optimale ou null si aucune.
     */
    private AIAction findBestEscapeRoute(int x, int y, List<Bomb> bombs, List<Explosion> explosions) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        AIAction[] actions = {AIAction.MOVE_LEFT, AIAction.MOVE_RIGHT, AIAction.MOVE_UP, AIAction.MOVE_DOWN};

        List<AIAction> safeMoves = new ArrayList<>();
        List<AIAction> lessUnsafeMoves = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (canMoveTo(newX, newY, bombs)) {
                if (isSafeFromDanger(newX, newY, bombs, explosions)) {
                    safeMoves.add(actions[i]);
                } else if (!isInExplosion(newX, newY, explosions)) {
                    lessUnsafeMoves.add(actions[i]);
                }
            }
        }

        if (!safeMoves.isEmpty()) {
            return safeMoves.get(random.nextInt(safeMoves.size()));
        }

        if (!lessUnsafeMoves.isEmpty()) {
            return lessUnsafeMoves.get(random.nextInt(lessUnsafeMoves.size()));
        }

        return null;
    }

    /**
     * Vérifie si l'on peut se déplacer à la position donnée.
     *
     * @param x Position x cible.
     * @param y Position y cible.
     * @param bombs Liste des bombes.
     * @return true si le déplacement est possible, false sinon.
     */
    private boolean canMoveTo(int x, int y, List<Bomb> bombs) {
        if (!grid.inBounds(x, y)) {
            return false;
        }

        if (grid.isIndestructibleWall(x, y) || grid.isDestructibleWall(x, y)) {
            return false;
        }

        if (hasBombAt(x, y, bombs) && !(justPlacedBomb && x == lastBombX && y == lastBombY)) {
            return false;
        }

        return true;
    }

    /**
     * Vérifie si une position est sûre par rapport aux dangers actuels.
     *
     * @param x Position x.
     * @param y Position y.
     * @param bombs Liste des bombes.
     * @param explosions Liste des explosions.
     * @return true si sûre, false sinon.
     */
    private boolean isSafeFromDanger(int x, int y, List<Bomb> bombs, List<Explosion> explosions) {
        if (isInExplosion(x, y, explosions)) {
            return false;
        }

        for (Bomb bomb : bombs) {
            if (bomb.getTimer() <= 90 && isInBlastRange(x, y, bomb.getX(), bomb.getY())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Vérifie si l'on peut placer une bombe en toute sécurité à une position donnée.
     *
     * @param x Position x.
     * @param y Position y.
     * @param player1 Le joueur adverse.
     * @param bombs Liste des bombes actuelles.
     * @param explosions Liste des explosions.
     * @return true si placement sûr, false sinon.
     */
    private boolean canSafelyPlaceBomb(int x, int y, Player player1, List<Bomb> bombs, List<Explosion> explosions) {
        if (hasBombAt(x, y, bombs)) {
            return false;
        }

        if (isInDanger(x, y, bombs, explosions)) {
            return false;
        }

        List<Bomb> futureBombs = new ArrayList<>(bombs);
        futureBombs.add(new Bomb(x, y));

        AIAction escapeRoute = findBestEscapeRoute(x, y, futureBombs, explosions);
        if (escapeRoute == null) {
            return false;
        }

        return isBombUseful(x, y, player1);
    }

    /**
     * Détermine si une bombe placée à une position donnée serait utile (ex : proche d'un mur destructible ou d'un ennemi).
     *
     * @param x Position x.
     * @param y Position y.
     * @param player1 Joueur adverse.
     * @return true si la bombe est utile, false sinon.
     */
    private boolean isBombUseful(int x, int y, Player player1) {
        int[] dx = {-1, 1, 0, 0, -2, 2, 0, 0};
        int[] dy = {0, 0, -1, 1, 0, 0, -2, 2};

        for (int i = 0; i < dx.length; i++) {
            int checkX = x + dx[i];
            int checkY = y + dy[i];

            if (grid.inBounds(checkX, checkY) && grid.isDestructibleWall(checkX, checkY)) {
                return true;
            }
        }

        if (player1 != null && random.nextDouble() < 0.15) {
            int distance = Math.abs(player1.getX() - x) + Math.abs(player1.getY() - y);
            if (distance >= 2 && distance <= 4) {
                return isInBlastRange(player1.getX(), player1.getY(), x, y);
            }
        }

        return false;
    }

    /**
     * Choisit un déplacement intelligent en fonction de la position du joueur adverse et des dangers.
     *
     * @param x Position x.
     * @param y Position y.
     * @param player1 Joueur adverse.
     * @param bombs Liste des bombes.
     * @param explosions Liste des explosions.
     * @return L'action de déplacement choisie ou null.
     */
    private AIAction chooseSmartMove(int x, int y, Player player1, List<Bomb> bombs, List<Explosion> explosions) {
        List<AIAction> possibleMoves = getPossibleMoves(x, y, bombs, explosions);

        if (possibleMoves.isEmpty()) {
            return null;
        }

        if (player1 != null && random.nextDouble() < 0.6) {
            int distance = Math.abs(player1.getX() - x) + Math.abs(player1.getY() - y);

            if (distance <= 2) {
                return moveAwayFromPlayer(x, y, player1, possibleMoves);
            } else if (distance > 5) {
                return moveTowardsPlayer(x, y, player1, possibleMoves);
            }
        }

        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    /**
     * Choisit un mouvement pour se rapprocher du joueur adverse.
     *
     * @param x Position x.
     * @param y Position y.
     * @param player1 Joueur adverse.
     * @param possibleMoves Liste des mouvements possibles.
     * @return L'action choisie pour se rapprocher.
     */
    private AIAction moveTowardsPlayer(int x, int y, Player player1, List<AIAction> possibleMoves) {
        int playerX = player1.getX();
        int playerY = player1.getY();

        AIAction bestMove = null;
        int bestDistance = Integer.MAX_VALUE;

        for (AIAction move : possibleMoves) {
            int newX = x, newY = y;
            switch (move) {
                case MOVE_LEFT -> newX--;
                case MOVE_RIGHT -> newX++;
                case MOVE_UP -> newY--;
                case MOVE_DOWN -> newY++;
            }

            int distance = Math.abs(playerX - newX) + Math.abs(playerY - newY);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Choisit un mouvement pour s'éloigner du joueur adverse.
     *
     * @param x Position x.
     * @param y Position y.
     * @param player1 Joueur adverse.
     * @param possibleMoves Liste des mouvements possibles.
     * @return L'action choisie pour s'éloigner.
     */
    private AIAction moveAwayFromPlayer(int x, int y, Player player1, List<AIAction> possibleMoves) {
        int playerX = player1.getX();
        int playerY = player1.getY();

        AIAction bestMove = null;
        int bestDistance = -1;

        for (AIAction move : possibleMoves) {
            int newX = x, newY = y;
            switch (move) {
                case MOVE_LEFT -> newX--;
                case MOVE_RIGHT -> newX++;
                case MOVE_UP -> newY--;
                case MOVE_DOWN -> newY++;
            }

            int distance = Math.abs(playerX - newX) + Math.abs(playerY - newY);
            if (distance > bestDistance) {
                bestDistance = distance;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Retourne la liste des déplacements possibles et sûrs depuis une position donnée.
     *
     * @param x Position x.
     * @param y Position y.
     * @param bombs Liste des bombes.
     * @param explosions Liste des explosions.
     * @return Liste des actions possibles.
     */
    private List<AIAction> getPossibleMoves(int x, int y, List<Bomb> bombs, List<Explosion> explosions) {
        List<AIAction> moves = new ArrayList<>();

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        AIAction[] actions = {AIAction.MOVE_LEFT, AIAction.MOVE_RIGHT, AIAction.MOVE_UP, AIAction.MOVE_DOWN};

        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (canMoveTo(newX, newY, bombs) && isSafeFromDanger(newX, newY, bombs, explosions)) {
                moves.add(actions[i]);
            }
        }

        return moves;
    }

    /**
     * Vérifie si une bombe est présente à une position donnée.
     *
     * @param x Position x.
     * @param y Position y.
     * @param bombs Liste des bombes.
     * @return true si une bombe est présente, false sinon.
     */
    private boolean hasBombAt(int x, int y, List<Bomb> bombs) {
        return bombs.stream().anyMatch(bomb -> bomb.getX() == x && bomb.getY() == y);
    }

    /**
     * Vérifie si une position est dans une explosion en cours.
     *
     * @param x Position x.
     * @param y Position y.
     * @param explosions Liste des explosions.
     * @return true si dans une explosion, false sinon.
     */
    private boolean isInExplosion(int x, int y, List<Explosion> explosions) {
        return explosions.stream().anyMatch(explosion -> explosion.getX() == x && explosion.getY() == y);
    }
}
