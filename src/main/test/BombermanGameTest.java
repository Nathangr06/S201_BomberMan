import static org.junit.jupiter.api.Assertions.*;

import bomberman.model.game.BombermanGame;
import bomberman.model.game.GameGrid;
import bomberman.utils.GameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

class BombermanGameTest {

    private BombermanGame bombermanGame;
    private List<File> tempFiles;

    @BeforeEach
    void setUp() {
        bombermanGame = new BombermanGame();
        tempFiles = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        // Nettoyer les fichiers temporaires créés pendant les tests
        for (File file : tempFiles) {
            if (file.exists()) {
                file.delete();
            }
        }
        tempFiles.clear();
    }

    @Test
    void testConstructor() {
        assertNotNull(bombermanGame);
        assertEquals(2, bombermanGame.getPlayerCount()); // Valeur par défaut
    }

    @Test
    void testSetPlayerCountValidValues() {
        // Test valeurs valides
        bombermanGame.setPlayerCount(2);
        assertEquals(2, bombermanGame.getPlayerCount());

        bombermanGame.setPlayerCount(3);
        assertEquals(3, bombermanGame.getPlayerCount());

        bombermanGame.setPlayerCount(4);
        assertEquals(4, bombermanGame.getPlayerCount());
    }

    @Test
    void testSetPlayerCountBoundaries() {
        // Test valeurs limites - minimum sera 2
        bombermanGame.setPlayerCount(1);
        assertEquals(2, bombermanGame.getPlayerCount());

        bombermanGame.setPlayerCount(0);
        assertEquals(2, bombermanGame.getPlayerCount());

        bombermanGame.setPlayerCount(-1);
        assertEquals(2, bombermanGame.getPlayerCount());

        bombermanGame.setPlayerCount(-10);
        assertEquals(2, bombermanGame.getPlayerCount());
    }

    @Test
    void testSetPlayerCountMaximumBoundary() {
        // Test valeurs limites - maximum sera 4
        bombermanGame.setPlayerCount(5);
        assertEquals(4, bombermanGame.getPlayerCount());

        bombermanGame.setPlayerCount(10);
        assertEquals(4, bombermanGame.getPlayerCount());

        bombermanGame.setPlayerCount(100);
        assertEquals(4, bombermanGame.getPlayerCount());
    }

    @Test
    void testCreateDefaultLevel() throws Exception {
        // Utilisation de la réflexion pour accéder à la méthode privée
        java.lang.reflect.Method createDefaultLevelMethod =
                BombermanGame.class.getDeclaredMethod("createDefaultLevel");
        createDefaultLevelMethod.setAccessible(true);

        GameGrid grid = (GameGrid) createDefaultLevelMethod.invoke(bombermanGame);

        assertNotNull(grid);
        assertEquals(GameConstants.GRID_WIDTH, grid.getWidth());
        assertEquals(GameConstants.GRID_HEIGHT, grid.getHeight());
    }

    @Test
    void testLoadCustomLevelValidFile() throws Exception {
        // Créer un fichier de niveau temporaire valide
        File validLevelFile = createValidLevelFile();

        // Utilisation de la réflexion pour accéder à la méthode privée
        java.lang.reflect.Method loadCustomLevelMethod =
                BombermanGame.class.getDeclaredMethod("loadCustomLevel", File.class);
        loadCustomLevelMethod.setAccessible(true);

        GameGrid grid = (GameGrid) loadCustomLevelMethod.invoke(bombermanGame);

        assertNotNull(grid);
        assertEquals(GameConstants.GRID_WIDTH, grid.getWidth());
        assertEquals(GameConstants.GRID_HEIGHT, grid.getHeight());
    }

