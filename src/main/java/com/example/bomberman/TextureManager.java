package com.example.bomberman;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire singleton des textures utilisées dans le jeu Bomberman.
 *
 * <p>Cette classe charge et stocke les textures graphiques à partir de packs de textures,
 * permet de sélectionner un pack actif, et fournit des méthodes pour récupérer les textures.
 * En cas d'absence de fichiers, des textures par défaut programmées sont utilisées.</p>
 */
public class TextureManager {

    /** Instance unique du gestionnaire (pattern singleton). */
    private static TextureManager instance;

    /** Map contenant les textures chargées, identifiées par un nom clé. */
    private Map<String, Image> textures;

    /** Nom du pack de textures actuellement chargé. */
    private String currentTexturePack = "default";

    /** Liste des packs de textures disponibles. */
    private List<String> availableTexturePacks;

    /**
     * Constructeur privé (singleton).
     * Initialise les structures, scanne les packs disponibles, et charge le pack par défaut.
     */
    TextureManager() {
        textures = new HashMap<>();
        availableTexturePacks = new ArrayList<>();
        scanAvailableTexturePacks();
        loadTextures("default");
    }

    /**
     * Retourne l'instance unique du gestionnaire de textures.
     *
     * @return instance unique de TextureManager
     */
    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }

    /**
     * Scanne les packs de textures disponibles dans le dossier resources/texturepacks/.
     *
     * <p>Ajoute à la liste les packs valides détectés, en s'assurant que le pack "default" est toujours présent.</p>
     */
    private void scanAvailableTexturePacks() {
        String[] potentialPacks = {"default","textures2"};

        for (String packName : potentialPacks) {
            if (isTexturePackAvailable(packName)) {
                availableTexturePacks.add(packName);
                System.out.println("Texture pack trouvé: " + packName);
            }
        }

        if (!availableTexturePacks.contains("default")) {
            availableTexturePacks.add("default");
        }

        System.out.println("Texture packs disponibles: " + availableTexturePacks);
    }

    /**
     * Vérifie la disponibilité d'un pack de textures en contrôlant la présence des fichiers requis.
     *
     * @param packName nom du pack à vérifier
     * @return true si tous les fichiers requis sont présents, false sinon
     */
    private boolean isTexturePackAvailable(String packName) {
        if (packName.equals("default")) {
            return true; // Le pack par défaut est toujours disponible
        }

        String basePath = "/texturepacks/" + packName + "/";
        String[] requiredFiles = {"player.png", "player2.png", "bombe.png", "explosion.png",
                "mur_indestructible.png", "mur_destructible.png", "sol.png"};

        for (String fileName : requiredFiles) {
            String fullPath = basePath + fileName;
            if (getClass().getResourceAsStream(fullPath) == null) {
                System.out.println("Fichier manquant: " + fullPath + " (getResource = " + (getClass().getResource(fullPath) != null) + ")");
                return false;
            }
        }
        return true;
    }

    /**
     * Retourne une copie de la liste des packs de textures disponibles.
     *
     * @return liste des noms des packs disponibles
     */
    public List<String> getAvailableTexturePacks() {
        return new ArrayList<>(availableTexturePacks);
    }

    /**
     * Retourne un nom d'affichage formaté pour un pack de textures donné.
     *
     * @param packName nom technique du pack
     * @return nom formaté avec emoji pour l'affichage dans l'interface
     */
    public String getDisplayName(String packName) {
        switch (packName) {
            case "default": return "🎨 Défaut";
            case "classic": return "👾 Classique";
            case "modern": return "✨ Moderne";
            case "retro": return "🕹️ Rétro";
            case "neon": return "🌈 Néon";
            case "pixel": return "🎮 Pixel Art";
            case "cartoon": return "🎭 Cartoon";
            case "realistic": return "📸 Réaliste";
            default: return "🎨 " + packName.substring(0, 1).toUpperCase() + packName.substring(1);
        }
    }

    /**
     * Définit le pack de textures actif et charge ses textures.
     * Si le pack n'est pas disponible, le pack par défaut est chargé.
     *
     * @param packName nom du pack à activer
     */
    public void setTexturePack(String packName) {
        if (!availableTexturePacks.contains(packName)) {
            System.err.println("Le texture pack '" + packName + "' n'est pas disponible. Utilisation du pack par défaut.");
            packName = "default";
        }
        loadTextures(packName);
    }

    /**
     * Retourne le nom du pack de textures actuellement chargé.
     *
     * @return nom du pack actif
     */
    public String getCurrentTexturePack() {
        return currentTexturePack;
    }

    /**
     * Charge les textures du pack spécifié dans la map interne.
     * Si le pack est invalide, charge le pack par défaut.
     *
     * @param packName nom du pack à charger
     */
    private void loadTextures(String packName) {
        textures.clear();
        currentTexturePack = packName;

        if (!isTexturePackAvailable(packName)) {
            System.err.println("Pack " + packName + " non disponible, utilisation du pack default.");
            packName = "default";
            currentTexturePack = packName;
        }

        try {
            String basePath = "/texturepacks/" + packName + "/";
            textures.put("player", new Image(getClass().getResourceAsStream(basePath + "player.png")));
            textures.put("player2", new Image(getClass().getResourceAsStream(basePath + "player2.png")));
            textures.put("bomb", new Image(getClass().getResourceAsStream(basePath + "bombe.png")));
            textures.put("explosion", new Image(getClass().getResourceAsStream(basePath + "explosion.png")));
            textures.put("wall_indestructible", new Image(getClass().getResourceAsStream(basePath + "mur_indestructible.png")));
            textures.put("wall_destructible", new Image(getClass().getResourceAsStream(basePath + "mur_destructible.png")));
            textures.put("ground", new Image(getClass().getResourceAsStream(basePath + "sol.png")));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du texture pack '" + packName + "': " + e.getMessage());
            createDefaultTextures();
        }
    }

    /**
     * Crée des textures par défaut programmatiques (images simples) si le chargement des fichiers échoue.
     */
    private void createDefaultTextures() {
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(32, 32);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Player (bleu)
        gc.setFill(javafx.scene.paint.Color.BLUE);
        gc.fillOval(4, 4, 24, 24);
        textures.put("player", canvas.snapshot(null, null));

        // Player 2 (rouge)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.RED);
        gc.fillOval(4, 4, 24, 24);
        textures.put("player2", canvas.snapshot(null, null));

        // Bombe (noir)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillOval(4, 4, 24, 24);
        textures.put("bomb", canvas.snapshot(null, null));

        // Explosion (orange)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.ORANGE);
        gc.fillRect(2, 2, 28, 28);
        textures.put("explosion", canvas.snapshot(null, null));

        // Mur indestructible (gris)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.GRAY);
        gc.fillRect(0, 0, 32, 32);
        textures.put("wall_indestructible", canvas.snapshot(null, null));

        // Mur destructible (marron)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.BROWN);
        gc.fillRect(0, 0, 32, 32);
        textures.put("wall_destructible", canvas.snapshot(null, null));

        // Sol (vert clair)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.LIGHTGREEN);
        gc.fillRect(0, 0, 32, 32);
        textures.put("ground", canvas.snapshot(null, null));

        System.out.println("Textures par défaut créées programmatiquement");
    }

    /**
     * Retourne la texture associée au nom donné.
     *
     * @param name clé de la texture (ex: "player", "bomb")
     * @return instance Image correspondante ou null si inexistante
     */
    public Image getTexture(String name) {
        return textures.get(name);
    }

    /**
     * Indique si une texture donnée existe dans le pack chargé.
     *
     * @param name clé de la texture
     * @return true si la texture est présente, false sinon
     */
    public boolean hasTexture(String name) {
        return textures.containsKey(name);
    }

    /**
     * Retourne une version de la texture avec une rotation appliquée.
     *
     * @param name  clé de la texture à récupérer
     * @param angle angle de rotation en degrés
     * @return nouvelle Image de la texture tournée, ou null si la texture n'existe pas
     */
    public Image getRotatedTexture(String name, double angle) {
        Image original = getTexture(name);
        if (original == null) return null;

        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(original.getWidth(), original.getHeight());
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.save();
        gc.translate(original.getWidth() / 2, original.getHeight() / 2);
        gc.rotate(angle);
        gc.translate(-original.getWidth() / 2, -original.getHeight() / 2);
        gc.drawImage(original, 0, 0);
        gc.restore();

        return canvas.snapshot(null, null);
    }
}
