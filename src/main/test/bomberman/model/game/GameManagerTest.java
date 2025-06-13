package bomberman.model.game;

import bomberman.model.entities.GamePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

    private GameManager gameManager;
    private GameGrid mockGrid;

    @BeforeEach
    void setUp() {
        gameManager = new GameManager(2);
        mockGrid = new GameGrid(15, 13);
        mockGrid.generate();
    }

    @Nested
    class ConstructionTests {

        @Test
        void shouldCreateGameManagerWithValidPlayerCount() {
            assertEquals(2, gameManager.getPlayerCount());
            assertFalse(gameManager.isGameRunning());
            assertNotNull(gameManager.getPlayers());
            assertNotNull(gameManager.getBombSystem());
            assertNotNull(gameManager.getPowerUpSystem());
            assertNotNull(gameManager.getGameTimer());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 10})
        void shouldClampPlayerCountBetween2And4(int inputCount) {
            GameManager manager = new GameManager(inputCount);
            int expectedCount = Math.max(2, Math.min(4, inputCount));
            assertEquals(expectedCount, manager.getPlayerCount());
        }

        @Test
        void shouldInitializeWithCorrectDefaults() {
            assertFalse(gameManager.isAiMode());
            assertFalse(gameManager.isGameRunning());
            assertTrue(gameManager.getPlayers().isEmpty());
        }
    }

    @Nested
    class InitializationTests {

        @Test
        void shouldInitializeGameCorrectly() {
            gameManager.initializeGame(mockGrid);

            assertTrue(gameManager.isGameRunning());
            assertEquals(mockGrid, gameManager.getGrid());
            assertEquals(2, gameManager.getPlayers().size());
        }

        @Test
        void shouldInitializePlayersWithCorrectSpawnPositions() {
            gameManager.initializeGame(mockGrid);
            List<GamePlayer> players = gameManager.getPlayers();

            assertEquals(2, players.size());
            
            // Vérifier les positions de spawn par défaut
            GamePlayer player1 = players.get(0);
            assertEquals(1, player1.getPlayerNumber());
            assertEquals(1, player1.getPlayer().getX());
            assertEquals(1, player1.getPlayer().getY());

            GamePlayer player2 = players.get(1);
            assertEquals(2, player2.getPlayerNumber());
            assertEquals(13, player2.getPlayer().getX());
            assertEquals(11, player2.getPlayer().getY());
        }

        @Test
        void shouldInitializeAllPlayersForMaxCount() {
            GameManager manager = new GameManager(4);
            manager.initializeGame(mockGrid);
            
            List<GamePlayer> players = manager.getPlayers();
            assertEquals(4, players.size());

            // Vérifier toutes les positions de spawn
            int[][] expectedPositions = {{1, 1}, {13, 11}, {13, 1}, {1, 11}};
            for (int i = 0; i < 4; i++) {
                GamePlayer player = players.get(i);
                assertEquals(i + 1, player.getPlayerNumber());
                assertEquals(expectedPositions[i][0], player.getPlayer().getX());
                assertEquals(expectedPositions[i][1], player.getPlayer().getY());
            }
        }

        @Test
        void shouldClearSystemsOnInitialization() {
            // Simuler un état précédent
            gameManager.initializeGame(mockGrid);
            
            // Réinitialiser
            gameManager.initializeGame(mockGrid);
            
            assertTrue(gameManager.isGameRunning());
            assertNotNull(gameManager.getBombSystem());
            assertNotNull(gameManager.getPowerUpSystem());
        }
    }

    @Nested
    class GameStateTests {

        @BeforeEach
        void initializeGame() {
            gameManager.initializeGame(mockGrid);
        }

        @Test
        void shouldStartGameRunning() {
            assertTrue(gameManager.isGameRunning());
        }

        @Test
        void shouldAllowSettingGameRunningState() {
            gameManager.setGameRunning(false);
            assertFalse(gameManager.isGameRunning());

            gameManager.setGameRunning(true);
            assertTrue(gameManager.isGameRunning());
        }

        @Test
        void shouldNotUpdateWhenGameNotRunning() {
            gameManager.setGameRunning(false);
            
            // L'update ne devrait rien faire quand le jeu n'est pas en cours
            assertDoesNotThrow(() -> gameManager.update());
            assertFalse(gameManager.isGameRunning());
        }
    }

    @Nested
    class MovementTests {

        @BeforeEach
        void initializeGame() {
            gameManager.initializeGame(mockGrid);
        }

        @Test
        void shouldAllowValidMovement() {
            GamePlayer player = gameManager.getPlayers().get(0);
            
            // Position de départ (1,1), mouvement vers (2,1) devrait être valide
            assertTrue(gameManager.canPlayerMoveTo(player, 2, 1));
        }

        @Test
        void shouldBlockMovementToWalls() {
            GamePlayer player = gameManager.getPlayers().get(0);
            
            // Mouvement vers un mur indestructible
            assertFalse(gameManager.canPlayerMoveTo(player, 0, 0));
        }

        @Test
        void shouldBlockMovementOutOfBounds() {
            GamePlayer player = gameManager.getPlayers().get(0);
            
            assertFalse(gameManager.canPlayerMoveTo(player, -1, 0));
            assertFalse(gameManager.canPlayerMoveTo(player, 15, 0));
            assertFalse(gameManager.canPlayerMoveTo(player, 0, 13));
        }

        @Test
        void shouldBlockMovementForEliminatedPlayer() {
            GamePlayer player = gameManager.getPlayers().get(0);
            
            // Simuler l'élimination du joueur
            while (!player.getStats().isEliminated()) {
                player.getStats().takeDamage();
            }
            
            assertFalse(gameManager.canPlayerMoveTo(player, 2, 1));
        }
    }

    @Nested
    class BombTests {

        @BeforeEach
        void initializeGame() {
            gameManager.initializeGame(mockGrid);
        }

        @Test
        void shouldPlaceBombForValidPlayer() {
            GamePlayer player = gameManager.getPlayers().get(0);
            
            assertDoesNotThrow(() -> gameManager.placeBombForPlayer(player));
        }

        @Test
        void shouldNotPlaceBombForEliminatedPlayer() {
            GamePlayer player = gameManager.getPlayers().get(0);
            
            // Éliminer le joueur
            while (!player.getStats().isEliminated()) {
                player.getStats().takeDamage();
            }
            
            assertDoesNotThrow(() -> gameManager.placeBombForPlayer(player));
        }
    }

    @Nested
    class GameEndTests {

        @BeforeEach
        void initializeGame() {
            gameManager.initializeGame(mockGrid);
        }

        @Test
        void shouldDetectWinner() {
            List<GamePlayer> players = gameManager.getPlayers();
            GamePlayer player1 = players.get(0);
            GamePlayer player2 = players.get(1);

            // Éliminer le joueur 2
            while (!player2.getStats().isEliminated()) {
                player2.getStats().takeDamage();
            }

            gameManager.update();

            assertFalse(gameManager.isGameRunning());
            assertEquals("Joueur 1", gameManager.getWinnerText());
        }

        @Test
        void shouldDetectDraw() {
            List<GamePlayer> players = gameManager.getPlayers();
            
            // Éliminer tous les joueurs
            for (GamePlayer player : players) {
                while (!player.getStats().isEliminated()) {
                    player.getStats().takeDamage();
                }
            }

            gameManager.update();

            assertFalse(gameManager.isGameRunning());
            assertEquals("Match nul", gameManager.getWinnerText());
        }

        @Test
        void shouldContinueWithMultiplePlayersAlive() {
            // Avec 2 joueurs vivants, le jeu devrait continuer
            gameManager.update();
            assertTrue(gameManager.isGameRunning());
        }
    }

    @Nested
    class RestartTests {

        @BeforeEach
        void initializeGame() {
            gameManager.initializeGame(mockGrid);
        }

        @Test
        void shouldRestartGameCorrectly() {
            // Modifier l'état du jeu
            gameManager.setGameRunning(false);
            
            gameManager.restartGame();
            
            assertTrue(gameManager.isGameRunning());
        }

        @Test
        void shouldResetPlayersOnRestart() {
            List<GamePlayer> players = gameManager.getPlayers();
            GamePlayer player = players.get(0);
            
            // Endommager le joueur
            player.getStats().takeDamage();
            
            gameManager.restartGame();
            
            // Le joueur devrait être réinitialisé
            assertTrue(gameManager.isGameRunning());
        }
    }

    @Nested
    class GetterTests {

        @BeforeEach
        void initializeGame() {
            gameManager.initializeGame(mockGrid);
        }

        @Test
        void shouldReturnCorrectComponents() {
            assertNotNull(gameManager.getPlayers());
            assertNotNull(gameManager.getBombSystem());
            assertNotNull(gameManager.getPowerUpSystem());
            assertNotNull(gameManager.getGrid());
            assertNotNull(gameManager.getGameTimer());
            
            assertEquals(2, gameManager.getPlayerCount());
            assertFalse(gameManager.isAiMode());
        }

        @Test
        void shouldReturnNullAIPlayerByDefault() {
            assertNull(gameManager.getAiPlayer());
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void shouldHandleUpdateWithoutInitialization() {
            // Tenter de faire un update sans initialisation
            assertDoesNotThrow(() -> gameManager.update());
            assertFalse(gameManager.isGameRunning());
        }

        @Test
        void shouldHandleMultipleInitializations() {
            gameManager.initializeGame(mockGrid);
            assertTrue(gameManager.isGameRunning());
            assertEquals(2, gameManager.getPlayers().size());

            // Réinitialiser
            gameManager.initializeGame(mockGrid);
            assertTrue(gameManager.isGameRunning());
            assertEquals(2, gameManager.getPlayers().size());
        }

        @Test
        void shouldHandleWinnerTextWithoutPlayers() {
            // Sans initialisation, pas de joueurs
            assertEquals("Match nul", gameManager.getWinnerText());
        }

        @Test
        void shouldHandleRestartWithoutInitialization() {
            assertDoesNotThrow(() -> gameManager.restartGame());
            assertTrue(gameManager.isGameRunning());
        }
    }

    @Nested
    class AITests {

        @Test
        void shouldInitializeWithoutAIByDefault() {
            assertFalse(gameManager.isAiMode());
            assertNull(gameManager.getAiPlayer());
        }
    }

    @Nested
    class PlayerCountVariationTests {

        @ParameterizedTest
        @ValueSource(ints = {2, 3, 4})
        void shouldInitializeCorrectNumberOfPlayers(int count) {
            GameManager manager = new GameManager(count);
            manager.initializeGame(mockGrid);
            
            assertEquals(count, manager.getPlayers().size());
            assertEquals(count, manager.getPlayerCount());
        }

        @Test
        void shouldAssignUniquePlayerNumbers() {
            GameManager manager = new GameManager(4);
            manager.initializeGame(mockGrid);
            
            List<GamePlayer> players = manager.getPlayers();
            for (int i = 0; i < players.size(); i++) {
                assertEquals(i + 1, players.get(i).getPlayerNumber());
            }
        }
    }
}