    @Test
    void testLoadCustomLevelInvalidSize() throws Exception {
        // Créer un fichier avec une taille invalide
        File invalidSizeFile = createInvalidSizeLevelFile();

        java.lang.reflect.Method loadCustomLevelMethod =
                BombermanGame.class.getDeclaredMethod("loadCustomLevel", File.class);
        loadCustomLevelMethod.setAccessible(true);

        GameGrid grid = (GameGrid) loadCustomLevelMethod.invoke(bombermanGame, invalidSizeFile);

        // Doit retourner un niveau par défaut en cas d'erreur
        assertNotNull(grid);
        assertEquals(GameConstants.GRID_WIDTH, grid.getWidth());
        assertEquals(GameConstants.GRID_HEIGHT, grid.getHeight());
    }

    @Test
    void testLoadCustomLevelInvalidFormat() throws Exception {
        // Créer un fichier avec un format invalide
        File invalidFormatFile = createInvalidFormatLevelFile();

        java.lang.reflect.Method loadCustomLevelMethod =
                BombermanGame.class.getDeclaredMethod("loadCustomLevel", File.class);
        loadCustomLevelMethod.setAccessible(true);

        GameGrid grid = (GameGrid) loadCustomLevelMethod.invoke(bombermanGame, invalidFormatFile);

        // Doit retourner un niveau par défaut en cas d'erreur
        assertNotNull(grid);
        assertEquals(GameConstants.GRID_WIDTH, grid.getWidth());
        assertEquals(GameConstants.GRID_HEIGHT, grid.getHeight());
    }

    @Test
    void testLoadCustomLevelNonExistentFile() throws Exception {
        File nonExistentFile = new File("nonexistent_file_that_does_not_exist.txt");

        java.lang.reflect.Method loadCustomLevelMethod =
                BombermanGame.class.getDeclaredMethod("loadCustomLevel", File.class);
        loadCustomLevelMethod.setAccessible(true);

        GameGrid grid = (GameGrid) loadCustomLevelMethod.invoke(bombermanGame, nonExistentFile);

        // Doit retourner un niveau par défaut en cas d'erreur
        assertNotNull(grid);
        assertEquals(GameConstants.GRID_WIDTH, grid.getWidth());
        assertEquals(GameConstants.GRID_HEIGHT, grid.getHeight());
    }

    @Test
    void testLoadCustomLevelEmptyFile() throws Exception {
        // Créer un fichier vide
        File emptyFile = createEmptyLevelFile();

        java.lang.reflect.Method loadCustomLevelMethod =
                BombermanGame.class.getDeclaredMethod("loadCustomLevel", File.class);
        loadCustomLevelMethod.setAccessible(true);

        GameGrid grid = (GameGrid) loadCustomLevelMethod.invoke(bombermanGame, emptyFile);

        // Doit retourner un niveau par défaut en cas d'erreur
        assertNotNull(grid);
        assertEquals(GameConstants.GRID_WIDTH, grid.getWidth());
        assertEquals(GameConstants.GRID_HEIGHT, grid.getHeight());
    }

    @Test
    void testGetPlayerKeyPlayer1() throws Exception {
        java.lang.reflect.Method getPlayerKeyMethod =
                BombermanGame.class.getDeclaredMethod("getPlayerKey", int.class, String.class);
        getPlayerKeyMethod.setAccessible(true);

        // Test Player 1 - touches fléchées
        assertEquals(javafx.scene.input.KeyCode.LEFT,
                getPlayerKeyMethod.invoke(bombermanGame, 1, "LEFT"));
        assertEquals(javafx.scene.input.KeyCode.RIGHT,
                getPlayerKeyMethod.invoke(bombermanGame, 1, "RIGHT"));
        assertEquals(javafx.scene.input.KeyCode.UP,
                getPlayerKeyMethod.invoke(bombermanGame, 1, "UP"));
        assertEquals(javafx.scene.input.KeyCode.DOWN,
                getPlayerKeyMethod.invoke(bombermanGame, 1, "DOWN"));
        assertEquals(javafx.scene.input.KeyCode.ENTER,
                getPlayerKeyMethod.invoke(bombermanGame, 1, "BOMB"));
    }

