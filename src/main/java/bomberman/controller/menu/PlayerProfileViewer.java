package bomberman.controller.menu;

import bomberman.model.profile.PlayerProfile;
import bomberman.model.profile.PlayerProfileManager;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Interface graphique de gestion des profils joueurs.
 * Cette application JavaFX permet de visualiser, créer, sélectionner et supprimer
 * les profils de joueurs du jeu Bomberman. Elle offre une interface intuitive
 * avec un tableau de profils existants et des formulaires de gestion.
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Visualisation de tous les profils existants dans un tableau</li>
 *   <li>Création de nouveaux profils avec nom et prénom</li>
 *   <li>Sélection d'un profil actif pour les parties</li>
 *   <li>Suppression de profils existants</li>
 *   <li>Affichage des statistiques (parties jouées/gagnées)</li>
 * </ul>
 *
 * <p>Architecture de l'interface :</p>
 * <pre>
 * ┌─────────────────────────────────┐
 * │        Profils existants        │
 * ├─────────────────────────────────┤
 * │  TableView avec colonnes :      │
 * │  - Nom                          │
 * │  - Prénom                       │
 * │  - Parties jouées               │
 * │  - Parties gagnées              │
 * ├─────────────────────────────────┤
 * │    Créer un nouveau profil      │
 * │  [Nom] [Prénom]                 │
 * │  [Créer] [Sélectionner] [Suppr] │
 * └─────────────────────────────────┘
 * </pre>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class PlayerProfileViewer extends Application {

    /** Gestionnaire de profils pour les opérations CRUD */
    private PlayerProfileManager profileManager;

    /** Tableau d'affichage des profils existants */
    private TableView<PlayerProfile> profileTable;

    /**
     * Point d'entrée principal de l'application JavaFX.
     * Initialise l'interface utilisateur, configure les composants
     * et affiche la fenêtre de gestion des profils.
     *
     * @param stage La fenêtre principale de l'application
     */
    @Override
    public void start(Stage stage) {
        profileManager = PlayerProfileManager.getInstance();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2C3E50;");

        // Section des profils existants
        Label existingProfilesLabel = new Label("Profils existants :");
        existingProfilesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Initialisation de la TableView
        setupTableView();

        // Section création de profil
        GridPane inputGrid = createInputGrid();
        TextField usernameField = (TextField) inputGrid.getChildren().get(1);
        TextField firstNameField = (TextField) inputGrid.getChildren().get(3);

        // Boutons
        HBox buttonBox = createButtonBox(stage, usernameField, firstNameField);

        root.getChildren().addAll(
                existingProfilesLabel,
                profileTable,
                new Label("Créer un nouveau profil :") {{
                    setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
                }},
                inputGrid,
                buttonBox
        );

        Scene scene = new Scene(root, 600, 500);
        stage.setTitle("Gestion des profils");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Configure le tableau d'affichage des profils.
     * Crée et configure toutes les colonnes du tableau avec leurs propriétés
     * de liaison aux données des profils. Applique le style visuel et
     * initialise les données d'affichage.
     *
     * <p>Colonnes configurées :</p>
     * <ul>
     *   <li><strong>Nom</strong> : Nom d'utilisateur du profil</li>
     *   <li><strong>Prénom</strong> : Prénom du joueur</li>
     *   <li><strong>Parties jouées</strong> : Nombre total de parties</li>
     *   <li><strong>Parties gagnées</strong> : Nombre de victoires</li>
     * </ul>
     */
    private void setupTableView() {
        profileTable = new TableView<>();
        profileTable.setStyle("-fx-background-color: #34495E; -fx-text-fill: white;");

        // Configuration des colonnes avec liaison de données
        TableColumn<PlayerProfile, String> usernameCol = new TableColumn<>("Nom");
        usernameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUsername()));

        TableColumn<PlayerProfile, String> firstNameCol = new TableColumn<>("Prénom");
        firstNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFirstName()));

        TableColumn<PlayerProfile, Number> gamesPlayedCol = new TableColumn<>("Parties jouées");
        gamesPlayedCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getGamesPlayed()));

        TableColumn<PlayerProfile, Number> gamesWonCol = new TableColumn<>("Parties gagnées");
        gamesWonCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getGamesWon()));

        profileTable.getColumns().addAll(usernameCol, firstNameCol, gamesPlayedCol, gamesWonCol);
        updateTableData();
    }

    /**
     * Crée la grille de saisie pour un nouveau profil.
     * Configure un formulaire avec les champs nom et prénom,
     * applique les styles appropriés et organise la mise en page.
     *
     * @return La grille configurée avec les champs de saisie
     */
    private GridPane createInputGrid() {
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setAlignment(Pos.CENTER);

        // Champs de saisie avec texte d'aide
        TextField usernameField = new TextField();
        usernameField.setPromptText("Nom");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prénom");

        // Labels avec style
        Label nomLabel = new Label("Nom :");
        Label prenomLabel = new Label("Prénom :");
        nomLabel.setStyle("-fx-text-fill: white;");
        prenomLabel.setStyle("-fx-text-fill: white;");

        // Organisation dans la grille
        inputGrid.add(nomLabel, 0, 0);
        inputGrid.add(usernameField, 1, 0);
        inputGrid.add(prenomLabel, 0, 1);
        inputGrid.add(firstNameField, 1, 1);

        return inputGrid;
    }

    /**
     * Crée la boîte de boutons d'action.
     * Configure les boutons pour créer, sélectionner et supprimer des profils
     * avec leurs gestionnaires d'événements respectifs.
     *
     * @param stage La fenêtre principale pour la fermeture
     * @param usernameField Le champ nom pour la création de profil
     * @param firstNameField Le champ prénom pour la création de profil
     * @return La boîte de boutons configurée
     */
    private HBox createButtonBox(Stage stage, TextField usernameField, TextField firstNameField) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        String buttonStyle = "-fx-background-color: #3498DB; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 15;";

        Button createButton = new Button("Créer");
        Button selectButton = new Button("Sélectionner");
        Button deleteButton = new Button("Supprimer");

        createButton.setStyle(buttonStyle);
        selectButton.setStyle(buttonStyle);
        deleteButton.setStyle(buttonStyle);

        // Gestionnaires d'événements
        createButton.setOnAction(e -> handleCreateProfile(usernameField, firstNameField));
        selectButton.setOnAction(e -> handleSelectProfile(stage));
        deleteButton.setOnAction(e -> handleDeleteProfile());

        buttonBox.getChildren().addAll(createButton, selectButton, deleteButton);
        return buttonBox;
    }

    /**
     * Met à jour les données affichées dans le tableau.
     * Récupère tous les profils depuis le gestionnaire et rafraîchit
     * l'affichage du tableau avec les données actuelles.
     */
    private void updateTableData() {
        profileTable.setItems(FXCollections.observableArrayList(profileManager.getAllProfiles()));
    }

    /**
     * Gestionnaire pour la création d'un nouveau profil.
     * Valide les données saisies, crée le profil via le gestionnaire,
     * met à jour l'affichage et vide les champs de saisie.
     *
     * @param usernameField Le champ contenant le nom d'utilisateur
     * @param firstNameField Le champ contenant le prénom
     */
    private void handleCreateProfile(TextField usernameField, TextField firstNameField) {
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();

        if (!username.isEmpty() && !firstName.isEmpty()) {
            profileManager.getOrCreateProfile(username, firstName);
            updateTableData();
            usernameField.clear();
            firstNameField.clear();
        } else {
            showAlert("Erreur", "Les champs nom et prénom sont obligatoires.");
        }
    }

    /**
     * Gestionnaire pour la sélection d'un profil actif.
     * Récupère le profil sélectionné dans le tableau, le définit comme
     * profil actuel dans le gestionnaire et ferme la fenêtre.
     *
     * @param stage La fenêtre à fermer après sélection
     */
    private void handleSelectProfile(Stage stage) {
        PlayerProfile selectedProfile = profileTable.getSelectionModel().getSelectedItem();
        if (selectedProfile != null) {
            profileManager.setCurrentProfile(selectedProfile);
            stage.close();
        } else {
            showAlert("Sélection", "Veuillez sélectionner un profil.");
        }
    }

    /**
     * Gestionnaire pour la suppression d'un profil.
     * Récupère le profil sélectionné, le supprime via le gestionnaire
     * et met à jour l'affichage du tableau.
     */
    private void handleDeleteProfile() {
        PlayerProfile selectedProfile = profileTable.getSelectionModel().getSelectedItem();
        if (selectedProfile != null) {
            profileManager.deleteProfile(selectedProfile.getUsername());
            updateTableData();
        } else {
            showAlert("Suppression", "Veuillez sélectionner un profil à supprimer.");
        }
    }

    /**
     * Affiche une boîte de dialogue d'information à l'utilisateur.
     * Utilise une Alert JavaFX pour communiquer les messages d'erreur,
     * de validation ou d'information à l'utilisateur.
     *
     * @param title Le titre de la boîte de dialogue
     * @param content Le message à afficher
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}