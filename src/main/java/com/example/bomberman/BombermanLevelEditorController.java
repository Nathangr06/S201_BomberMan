package com.example.bomberman;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class BombermanLevelEditorController implements Initializable {

    // Types d'éléments du niveau
    public enum CellType {
        EMPTY("Vide", Color.LIGHTGRAY, "cell-empty"),
        WALL("Mur", Color.GREY, "cell-wall"),
        DESTRUCTIBLE_WALL("Mur Destructible", Color.BROWN, "cell-destructible-wall"),
        PLAYER_SPAWN("Spawn Joueur", Color.BLUE, "cell-player-spawn");

        private final String name;
        private final Color color;
        private final String styleClass;

        CellType(String name, Color color, String styleClass) {
            this.name = name;
            this.color = color;
            this.styleClass = styleClass;
        }

        public String getName() { return name; }
        public Color getColor() { return color; }
        public String getStyleClass() { return styleClass; }

        @Override
        public String toString() { return name; }
    }

    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 15;
    private static final int CELL_SIZE = 30;

    // FXML Elements
    @FXML private HBox toolPanel;
    @FXML private ComboBox<CellType> toolSelector;
    @FXML private Button clearButton;
    @FXML private Button saveButton;
    @FXML private Button loadButton;
    @FXML private GridPane gameGrid;
    @FXML private VBox statusPanel;
    @FXML private Label statusLabel;
    @FXML private HBox legend;

    // Game state
    private CellType[][] grid = new CellType[GRID_ROWS][GRID_COLS];
    private Rectangle[][] gridRectangles = new Rectangle[GRID_ROWS][GRID_COLS];
    private CellType selectedTool;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeGrid();
        setupToolSelector();
        createGameGrid();
        createLegend();
        updateStatus();
    }

    private void initializeGrid() {
        selectedTool = CellType.WALL;

        // Initialiser la grille avec des cases vides
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
    }

    private void setupToolSelector() {
        toolSelector.getItems().addAll(CellType.values());
        toolSelector.setValue(selectedTool);
        toolSelector.setOnAction(e -> selectedTool = toolSelector.getValue());

        // Custom cell factory pour afficher les noms
        toolSelector.setCellFactory(listView -> new ListCell<CellType>() {
            @Override
            protected void updateItem(CellType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        toolSelector.setButtonCell(new ListCell<CellType>() {
            @Override
            protected void updateItem(CellType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    private void createGameGrid() {
        gameGrid.getChildren().clear();

        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(grid[i][j].getColor());
                cell.getStyleClass().addAll("game-cell", grid[i][j].getStyleClass());

                final int r = i;
                final int c = j;

                cell.setOnMouseClicked(e -> {
                    switch (e.getButton()) {
                        case PRIMARY:
                            placeTool(r, c);
                            break;
                        case SECONDARY:
                            grid[r][c] = CellType.EMPTY;
                            updateCell(r, c);
                            break;
                    }
                    updateStatus();
                });

                // Effet de survol géré par CSS
                cell.setOnMouseEntered(e -> {
                    if (!cell.getFill().equals(selectedTool.getColor())) {
                        cell.setOpacity(0.7);
                    }
                });

                cell.setOnMouseExited(e -> cell.setOpacity(1.0));

                gridRectangles[i][j] = cell;
                gameGrid.add(cell, j, i);
            }
        }
    }

    private void createLegend() {
        legend.getChildren().clear();

        for (CellType type : CellType.values()) {
            VBox legendItem = new VBox(2);
            legendItem.setAlignment(Pos.CENTER);
            legendItem.getStyleClass().add("legend-item");

            Rectangle colorBox = new Rectangle(15, 15);
            colorBox.setFill(type.getColor());
            colorBox.getStyleClass().addAll("legend-box", "legend-" + type.name().toLowerCase().replace("_", "-"));

            Label typeLabel = new Label(type.getName());
            typeLabel.getStyleClass().add("legend-label");

            legendItem.getChildren().addAll(colorBox, typeLabel);
            legend.getChildren().add(legendItem);
        }
    }

    private void placeTool(int row, int col) {
        // Vérifier les contraintes spéciales
        if (selectedTool == CellType.PLAYER_SPAWN) {
            // Un seul spawn joueur autorisé
            removeExistingType(CellType.PLAYER_SPAWN);
        }

        grid[row][col] = selectedTool;
        updateCell(row, col);
    }

    private void removeExistingType(CellType type) {
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                if (grid[i][j] == type) {
                    grid[i][j] = CellType.EMPTY;
                    updateCell(i, j);
                }
            }
        }
    }

    private void updateCell(int row, int col) {
        Rectangle cell = gridRectangles[row][col];
        cell.setFill(grid[row][col].getColor());

        // Mettre à jour les classes CSS
        cell.getStyleClass().removeIf(styleClass -> styleClass.startsWith("cell-"));
        cell.getStyleClass().add(grid[row][col].getStyleClass());
    }

    @FXML
    private void onClearGrid() {
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                grid[i][j] = CellType.EMPTY;
                updateCell(i, j);
            }
        }
        updateStatus();
    }

    private void updateStatus() {
        int walls = 0, destructibleWalls = 0;
        boolean hasPlayerSpawn = false;

        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                switch (grid[i][j]) {
                    case WALL: walls++; break;
                    case DESTRUCTIBLE_WALL: destructibleWalls++; break;
                    case PLAYER_SPAWN: hasPlayerSpawn = true; break;
                }
            }
        }

        statusLabel.setText(String.format(
                "Murs: %d | Murs destructibles: %d | Spawn joueur: %s",
                walls, destructibleWalls, hasPlayerSpawn ? "Oui" : "Non"
        ));
    }

    @FXML
    private void onSaveLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le niveau");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers niveau (*.bmn)", "*.bmn")
        );

        Stage stage = (Stage) saveButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(GRID_ROWS + "," + GRID_COLS);
                for (int i = 0; i < GRID_ROWS; i++) {
                    for (int j = 0; j < GRID_COLS; j++) {
                        writer.print(grid[i][j].ordinal());
                        if (j < GRID_COLS - 1) writer.print(",");
                    }
                    writer.println();
                }
                showAlert("Sauvegarde", "Niveau sauvegardé avec succès!");
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onLoadLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger un niveau");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers niveau (*.bmn)", "*.bmn")
        );

        Stage stage = (Stage) loadButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String sizeStr = reader.readLine();
                String[] sizeParts = sizeStr.split(",");
                int rows = Integer.parseInt(sizeParts[0]);
                int cols = Integer.parseInt(sizeParts[1]);

                if (rows != GRID_ROWS || cols != GRID_COLS) {
                    showAlert("Erreur", "Taille de grille incompatible !");
                    return;
                }

                for (int i = 0; i < GRID_ROWS; i++) {
                    String line = reader.readLine();
                    String[] values = line.split(",");
                    for (int j = 0; j < GRID_COLS; j++) {
                        int typeIndex = Integer.parseInt(values[j]);
                        grid[i][j] = CellType.values()[typeIndex];
                        updateCell(i, j);
                    }
                }

                updateStatus();
                showAlert("Chargement", "Niveau chargé avec succès!");

            } catch (IOException | NumberFormatException e) {
                showAlert("Erreur", "Erreur lors du chargement: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}