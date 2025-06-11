package com.example.bomberman;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Contrôleur de la scène du menu principal de Bomberman.
 * Gère l'interface utilisateur du menu principal, les animations,
 * la sélection de cartes personnalisées, le changement de pack de textures,
 * et le lancement des différentes parties du jeu (mode normal, IA, éditeur).
 */
public class MainMenuController implements Initializable {

    /** Conteneur principal du menu (VBox). */
    @FXML private VBox mainContainer;

    /** Label du titre du menu. */
    @FXML private Label titleLabel;

    /** Bouton pour lancer une partie normale. */
    @FXML private Button playButton;

    /** Bouton pour ouvrir l'éditeur de niveaux. */
    @FXML private Button editorButton;

    /** Bouton pour charger une carte personnalisée. */
    @FXML private Button loadMapButton;

    /** Bouton pour quitter l'application. */
    @FXML private Button exitButton;

    /** Label affichant la carte sélectionnée. */
    @FXML private Label selectedMapLabel;

    /** Conteneur affichant les infos de la carte chargée. */
    @FXML private VBox mapInfoContainer;

    /** Panneau servant de fond pour les animations et notifications. */
    @FXML private Pane backgroundPane;

    /** Bouton pour lancer une partie contre l'IA. */
    @FXML private Button playAIButton;

    /** ComboBox pour sélectionner le pack de textures. */
    @FXML private ComboBox<String> texturePackComboBox;

    /** Label décrivant la sélection du pack de textures. */
    @FXML private Label texturePackLabel;

    /** Fichier de la carte personnalisée sélectionnée par l'utilisateur. */
    private File selectedMapFile;

    /** Liste des éléments graphiques animés en arrière-plan. */
    private List<Circle> backgroundElements;

    /** Gestionnaire des packs de textures. */
    private TextureManager textureManager;

