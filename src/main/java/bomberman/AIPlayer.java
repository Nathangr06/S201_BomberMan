package bomberman;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer {

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

    public AIPlayer(GameGrid grid, BombermanGame game) {
        this.grid = grid;
        this.game = game;
        this.random = new Random();
    }

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

    private AIAction findBestEscapeRoute(int x, int y, List<Bomb> bombs, List<Explosion> explosions) {
        // Directions possibles
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
                    // Pas parfaitement sûr mais mieux que rester dans une explosion
                    lessUnsafeMoves.add(actions[i]);
                }
            }
        }

        // Prioriser les mouvements complètement sûrs
        if (!safeMoves.isEmpty()) {
            return safeMoves.get(random.nextInt(safeMoves.size()));
        }

        // Sinon, prendre le moins dangereux
        if (!lessUnsafeMoves.isEmpty()) {
            return lessUnsafeMoves.get(random.nextInt(lessUnsafeMoves.size()));
        }

        return null;
    }

    private boolean canMoveTo(int x, int y, List<Bomb> bombs) {
        // Vérifier les limites
        if (!grid.inBounds(x, y)) {
            return false;
        }

        // Vérifier les murs
        if (grid.isIndestructibleWall(x, y) || grid.isDestructibleWall(x, y)) {
            return false;
        }

        // Vérifier les bombes (on peut passer sur une bombe qu'on vient de placer)
        if (hasBombAt(x, y, bombs) && !(justPlacedBomb && x == lastBombX && y == lastBombY)) {
            return false;
        }

        return true;
    }

    private boolean isSafeFromDanger(int x, int y, List<Bomb> bombs, List<Explosion> explosions) {
        // Pas dans une explosion
        if (isInExplosion(x, y, explosions)) {
            return false;
        }

        // Pas dans le rayon d'une bombe dangereuse
        for (Bomb bomb : bombs) {
            if (bomb.getTimer() <= 90 && isInBlastRange(x, y, bomb.getX(), bomb.getY())) {
                return false;
            }
        }

        return true;
    }

    private boolean canSafelyPlaceBomb(int x, int y, Player player1, List<Bomb> bombs, List<Explosion> explosions) {
        // Ne pas placer si il y a déjà une bombe
        if (hasBombAt(x, y, bombs)) {
            return false;
        }

        // Ne pas placer si on est déjà en danger
        if (isInDanger(x, y, bombs, explosions)) {
            return false;
        }

        // Vérifier qu'on peut s'échapper après avoir placé la bombe
        List<Bomb> futureBombs = new ArrayList<>(bombs);
        futureBombs.add(new Bomb(x, y)); // Simuler notre future bombe

        AIAction escapeRoute = findBestEscapeRoute(x, y, futureBombs, explosions);
        if (escapeRoute == null) {
            return false; // Pas d'échappatoire
        }

        // Vérifier si c'est utile de placer une bombe
        return isBombUseful(x, y, player1);
    }

    private boolean isBombUseful(int x, int y, Player player1) {
        // Vérifier les murs destructibles à proximité
        int[] dx = {-1, 1, 0, 0, -2, 2, 0, 0};
        int[] dy = {0, 0, -1, 1, 0, 0, -2, 2};

        for (int i = 0; i < dx.length; i++) {
            int checkX = x + dx[i];
            int checkY = y + dy[i];

            if (grid.inBounds(checkX, checkY) && grid.isDestructibleWall(checkX, checkY)) {
                return true;
            }
        }

        // Vérifier si le joueur adverse est à portée (avec une certaine probabilité)
        if (player1 != null && random.nextDouble() < 0.15) {
            int distance = Math.abs(player1.getX() - x) + Math.abs(player1.getY() - y);
            if (distance >= 2 && distance <= 4) {
                return isInBlastRange(player1.getX(), player1.getY(), x, y);
            }
        }

        return false;
    }

    private AIAction chooseSmartMove(int x, int y, Player player1, List<Bomb> bombs, List<Explosion> explosions) {
        List<AIAction> possibleMoves = getPossibleMoves(x, y, bombs, explosions);

        if (possibleMoves.isEmpty()) {
            return null;
        }

        // Stratégie basée sur la distance au joueur
        if (player1 != null && random.nextDouble() < 0.6) {
            int distance = Math.abs(player1.getX() - x) + Math.abs(player1.getY() - y);

            if (distance <= 2) {
                // Trop proche, s'éloigner
                return moveAwayFromPlayer(x, y, player1, possibleMoves);
            } else if (distance > 5) {
                // Trop loin, se rapprocher
                return moveTowardsPlayer(x, y, player1, possibleMoves);
            }
        }

        // Mouvement aléatoirement parmi les options sûres
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

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

    private boolean hasBombAt(int x, int y, List<Bomb> bombs) {
        return bombs.stream().anyMatch(bomb -> bomb.getX() == x && bomb.getY() == y);
    }

    private boolean isInExplosion(int x, int y, List<Explosion> explosions) {
        return explosions.stream().anyMatch(explosion -> explosion.getX() == x && explosion.getY() == y);
    }
}