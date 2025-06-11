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

/**
 * Éditeur de niveaux pour le jeu Bomberman.
 * Permet de créer, sauvegarder et charger des niveaux via une interface graphique.
 */
public class BombermanLevelEditor extends Application {

    /**
     * Types d'éléments pouvant composer une cellule du niveau.
     */
    public enum CellType {
        /** Cellule vide */
        EMPTY("Vide", Color.LIGHTGRAY),
        /** Mur indestructible */
        WALL("Mur", Color.GREY),
        /** Mur destructible */
        DESTRUCTIBLE_WALL("Mur Destructible", Color.BROWN),
        /** Position de spawn du joueur */
        PLAYER_SPAWN("Spawn Joueur", Color.BLUE);

        private final String name;
        private final Color color;

        /**
         * Constructeur de CellType.
         * @param name Nom affiché dans l'interface
         * @param color Couleur associée à ce type de cellule
         */
        CellType(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        /**
         * Retourne le nom affiché du type.
         * @return Nom du type
         */
        public String getName() { return name; }

        /**
         * Retourne la couleur associée au type.
         * @return Couleur du type
         */
        public Color getColor() { return color; }
    }

    /** Nombre de lignes de la grille */
    private static final int GRID_ROWS = 13;
    /** Nombre de colonnes de la grille */
    private static final int GRID_COLS = 15;
    /** Taille d'une cellule en pixels */
    private static final int CELL_SIZE = 30;

    /** Grille représentant les types de chaque cellule */
    private CellType[][] grid = new CellType[GRID_ROWS][GRID_COLS];
    /** Tableaux des rectangles graphiques affichés */
    private Rectangle[][] gridRectangles = new Rectangle[GRID_ROWS][GRID_COLS];
    /** Outil actuellement sélectionné */
    private CellType selectedTool;
    /** Label affichant le statut */
    private Label statusLabel;
    /** Sélecteur d'outil */
    private ComboBox<CellType> toolSelector;

    /**
     * Point d'entrée JavaFX : initialise et affiche l'interface.
     * @param primaryStage fenêtre principale
     */
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

    /**
     * Initialise la grille avec des cellules vides et l'outil sélectionné par défaut.
     */
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

    /**
     * Crée la barre d'outils avec sélection d'outil et boutons d'action.
     * @return conteneur HBox avec les outils
     */
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

    /**
     * Crée la grille graphique de jeu où l'utilisateur peut placer des éléments.
     * @return GridPane représentant la grille de jeu
     */
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

                // Gestion du clic souris pour placer ou effacer
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

                // Effet visuel au survol de la souris
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

    /**
     * Crée le panneau d'affichage du statut et de la légende des couleurs.
     * @return VBox contenant les informations de statut et la légende
     */
    private VBox createStatusPanel() {
        VBox statusPanel = new VBox(5);
        statusPanel.setPadding(new Insets(10));

        statusLabel = new Label("Prêt - Clic gauche: placer, Clic droit: effacer");

        // Légende des couleurs pour chaque type de cellule
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

    /**
     * Place l'outil sélectionné à la position spécifiée dans la grille.
     * Si l'outil est un spawn joueur, supprime les autres spawns existants.
     * @param row ligne de la cellule
     * @param col colonne de la cellule
     */
    private void placeTool(int row, int col) {
        if (selectedTool == CellType.PLAYER_SPAWN) {
            // Un seul spawn joueur autorisé, suppression des existants
            removeExistingType(CellType.PLAYER_SPAWN);
        }

        grid[row][col] = selectedTool;
        updateCell(row, col);
    }

    /**
     * Supprime toutes les occurrences d'un type donné dans la grille en les remplaçant par vide.
     * @param type type de cellule à supprimer
     */
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

    /**
     * Met à jour graphiquement une cellule spécifique en fonction de son type.
     * @param row ligne de la cellule
     * @param col colonne de la cellule
     */
    private void updateCell(int row, int col) {
        System.out.println("Mise à jour cellule [" + row + "," + col + "] -> " + grid[row][col]);
        gridRectangles[row][col].setFill(grid[row][col].getColor());
    }

    /**
     * Vide entièrement la grille en remettant toutes les cellules à vide.
     */
    private void clearGrid() {
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                grid[i][j] = CellType.EMPTY;
                updateCell(i, j);
            }
        }
        updateStatus();
    }

    /**
     * Met à jour le label de statut avec le compte des différents éléments présents.
     */
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

    /**
     * Ouvre une boîte de dialogue pour sauvegarder le niveau dans un fichier.
     * Le format sauvegardé est simple CSV d'indices des CellType.
     */
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

    /**
     * Ouvre une boîte de dialogue pour charger un niveau depuis un fichier.
     * Vérifie la compatibilité de la taille avant chargement.
     */
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

    /**
     * Affiche une boîte d'alerte d'information avec un titre et un message.
     * @param title Titre de la fenêtre d'alerte
     * @param message Message à afficher
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Point d'entrée principal pour lancer l'application.
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}