    @Test
    void testGetPlayerKeyPlayer2() throws Exception {
        java.lang.reflect.Method getPlayerKeyMethod =
                BombermanGame.class.getDeclaredMethod("getPlayerKey", int.class, String.class);
        getPlayerKeyMethod.setAccessible(true);

        // Test Player 2 - touches ZQSD
        assertEquals(javafx.scene.input.KeyCode.Q,
                getPlayerKeyMethod.invoke(bombermanGame, 2, "LEFT"));
        assertEquals(javafx.scene.input.KeyCode.D,
                getPlayerKeyMethod.invoke(bombermanGame, 2, "RIGHT"));
        assertEquals(javafx.scene.input.KeyCode.Z,
                getPlayerKeyMethod.invoke(bombermanGame, 2, "UP"));
        assertEquals(javafx.scene.input.KeyCode.S,
                getPlayerKeyMethod.invoke(bombermanGame, 2, "DOWN"));
        assertEquals(javafx.scene.input.KeyCode.SPACE,
                getPlayerKeyMethod.invoke(bombermanGame, 2, "BOMB"));
    }

    @Test
    void testGetPlayerKeyPlayer3() throws Exception {
        java.lang.reflect.Method getPlayerKeyMethod =
                BombermanGame.class.getDeclaredMethod("getPlayerKey", int.class, String.class);
        getPlayerKeyMethod.setAccessible(true);

        // Test Player 3 - touches IJKL
        assertEquals(javafx.scene.input.KeyCode.J,
                getPlayerKeyMethod.invoke(bombermanGame, 3, "LEFT"));
        assertEquals(javafx.scene.input.KeyCode.L,
                getPlayerKeyMethod.invoke(bombermanGame, 3, "RIGHT"));
        assertEquals(javafx.scene.input.KeyCode.I,
                getPlayerKeyMethod.invoke(bombermanGame, 3, "UP"));
        assertEquals(javafx.scene.input.KeyCode.K,
                getPlayerKeyMethod.invoke(bombermanGame, 3, "DOWN"));
        assertEquals(javafx.scene.input.KeyCode.U,
                getPlayerKeyMethod.invoke(bombermanGame, 3, "BOMB"));
    }

    @Test
    void testGetPlayerKeyPlayer4() throws Exception {
        java.lang.reflect.Method getPlayerKeyMethod =
                BombermanGame.class.getDeclaredMethod("getPlayerKey", int.class, String.class);
        getPlayerKeyMethod.setAccessible(true);

        // Test Player 4 - pavé numérique
        assertEquals(javafx.scene.input.KeyCode.NUMPAD4,
                getPlayerKeyMethod.invoke(bombermanGame, 4, "LEFT"));
        assertEquals(javafx.scene.input.KeyCode.NUMPAD6,
                getPlayerKeyMethod.invoke(bombermanGame, 4, "RIGHT"));
        assertEquals(javafx.scene.input.KeyCode.NUMPAD8,
                getPlayerKeyMethod.invoke(bombermanGame, 4, "UP"));
        assertEquals(javafx.scene.input.KeyCode.NUMPAD5,
                getPlayerKeyMethod.invoke(bombermanGame, 4, "DOWN"));
        assertEquals(javafx.scene.input.KeyCode.NUMPAD0,
                getPlayerKeyMethod.invoke(bombermanGame, 4, "BOMB"));
    }

    @Test
    void testGetPlayerKeyInvalidPlayer() throws Exception {
        java.lang.reflect.Method getPlayerKeyMethod =
                BombermanGame.class.getDeclaredMethod("getPlayerKey", int.class, String.class);
        getPlayerKeyMethod.setAccessible(true);

        // Test joueurs invalides
        assertNull(getPlayerKeyMethod.invoke(bombermanGame, 0, "LEFT"));
        assertNull(getPlayerKeyMethod.invoke(bombermanGame, 5, "LEFT"));
        assertNull(getPlayerKeyMethod.invoke(bombermanGame, -1, "LEFT"));
        assertNull(getPlayerKeyMethod.invoke(bombermanGame, 10, "LEFT"));
    }

