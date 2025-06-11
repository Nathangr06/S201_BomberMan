//// MainMenuController.java - Version améliorée
//package com.example.bomberman;
//
//import javafx.animation.*;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.*;
//import javafx.scene.effect.DropShadow;
//import javafx.scene.effect.Glow;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Circle;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//import javafx.util.Duration;
//
//import java.io.File;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.ResourceBundle;
//
//public class MainMenuController implements Initializable {
//
//    @FXML private VBox mainContainer;
//    @FXML private Label titleLabel;
//    @FXML private Button playButton;
//    @FXML private Button editorButton;
//    @FXML private Button loadMapButton;
//    @FXML private Button exitButton;
//    @FXML private Label selectedMapLabel;
//    @FXML private VBox mapInfoContainer;
//    @FXML private Pane backgroundPane;
//    @FXML private Button playAIButton;
//    @FXML private ComboBox<String> texturePackComboBox;
//    @FXML private Label texturePackLabel;
//
//    private File selectedMapFile;
//    private Timeline backgroundAnimation;
//    private List<Circle> backgroundElements;
//    private TextureManager textureManager;
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        textureManager = TextureManager.getInstance();
//        setupTexturePacks();
//        setupUI();
//        setupAnimations();
//        createBackgroundAnimation();
//    }
//
//    private void setupTexturePacks() {
//        if (texturePackComboBox != null) {
//            // Récupérer les texture packs disponibles depuis le TextureManager
//            List<String> availablePacks = textureManager.getAvailableTexturePacks();
//
//            // Vider la ComboBox et ajouter seulement les packs disponibles
//            texturePackComboBox.getItems().clear();
//
//            for (String packName : availablePacks) {
//                // Utiliser le nom d'affichage formaté
//                String displayName = textureManager.getDisplayName(packName);
//                texturePackComboBox.getItems().add(displayName);
//            }
//
//            // Sélectionner le pack actuel
//            String currentPack = textureManager.getCurrentTexturePack();
//            String currentDisplayName = textureManager.getDisplayName(currentPack);
//            texturePackComboBox.setValue(currentDisplayName);
//
//            // Style de la ComboBox
//            texturePackComboBox.setStyle(
//                    "-fx-background-color: #34495E; " +
//                            "-fx-text-fill: white; " +
//                            "-fx-font-weight: bold; " +
//                            "-fx-font-size: 14px; " +
//                            "-fx-background-radius: 15;"
//            );
//
//            // Style des éléments du dropdown
//            texturePackComboBox.setCellFactory(listView -> new ListCell<String>() {
//                @Override
//                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty || item == null) {
//                        setText(null);
//                    } else {
//                        setText(item);
//                        setStyle("-fx-background-color: #2C3E50; -fx-text-fill: white; -fx-padding: 8px;");
//                    }
//                }
//            });
//
//            // Gestionnaire d'événements pour le changement de texture pack
//            texturePackComboBox.setOnAction(e -> handleTexturePackChange());
//
//            // Afficher le nombre de packs disponibles
//            System.out.println("Texture packs disponibles dans l'interface: " + availablePacks.size());
//        }
//
//        if (texturePackLabel != null) {
//            texturePackLabel.setStyle(
//                    "-fx-text-fill: white; " +
//                            "-fx-font-weight: bold; " +
//                            "-fx-font-size: 16px;"
//            );
//            texturePackLabel.setEffect(new Glow(0.3));
//        }
//    }
//
//    // Méthode pour convertir le nom d'affichage vers le nom technique
//    private String getPackNameFromDisplayName(String displayName) {
//        List<String> availablePacks = textureManager.getAvailableTexturePacks();
//
//        for (String packName : availablePacks) {
//            if (textureManager.getDisplayName(packName).equals(displayName)) {
//                return packName;
//            }
//        }
//        return "default"; // Fallback
//    }
//
//    private void setupUI() {
//        // Configuration du titre avec effet de lueur
//        titleLabel.setEffect(new Glow(0.8));
//
//        // Configuration des boutons avec effets
//        setupButton(playButton, "#FF6B35");
//        setupButton(editorButton, "#4ECDC4");
//        setupButton(loadMapButton, "#9B59B6");
//        setupButton(exitButton, "#E74C3C");
//        setupButton(playAIButton, "#8E44AD");
//
//        // Masquer les informations de carte par défaut
//        mapInfoContainer.setVisible(false);
//    }
//
//    private void setupButton(Button button, String color) {
//        button.setStyle("-fx-background-color: " + color + "; " +
//                "-fx-text-fill: white; " +
//                "-fx-font-weight: bold; " +
//                "-fx-font-size: 16px; " +
//                "-fx-background-radius: 25;");
//
//        DropShadow dropShadow = new DropShadow();
//        dropShadow.setColor(Color.web(color, 0.5));
//        dropShadow.setOffsetX(0);
//        dropShadow.setOffsetY(5);
//        dropShadow.setRadius(10);
//        button.setEffect(dropShadow);
//
//        // Animations de survol
//        button.setOnMouseEntered(e -> {
//            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
//            st.setToX(1.05);
//            st.setToY(1.05);
//            st.play();
//
//            Glow glow = new Glow(0.5);
//            button.setEffect(glow);
//        });
//
//        button.setOnMouseExited(e -> {
//            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
//            st.setToX(1.0);
//            st.setToY(1.0);
//            st.play();
//
//            button.setEffect(dropShadow);
//        });
//    }
//
//    private void setupAnimations() {
//        // Animation du titre
//        RotateTransition titleRotate = new RotateTransition(Duration.seconds(0.5), titleLabel);
//        titleRotate.setFromAngle(-2);
//        titleRotate.setToAngle(2);
//        titleRotate.setCycleCount(Timeline.INDEFINITE);
//        titleRotate.setAutoReverse(true);
//        titleRotate.play();
//
//        // Animation d'apparition des boutons et éléments
//        Timeline buttonAnimation = new Timeline();
//        Button[] buttons = {playButton, playAIButton, editorButton, loadMapButton, exitButton};
//
//        for (int i = 0; i < buttons.length; i++) {
//            buttons[i].setOpacity(0);
//            buttons[i].setTranslateX(-50);
//
//            KeyFrame kf = new KeyFrame(
//                    Duration.millis(300 + i * 150),
//                    new KeyValue(buttons[i].opacityProperty(), 1),
//                    new KeyValue(buttons[i].translateXProperty(), 0)
//            );
//            buttonAnimation.getKeyFrames().add(kf);
//        }
//
//        // Animation pour le sélecteur de texture pack
//        if (texturePackComboBox != null && texturePackLabel != null) {
//            texturePackLabel.setOpacity(0);
//            texturePackComboBox.setOpacity(0);
//            texturePackLabel.setTranslateX(50);
//            texturePackComboBox.setTranslateX(50);
//
//            KeyFrame labelKf = new KeyFrame(
//                    Duration.millis(1200),
//                    new KeyValue(texturePackLabel.opacityProperty(), 1),
//                    new KeyValue(texturePackLabel.translateXProperty(), 0)
//            );
//            KeyFrame comboKf = new KeyFrame(
//                    Duration.millis(1350),
//                    new KeyValue(texturePackComboBox.opacityProperty(), 1),
//                    new KeyValue(texturePackComboBox.translateXProperty(), 0)
//            );
//
//            buttonAnimation.getKeyFrames().addAll(List.of(labelKf, comboKf));
//        }
//
//        buttonAnimation.play();
//    }
//
//    private void createBackgroundAnimation() {
//        backgroundElements = new ArrayList<>();
//        Random random = new Random();
//
//        // Créer des éléments de fond animés (bombes et explosions)
//        for (int i = 0; i < 15; i++) {
//            Circle element = new Circle();
//            element.setRadius(random.nextDouble() * 10 + 5);
//            element.setFill(i % 2 == 0 ? Color.web("#333333", 0.3) : Color.web("#FF6B35", 0.2));
//            element.setCenterX(random.nextDouble() * 800);
//            element.setCenterY(random.nextDouble() * 600);
//
//            backgroundElements.add(element);
//            backgroundPane.getChildren().add(element);
//
//            // Animation de flottement
//            TranslateTransition tt = new TranslateTransition(
//                    Duration.seconds(3 + random.nextDouble() * 4), element);
//            tt.setFromY(0);
//            tt.setToY(-30 - random.nextDouble() * 20);
//            tt.setCycleCount(Timeline.INDEFINITE);
//            tt.setAutoReverse(true);
//            tt.setDelay(Duration.seconds(random.nextDouble() * 2));
//            tt.play();
//
//            // Animation de rotation
//            RotateTransition rt = new RotateTransition(
//                    Duration.seconds(5 + random.nextDouble() * 5), element);
//            rt.setFromAngle(0);
//            rt.setToAngle(360);
//            rt.setCycleCount(Timeline.INDEFINITE);
//            rt.play();
//        }
//    }
//
//    @FXML
//    private void handleTexturePackChange() {
//        String selectedDisplayName = texturePackComboBox.getValue();
//        if (selectedDisplayName != null) {
//            try {
//                // Convertir le nom d'affichage vers le nom technique
//                String selectedPack = getPackNameFromDisplayName(selectedDisplayName);
//
//                // Charger le nouveau texture pack
//                textureManager.setTexturePack(selectedPack);
//
//                // Animation de confirmation
//                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), texturePackComboBox);
//                scaleTransition.setToX(1.1);
//                scaleTransition.setToY(1.1);
//                scaleTransition.setAutoReverse(true);
//                scaleTransition.setCycleCount(2);
//                scaleTransition.play();
//
//                showNotification("🎨 Texture pack '" + selectedDisplayName + "' appliqué!", NotificationType.SUCCESS);
//
//            } catch (Exception e) {
//                showNotification("❌ Erreur lors du changement de texture pack: " + e.getMessage(),
//                        NotificationType.ERROR);
//                // Remettre la valeur précédente en cas d'erreur
//                String currentPack = textureManager.getCurrentTexturePack();
//                String currentDisplayName = textureManager.getDisplayName(currentPack);
//                texturePackComboBox.setValue(currentDisplayName);
//            }
//        }
//    }
//
//    @FXML
//    private void handlePlayGame() {
//        showNotification("🎮 Lancement du jeu...", NotificationType.INFO);
//
//        try {
//            // Fermer la fenêtre actuelle
//            Stage currentStage = (Stage) playButton.getScene().getWindow();
//
//            // Lancer le jeu principal
//            BombermanGame game = new BombermanGame();
//            Stage gameStage = new Stage();
//            game.startGame(gameStage);
//
//            currentStage.close();
//
//        } catch (Exception e) {
//            showNotification("❌ Erreur lors du lancement du jeu: " + e.getMessage(),
//                    NotificationType.ERROR);
//            e.printStackTrace();
//        }
//    }
//
//    @FXML
//    private void handlePlayAI() {
//        showNotification("🤖 Lancement du jeu contre l'IA...", NotificationType.INFO);
//
//        try {
//            Stage currentStage = (Stage) playAIButton.getScene().getWindow();
//
//            BombermanGame game = new BombermanGame();
//            game.setAIMode(true);
//            Stage gameStage = new Stage();
//            game.startGame(gameStage);
//
//            currentStage.close();
//
//        } catch (Exception e) {
//            showNotification("❌ Erreur lors du lancement du jeu IA: " + e.getMessage(),
//                    NotificationType.ERROR);
//            e.printStackTrace();
//        }
//    }
//
//    @FXML
//    private void handleOpenEditor() {
//        showNotification("🛠️ Ouverture de l'éditeur...", NotificationType.SUCCESS);
//
//        try {
//            // Fermer la fenêtre actuelle
//            Stage currentStage = (Stage) editorButton.getScene().getWindow();
//
//            // Lancer l'éditeur
//            BombermanLevelEditor editor = new BombermanLevelEditor();
//            Stage editorStage = new Stage();
//            editor.start(editorStage);
//
//            currentStage.close();
//
//        } catch (Exception e) {
//            showNotification("❌ Erreur lors de l'ouverture de l'éditeur: " + e.getMessage(),
//                    NotificationType.ERROR);
//            e.printStackTrace();
//        }
//    }
//
//    @FXML
//    private void handleLoadMap() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("📂 Choisir une carte personnalisée");
//        fileChooser.getExtensionFilters().add(
//                new FileChooser.ExtensionFilter("Fichiers Bomberman (*.bmn)", "*.bmn")
//        );
//
//        Stage stage = (Stage) loadMapButton.getScene().getWindow();
//        File file = fileChooser.showOpenDialog(stage);
//
//        if (file != null) {
//            selectedMapFile = file;
//            selectedMapLabel.setText("📋 Carte sélectionnée: " + file.getName());
//            mapInfoContainer.setVisible(true);
//
//            // Animation d'apparition des infos de carte
//            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mapInfoContainer);
//            fadeIn.setFromValue(0);
//            fadeIn.setToValue(1);
//            fadeIn.play();
//
//            showNotification("✅ Carte \"" + file.getName() + "\" sélectionnée!",
//                    NotificationType.SUCCESS);
//        }
//    }
//
//    @FXML
//    private void handlePlayWithCustomMap() {
//        if (selectedMapFile != null) {
//            showNotification("🚀 Lancement avec carte personnalisée...", NotificationType.INFO);
//
//            try {
//                Stage currentStage = (Stage) playButton.getScene().getWindow();
//
//                BombermanGame game = new BombermanGame();
//                Stage gameStage = new Stage();
//                game.startGame(gameStage, selectedMapFile);
//
//                currentStage.close();
//
//            } catch (Exception e) {
//                showNotification("❌ Erreur lors du lancement: " + e.getMessage(),
//                        NotificationType.ERROR);
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @FXML
//    private void handleClearMap() {
//        selectedMapFile = null;
//        mapInfoContainer.setVisible(false);
//        showNotification("🗑️ Sélection de carte effacée", NotificationType.INFO);
//    }
//
//    @FXML
//    private void handleExit() {
//        showNotification("👋 Au revoir!", NotificationType.INFO);
//
//        // Animation de fermeture
//        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), mainContainer);
//        fadeOut.setFromValue(1);
//        fadeOut.setToValue(0);
//        fadeOut.setOnFinished(e -> {
//            Stage stage = (Stage) exitButton.getScene().getWindow();
//            stage.close();
//        });
//        fadeOut.play();
//    }
//
//    private void showNotification(String message, NotificationType type) {
//        // Créer une notification temporaire
//        Label notification = new Label(message);
//        notification.setStyle(
//                "-fx-background-color: " + type.getColor() + "; " +
//                        "-fx-text-fill: white; " +
//                        "-fx-padding: 10 20; " +
//                        "-fx-background-radius: 20; " +
//                        "-fx-font-weight: bold; " +
//                        "-fx-font-size: 14px;"
//        );
//
//        // Positionner la notification
//        notification.setLayoutX(10);
//        notification.setLayoutY(10);
//        notification.setOpacity(0);
//
//        backgroundPane.getChildren().add(notification);
//
//        // Animation d'apparition et disparition
//        Timeline notificationTimeline = new Timeline(
//                new KeyFrame(Duration.ZERO,
//                        new KeyValue(notification.opacityProperty(), 0),
//                        new KeyValue(notification.translateYProperty(), -20)
//                ),
//                new KeyFrame(Duration.millis(300),
//                        new KeyValue(notification.opacityProperty(), 1),
//                        new KeyValue(notification.translateYProperty(), 0)
//                ),
//                new KeyFrame(Duration.millis(2700),
//                        new KeyValue(notification.opacityProperty(), 1)
//                ),
//                new KeyFrame(Duration.millis(3000),
//                        new KeyValue(notification.opacityProperty(), 0),
//                        new KeyValue(notification.translateYProperty(), -20)
//                )
//        );
//
//        notificationTimeline.setOnFinished(e -> backgroundPane.getChildren().remove(notification));
//        notificationTimeline.play();
//    }
//
//    private enum NotificationType {
//        INFO("#3498DB"), SUCCESS("#27AE60"), ERROR("#E74C3C");
//
//        private final String color;
//
//        NotificationType(String color) {
//            this.color = color;
//        }
//
//        public String getColor() {
//            return color;
//        }
//    }
//}