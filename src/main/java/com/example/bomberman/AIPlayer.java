package com.example.bomberman;

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
    private static final long ACTION_COOLDOWN = 300_000_000; // 300ms entre les actions

    public AIPlayer(GameGrid grid, BombermanGame game) {
        this.grid = grid;
        this.game = game;
        this.random = new Random();
    }

    public AIAction getNextAction(List<Bomb> bombs, List<Explosion> explosions) {
        long currentTime = System.nanoTime();

        // Cooldown entre les actions pour éviter les mouvements trop rapides
        if (currentTime - lastActionTime < ACTION_COOLDOWN) {
            return AIAction.WAIT;
        }

        Player player2 = game.getPlayer2();
        Player player1 = game.getPlayer1();

        if (player2 == null) return AIAction.WAIT;

        int x = player2.getX();
        int y = player2.getY();

        // PRIORITÉ 1: Vérifier si l'IA est en danger immédiat
        if (isInImmediateDanger(x, y, bombs, explosions)) {
            AIAction escapeAction = findEscapeRoute(x, y, bombs, explosions);
            if (escapeAction != null) {
                lastActionTime = currentTime;
                return escapeAction;
            }
        }

        // PRIORITÉ 2: Placer une bombe si c'est stratégique (20% de chance)
        if (random.nextDouble() < 0.2) {
            if (shouldPlaceBomb(x, y, player1, bombs)) {
                lastActionTime = currentTime;
                return AIAction.PLACE_BOMB;
            }
        }

        // PRIORITÉ 3: Se déplacer vers un objectif ou aléatoirement
        AIAction moveAction = chooseMoveAction(x, y, player1, bombs, explosions);
        if (moveAction != null) {
            lastActionTime = currentTime;
            return moveAction;
        }

        return AIAction.WAIT;
    }

    private boolean isInImmediateDanger(int x, int y, List<Bomb> bombs, List<Explosion> explosions) {
        // Vérifier si l'IA est dans une explosion
        for (Explosion explosion : explosions) {
            if (explosion.getX() == x && explosion.getY() == y) {
                return true;
            }
        }

        // Vérifier si une bombe va exploser à proximité
        for (Bomb bomb : bombs) {
            if (bomb.getTimer() <= 30) { // Bombe va exploser bientôt
                int bombX = bomb.getX();
                int bombY = bomb.getY();

                // Vérifier si l'IA est dans la ligne d'explosion (horizontale ou verticale)
                if ((bombX == x && Math.abs(bombY - y) <= 2) ||
                        (bombY == y && Math.abs(bombX - x) <= 2)) {
                    return true;
                }
            }
        }

        return false;
    }

    private AIAction findEscapeRoute(int x, int y, List<Bomb> bombs, List<Explosion> explosions) {
        // Directions possibles : haut, bas, gauche, droite
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};
        AIAction[] actions = {AIAction.MOVE_UP, AIAction.MOVE_DOWN, AIAction.MOVE_LEFT, AIAction.MOVE_RIGHT};

        List<AIAction> safeMoves = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (isSafePosition(newX, newY, bombs, explosions)) {
                safeMoves.add(actions[i]);
            }
        }

        if (!safeMoves.isEmpty()) {
            return safeMoves.get(random.nextInt(safeMoves.size()));
        }

        return null;
    }

    private boolean isSafePosition(int x, int y, List<Bomb> bombs, List<Explosion> explosions) {
        // Vérifier les limites de la grille
        if (!grid.inBounds(x, y)) {
            return false;
        }

        // Vérifier les murs
        if (grid.isIndestructibleWall(x, y) || grid.isDestructibleWall(x, y)) {
            return false;
        }

        // Vérifier les bombes
        if (hasBombAt(x, y, bombs)) {
            return false;
        }

        // Vérifier les explosions
        if (isInExplosion(x, y, explosions)) {
            return false;
        }

        // Vérifier si la position sera dangereuse à cause d'une bombe
        for (Bomb bomb : bombs) {
            int bombX = bomb.getX();
            int bombY = bomb.getY();

            // Si la position est dans la ligne d'explosion d'une bombe
            if ((bombX == x && Math.abs(bombY - y) <= 2) ||
                    (bombY == y && Math.abs(bombX - x) <= 2)) {
                return false;
            }
        }

        return true;
    }

    private boolean shouldPlaceBomb(int x, int y, Player player1, List<Bomb> bombs) {
        // Ne pas placer de bombe s'il y en a déjà une ici
        if (hasBombAt(x, y, bombs)) {
            return false;
        }

        // Ne pas placer de bombe si on est déjà en danger
        if (isInImmediateDanger(x, y, bombs, new ArrayList<>())) {
            return false;
        }

        // Vérifier s'il y a des murs destructibles à proximité
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        for (int i = 0; i < 4; i++) {
            for (int range = 1; range <= 2; range++) { // Portée de la bombe
                int checkX = x + dx[i] * range;
                int checkY = y + dy[i] * range;

                if (!grid.inBounds(checkX, checkY)) {
                    break;
                }

                if (grid.isIndestructibleWall(checkX, checkY)) {
                    break;
                }

                if (grid.isDestructibleWall(checkX, checkY)) {
                    return true; // Il y a un mur destructible à détruire
                }
            }
        }

        // Vérifier si le joueur 1 est à portée (mais pas trop proche)
        if (player1 != null) {
            int distance = Math.abs(player1.getX() - x) + Math.abs(player1.getY() - y);
            if (distance >= 2 && distance <= 3) {
                // Vérifier si le joueur 1 est dans la ligne d'explosion
                if ((player1.getX() == x && Math.abs(player1.getY() - y) <= 2) ||
                        (player1.getY() == y && Math.abs(player1.getX() - x) <= 2)) {
                    return random.nextDouble() < 0.3; // 30% de chance d'attaquer
                }
            }
        }

        return false;
    }

    private AIAction chooseMoveAction(int x, int y, Player player1, List<Bomb> bombs, List<Explosion> explosions) {
        List<AIAction> possibleMoves = getPossibleMoves(x, y, bombs, explosions);

        if (possibleMoves.isEmpty()) {
            return null;
        }

        // Si le joueur 1 est proche, essayer de s'en rapprocher ou de s'en éloigner
        if (player1 != null && random.nextDouble() < 0.4) {
            AIAction targetedMove = moveTowardsOrAwayFromPlayer(x, y, player1, possibleMoves);
            if (targetedMove != null) {
                return targetedMove;
            }
        }

        // Sinon, mouvement aléatoirement
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    private AIAction moveTowardsOrAwayFromPlayer(int x, int y, Player player1, List<AIAction> possibleMoves) {
        int playerX = player1.getX();
        int playerY = player1.getY();
        int distance = Math.abs(playerX - x) + Math.abs(playerY - y);

        // Si trop proche (distance <= 2), s'éloigner
        // Si assez loin (distance > 4), se rapprocher
        boolean moveAway = distance <= 2;

        List<AIAction> preferredMoves = new ArrayList<>();

        for (AIAction move : possibleMoves) {
            int newX = x, newY = y;

            switch (move) {
                case MOVE_LEFT -> newX--;
                case MOVE_RIGHT -> newX++;
                case MOVE_UP -> newY--;
                case MOVE_DOWN -> newY++;
                default -> {
                    continue;
                }
            }

            int newDistance = Math.abs(playerX - newX) + Math.abs(playerY - newY);

            if (moveAway && newDistance > distance) {
                preferredMoves.add(move);
            } else if (!moveAway && newDistance < distance) {
                preferredMoves.add(move);
            }
        }

        if (!preferredMoves.isEmpty()) {
            return preferredMoves.get(random.nextInt(preferredMoves.size()));
        }

        return null;
    }

    private List<AIAction> getPossibleMoves(int x, int y, List<Bomb> bombs, List<Explosion> explosions) {
        List<AIAction> moves = new ArrayList<>();

        // Vérifier chaque direction
        if (isSafePosition(x - 1, y, bombs, explosions)) {
            moves.add(AIAction.MOVE_LEFT);
        }
        if (isSafePosition(x + 1, y, bombs, explosions)) {
            moves.add(AIAction.MOVE_RIGHT);
        }
        if (isSafePosition(x, y - 1, bombs, explosions)) {
            moves.add(AIAction.MOVE_UP);
        }
        if (isSafePosition(x, y + 1, bombs, explosions)) {
            moves.add(AIAction.MOVE_DOWN);
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