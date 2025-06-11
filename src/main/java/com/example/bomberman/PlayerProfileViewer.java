package com.example.bomberman;

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

public class PlayerProfileViewer extends Application {
    private PlayerProfileManager profileManager;
    private TableView<PlayerProfile> profileTable;

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

    private void setupTableView() {
        profileTable = new TableView<>();
        profileTable.setStyle("-fx-background-color: #34495E; -fx-text-fill: white;");

        // Configuration des colonnes
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

    private GridPane createInputGrid() {
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setAlignment(Pos.CENTER);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Nom");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prénom");

        Label nomLabel = new Label("Nom :");
        Label prenomLabel = new Label("Prénom :");
        nomLabel.setStyle("-fx-text-fill: white;");
        prenomLabel.setStyle("-fx-text-fill: white;");

        inputGrid.add(nomLabel, 0, 0);
        inputGrid.add(usernameField, 1, 0);
        inputGrid.add(prenomLabel, 0, 1);
        inputGrid.add(firstNameField, 1, 1);

        return inputGrid;
    }

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

    private void updateTableData() {
        profileTable.setItems(FXCollections.observableArrayList(profileManager.getAllProfiles()));
    }

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

    private void handleSelectProfile(Stage stage) {
        PlayerProfile selectedProfile = profileTable.getSelectionModel().getSelectedItem();
        if (selectedProfile != null) {
            profileManager.setCurrentProfile(selectedProfile);
            stage.close();
        } else {
            showAlert("Sélection", "Veuillez sélectionner un profil.");
        }
    }

    private void handleDeleteProfile() {
        PlayerProfile selectedProfile = profileTable.getSelectionModel().getSelectedItem();
        if (selectedProfile != null) {
            profileManager.deleteProfile(selectedProfile.getUsername());
            updateTableData();
        } else {
            showAlert("Suppression", "Veuillez sélectionner un profil à supprimer.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}