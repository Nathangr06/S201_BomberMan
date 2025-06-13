package bomberman.controller.game;

import bomberman.model.entities.Bomb;
import bomberman.model.entities.Explosion;
import bomberman.model.entities.GamePlayer;
import bomberman.model.game.BombSystem;
import bomberman.model.game.GameGrid;
import bomberman.model.game.GameTimer;
import bomberman.model.game.PowerUpSystem;
import bomberman.utils.GameConstants;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

/**
 * Moteur de rendu principal pour le jeu Bomberman.
 * Cette classe centralise toute la logique d'affichage du jeu, depuis le rendu de la grille
 * jusqu'aux éléments dynamiques comme les joueurs, bombes et explosions.
 * Elle gère également l'interface utilisateur avec le timer, les statistiques des joueurs
 * et les écrans de fin de partie.
 *
 * <p>Responsabilités principales :</p>
 * <ul>
 *   <li>Rendu de tous les éléments visuels du jeu</li>
 *   <li>Gestion des textures et fallbacks</li>
 *   <li>Affichage adaptatif selon le nombre de joueurs</li>
 *   <li>Interface utilisateur temps réel (timer, stats)</li>
 *   <li>Effets visuels (invincibilité, animations)</li>
 * </ul>
 *
 * <p>Architecture de rendu :</p>
 * Le rendu s'effectue par couches successives :
 * fond → grille → explosions → bombes → power-ups → joueurs → UI
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class GameRenderer {

    /** Contexte graphique JavaFX pour les opérations de dessin */
    private GraphicsContext gc;

    /** Gestionnaire de textures pour les sprites du jeu */
    private TextureManager textureManager;

    /**
     * Constructeur du moteur de rendu.
     * Initialise le renderer avec le contexte graphique et le gestionnaire de textures.
     *
     * @param gc Le contexte graphique JavaFX pour le dessin
     * @param textureManager Le gestionnaire de textures pour les sprites
     */
    public GameRenderer(GraphicsContext gc, TextureManager textureManager) {
        this.gc = gc;
        this.textureManager = textureManager;
    }

    /**
     * Effectue le rendu complet d'une frame du jeu.
     * Cette méthode orchestre l'affichage de tous les éléments du jeu dans l'ordre correct
     * pour assurer un rendu cohérent et optimisé.
     *
     * <p>Ordre de rendu :</p>
     * <ol>
     *   <li>Fond et terrain</li>
     *   <li>Grille de jeu</li>
     *   <li>Explosions (sous les autres éléments)</li>
     *   <li>Bombes (statiques et en mouvement)</li>
     *   <li>Power-ups</li>
     *   <li>Joueurs</li>
     *   <li>Interface utilisateur (timer, stats)</li>
     * </ol>
     *
     * @param grid La grille de jeu contenant les murs et obstacles
     * @param players La liste des joueurs à afficher
     * @param bombSystem Le système de bombes contenant toutes les bombes actives
     * @param powerUpSystem Le système de power-ups avec les bonus disponibles
     * @param gameTimer Le timer de jeu pour l'affichage du temps
     * @param playerCount Le nombre total de joueurs pour adapter l'interface
     */
    public void renderGame(GameGrid grid, List<GamePlayer> players, BombSystem bombSystem,
                           PowerUpSystem powerUpSystem, GameTimer gameTimer,
                           int playerCount) {
        // Fond
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);

        // Timer
        renderTimer(gameTimer, players, playerCount);

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

    /**
     * Affiche toutes les explosions actives sur le terrain.
     * Utilise les textures si disponibles, sinon utilise un rendu de fallback
     * avec des rectangles orange.
     *
     * @param explosions La liste des explosions à afficher
     */
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

    /**
     * Affiche toutes les bombes présentes sur le terrain.
     * Gère séparément les bombes statiques et les bombes en mouvement (poussées).
     * Utilise les textures si disponibles, sinon dessine des cercles noirs.
     *
     * @param bombSystem Le système de bombes contenant toutes les bombes actives
     */
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

    /**
     * Affiche tous les power-ups disponibles sur le terrain.
     * Chaque power-up est représenté par un carré coloré avec une lettre
     * correspondant au type de bonus (R=Range, S=Speed, P=Push, etc.).
     *
     * @param powerUps La liste des power-ups à afficher
     */
    private void renderPowerUps(List<PowerUpSystem.PowerUp> powerUps) {
        for (PowerUpSystem.PowerUp powerUp : powerUps) {
            int x = powerUp.getX() * GameConstants.TILE_SIZE;
            int y = powerUp.getY() * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT;

            // Fond blanc
            gc.setFill(Color.WHITE);
            gc.fillRect(x + 5, y + 5, GameConstants.TILE_SIZE - 10, GameConstants.TILE_SIZE - 10);

            // Couleur du power-up
            gc.setFill(powerUp.getType().getColor());
            gc.fillRect(x + 8, y + 8, GameConstants.TILE_SIZE - 16, GameConstants.TILE_SIZE - 16);

            // Lettre identificatrice
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            String label = powerUp.getType().getLabel().substring(0, 1).toUpperCase();
            gc.fillText(label, x + GameConstants.TILE_SIZE / 2 - 3, y + GameConstants.TILE_SIZE / 2 + 3);
        }
    }

    /**
     * Affiche tous les joueurs actifs sur le terrain.
     * Gère les textures spécifiques à chaque joueur et les couleurs de fallback.
     * Exclut automatiquement les joueurs éliminés du rendu.
     *
     * @param players La liste des joueurs à afficher
     */
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

    /**
     * Affiche un joueur individuel avec gestion des effets visuels.
     * Gère l'effet de clignotement pendant l'invincibilité et utilise
     * la texture ou la couleur de fallback selon la disponibilité.
     *
     * @param gamePlayer Le joueur à afficher
     * @param texture La texture du joueur (peut être null)
     * @param fallbackColor La couleur de fallback si pas de texture
     */
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

    /**
     * Affiche la grille de jeu avec les lignes de séparation.
     * Dessine un quadrillage vert foncé par-dessus le terrain pour améliorer
     * la lisibilité et délimiter les cases de jeu.
     */
    private void renderGrid() {
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(1);

        // Lignes verticales
        for (int x = 0; x <= GameConstants.GRID_WIDTH; x++) {
            gc.strokeLine(x * GameConstants.TILE_SIZE, GameConstants.TIMER_HEIGHT,
                    x * GameConstants.TILE_SIZE, GameConstants.CANVAS_HEIGHT);
        }

        // Lignes horizontales
        for (int y = 0; y <= GameConstants.GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT,
                    GameConstants.CANVAS_WIDTH, y * GameConstants.TILE_SIZE + GameConstants.TIMER_HEIGHT);
        }
    }

    /**
     * Affiche la barre de timer et les informations des joueurs.
     * Adapte automatiquement l'affichage selon le nombre de joueurs
     * (interface différente pour 2 ou 4 joueurs).
     *
     * @param gameTimer Le timer de jeu pour afficher le temps restant
     * @param players La liste des joueurs pour afficher leurs statistiques
     * @param playerCount Le nombre total de joueurs pour adapter l'interface
     */
    private void renderTimer(GameTimer gameTimer, List<GamePlayer> players,
                             int playerCount) {
        // Fond orange de la barre de timer
        gc.setFill(Color.web("#FF8C00"));
        gc.fillRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.TIMER_HEIGHT);

        // Bordure noire
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.TIMER_HEIGHT);

        // Boîte de timer centrée
        double timerBoxWidth = 80;
        double timerBoxHeight = 30;
        renderTimerBox((GameConstants.CANVAS_WIDTH - timerBoxWidth) / 2,
                (GameConstants.TIMER_HEIGHT - timerBoxHeight) / 2,
                timerBoxWidth, timerBoxHeight, gameTimer);

        // Informations des joueurs selon le nombre
        if (playerCount == 2) {
            renderTwoPlayersInfo(players);
        } else {
            renderFourPlayersInfo(players);
        }
    }

    /**
     * Affiche la boîte du timer au centre de la barre supérieure.
     * Dessine un rectangle noir avec bordure blanche contenant le temps formaté.
     *
     * @param x Position X de la boîte
     * @param y Position Y de la boîte
     * @param width Largeur de la boîte
     * @param height Hauteur de la boîte
     * @param gameTimer Le timer pour récupérer le temps formaté
     */
    private void renderTimerBox(double x, double y, double width, double height, GameTimer gameTimer) {
        // Fond noir
        gc.setFill(Color.BLACK);
        gc.fillRect(x, y, width, height);

        // Bordure blanche
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);

        // Texte du timer
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        String timeText = gameTimer.getFormattedTime();

        double textX = x + (width - timeText.length() * 9) / 2;
        double textY = y + height / 2 + 6;
        gc.fillText(timeText, textX, textY);
    }

    /**
     * Affiche les informations détaillées pour une partie à 2 joueurs.
     * Affiche le nom, les vies et les capacités de chaque joueur
     * de part et d'autre du timer central.
     *
     * <p>Informations affichées :</p>
     * <ul>
     *   <li>Nom du joueur</li>
     *   <li>Nombre de vies restantes</li>
     *   <li>Portée des bombes (R)</li>
     *   <li>Vitesse relative (S)</li>
     *   <li>Capacité de pousser les bombes (P)</li>
     * </ul>
     *
     * @param players La liste des joueurs (doit contenir au moins 2 joueurs)
     */
    private void renderTwoPlayersInfo(List<GamePlayer> players) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Joueur 1 (à gauche)
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

        // Joueur 2 (à droite)
        if (players.size() > 1) {
            GamePlayer player2 = players.get(1);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            String player2Text ="Joueur 2";
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

    /**
     * Affiche les informations compactes pour une partie à 4 joueurs.
     * Utilise un format condensé pour afficher les statistiques essentielles
     * de chaque joueur dans les coins de la barre de timer.
     *
     * <p>Disposition :</p>
     * <pre>
     * J1: 3  |  Timer  |  J2: 2
     * R: 2   |         |  R: 1
     * -------|---------|-------
     * J3: 1  |         |  J4: 3
     * R: 3   |         |  R: 2
     * </pre>
     *
     * @param players La liste des joueurs (jusqu'à 4 joueurs)
     */
    private void renderFourPlayersInfo(List<GamePlayer> players) {
        for (int i = 0; i < players.size() && i < 4; i++) {
            GamePlayer player = players.get(i);
            int playerNum = i + 1;

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            // Positionnement en quadrants
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

    /**
     * Affiche l'écran de fin de partie avec le gagnant.
     * Superpose un fond semi-transparent noir avec le texte de victoire
     * et les instructions pour rejouer.
     *
     * @param winner Le nom du gagnant à afficher
     */
    public void renderGameOver(String winner) {
        // Fond semi-transparent
        gc.setFill(new Color(0, 0, 0, 0.8));
        gc.fillRect(0, 0, GameConstants.CANVAS_WIDTH, GameConstants.CANVAS_HEIGHT);

        // Texte de victoire
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.fillText(winner + " gagne !",
                GameConstants.CANVAS_WIDTH / 2 - 100,
                GameConstants.CANVAS_HEIGHT / 2);

        // Instructions pour rejouer
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        gc.fillText("Appuyez sur ESPACE pour rejouer",
                GameConstants.CANVAS_WIDTH / 2 - 140,
                GameConstants.CANVAS_HEIGHT / 2 + 40);
    }
}