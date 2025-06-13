package bomberman.controller.game;

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

/**
 * Éditeur de niveaux graphique pour le jeu Bomberman.
 * Cette application JavaFX permet de créer, modifier et sauvegarder des niveaux personnalisés
 * pour le jeu Bomberman. L'éditeur offre une interface intuitive avec une grille interactive,
 * des outils de placement, et des fonctionnalités de sauvegarde/chargement.
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Création de niveaux avec différents types de cellules</li>
 *   <li>Interface graphique intuitive avec outils de placement</li>
 *   <li>Sauvegarde et chargement de niveaux au format .bmn</li>
 *   <li>Validation des contraintes de niveau (spawn unique)</li>
 *   <li>Aperçu en temps réel des statistiques du niveau</li>
 * </ul>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class BombermanLevelEditor extends Application {

    /**
     * Énumération des types de cellules disponibles dans l'éditeur.
     * Chaque type a un nom d'affichage et une couleur associée pour la représentation visuelle.
     */
    public enum CellType {
        /** Case vide - Les joueurs peuvent se déplacer dessus */
        EMPTY("Vide", Color.LIGHTGRAY),

        /** Mur indestructible - Bloque les mouvements et les explosions */
        WALL("Mur", Color.GREY),

        /** Mur destructible - Peut être détruit par les explosions */
        DESTRUCTIBLE_WALL("Mur Destructible", Color.BROWN),

        /** Point d'apparition des joueurs - Position de départ dans le niveau */
        PLAYER_SPAWN("Spawn Joueur", Color.BLUE);

        /** Nom d'affichage du type de cellule */
        private final String name;

        /** Couleur de représentation visuelle */
        private final Color color;

        /**
         * Constructeur d'un type de cellule.
         *
         * @param name Le nom d'affichage du type
         * @param color La couleur de représentation
         */
        CellType(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        /**
         * Retourne le nom d'affichage du type de cellule.
         *
         * @return Le nom d'affichage
         */
        public String getName() { return name; }

        /**
         * Retourne la couleur de représentation du type de cellule.
         *
         * @return La couleur associée
         */
        public Color getColor() { return color; }
    }

    /** Nombre de lignes de la grille de niveau */
    private static final int GRID_ROWS = 13;

    /** Nombre de colonnes de la grille de niveau */
    private static final int GRID_COLS = 15;

    /** Taille en pixels de chaque cellule de la grille */
    private static final int CELL_SIZE = 30;

    /** Matrice représentant l'état logique de chaque cellule du niveau */
    private CellType[][] grid = new CellType[GRID_ROWS][GRID_COLS];

    /** Matrice des rectangles JavaFX pour l'affichage visuel de la grille */
    private Rectangle[][] gridRectangles = new Rectangle[GRID_ROWS][GRID_COLS];

    /** Type d'outil actuellement sélectionné pour le placement */
    private CellType selectedTool;

    /** Label d'affichage des statistiques du niveau */
    private Label statusLabel;

    /** ComboBox pour la sélection des outils */
    private ComboBox<CellType> toolSelector;

    /**
     * Point d'entrée principal de l'application JavaFX.
     * Initialise l'interface utilisateur et configure la fenêtre principale.
     *
     * @param primaryStage La fenêtre principale de l'application
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
     * Initialise la grille de niveau avec des valeurs par défaut.
     * Crée les matrices de données et d'affichage, et définit l'outil initial.
     * Toutes les cellules sont initialisées comme vides.
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
     * Crée le panneau d'outils contenant les contrôles de l'éditeur.
     * Inclut le sélecteur d'outils et les boutons d'action (effacer, sauvegarder, charger).
     *
     * @return Le panneau d'outils configuré
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
     * Crée la grille interactive de l'éditeur.
     * Chaque cellule est représentée par un Rectangle cliquable avec gestion des événements souris.
     * Supporte le clic gauche (placement) et le clic droit (effacement).
     *
     * @return La grille JavaFX configurée avec les gestionnaires d'événements
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

                // Gestion des clics souris
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

                // Effet de survol pour preview
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
     * Crée le panneau de statut contenant les informations du niveau.
     * Affiche les statistiques en temps réel et la légende des couleurs.
     *
     * @return Le panneau de statut configuré
     */
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

    /**
     * Place l'outil sélectionné à la position spécifiée.
     * Applique les contraintes spéciales selon le type d'outil
     * (ex: un seul spawn joueur autorisé).
     *
     * @param row La ligne de placement
     * @param col La colonne de placement
     */
    private void placeTool(int row, int col) {
        // Vérifier les contraintes spéciales
        if (selectedTool == CellType.PLAYER_SPAWN) {
            // Un seul spawn joueur autorisé
            removeExistingType(CellType.PLAYER_SPAWN);
        }

        grid[row][col] = selectedTool;
        updateCell(row, col);
    }

    /**
     * Supprime toutes les instances d'un type de cellule spécifique de la grille.
     * Utilisé pour appliquer des contraintes d'unicité (ex: spawn unique).
     *
     * @param type Le type de cellule à supprimer
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
     * Met à jour l'affichage visuel d'une cellule spécifique.
     * Synchronise la couleur du Rectangle avec le type de cellule.
     *
     * @param row La ligne de la cellule à mettre à jour
     * @param col La colonne de la cellule à mettre à jour
     */
    private void updateCell(int row, int col) {
        System.out.println("Mise à jour cellule [" + row + "," + col + "] -> " + grid[row][col]);
        gridRectangles[row][col].setFill(grid[row][col].getColor());
    }

    /**
     * Efface complètement la grille en remettant toutes les cellules à vide.
     * Met à jour l'affichage et les statistiques après l'opération.
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
     * Met à jour l'affichage des statistiques du niveau.
     * Compte les différents types d'éléments et affiche un résumé
     * dans la barre de statut.
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
     * Sauvegarde le niveau actuel dans un fichier.
     * Utilise un FileChooser pour permettre à l'utilisateur de choisir
     * l'emplacement et le nom du fichier. Le format de sauvegarde est .bmn.
     *
     * <p>Format du fichier :</p>
     * <ul>
     *   <li>Première ligne : dimensions (lignes,colonnes)</li>
     *   <li>Lignes suivantes : valeurs ordinales des types de cellules</li>
     * </ul>
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
     * Charge un niveau depuis un fichier.
     * Utilise un FileChooser pour permettre à l'utilisateur de sélectionner
     * le fichier à charger. Valide la compatibilité des dimensions avant le chargement.
     *
     * @throws IOException Si une erreur survient lors de la lecture du fichier
     * @throws NumberFormatException Si le format du fichier est invalide
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

                // Validation des dimensions
                if (rows != GRID_ROWS || cols != GRID_COLS) {
                    showAlert("Erreur", "Taille de grille incompatible !");
                    return;
                }

                // Chargement des données de niveau
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
     * Affiche une boîte de dialogue d'information à l'utilisateur.
     *
     * @param title Le titre de la boîte de dialogue
     * @param message Le message à afficher
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Point d'entrée principal de l'application.
     * Lance l'interface graphique JavaFX.
     *
     * @param args Arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        launch(args);
    }
}