    @Test
    void testGetPlayerKeyInvalidAction() throws Exception {
        java.lang.reflect.Method getPlayerKeyMethod =
                BombermanGame.class.getDeclaredMethod("getPlayerKey", int.class, String.class);
        getPlayerKeyMethod.setAccessible(true);

        // Test actions invalides pour un joueur valide
        assertNull(getPlayerKeyMethod.invoke(bombermanGame, 1, "INVALID"));
        assertNull(getPlayerKeyMethod.invoke(bombermanGame, 1, ""));
        assertNull(getPlayerKeyMethod.invoke(bombermanGame, 1, "JUMP"));
        assertNull(getPlayerKeyMethod.invoke(bombermanGame, 1, "SHOOT"));
        assertNull(getPlayerKeyMethod.invoke(bombermanGame, 1, null));
    }

    @Test
    void testGetPlayerKeyAllPlayersAllActions() throws Exception {
        java.lang.reflect.Method getPlayerKeyMethod =
                BombermanGame.class.getDeclaredMethod("getPlayerKey", int.class, String.class);
        getPlayerKeyMethod.setAccessible(true);

        String[] actions = {"LEFT", "RIGHT", "UP", "DOWN", "BOMB"};

        // Vérifier que chaque joueur a une touche pour chaque action
        for (int player = 1; player <= 4; player++) {
            for (String action : actions) {
                Object result = getPlayerKeyMethod.invoke(bombermanGame, player, action);
                assertNotNull(result,
                        "Player " + player + " should have a key for action " + action);
                assertTrue(result instanceof javafx.scene.input.KeyCode,
                        "Result should be a KeyCode for player " + player + " action " + action);
            }
        }
    }

    @Test
    void testCellTypeEnum() {
        // Test que toutes les valeurs de l'enum sont présentes
        BombermanGame.CellType[] cellTypes = BombermanGame.CellType.values();
        assertEquals(5, cellTypes.length);

        // Vérifier que tous les types attendus sont présents
        assertTrue(Arrays.asList(cellTypes).contains(BombermanGame.CellType.EMPTY));
        assertTrue(Arrays.asList(cellTypes).contains(BombermanGame.CellType.WALL));
        assertTrue(Arrays.asList(cellTypes).contains(BombermanGame.CellType.DESTRUCTIBLE_WALL));
        assertTrue(Arrays.asList(cellTypes).contains(BombermanGame.CellType.PLAYER_SPAWN));
        assertTrue(Arrays.asList(cellTypes).contains(BombermanGame.CellType.PLAYER2_SPAWN));
    }

    @Test
    void testCellTypeEnumValues() {
        // Test les valeurs ordinales
        assertEquals(0, BombermanGame.CellType.EMPTY.ordinal());
        assertEquals(1, BombermanGame.CellType.WALL.ordinal());
        assertEquals(2, BombermanGame.CellType.DESTRUCTIBLE_WALL.ordinal());
        assertEquals(3, BombermanGame.CellType.PLAYER_SPAWN.ordinal());
        assertEquals(4, BombermanGame.CellType.PLAYER2_SPAWN.ordinal());
    }

    @Test
    void testCellTypeValueOf() {
        // Test valueOf
        assertEquals(BombermanGame.CellType.EMPTY, BombermanGame.CellType.valueOf("EMPTY"));
        assertEquals(BombermanGame.CellType.WALL, BombermanGame.CellType.valueOf("WALL"));
        assertEquals(BombermanGame.CellType.DESTRUCTIBLE_WALL, BombermanGame.CellType.valueOf("DESTRUCTIBLE_WALL"));
        assertEquals(BombermanGame.CellType.PLAYER_SPAWN, BombermanGame.CellType.valueOf("PLAYER_SPAWN"));
        assertEquals(BombermanGame.CellType.PLAYER2_SPAWN, BombermanGame.CellType.valueOf("PLAYER2_SPAWN"));

        // Test valueOf avec valeur invalide
        assertThrows(IllegalArgumentException.class, () -> {
            BombermanGame.CellType.valueOf("INVALID_TYPE");
        });
    }