    /**
     * Initialise le contrôleur après le chargement du fichier FXML.
     * Configure les packs de textures, l'interface utilisateur,
     * les animations, et crée l'animation du fond.
     *
     * @param location emplacement du fichier FXML (non utilisé).
     * @param resources ressources associées (non utilisées).
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textureManager = TextureManager.getInstance();
        setupTexturePacks();
        setupUI();
        setupAnimations();
        createBackgroundAnimation();
    }

    /**
     * Configure la ComboBox des packs de textures avec les packs disponibles,
     * définit le pack sélectionné et applique le style personnalisé.
     */
    private void setupTexturePacks() {
        if (texturePackComboBox != null) {
            List<String> availablePacks = textureManager.getAvailableTexturePacks();
            texturePackComboBox.getItems().clear();
            for (String packName : availablePacks) {
                String displayName = textureManager.getDisplayName(packName);
                texturePackComboBox.getItems().add(displayName);
            }
            String currentPack = textureManager.getCurrentTexturePack();
            texturePackComboBox.setValue(textureManager.getDisplayName(currentPack));
            texturePackComboBox.setStyle(
                    "-fx-background-color: #34495E; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-background-radius: 15;"
            );
            texturePackComboBox.setCellFactory(listView -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-background-color: #2C3E50; -fx-text-fill: white; -fx-padding: 8px;");
                    }
                }
            });
            texturePackComboBox.setOnAction(e -> handleTexturePackChange());
        }

        if (texturePackLabel != null) {
            texturePackLabel.setStyle(
                    "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 16px;"
            );
            texturePackLabel.setEffect(new Glow(0.3));
        }
    }

    /**
     * Récupère le nom interne du pack de textures à partir de son nom affiché.
     *
     * @param displayName nom affiché du pack de textures.
     * @return nom interne du pack, ou "default" si non trouvé.
     */
    private String getPackNameFromDisplayName(String displayName) {
        List<String> availablePacks = textureManager.getAvailableTexturePacks();
        for (String packName : availablePacks) {
            if (textureManager.getDisplayName(packName).equals(displayName)) {
                return packName;
            }
        }
        return "default";
    }

    /**
     * Configure le style des boutons et effets visuels du menu.
     */
    private void setupUI() {
        titleLabel.setEffect(new Glow(0.8));
        setupButton(playButton, "#FF6B35");
        setupButton(editorButton, "#4ECDC4");
        setupButton(loadMapButton, "#9B59B6");
        setupButton(exitButton, "#E74C3C");
        setupButton(playAIButton, "#8E44AD");
        mapInfoContainer.setVisible(false);
    }

    /**
     * Applique le style et les effets d'animation au bouton donné.
     *
     * @param button bouton à configurer.
     * @param color couleur de fond en hexadécimal.
     */
    private void setupButton(Button button, String color) {
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

        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
            button.setEffect(new Glow(0.5));
        });

        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            button.setEffect(dropShadow);
        });
    }

    /**
     * Configure les animations d'entrée des éléments du menu (titre, boutons, combo).
     */
    private void setupAnimations() {
        RotateTransition titleRotate = new RotateTransition(Duration.seconds(0.5), titleLabel);
        titleRotate.setFromAngle(-2);
        titleRotate.setToAngle(2);
        titleRotate.setCycleCount(Animation.INDEFINITE);
        titleRotate.setAutoReverse(true);
        titleRotate.play();

        Timeline buttonAnimation = new Timeline();
        Button[] buttons = {playButton, playAIButton, editorButton, loadMapButton, exitButton};
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setOpacity(0);
            buttons[i].setTranslateX(-50);
            KeyFrame kf = new KeyFrame(Duration.millis(300 + i * 150),
                    new KeyValue(buttons[i].opacityProperty(), 1),
                    new KeyValue(buttons[i].translateXProperty(), 0));
            buttonAnimation.getKeyFrames().add(kf);
        }

        if (texturePackComboBox != null && texturePackLabel != null) {
            texturePackLabel.setOpacity(0);
            texturePackComboBox.setOpacity(0);
            texturePackLabel.setTranslateX(50);
            texturePackComboBox.setTranslateX(50);
            KeyFrame labelKf = new KeyFrame(Duration.millis(1200),
                    new KeyValue(texturePackLabel.opacityProperty(), 1),
                    new KeyValue(texturePackLabel.translateXProperty(), 0));
            KeyFrame comboKf = new KeyFrame(Duration.millis(1350),
                    new KeyValue(texturePackComboBox.opacityProperty(), 1),
                    new KeyValue(texturePackComboBox.translateXProperty(), 0));
            buttonAnimation.getKeyFrames().addAll(labelKf, comboKf);
        }
        buttonAnimation.play();
    }

    /**
     * Crée et lance l'animation des éléments graphiques circulaires en arrière-plan.
     */
    private void createBackgroundAnimation() {
        backgroundElements = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 15; i++) {
            Circle element = new Circle();
            element.setRadius(random.nextDouble() * 10 + 5);
            element.setFill(i % 2 == 0 ? Color.web("#333333", 0.3) : Color.web("#FF6B35", 0.2));
            element.setCenterX(random.nextDouble() * 800);
            element.setCenterY(random.nextDouble() * 600);

            backgroundElements.add(element);
            backgroundPane.getChildren().add(element);

            TranslateTransition tt = new TranslateTransition(Duration.seconds(3 + random.nextDouble() * 4), element);
            tt.setFromY(0);
            tt.setToY(-30 - random.nextDouble() * 20);
            tt.setCycleCount(Animation.INDEFINITE);
            tt.setAutoReverse(true);
            tt.setDelay(Duration.seconds(random.nextDouble() * 2));
            tt.play();

            RotateTransition rt = new RotateTransition(Duration.seconds(5 + random.nextDouble() * 5), element);
            rt.setFromAngle(0);
            rt.setToAngle(360);
            rt.setCycleCount(Animation.INDEFINITE);
            rt.play();
        }
    }

    /**
     * Gère le changement de pack de textures sélectionné par l'utilisateur.
     * Met à jour le pack dans le gestionnaire et affiche une notification.
     */
    @FXML
    private void handleTexturePackChange() {
        String selectedDisplayName = texturePackComboBox.getValue();
        if (selectedDisplayName != null) {
            try {
                String selectedPack = getPackNameFromDisplayName(selectedDisplayName);
                textureManager.setTexturePack(selectedPack);

                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), texturePackComboBox);
                scaleTransition.setToX(1.1);
                scaleTransition.setToY(1.1);
                scaleTransition.setAutoReverse(true);
                scaleTransition.setCycleCount(2);
                scaleTransition.play();

                showNotification("🎨 Texture pack '" + selectedDisplayName + "' appliqué!", NotificationType.SUCCESS);

            } catch (Exception e) {
                showNotification("❌ Erreur lors du changement de texture pack: " + e.getMessage(), NotificationType.ERROR);
                String currentPack = textureManager.getCurrentTexturePack();
                texturePackComboBox.setValue(textureManager.getDisplayName(currentPack));
            }
        }
    }

    /**
     * Lance une partie normale du jeu.
     * Ferme la fenêtre du menu principal et ouvre la fenêtre du jeu.
     */
    @FXML
    private void handlePlayGame() {
        showNotification("🎮 Lancement du jeu...", NotificationType.INFO);
        try {
            Stage currentStage = (Stage) playButton.getScene().getWindow();
            BombermanGame game = new BombermanGame();
            Stage gameStage = new Stage();
            game.startGame(gameStage);
            currentStage.close();
        } catch (Exception e) {
            showNotification("❌ Erreur lors du lancement du jeu: " + e.getMessage(), NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Lance une partie contre l'IA.
     * Ferme la fenêtre du menu principal et ouvre la fenêtre du jeu en mode IA.
     */
    @FXML
    private void handlePlayAI() {
        showNotification("🤖 Lancement du jeu contre l'IA...", NotificationType.INFO);
        try {
            Stage currentStage = (Stage) playAIButton.getScene().getWindow();
            BombermanGame game = new BombermanGame();
            game.setAIMode(true);
            Stage gameStage = new Stage();
            game.startGame(gameStage);
            currentStage.close();
        } catch (Exception e) {
            showNotification("❌ Erreur lors du lancement du jeu IA: " + e.getMessage(), NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Ouvre l'éditeur de niveaux.
     * Ferme la fenêtre du menu principal et ouvre la fenêtre de l'éditeur.
     */
    @FXML
    private void handleOpenEditor() {
        showNotification("🛠️ Ouverture de l'éditeur...", NotificationType.SUCCESS);
        try {
            Stage currentStage = (Stage) editorButton.getScene().getWindow();
            BombermanLevelEditor editor = new BombermanLevelEditor();
            Stage editorStage = new Stage();
            editor.start(editorStage);
            currentStage.close();
        } catch (Exception e) {
            showNotification("❌ Erreur lors de l'ouverture de l'éditeur: " + e.getMessage(), NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Ouvre un dialogue pour sélectionner un fichier de carte personnalisé.
     * Met à jour l'interface avec le fichier sélectionné.
     */
    @FXML
    private void handleLoadMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("📂 Choisir une carte personnalisée");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Bomberman (*.bmn)", "*.bmn"));

        Stage stage = (Stage) loadMapButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedMapFile = file;
            selectedMapLabel.setText("📋 Carte sélectionnée: " + file.getName());
            mapInfoContainer.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mapInfoContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            showNotification("✅ Carte \"" + file.getName() + "\" sélectionnée!", NotificationType.SUCCESS);
        }
    }

    /**
     * Lance une partie avec la carte personnalisée sélectionnée.
     * Ferme la fenêtre du menu principal et ouvre la fenêtre du jeu avec la carte.
     */
    @FXML
    private void handlePlayWithCustomMap() {
        if (selectedMapFile != null) {
            showNotification("🚀 Lancement avec carte personnalisée...", NotificationType.INFO);
            try {
                Stage currentStage = (Stage) playButton.getScene().getWindow();
                BombermanGame game = new BombermanGame();
                Stage gameStage = new Stage();
                game.startGame(gameStage, selectedMapFile);
                currentStage.close();
            } catch (Exception e) {
                showNotification("❌ Erreur lors du lancement: " + e.getMessage(), NotificationType.ERROR);
                e.printStackTrace();
            }
        }
    }

    /**
     * Efface la sélection de la carte personnalisée.
     * Cache la zone d'informations et affiche une notification.
     */
    @FXML
    private void handleClearMap() {
        selectedMapFile = null;
        mapInfoContainer.setVisible(false);
        showNotification("🗑️ Sélection de carte effacée", NotificationType.INFO);
    }

    /**
     * Quitte proprement l'application avec une animation de fondu.
     */
    @FXML
    private void handleExit() {
        showNotification("👋 Au revoir!", NotificationType.INFO);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), mainContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.close();
        });
        fadeOut.play();
    }

    /**
     * Affiche une notification visuelle dans la fenêtre principale.
     *
     * @param message message à afficher.
     * @param type type de notification (INFO, SUCCESS, ERROR) influençant la couleur.
     */
    private void showNotification(String message, NotificationType type) {
        Label notification = new Label(message);
        notification.setStyle(
                "-fx-background-color: " + type.getColor() + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px;"
        );
        notification.setLayoutX(10);
        notification.setLayoutY(10);
        notification.setOpacity(0);

        backgroundPane.getChildren().add(notification);

        Timeline notificationTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(notification.opacityProperty(), 0),
                        new KeyValue(notification.translateYProperty(), -20)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(notification.opacityProperty(), 1),
                        new KeyValue(notification.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(2700),
                        new KeyValue(notification.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(3000),
                        new KeyValue(notification.opacityProperty(), 0),
                        new KeyValue(notification.translateYProperty(), -20))
        );

        notificationTimeline.setOnFinished(e -> backgroundPane.getChildren().remove(notification));
        notificationTimeline.play();
    }

    /**
     * Types de notifications possibles avec leurs couleurs associées.
     */
    private enum NotificationType {
        INFO("#3498DB"),
        SUCCESS("#2ECC71"),
        ERROR("#E74C3C");

        private final String color;

        NotificationType(String color) {
            this.color = color;
        }

        /**
         * Retourne la couleur associée au type de notification.
         *
         * @return couleur CSS hexadécimale.
         */
        public String getColor() {
            return color;
        }
    }
}
