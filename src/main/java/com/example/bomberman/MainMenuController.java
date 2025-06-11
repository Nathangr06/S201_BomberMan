// MainMenuController.java - Version corrigée avec modes séparés
package com.example.bomberman;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    // Éléments FXML - doivent correspondre aux fx:id dans le fichier FXML
    @FXML private VBox mainContainer;
    @FXML private Label titleLabel;
    @FXML private Button playButton;
    @FXML private Button editorButton;
    @FXML private Button loadMapButton;
    @FXML private Button exitButton;
    @FXML private Label selectedMapLabel;
    @FXML private VBox mapInfoContainer;
    @FXML private Pane backgroundPane;
    @FXML private Button playAIButton;
    @FXML private Button profileButton; // Ajout du bouton profil

    // Variables d'instance
    private File selectedMapFile;
    private Timeline backgroundAnimation;
    private List<Circle> backgroundElements;

    // Boutons ajoutés programmatiquement
    private Button play4PlayersButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setupUI();
            addMissingButtons();
            setupAnimations();
            createBackgroundAnimation();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupUI() {
        try {
            // Configuration du titre avec effet de lueur
            if (titleLabel != null) {
                titleLabel.setEffect(new Glow(0.8));
            }

            // Configuration des boutons avec effets
            if (playButton != null) {
                setupButton(playButton, "#FF6B35");
                // Garder le texte original pour 2 joueurs
                playButton.setText("🎮 JOUER (2 JOUEURS)");
            }
            if (playAIButton != null) setupButton(playAIButton, "#8E44AD");
            if (editorButton != null) setupButton(editorButton, "#4ECDC4");
            if (loadMapButton != null) setupButton(loadMapButton, "#9B59B6");
            if (exitButton != null) setupButton(exitButton, "#E74C3C");

            // Masquer les informations de carte par défaut
            if (mapInfoContainer != null) {
                mapInfoContainer.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration de l'UI: " + e.getMessage());
        }
    }

    private void addMissingButtons() {
        try {
            // Créer le bouton 4 joueurs programmatiquement
            play4PlayersButton = new Button("👥 JOUER (4 JOUEURS)");
            play4PlayersButton.setPrefHeight(50.0);
            play4PlayersButton.setPrefWidth(300.0);
            play4PlayersButton.setFont(Font.font("System Bold", 18.0));
            setupButton(play4PlayersButton, "#FF4757");
            play4PlayersButton.setOnAction(e -> handlePlay4Players());

            // Trouver le conteneur des boutons (VBox avec les boutons)
            if (mainContainer != null && mainContainer.getChildren().size() > 1) {
                // Le deuxième enfant devrait être le VBox contenant les boutons
                var buttonContainer = mainContainer.getChildren().get(1);
                if (buttonContainer instanceof VBox) {
                    VBox vbox = (VBox) buttonContainer;

                    // Insérer le bouton 4 joueurs après le bouton jouer (index 1)
                    if (vbox.getChildren().size() > 1) {
                        vbox.getChildren().add(1, play4PlayersButton);
                    } else {
                        vbox.getChildren().add(play4PlayersButton);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout des boutons manquants: " + e.getMessage());
        }
    }

    private void setupButton(Button button, String color) {
        if (button == null) return;

        try {
            button.setStyle("-fx-background-color: " + color + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 16px; " +
                    "-fx-background-radius: 25;");

            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.web(color, 0.5));
            dropShadow.setOffsetX(0);
            dropShadow.setOffsetY(5);
            dropShadow.setRadius(10);
            button.setEffect(dropShadow);

            // Animations de survol
            button.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
                st.setToX(1.05);
                st.setToY(1.05);
                st.play();

                Glow glow = new Glow(0.5);
                button.setEffect(glow);
            });

            button.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();

                button.setEffect(dropShadow);
            });
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration du bouton: " + e.getMessage());
        }
    }

    private void setupAnimations() {
        try {
            if (titleLabel != null) {
                // Animation du titre
                RotateTransition titleRotate = new RotateTransition(Duration.seconds(0.5), titleLabel);
                titleRotate.setFromAngle(-2);
                titleRotate.setToAngle(2);
                titleRotate.setCycleCount(Timeline.INDEFINITE);
                titleRotate.setAutoReverse(true);
                titleRotate.play();
            }

            // Animation d'apparition des boutons
            Timeline buttonAnimation = new Timeline();
            List<Button> buttons = new ArrayList<>();
            if (playButton != null) buttons.add(playButton);
            if (play4PlayersButton != null) buttons.add(play4PlayersButton);
            if (playAIButton != null) buttons.add(playAIButton);
            if (editorButton != null) buttons.add(editorButton);
            if (loadMapButton != null) buttons.add(loadMapButton);
            if (exitButton != null) buttons.add(exitButton);

            for (int i = 0; i < buttons.size(); i++) {
                Button button = buttons.get(i);
                if (button != null) {
                    button.setOpacity(0);
                    button.setTranslateX(-50);

                    KeyFrame kf = new KeyFrame(
                            Duration.millis(300 + i * 150),
                            new KeyValue(button.opacityProperty(), 1),
                            new KeyValue(button.translateXProperty(), 0)
                    );
                    buttonAnimation.getKeyFrames().add(kf);
                }
            }
            buttonAnimation.play();
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des animations: " + e.getMessage());
        }
    }

    private void createBackgroundAnimation() {
        if (backgroundPane == null) return;

        try {
            backgroundElements = new ArrayList<>();
            Random random = new Random();

            // Créer des éléments de fond animés (bombes et explosions)
            for (int i = 0; i < 10; i++) { // Moins d'éléments car le pane est plus petit
                Circle element = new Circle();
                element.setRadius(random.nextDouble() * 8 + 3);
                element.setFill(i % 2 == 0 ? Color.web("#333333", 0.2) : Color.web("#FF6B35", 0.15));
                element.setCenterX(random.nextDouble() * 800);
                element.setCenterY(random.nextDouble() * 100);

                backgroundElements.add(element);
                backgroundPane.getChildren().add(element);

                // Animation de flottement
                TranslateTransition tt = new TranslateTransition(
                        Duration.seconds(3 + random.nextDouble() * 4), element);
                tt.setFromY(0);
                tt.setToY(-20 - random.nextDouble() * 15);
                tt.setCycleCount(Timeline.INDEFINITE);
                tt.setAutoReverse(true);
                tt.setDelay(Duration.seconds(random.nextDouble() * 2));
                tt.play();

                // Animation de rotation
                RotateTransition rt = new RotateTransition(
                        Duration.seconds(5 + random.nextDouble() * 5), element);
                rt.setFromAngle(0);
                rt.setToAngle(360);
                rt.setCycleCount(Timeline.INDEFINITE);
                rt.play();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'animation de fond: " + e.getMessage());
        }
    }

    // MÉTHODES FXML - Mode 2 joueurs direct
    @FXML
    public void handlePlayGame() {
        showNotification("🎮 Lancement du jeu 2 joueurs...", NotificationType.INFO);

        try {
            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) playButton.getScene().getWindow();

            // Lancer le jeu principal
            BombermanGame game = new BombermanGame();
            game.setPlayerCount(2); // Mode 2 joueurs
            Stage gameStage = new Stage();
            game.startGame(gameStage);

            currentStage.close();

        } catch (Exception e) {
            showNotification("❌ Erreur lors du lancement du jeu: " + e.getMessage(),
                    NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    // Mode 4 joueurs direct
    public void handlePlay4Players() {
        showNotification("🎮👥 Lancement du jeu 4 joueurs...", NotificationType.INFO);

        try {
            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) playButton.getScene().getWindow();

            // Lancer le jeu principal avec 4 joueurs
            BombermanGame game = new BombermanGame();
            game.setPlayerCount(4); // Mode 4 joueurs
            Stage gameStage = new Stage();
            game.startGame(gameStage);

            currentStage.close();

        } catch (Exception e) {
            showNotification("❌ Erreur lors du lancement du jeu 4 joueurs: " + e.getMessage(),
                    NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePlayAI() {
        showNotification("🤖 Lancement du jeu contre l'IA...", NotificationType.INFO);

        try {
            Stage currentStage = (Stage) playAIButton.getScene().getWindow();

            BombermanGame game = new BombermanGame();
            game.setAIMode(true);
            Stage gameStage = new Stage();
            game.startGame(gameStage);

            currentStage.close();

        } catch (Exception e) {
            showNotification("❌ Erreur lors du lancement du jeu IA: " + e.getMessage(),
                    NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleOpenEditor() {
        showNotification("🛠️ Ouverture de l'éditeur...", NotificationType.SUCCESS);

        try {
            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) editorButton.getScene().getWindow();

            // Lancer l'éditeur
            BombermanLevelEditor editor = new BombermanLevelEditor();
            Stage editorStage = new Stage();
            editor.start(editorStage);

            currentStage.close();

        } catch (Exception e) {
            showNotification("❌ Erreur lors de l'ouverture de l'éditeur: " + e.getMessage(),
                    NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLoadMap() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("📂 Choisir une carte personnalisée");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers Bomberman (*.bmn)", "*.bmn")
            );

            Stage stage = (Stage) loadMapButton.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                selectedMapFile = file;
                if (selectedMapLabel != null) {
                    selectedMapLabel.setText("📋 Carte sélectionnée: " + file.getName());
                }
                if (mapInfoContainer != null) {
                    mapInfoContainer.setVisible(true);

                    // Animation d'apparition des infos de carte
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mapInfoContainer);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();
                }

                showNotification("✅ Carte \"" + file.getName() + "\" sélectionnée!",
                        NotificationType.SUCCESS);
            }
        } catch (Exception e) {
            showNotification("❌ Erreur lors du chargement de la carte: " + e.getMessage(),
                    NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePlayWithCustomMap() {
        if (selectedMapFile != null) {
            // Créer un menu pour choisir le mode avec la carte personnalisée
            ContextMenu customMapMenu = new ContextMenu();

            MenuItem map2Players = new MenuItem("🎮 2 Joueurs avec cette carte");
            map2Players.setOnAction(e -> launchGameWithMap(2));

            MenuItem map4Players = new MenuItem("👥 4 Joueurs avec cette carte");
            map4Players.setOnAction(e -> launchGameWithMap(4));

            MenuItem mapAI = new MenuItem("🤖 IA avec cette carte");
            mapAI.setOnAction(e -> launchGameWithMapAI());

            customMapMenu.getItems().addAll(map2Players, map4Players, mapAI);

            // Afficher le menu près du bouton
            customMapMenu.show(mapInfoContainer,
                    mapInfoContainer.getLayoutX() + mapInfoContainer.getWidth()/2,
                    mapInfoContainer.getLayoutY());
        }
    }

    private void launchGameWithMap(int playerCount) {
        showNotification("🚀 Lancement avec carte personnalisée (" + playerCount + " joueurs)...", NotificationType.INFO);

        try {
            Stage currentStage = (Stage) playButton.getScene().getWindow();

            BombermanGame game = new BombermanGame();
            game.setPlayerCount(playerCount);
            Stage gameStage = new Stage();
            game.startGame(gameStage, selectedMapFile);

            currentStage.close();

        } catch (Exception e) {
            showNotification("❌ Erreur lors du lancement: " + e.getMessage(),
                    NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    private void launchGameWithMapAI() {
        showNotification("🚀 Lancement IA avec carte personnalisée...", NotificationType.INFO);

        try {
            Stage currentStage = (Stage) playButton.getScene().getWindow();

            BombermanGame game = new BombermanGame();
            game.setAIMode(true);
            Stage gameStage = new Stage();
            game.startGame(gameStage, selectedMapFile);

            currentStage.close();

        } catch (Exception e) {
            showNotification("❌ Erreur lors du lancement: " + e.getMessage(),
                    NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClearMap() {
        selectedMapFile = null;
        if (selectedMapLabel != null) {
            selectedMapLabel.setText("📋 Aucune carte sélectionnée");
        }
        if (mapInfoContainer != null) {
            mapInfoContainer.setVisible(false);
        }
        showNotification("🗑️ Sélection de carte effacée", NotificationType.INFO);
    }

    @FXML
    public void handleExit() {
        showNotification("👋 Au revoir!", NotificationType.INFO);

        try {
            // Animation de fermeture
            if (mainContainer != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), mainContainer);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    Stage stage = (Stage) exitButton.getScene().getWindow();
                    stage.close();
                });
                fadeOut.play();
            } else {
                // Fermeture directe si pas d'animation possible
                Stage stage = (Stage) exitButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture: " + e.getMessage());
            System.exit(0);
        }
    }

    @FXML
    private void handlePlayCaptureTheFlag() {
        System.out.println("Capture The Flag mode launched");
        Stage stage = new Stage();
        CaptureTheFlag ctf = new CaptureTheFlag();
        try {
            ctf.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showNotification(String message, NotificationType type) {
        // Utiliser la console comme fallback si pas d'interface graphique disponible
        System.out.println(message);

        if (mainContainer != null) {
            try {
                // Créer une notification temporaire
                Label notification = new Label(message);
                notification.setStyle(
                        "-fx-background-color: " + type.getColor() + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 10 20; " +
                                "-fx-background-radius: 20; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 12px;"
                );

                // Positionner la notification en haut
                notification.setOpacity(0);

                // Ajouter au conteneur principal temporairement
                mainContainer.getChildren().add(0, notification);

                // Animation d'apparition et disparition
                Timeline notificationTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(notification.opacityProperty(), 0)
                        ),
                        new KeyFrame(Duration.millis(300),
                                new KeyValue(notification.opacityProperty(), 1)
                        ),
                        new KeyFrame(Duration.millis(2700),
                                new KeyValue(notification.opacityProperty(), 1)
                        ),
                        new KeyFrame(Duration.millis(3000),
                                new KeyValue(notification.opacityProperty(), 0)
                        )
                );

                notificationTimeline.setOnFinished(e -> mainContainer.getChildren().remove(notification));
                notificationTimeline.play();
            } catch (Exception e) {
                System.err.println("Erreur lors de l'affichage de la notification: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleProfileButton() {
        showNotification("📊 Ouverture du profil...", NotificationType.INFO);

        try {
            PlayerProfileViewer profileView = new PlayerProfileViewer(); // Ta classe vue
            Stage profileStage = new Stage();
            profileView.start(profileStage);
        } catch (Exception e) {
            showNotification("❌ Erreur lors de l'ouverture du profil: " + e.getMessage(),
                    NotificationType.ERROR);
            e.printStackTrace();
        }
    }
    private enum NotificationType {
        INFO("#3498DB"), SUCCESS("#27AE60"), ERROR("#E74C3C");

        private final String color;

        NotificationType(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }
}