    @Test
    void testIsGameRunningWithoutGameManager() {
        // Sans GameManager initialisé, le jeu ne devrait pas être en cours
        assertFalse(bombermanGame.isGameRunning());
    }

    @Test
    void testGetPlayersWithoutGameManager() {
        // Sans GameManager initialisé, tous les getters de joueurs devraient retourner null
        assertNull(bombermanGame.getPlayer1());
        assertNull(bombermanGame.getPlayer2());
        assertNull(bombermanGame.getPlayer3());
        assertNull(bombermanGame.getPlayer4());
    }

    @Test
    void testStopGameWithoutGameManager() {
        // Ne doit pas lever d'exception même sans GameManager
        assertDoesNotThrow(() -> bombermanGame.stopGame());
    }

    @Test
    void testRestartGameWithoutGameManager() {
        // Ne doit pas lever d'exception même sans GameManager
        assertDoesNotThrow(() -> bombermanGame.restartGame());
    }

    @Test
    void testPlayerCountPersistence() {
        // Vérifier que le nombre de joueurs persiste entre les appels
        bombermanGame.setPlayerCount(3);
        assertEquals(3, bombermanGame.getPlayerCount());

        bombermanGame.setPlayerCount(4);
        assertEquals(4, bombermanGame.getPlayerCount());

        bombermanGame.setPlayerCount(2);
        assertEquals(2, bombermanGame.getPlayerCount());
    }

    // Méthodes utilitaires pour créer des fichiers de test
    private File createValidLevelFile() throws IOException {
        File levelFile = File.createTempFile("valid_level", ".txt");
        tempFiles.add(levelFile);

        try (FileWriter writer = new FileWriter(levelFile)) {
            writer.write(GameConstants.GRID_HEIGHT + "," + GameConstants.GRID_WIDTH + "\n");

            // Créer une grille simple pour les tests
            for (int i = 0; i < GameConstants.GRID_HEIGHT; i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < GameConstants.GRID_WIDTH; j++) {
                    if (j > 0) line.append(",");

                    // Bordures = murs, intérieur = vide
                    if (i == 0 || i == GameConstants.GRID_HEIGHT - 1 ||
                            j == 0 || j == GameConstants.GRID_WIDTH - 1) {
                        line.append("1"); // WALL
                    } else if (i == 1 && j == 1) {
                        line.append("3"); // PLAYER_SPAWN
                    } else if (i == 1 && j == GameConstants.GRID_WIDTH - 2) {
                        line.append("4"); // PLAYER2_SPAWN
                    } else {
                        line.append("0"); // EMPTY
                    }
                }
                writer.write(line.toString() + "\n");
            }
        }

        return levelFile;
    }

    private File createInvalidSizeLevelFile() throws IOException {
        File levelFile = File.createTempFile("invalid_size_level", ".txt");
        tempFiles.add(levelFile);

        try (FileWriter writer = new FileWriter(levelFile)) {
            writer.write("5,5\n"); // Taille différente de celle attendue
            writer.write("0,1,0,1,0\n");
            writer.write("1,0,1,0,1\n");
            writer.write("0,1,0,1,0\n");
            writer.write("1,0,1,0,1\n");
            writer.write("0,1,0,1,0\n");
        }

        return levelFile;
    }

    private File createInvalidFormatLevelFile() throws IOException {
        File levelFile = File.createTempFile("invalid_format_level", ".txt");
        tempFiles.add(levelFile);

        try (FileWriter writer = new FileWriter(levelFile)) {
            writer.write("invalid,format\n");
            writer.write("not,a,number,grid\n");
            writer.write("abc,def,ghi\n");
        }

        return levelFile;
    }

    private File createEmptyLevelFile() throws IOException {
        File levelFile = File.createTempFile("empty_level", ".txt");
        tempFiles.add(levelFile);
        // Le fichier reste vide
        return levelFile;
    }
}