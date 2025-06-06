package com.example.bomberman;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BombermanLevelEditor extends Application {

    // Types d'éléments du niveau
    public enum CellType {
        EMPTY("Vide", Color.LIGHTGRAY),
        WALL("Mur", Color.GREY),
        DESTRUCTIBLE_WALL("Mur Destructible", Color.BROWN),
        PLAYER_SPAWN("Spawn Joueur", Color.BLUE);

        private final String name;
        private final Color color;

        CellType(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        public String getName() { return name; }
        public Color getColor() { return color; }
    }

    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 15;
    private static final int CELL_SIZE = 30;

    private CellType[][] grid = new CellType[GRID_ROWS][GRID_COLS];
    private Rectangle[][] gridRectangles = new Rectangle[GRID_ROWS][GRID_COLS];
    private CellType selectedTool;
    private Label statusLabel;
    private ComboBox<CellType> toolSelector;

    @Override
    public void start(Stage primaryStage) {
        initializeGrid();

        BorderPane root = new BorderPane();

        // Panneau d'outils en haut
        HBox toolPanel = createToolPanel();
        root.setTop(toolPanel);

        // Grille de jeu au centre
        GridPane gameGrid = createGameGrid();
        root.setCenter(gameGrid);

        // Panneau de statut en bas
        VBox statusPanel = createStatusPanel();
        root.setBottom(statusPanel);

        Scene scene = new Scene(root, 600, 650);
        primaryStage.setTitle("Éditeur de Niveau Bomberman");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void initializeGrid() {
        grid = new CellType[GRID_ROWS][GRID_COLS];
        gridRectangles = new Rectangle[GRID_ROWS][GRID_COLS];
        selectedTool = CellType.WALL;

        // Initialiser la grille avec des cases vides
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
    }

    private HBox createToolPanel() {
        HBox toolPanel = new HBox(10);
        toolPanel.setPadding(new Insets(10));
        toolPanel.setAlignment(Pos.CENTER);

        Label toolLabel = new Label("Outil sélectionné:");

        toolSelector = new ComboBox<>();
        for (CellType type : CellType.values()) {
            toolSelector.getItems().add(type);
        }
        toolSelector.setValue(selectedTool);
        toolSelector.setOnAction(e -> selectedTool = toolSelector.getValue());

        // Boutons d'action
        Button clearButton = new Button("Effacer tout");
        clearButton.setOnAction(e -> clearGrid());

        Button saveButton = new Button("Sauvegarder");
        saveButton.setOnAction(e -> saveLevel());

        Button loadButton = new Button("Charger");
        loadButton.setOnAction(e -> loadLevel());

        toolPanel.getChildren().addAll(
                toolLabel, toolSelector,
                new Separator(),
                clearButton, saveButton, loadButton
        );

        return toolPanel;
    }

    private GridPane createGameGrid() {
        GridPane gameGrid = new GridPane();
        gameGrid.setAlignment(Pos.CENTER);
        gameGrid.setHgap(1);
        gameGrid.setVgap(1);
        gameGrid.setStyle("-fx-background-color: black;");

        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(grid[i][j].getColor());
                cell.setStroke(Color.BLACK);
                cell.setStrokeWidth(0.5);

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

                // Effet de survol
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

        return gameGrid;
    }

    private VBox createStatusPanel() {
        VBox statusPanel = new VBox(5);
        statusPanel.setPadding(new Insets(10));

        statusLabel = new Label("Prêt - Clic gauche: placer, Clic droit: effacer");

        // Légende des couleurs
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);

        for (CellType type : CellType.values()) {
            VBox legendItem = new VBox(2);
            legendItem.setAlignment(Pos.CENTER);

            Rectangle colorBox = new Rectangle(15, 15);
            colorBox.setFill(type.getColor());
            colorBox.setStroke(Color.BLACK);

            Label typeLabel = new Label(type.getName());
            typeLabel.setStyle("-fx-font-size: 10px;");

            legendItem.getChildren().addAll(colorBox, typeLabel);
            legend.getChildren().add(legendItem);
        }

        statusPanel.getChildren().addAll(statusLabel, legend);
        return statusPanel;
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
        System.out.println("Mise à jour cellule [" + row + "," + col + "] -> " + grid[row][col]);
        gridRectangles[row][col].setFill(grid[row][col].getColor());
    }

    private void clearGrid() {
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                grid[i][j] = CellType.EMPTY;
                updateCell(i, j);
            }
        }
        updateStatus();
    }

    private void updateStatus() {
        int walls = 0, destructibleWalls = 0, enemies = 0, powerUps = 0;
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
                "Murs: %d | Murs destructibles: %d | Ennemis: %d | Power-ups: %d | Spawn joueur: %s",
                walls, destructibleWalls, enemies, powerUps, hasPlayerSpawn ? "Oui" : "Non"
        ));
    }

    private void saveLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le niveau");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers niveau (*.bmn)", "*.bmn")
        );

        File file = fileChooser.showSaveDialog(null);
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

    private void loadLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger un niveau");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers niveau (*.bmn)", "*.bmn")
        );

        File file = fileChooser.showOpenDialog(null);
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

    public static void main(String[] args) {
        launch(args);
    }
}