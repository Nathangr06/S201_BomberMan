package bomberman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

public class GameRenderer {
    private GraphicsContext gc;
    private TextureManager textureManager;

    public GameRenderer(GraphicsContext gc, TextureManager textureManager) {
        this.gc = gc;
        this.textureManager = textureManager;
    }

    public void renderGame(GameGrid grid, List<GamePlayer> players, BombSystem bombSystem,
                           PowerUpSystem powerUpSystem, GameTimer gameTimer,
                           int playerCount, boolean aiMode) {
        // Fond
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);

        // Timer
        renderTimer(gameTimer, players, playerCount, aiMode);

        // Terrain
        gc.setFill(Color.GREEN);
        gc.fillRect(0, GameConstants.TIMER_HEIGHT,
                GameConstants.CANVAS_WIDTH,
                GameConstants.CANVAS_HEIGHT - GameConstants.TIMER_HEIGHT);

        // Grille
        if (grid != null) {
            gc.save();
            gc.translate(0, GameConstants.TIMER_HEIGHT);
            grid.render(gc);
            gc.restore();
        }

        // Éléments du jeu
        renderExplosions(bombSystem.getExplosions());
        renderBombs(bombSystem);
        renderPowerUps(powerUpSystem.getPowerUps());
        renderPlayers(players);
        renderGrid();
    }

    private void renderExplosions(List<Explosion> explosions) {
        Image explosionTexture = textureManager.getTexture("explosion");
        for (Explosion explosion : explosions) {
            int x = explosion.getX() * GameConstants.TILE_SIZE;
            int y = explosion.getY() * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;
            if (explosionTexture != null) {
                gc.drawImage(explosionTexture, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
            } else {
                gc.setFill(Color.ORANGE);
                gc.fillRect(x + 5, y + 5, GameConstants.TILE_SIZE - 10, GameConstants.TILE_SIZE - 10);
            }
        }
    }

    private void renderBombs(BombSystem bombSystem) {
        Image bombTexture = textureManager.getTexture("bomb");

        // Bombes statiques
        for (Bomb bomb : bombSystem.getBombs()) {
            boolean isMoving = bombSystem.getMovingBombs().stream()
                    .anyMatch(mb -> mb.getBomb() == bomb);

            if (!isMoving) {
                int x = bomb.getX() * GameConstants.TILE_SIZE;
                int y = bomb.getY() * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;

                if (bombTexture != null) {
                    gc.drawImage(bombTexture, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
                } else {
                    gc.setFill(Color.BLACK);
                    gc.fillOval(x + 8, y + 8, GameConstants.TILE_SIZE - 16, GameConstants.TILE_SIZE - 16);
                }
            }
        }

        // Bombes en mouvement
        for (BombSystem.MovingBomb movingBomb : bombSystem.getMovingBombs()) {
            double x = movingBomb.getVisualX();
            double y = movingBomb.getVisualY();

            if (bombTexture != null) {
                gc.drawImage(bombTexture, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
            } else {
                gc.setFill(Color.BLACK);
                gc.fillOval(x + 8, y + 8, GameConstants.TILE_SIZE - 16, GameConstants.TILE_SIZE - 16);
            }
        }
    }

    private void renderPowerUps(List<PowerUpSystem.PowerUp> powerUps) {
        for (PowerUpSystem.PowerUp powerUp : powerUps) {
            int x = powerUp.getX() * GameConstants.TILE_SIZE;
            int y = powerUp.getY() * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;

            gc.setFill(Color.WHITE);
            gc.fillRect(x + 5, y + 5, GameConstants.TILE_SIZE - 10, GameConstants.TILE_SIZE - 10);

            gc.setFill(powerUp.getType().getColor());
            gc.fillRect(x + 8, y + 8, GameConstants.TILE_SIZE - 16, GameConstants.TILE_SIZE - 16);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            String label = powerUp.getType().getLabel().substring(0, 1).toUpperCase();
            gc.fillText(label, x + GameConstants.TILE_SIZE / 2 - 3, y + GameConstants.TILE_SIZE / 2 + 3);
        }
    }

    private void renderPlayers(List<GamePlayer> players) {
        Image[] playerTextures = {
                textureManager.getTexture("player"),
                textureManager.getTexture("player2"),
                textureManager.getTexture("player3"),
                textureManager.getTexture("player4")
        };

        for (GamePlayer gamePlayer : players) {
            if (!gamePlayer.getStats().isEliminated()) {
                int playerNum = gamePlayer.getPlayerNumber();
                Image texture = (playerNum - 1 < playerTextures.length) ?
                        playerTextures[playerNum - 1] : playerTextures[0];
                Color fallbackColor = GameConstants.PLAYER_COLORS[Math.min(playerNum - 1,
                        GameConstants.PLAYER_COLORS.length - 1)];

                renderPlayer(gamePlayer, texture, fallbackColor);
            }
        }
    }

    private void renderPlayer(GamePlayer gamePlayer, Image texture, Color fallbackColor) {
        int invincibilityTimer = gamePlayer.getStats().getInvincibilityTimer();
        boolean shouldRender = invincibilityTimer <= 0 || (invincibilityTimer / 5) % 2 != 0;

        if (shouldRender) {
            double x = gamePlayer.getVisualX();
            double y = gamePlayer.getVisualY();

            if (texture != null) {
                gc.drawImage(texture, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
            } else {
                if (invincibilityTimer > 0) {
                    gc.setFill(Color.color(fallbackColor.getRed(), fallbackColor.getGreen(),
                            fallbackColor.getBlue(), 0.5));
                } else {
                    gc.setFill(fallbackColor);
                }
                gc.fillOval(x + 5, y + 5, GameConstants.TILE_SIZE - 10, GameConstants.TILE_SIZE - 10);
            }
        }
    }

    private void renderGrid() {
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(1);
        for (int x = 0; x <= GameConstants.GRID_WIDTH; x++) {
            gc.strokeLine(x * GameConstants.TILE_SIZE, GameConstants.TIMER_HEIGHT,
                    x * GameConstants.TILE_SIZE, GameConstants.CANVAS_HEIGHT);
        }
        for (int y = 0; y <= GameConstants.GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT,
                    GameConstants.CANVAS_WIDTH, y * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT);
        }
    }

    private void renderTimer(GameTimer gameTimer, List<GamePlayer> players,
                             int playerCount, boolean aiMode) {
        gc.setFill(Color.web("#FF8C00"));
        gc.fillRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.TIMER_HEIGHT);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.TIMER_HEIGHT);

        double timerBoxWidth = 80;
        double timerBoxHeight = 30;
        renderTimerBox((GameConstants.CANVAS_WIDTH - timerBoxWidth) / 2,
                (GameConstants.TIMER_HEIGHT - timerBoxHeight) / 2,
                timerBoxWidth, timerBoxHeight, gameTimer);

        if (playerCount == 2) {
            renderTwoPlayersInfo(players, aiMode);
        } else {
            renderFourPlayersInfo(players);
        }
    }

    private void renderTimerBox(double x, double y, double width, double height, GameTimer gameTimer) {
        gc.setFill(Color.BLACK);
        gc.fillRect(x, y, width, height);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        String timeText = gameTimer.getFormattedTime();

        double textX = x + (width - timeText.length() * 9) / 2;
        double textY = y + height / 2 + 6;
        gc.fillText(timeText, textX, textY);
    }

    private void renderTwoPlayersInfo(List<GamePlayer> players, boolean aiMode) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Joueur 1
        GamePlayer player1 = players.get(0);
        if (!player1.getStats().isEliminated()) {
            gc.fillText("Joueur 1", 20, GameConstants.TIMER_HEIGHT / 2 - 5);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("Vies: " + player1.getStats().getLives(), 20, GameConstants.TIMER_HEIGHT / 2 + 15);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            String p1Powers = "R:" + player1.getStats().getBombRange() +
                    " S:" + String.format("%.1f", player1.getStats().getSpeed() / GameConstants.MOVEMENT_SPEED) +
                    (player1.getStats().canPushBombs() ? " P" : "");
            gc.fillText(p1Powers, 20, GameConstants.TIMER_HEIGHT / 2 + 30);
        } else {
            gc.setFill(Color.RED);
            gc.fillText("J1: ÉLIMINÉ", 20, GameConstants.TIMER_HEIGHT / 2 + 5);
        }

        // Joueur 2
        if (players.size() > 1) {
            GamePlayer player2 = players.get(1);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            String player2Text = aiMode ? "IA" : "Joueur 2";
            double textWidth = player2Text.length() * 9;

            if (!player2.getStats().isEliminated()) {
                gc.fillText(player2Text, GameConstants.CANVAS_WIDTH - textWidth - 20,
                        GameConstants.TIMER_HEIGHT / 2 - 5);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                String livesText = "Vies: " + player2.getStats().getLives();
                double livesWidth = livesText.length() * 8;
                gc.fillText(livesText, GameConstants.CANVAS_WIDTH - livesWidth - 20,
                        GameConstants.TIMER_HEIGHT / 2 + 15);
                gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
                String p2Powers = "R:" + player2.getStats().getBombRange() +
                        " S:" + String.format("%.1f", player2.getStats().getSpeed() / GameConstants.MOVEMENT_SPEED) +
                        (player2.getStats().canPushBombs() ? " P" : "");
                double p2PowersWidth = p2Powers.length() * 6;
                gc.fillText(p2Powers, GameConstants.CANVAS_WIDTH - p2PowersWidth - 20,
                        GameConstants.TIMER_HEIGHT / 2 + 30);
            } else {
                gc.setFill(Color.RED);
                gc.fillText("J2: ÉLIMINÉ", GameConstants.CANVAS_WIDTH - 100,
                        GameConstants.TIMER_HEIGHT / 2 + 5);
            }
        }
    }

    private void renderFourPlayersInfo(List<GamePlayer> players) {
        for (int i = 0; i < players.size() && i < 4; i++) {
            GamePlayer player = players.get(i);
            int playerNum = i + 1;

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            double x = (i % 2 == 0) ? 10 : GameConstants.CANVAS_WIDTH - 50;
            double y = (i < 2) ? 20 : 50;

            if (!player.getStats().isEliminated()) {
                gc.fillText("J" + playerNum + ":" + player.getStats().getLives(), x, y);
                gc.setFont(Font.font("Arial", FontWeight.NORMAL, 8));
                gc.fillText("R:" + player.getStats().getBombRange(), x, y + 15);
            } else {
                gc.setFill(Color.RED);
                gc.fillText("J" + playerNum + ": ÉLIMINÉ", x, y + 5);
                gc.setFill(Color.WHITE);
            }
        }
    }

    public void renderGameOver(String winner) {
        gc.setFill(new Color(0, 0, 0, 0.8));
        gc.fillRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.fillText(winner + " gagne !",
                GameConstants.CANVAS_WIDTH / 2 - 100,
                GameConstants.CANVAS_HEIGHT / 2);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        gc.fillText("Appuyez sur ESPACE pour rejouer",
                GameConstants.CANVAS_WIDTH / 2 - 140,
                GameConstants.CANVAS_HEIGHT / 2 + 40);
    }
}

