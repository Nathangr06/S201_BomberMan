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

    public AIPlayer(GameGrid grid, BombermanGame game) {
        this.grid = grid;
        this.game = game;
        this.random = new Random();
    }

    public AIAction getNextAction(List<Bomb> bombs, List<Explosion> explosions) {
        Player player2 = game.getPlayer2();
        Player player1 = game.getPlayer1();

        if (player2 == null) return AIAction.WAIT;

        int x = player2.getX();
        int y = player2.getY();

        // Vérifier si l'IA est en danger (dans une explosion)
        if (isInDanger(x, y, explosions)) {
            AIAction escapeAction = findEscapeRoute(x, y, bombs, explosions);
            if (escapeAction != null) {
                return escapeAction;
            }
        }

        // 30% de chance de placer une bombe si c'est stratégique
        if (random.nextDouble() < 0.3) {
            if (shouldPlaceBomb(x, y, player1)) {
                return AIAction.PLACE_BOMB;
            }
        }

        // 70% du temps, se déplacer aléatoirement
        List<AIAction> possibleMoves = getPossibleMoves(x, y, bombs);

        if (!possibleMoves.isEmpty()) {
            return possibleMoves.get(random.nextInt(possibleMoves.size()));
        }

        return AIAction.WAIT;
    }

    private boolean isInDanger(int x, int y, List<Explosion> explosions) {
        // Vérifier si le joueur est dans une explosion
        for (Explosion explosion : explosions) {
            if (explosion.getX() == x && explosion.getY() == y) {
                return true;
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

            if (grid.isWalkable(newX, newY) && !hasBombAt(newX, newY, bombs) && !isInExplosion(newX, newY, explosions)) {
                safeMoves.add(actions[i]);
            }
        }

        if (!safeMoves.isEmpty()) {
            return safeMoves.get(random.nextInt(safeMoves.size()));
        }

        return null;
    }

    private boolean shouldPlaceBomb(int x, int y, Player player1) {
        // Placer une bombe si :
        // 1. Il y a un mur destructible à proximité
        // 2. Le joueur 1 est à portée (mais pas trop proche pour éviter le suicide)

        // Vérifier les murs destructibles autour
        int[] dx = {0, 1, 0, -1, 1, -1, 1, -1};
        int[] dy = {-1, 0, 1, 0, -1, -1, 1, 1};

        for (int i = 0; i < 8; i++) {
            int checkX = x + dx[i];
            int checkY = y + dy[i];
            if (grid.isDestructibleWall(checkX, checkY)) {
                return true;
            }
        }

        // Vérifier si le joueur 1 est à portée (distance 2-3 cases)
        if (player1 != null) {
            int distance = Math.abs(player1.getX() - x) + Math.abs(player1.getY() - y);
            if (distance >= 2 && distance <= 3) {
                return random.nextDouble() < 0.4; // 40% de chance
            }
        }

        return false;
    }

    private List<AIAction> getPossibleMoves(int x, int y, List<Bomb> bombs) {
        List<AIAction> moves = new ArrayList<>();

        // Vérifier chaque direction
        if (grid.isWalkable(x - 1, y) && !hasBombAt(x - 1, y, bombs)) {
            moves.add(AIAction.MOVE_LEFT);
        }
        if (grid.isWalkable(x + 1, y) && !hasBombAt(x + 1, y, bombs)) {
            moves.add(AIAction.MOVE_RIGHT);
        }
        if (grid.isWalkable(x, y - 1) && !hasBombAt(x, y - 1, bombs)) {
            moves.add(AIAction.MOVE_UP);
        }
        if (grid.isWalkable(x, y + 1) && !hasBombAt(x, y + 1, bombs)) {
            moves.add(AIAction.MOVE_DOWN);
        }

        // Ajouter parfois l'action d'attendre
        if (random.nextDouble() < 0.1) {
            moves.add(AIAction.WAIT);
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