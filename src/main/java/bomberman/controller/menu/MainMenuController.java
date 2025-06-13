// MainMenuController.java - Version mergée avec texture packs et modes séparés
package bomberman.controller.menu;

import bomberman.model.ai.AIPlayer;
import bomberman.model.game.BombermanGame;
import bomberman.model.game.CaptureTheFlag;
import bomberman.controller.game.BombermanLevelEditor;
import bomberman.controller.game.TextureManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
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
 * Contrôleur principal du menu du jeu Bomberman.
 * Cette classe gère l'interface utilisateur du menu principal, incluant la navigation
 * entre les différents modes de jeu, la gestion des texture packs, le chargement
 * de cartes personnalisées et les animations visuelles. Elle sert de point d'entrée
 * central pour toutes les fonctionnalités du jeu.
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Navigation entre les modes de jeu (2/4 joueurs, IA, CTF)</li>
 *   <li>Gestion dynamique des texture packs</li>
 *   <li>Chargement et validation de cartes personnalisées</li>
 *   <li>Interface animée avec effets visuels</li>
 *   <li>Système de notifications utilisateur</li>
 *   <li>Intégration avec l'éditeur de niveaux</li>
 * </ul>
 *
 * <p>Architecture FXML :</p>
 * Cette classe utilise l'injection FXML pour lier les éléments d'interface
 * définis dans le fichier FXML correspondant. Les éléments sont automatiquement
 * injectés lors de l'initialisation grâce aux annotations @FXML.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class MainMenuController implements Initializable {

    // ==================== ÉLÉMENTS FXML ====================
    // Tous ces éléments doivent correspondre aux fx:id dans le fichier FXML

    /** Conteneur principal du menu */
    @FXML private VBox mainContainer;

    /** Label du titre du jeu */
    @FXML private Label titleLabel;

    /** Bouton pour lancer une partie à 2 joueurs */
    @FXML private Button playButton;

    /** Bouton pour ouvrir l'éditeur de niveaux */
    @FXML private Button editorButton;

    /** Bouton pour charger une carte personnalisée */
    @FXML private Button loadMapButton;

    /** Bouton pour quitter l'application */
    @FXML private Button exitButton;

    /** Label affichant la carte sélectionnée */
    @FXML private Label selectedMapLabel;

    /** Conteneur des informations de carte */
    @FXML private VBox mapInfoContainer;

    /** Panneau de fond pour les animations */
    @FXML private Pane backgroundPane;

    /** Bouton pour jouer contre l'IA */
    @FXML private Button playAIButton;

    /** Bouton pour accéder au profil joueur */
    @FXML private Button profileButton;

    /** Bouton pour le mode Capture the Flag */
    @FXML private Button captureTheFlagButton;

    /** ComboBox pour sélectionner les texture packs */
    @FXML private ComboBox<String> texturePackComboBox;

    /** Label du sélecteur de texture pack */
    @FXML private Label texturePackLabel;

    // ==================== VARIABLES D'INSTANCE ====================

    /** Fichier de carte personnalisée sélectionné */
    private File selectedMapFile;

    /** Timeline pour l'animation de fond */
    private Timeline backgroundAnimation;

    /** Liste des éléments animés du fond */
    private List<Circle> backgroundElements;

    /** Gestionnaire de textures pour les packs visuels */
    private TextureManager textureManager;

    /** Bouton pour jouer à 4 joueurs (ajouté programmatiquement) */
    private Button play4PlayersButton;

    /**
     * Méthode d'initialisation appelée automatiquement par JavaFX.
     * Configure tous les éléments de l'interface utilisateur, initialise les animations
     * et met en place les gestionnaires d'événements.
     *
     * @param location L'URL de localisation (utilisée par JavaFX)
     * @param resources Les ressources de localisation (utilisées par JavaFX)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            textureManager = TextureManager.getInstance();
            setupTexturePacks();
            setupUI();
            addMissingButtons();
            setupAnimations();
            createBackgroundAnimation();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure le système de texture packs.
     * Initialise la ComboBox avec les packs disponibles, applique le style visuel
     * et met en place les gestionnaires d'événements pour le changement de pack.
     */
    private void setupTexturePacks() {
        if (texturePackComboBox != null) {
            // Récupérer les texture packs disponibles depuis le TextureManager
            List<String> availablePacks = textureManager.getAvailableTexturePacks();

            // Vider la ComboBox et ajouter seulement les packs disponibles
            texturePackComboBox.getItems().clear();

            for (String packName : availablePacks) {
                // Utiliser le nom d'affichage formaté
                String displayName = textureManager.getDisplayName(packName);
                texturePackComboBox.getItems().add(displayName);
            }

            // Sélectionner le pack actuel
            String currentPack = textureManager.getCurrentTexturePack();
            String currentDisplayName = textureManager.getDisplayName(currentPack);
            texturePackComboBox.setValue(currentDisplayName);

            // Style de la ComboBox
            texturePackComboBox.setStyle(
                    "-fx-background-color: #34495E; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-background-radius: 15;"
            );

            // Style des éléments du dropdown
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

            // Gestionnaire d'événements pour le changement de texture pack
            texturePackComboBox.setOnAction(e -> handleTexturePackChange());

            // Afficher le nombre de packs disponibles
            System.out.println("Texture packs disponibles dans l'interface: " + availablePacks.size());
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
     * Convertit un nom d'affichage vers le nom technique du texture pack.
     * Effectue la correspondance inverse entre le nom formaté affiché à l'utilisateur
     * et le nom interne utilisé par le TextureManager.
     *
     * @param displayName Le nom d'affichage du texture pack
     * @return Le nom technique correspondant ou "default" en cas d'échec
     */
    private String getPackNameFromDisplayName(String displayName) {
        List<String> availablePacks = textureManager.getAvailableTexturePacks();

        for (String packName : availablePacks) {
            if (textureManager.getDisplayName(packName).equals(displayName)) {
                return packName;
            }
        }
        return "default"; // Fallback
    }

    /**
     * Gestionnaire pour le changement de texture pack.
     * Traite la sélection d'un nouveau pack, applique les changements
     * et fournit un retour visuel à l'utilisateur.
     */
    @FXML
    private void handleTexturePackChange() {
        String selectedDisplayName = texturePackComboBox.getValue();
        if (selectedDisplayName != null) {
            try {
                // Convertir le nom d'affichage vers le nom technique
                String selectedPack = getPackNameFromDisplayName(selectedDisplayName);

                // Charger le nouveau texture pack
                textureManager.setTexturePack(selectedPack);

                // Animation de confirmation
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), texturePackComboBox);
                scaleTransition.setToX(1.1);
                scaleTransition.setToY(1.1);
                scaleTransition.setAutoReverse(true);
                scaleTransition.setCycleCount(2);
                scaleTransition.play();

                showNotification("🎨 Texture pack '" + selectedDisplayName + "' appliqué!", NotificationType.SUCCESS);

            } catch (Exception e) {
                showNotification("❌ Erreur lors du changement de texture pack: " + e.getMessage(),
                        NotificationType.ERROR);
                // Remettre la valeur précédente en cas d'erreur
                String currentPack = textureManager.getCurrentTexturePack();
                String currentDisplayName = textureManager.getDisplayName(currentPack);
                texturePackComboBox.setValue(currentDisplayName);
            }
        }
    }

    /**
     * Configure l'interface utilisateur principale.
     * Applique les styles, effets visuels et configure l'état initial
     * de tous les éléments d'interface.
     */
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

            // Appliquer les effets aux nouveaux boutons
            if (captureTheFlagButton != null) setupButton(captureTheFlagButton, "#2980B9");
            if (profileButton != null) setupButton(profileButton, "#1ABC9C");

            // Masquer les informations de carte par défaut
            if (mapInfoContainer != null) {
                mapInfoContainer.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration de l'UI: " + e.getMessage());
        }
    }

    /**
     * Ajoute les boutons manquants créés programmatiquement.
     * Crée et insère le bouton "4 joueurs" dans l'interface existante
     * en trouvant dynamiquement le conteneur approprié.
     */
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

    /**
     * Configure le style et les animations d'un bouton.
     * Applique une couleur personnalisée, des effets d'ombre et
     * des animations de survol pour améliorer l'expérience utilisateur.
     *
     * @param button Le bouton à configurer
     * @param color La couleur principale du bouton (format hexadécimal)
     */
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

    /**
     * Configure toutes les animations de l'interface.
     * Met en place les animations du titre, l'apparition séquentielle des boutons
     * et les effets visuels pour créer une interface dynamique et engageante.
     */
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
            if (captureTheFlagButton != null) buttons.add(captureTheFlagButton);
            if (editorButton != null) buttons.add(editorButton);
            if (loadMapButton != null) buttons.add(loadMapButton);
            if (profileButton != null) buttons.add(profileButton);
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

            // Animation pour le sélecteur de texture pack
            if (texturePackComboBox != null && texturePackLabel != null) {
                texturePackLabel.setOpacity(0);
                texturePackComboBox.setOpacity(0);
                texturePackLabel.setTranslateX(50);
                texturePackComboBox.setTranslateX(50);

                KeyFrame labelKf = new KeyFrame(
                        Duration.millis(1200),
                        new KeyValue(texturePackLabel.opacityProperty(), 1),
                        new KeyValue(texturePackLabel.translateXProperty(), 0)
                );
                KeyFrame comboKf = new KeyFrame(
                        Duration.millis(1350),
                        new KeyValue(texturePackComboBox.opacityProperty(), 1),
                        new KeyValue(texturePackComboBox.translateXProperty(), 0)
                );

                buttonAnimation.getKeyFrames().addAll(labelKf, comboKf);
            }

            buttonAnimation.play();
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des animations: " + e.getMessage());
        }
    }

    /**
     * Crée l'animation de fond du menu.
     * Génère des éléments animés (cercles flottants) qui bougent en arrière-plan
     * pour créer une ambiance dynamique et immersive.
     */
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

    // ==================== GESTIONNAIRES D'ÉVÉNEMENTS FXML ====================

    /**
     * Lance une partie en mode 2 joueurs.
     * Ferme le menu actuel et démarre une nouvelle instance du jeu
     * configurée pour 2 joueurs avec les paramètres par défaut.
     */
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

    /**
     * Lance une partie en mode 4 joueurs.
     * Ferme le menu actuel et démarre une nouvelle instance du jeu
     * configurée pour 4 joueurs avec les paramètres par défaut.
     */
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

    /**
     * Lance une partie contre l'IA.
     * Ferme le menu actuel et démarre le mode de jeu contre l'intelligence artificielle.
     */
    @FXML
    public void handlePlayAI() {

        showNotification("🤖 Lancement du jeu contre l'IA...", NotificationType.INFO);
        Stage stage = new Stage();
        AIPlayer ai = new AIPlayer();
        ai.start(stage);
        stage.show();
        Stage currentStage = (Stage) captureTheFlagButton.getScene().getWindow();
        currentStage.close();
    }

    /**
     * Ouvre l'éditeur de niveaux.
     * Ferme le menu actuel et lance l'éditeur pour créer des cartes personnalisées.
     */
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

    /**
     * Ouvre un sélecteur de fichier pour charger une carte personnalisée.
     * Permet à l'utilisateur de choisir un fichier .bmn contenant
     * une carte créée avec l'éditeur de niveaux.
     */
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

    /**
     * Lance une partie avec la carte personnalisée sélectionnée.
     * Affiche un menu contextuel permettant de choisir le mode de jeu
     * (2 joueurs, 4 joueurs, ou IA) avec la carte personnalisée.
     */
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

    /**
     * Lance le jeu avec une carte personnalisée et un nombre de joueurs spécifié.
     *
     * @param playerCount Le nombre de joueurs pour la partie (2 ou 4)
     */
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

    /**
     * Lance le mode IA avec une carte personnalisée.
     */
    private void launchGameWithMapAI() {
        Stage currentStage = (Stage) playButton.getScene().getWindow();
        showNotification("🚀 Lancement IA avec carte personnalisée...", NotificationType.INFO);
        Stage stage = new Stage();
        AIPlayer ai = new AIPlayer();
        ai.start(stage);

        currentStage.close();
    }

    /**
     * Efface la sélection de carte personnalisée.
     * Remet l'interface dans l'état initial sans carte sélectionnée.
     */
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

    /**
     * Ferme l'application avec une animation de sortie.
     * Affiche une animation de fondu avant de fermer la fenêtre.
     */
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

    /**
     * Lance le mode Capture the Flag.
     * Ferme le menu actuel et démarre le mode de jeu CTF où les équipes
     * doivent capturer le drapeau adverse pour gagner.
     */
    @FXML
    private void handlePlayCaptureTheFlag() {
        showNotification("🏳️ Lancement du mode Capture the Flag...", NotificationType.INFO);

        try {
            Stage stage = new Stage();
            CaptureTheFlag ctf = new CaptureTheFlag();
            ctf.start(stage);

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) captureTheFlagButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            showNotification("❌ Erreur lors du lancement du mode CTF: " + e.getMessage(),
                    NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Ouvre l'interface de profil joueur.
     * Affiche les statistiques et les données de progression du joueur
     * dans une nouvelle fenêtre sans fermer le menu principal.
     */
    @FXML
    private void handleProfileButton() {
        showNotification("📊 Ouverture du profil...", NotificationType.INFO);

        try {
            PlayerProfileViewer profileView = new PlayerProfileViewer();
            Stage profileStage = new Stage();
            profileView.start(profileStage);
        } catch (Exception e) {
            showNotification("❌ Erreur lors de l'ouverture du profil: " + e.getMessage(),
                    NotificationType.ERROR);
            e.printStackTrace();
        }
    }

    // ==================== SYSTÈME DE NOTIFICATIONS ====================

    /**
     * Affiche une notification temporaire à l'utilisateur.
     * Crée un label stylisé qui apparaît en haut de l'interface avec une animation
     * d'apparition/disparition. Utilise la console comme fallback si l'interface
     * graphique n'est pas disponible.
     *
     * @param message Le message à afficher dans la notification
     * @param type Le type de notification (INFO, SUCCESS, ERROR) qui détermine la couleur
     */
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

    /**
     * Énumération des types de notifications disponibles.
     * Chaque type a une couleur associée pour différencier visuellement
     * les messages d'information, de succès et d'erreur.
     */
    private enum NotificationType {
        /** Notification d'information (bleu) */
        INFO("#3498DB"),

        /** Notification de succès (vert) */
        SUCCESS("#27AE60"),

        /** Notification d'erreur (rouge) */
        ERROR("#E74C3C");

        /** Code couleur hexadécimal du type de notification */
        private final String color;

        /**
         * Constructeur du type de notification.
         *
         * @param color Le code couleur hexadécimal associé au type
         */
        NotificationType(String color) {
            this.color = color;
        }

        /**
         * Retourne la couleur associée au type de notification.
         *
         * @return Le code couleur hexadécimal
         */
        public String getColor() {
            return color;
        }
    }
}