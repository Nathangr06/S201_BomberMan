import bomberman.model.game.GameGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class GameGridTest {

    private GameGrid gameGrid;
    private static final int TEST_WIDTH = 15;
    private static final int TEST_HEIGHT = 13;

    @BeforeEach
    void setUp() {
        gameGrid = new GameGrid(TEST_WIDTH, TEST_HEIGHT);
    }

    @Nested
    class ConstructionTests {

        @Test
        void shouldCreateGridWithCorrectDimensions() {
            assertEquals(TEST_WIDTH, gameGrid.getWidth());
            assertEquals(TEST_HEIGHT, gameGrid.getHeight());
        }

        @Test
        void shouldInitializeAllCellsToEmpty() {
            for (int y = 0; y < TEST_HEIGHT; y++) {
                for (int x = 0; x < TEST_WIDTH; x++) {
                    assertEquals(GameGrid.EMPTY, gameGrid.getCellType(x, y));
                }
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {5, 10, 20, 25})
        void shouldCreateGridWithDifferentSizes(int size) {
            GameGrid grid = new GameGrid(size, size);
            assertEquals(size, grid.getWidth());
            assertEquals(size, grid.getHeight());
        }
    }

    @Nested  
    class GenerationTests {

        @Test
        void shouldPlaceIndestructibleWallsOnBorders() {
            gameGrid.generate();

            // Vérifier les bordures horizontales
            for (int x = 0; x < TEST_WIDTH; x++) {
                assertEquals(GameGrid.WALL_INDESTRUCTIBLE, gameGrid.getCellType(x, 0));
                assertEquals(GameGrid.WALL_INDESTRUCTIBLE, gameGrid.getCellType(x, TEST_HEIGHT - 1));
            }

            // Vérifier les bordures verticales
            for (int y = 0; y < TEST_HEIGHT; y++) {
                assertEquals(GameGrid.WALL_INDESTRUCTIBLE, gameGrid.getCellType(0, y));
                assertEquals(GameGrid.WALL_INDESTRUCTIBLE, gameGrid.getCellType(TEST_WIDTH - 1, y));
            }
        }

        @Test
        void shouldPlaceIndestructibleWallsAtEvenPositions() {
            gameGrid.generate();

            for (int y = 2; y < TEST_HEIGHT - 1; y += 2) {
                for (int x = 2; x < TEST_WIDTH - 1; x += 2) {
                    assertEquals(GameGrid.WALL_INDESTRUCTIBLE, gameGrid.getCellType(x, y));
                }
            }
        }

        @Test
        void shouldClearStartingArea() {
            gameGrid.generate();

            assertEquals(GameGrid.EMPTY, gameGrid.getCellType(1, 1));
            assertEquals(GameGrid.EMPTY, gameGrid.getCellType(1, 2));
            assertEquals(GameGrid.EMPTY, gameGrid.getCellType(2, 1));
        }

        @Test
        void shouldGenerateConsistentGrid() {
            gameGrid.generate();
            
            // Vérifier que la structure de base est respectée
            assertTrue(gameGrid.isIndestructibleWall(0, 0));
            assertTrue(gameGrid.isIndestructibleWall(TEST_WIDTH - 1, TEST_HEIGHT - 1));
            assertTrue(gameGrid.isWalkable(1, 1));
        }
    }

    @Nested
    class BoundsCheckingTests {

        @ParameterizedTest
        @CsvSource({
            "0, 0, true",
            "5, 5, true", 
            "14, 12, true",
            "-1, 0, false",
            "0, -1, false",
            "15, 12, false",
            "14, 13, false",
            "-5, -5, false"
        })
        void shouldCheckBoundsCorrectly(int x, int y, boolean expected) {
            assertEquals(expected, gameGrid.inBounds(x, y));
        }

        @Test
        void shouldReturnInvalidValueForOutOfBounds() {
            assertEquals(-1, gameGrid.getCellType(-1, 0));
            assertEquals(-1, gameGrid.getCellType(0, -1));
            assertEquals(-1, gameGrid.getCellType(TEST_WIDTH, 0));
            assertEquals(-1, gameGrid.getCellType(0, TEST_HEIGHT));
        }
    }

    @Nested
    class CellTypeTests {

        @Test
        void shouldSetAndGetCellTypes() {
            gameGrid.setEmpty(5, 5);
            assertEquals(GameGrid.EMPTY, gameGrid.getCellType(5, 5));

            gameGrid.setIndestructibleWall(5, 5);
            assertEquals(GameGrid.WALL_INDESTRUCTIBLE, gameGrid.getCellType(5, 5));

            gameGrid.setDestructibleWall(5, 5);
            assertEquals(GameGrid.WALL_DESTRUCTIBLE, gameGrid.getCellType(5, 5));
        }

        @Test
        void shouldIgnoreSetOperationsOutOfBounds() {
            gameGrid.setEmpty(-1, 0);
            gameGrid.setIndestructibleWall(TEST_WIDTH, 0);
            gameGrid.setDestructibleWall(0, TEST_HEIGHT);
            
            // Aucune exception ne devrait être levée
            assertDoesNotThrow(() -> {
                gameGrid.setEmpty(-1, -1);
                gameGrid.setIndestructibleWall(100, 100);
            });
        }

        @Test
        void shouldSetCellTypeWithGenericMethod() {
            gameGrid.setCellType(5, 5, GameGrid.EMPTY);
            assertEquals(GameGrid.EMPTY, gameGrid.getCellType(5, 5));

            gameGrid.setCellType(5, 5, GameGrid.WALL_INDESTRUCTIBLE);
            assertEquals(GameGrid.WALL_INDESTRUCTIBLE, gameGrid.getCellType(5, 5));

            gameGrid.setCellType(5, 5, GameGrid.WALL_DESTRUCTIBLE);
            assertEquals(GameGrid.WALL_DESTRUCTIBLE, gameGrid.getCellType(5, 5));
        }

        @Test
        void shouldIgnoreInvalidCellTypes() {
            gameGrid.setCellType(5, 5, GameGrid.EMPTY);
            gameGrid.setCellType(5, 5, 999); // Type invalide
            assertEquals(GameGrid.EMPTY, gameGrid.getCellType(5, 5)); // Ne devrait pas changer
        }
    }

    @Nested
    class WalkabilityTests {

        @Test
        void shouldIdentifyWalkableCells() {
            gameGrid.setEmpty(5, 5);
            assertTrue(gameGrid.isWalkable(5, 5));

            gameGrid.setIndestructibleWall(5, 5);
            assertFalse(gameGrid.isWalkable(5, 5));

            gameGrid.setDestructibleWall(5, 5);
            assertFalse(gameGrid.isWalkable(5, 5));
        }

        @Test
        void shouldReturnFalseForOutOfBoundsWalkability() {
            assertFalse(gameGrid.isWalkable(-1, 0));
            assertFalse(gameGrid.isWalkable(0, -1));
            assertFalse(gameGrid.isWalkable(TEST_WIDTH, 0));
            assertFalse(gameGrid.isWalkable(0, TEST_HEIGHT));
        }
    }

    @Nested
    class WallTypeTests {

        @Test
        void shouldIdentifyIndestructibleWalls() {
            gameGrid.setIndestructibleWall(5, 5);
            assertTrue(gameGrid.isIndestructibleWall(5, 5));
            assertFalse(gameGrid.isDestructibleWall(5, 5));

            gameGrid.setEmpty(5, 5);
            assertFalse(gameGrid.isIndestructibleWall(5, 5));
        }

        @Test
        void shouldIdentifyDestructibleWalls() {
            gameGrid.setDestructibleWall(5, 5);
            assertTrue(gameGrid.isDestructibleWall(5, 5));
            assertFalse(gameGrid.isIndestructibleWall(5, 5));

            gameGrid.setEmpty(5, 5);
            assertFalse(gameGrid.isDestructibleWall(5, 5));
        }

        @Test
        void shouldReturnFalseForOutOfBoundsWallChecks() {
            assertFalse(gameGrid.isIndestructibleWall(-1, 0));
            assertFalse(gameGrid.isDestructibleWall(-1, 0));
            assertFalse(gameGrid.isIndestructibleWall(TEST_WIDTH, 0));
            assertFalse(gameGrid.isDestructibleWall(0, TEST_HEIGHT));
        }
    }

    @Nested
    class PowerUpTests {

        @Test
        void shouldHandlePowerUpCells() {
            // Test que les power-ups peuvent être placés via setCellType
            gameGrid.setCellType(5, 5, GameGrid.POWERUP_BOMB);
            assertEquals(GameGrid.POWERUP_BOMB, gameGrid.getCellType(5, 5));

            gameGrid.setCellType(6, 6, GameGrid.POWERUP_FIRE);
            assertEquals(GameGrid.POWERUP_FIRE, gameGrid.getCellType(6, 6));
        }

        @Test
        void shouldNotAllowPowerUpsThroughSpecificSetters() {
            // Les méthodes spécifiques ne permettent que les types de base
            gameGrid.setEmpty(5, 5);
            gameGrid.setCellType(5, 5, GameGrid.POWERUP_BOMB);
            assertEquals(GameGrid.POWERUP_BOMB, gameGrid.getCellType(5, 5));

            // Utiliser les setters spécifiques devrait changer le type
            gameGrid.setEmpty(5, 5);
            assertEquals(GameGrid.EMPTY, gameGrid.getCellType(5, 5));
        }
    }

    @Nested
    class ConstantsTests {

        @Test
        void shouldHaveCorrectConstantValues() {
            assertEquals(0, GameGrid.EMPTY);
            assertEquals(1, GameGrid.WALL_INDESTRUCTIBLE);
            assertEquals(2, GameGrid.WALL_DESTRUCTIBLE);
            assertEquals(3, GameGrid.POWERUP_BOMB);
            assertEquals(4, GameGrid.POWERUP_FIRE);
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void shouldHandleMinimalGrid() {
            GameGrid smallGrid = new GameGrid(3, 3);
            smallGrid.generate();
            
            // Même une grille 3x3 devrait avoir des bordures
            assertTrue(smallGrid.isIndestructibleWall(0, 0));
            assertTrue(smallGrid.isIndestructibleWall(2, 2));
        }

        @Test
        void shouldHandleMultipleGenerations() {
            gameGrid.generate();
            int firstGenCell = gameGrid.getCellType(1, 1);
            
            gameGrid.generate();
            int secondGenCell = gameGrid.getCellType(1, 1);
            
            // La zone de départ devrait toujours être vide
            assertEquals(GameGrid.EMPTY, firstGenCell);
            assertEquals(GameGrid.EMPTY, secondGenCell);
        }

        @Test
        void shouldMaintainGridIntegrityAfterModifications() {
            gameGrid.generate();
            
            // Modifier quelques cellules
            gameGrid.setEmpty(4, 4);
            gameGrid.setDestructibleWall(5, 5);
            
            // Les bordures doivent rester intactes
            assertTrue(gameGrid.isIndestructibleWall(0, 0));
            assertTrue(gameGrid.isIndestructibleWall(TEST_WIDTH - 1, TEST_HEIGHT - 1));
            
            // Les modifications doivent être préservées
            assertTrue(gameGrid.isWalkable(4, 4));
            assertTrue(gameGrid.isDestructibleWall(5, 5));
        }
    }
}