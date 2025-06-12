package bomberman;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureManager {
    private static TextureManager instance;
    private Map<String, Image> textures;
    private String currentTexturePack = "default";
    private List<String> availableTexturePacks;

    TextureManager() {
        textures = new HashMap<>();
        availableTexturePacks = new ArrayList<>();
        scanAvailableTexturePacks();
        loadTextures("default");
    }

    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }

    /**
     * Scanne les texture packs disponibles dans le dossier resources/texturepacks/
     */
    private void scanAvailableTexturePacks() {
        String[] potentialPacks = {"default","textures2"};

        for (String packName : potentialPacks) {
            if (isTexturePackAvailable(packName)) {
                availableTexturePacks.add(packName);
                System.out.println("Texture pack trouvé: " + packName);
            }
        }

        // Assurer qu'au moins le pack par défaut est disponible (créé programmatiquement)
        if (!availableTexturePacks.contains("default")) {
            availableTexturePacks.add("default");
        }

        System.out.println("Texture packs disponibles: " + availableTexturePacks);
    }

    /**
     * Vérifie si un texture pack est disponible en testant la présence des fichiers requis
     */
    private boolean isTexturePackAvailable(String packName) {
        if (packName.equals("default")) {
            return true; // Le pack par défaut est toujours disponible (créé programmatiquement)
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
     * Retourne la liste des texture packs disponibles
     */
    public List<String> getAvailableTexturePacks() {
        return new ArrayList<>(availableTexturePacks);
    }

    /**
     * Retourne le nom formaté d'un texture pack pour l'affichage
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

    public void setTexturePack(String packName) {
        // Vérifier si le pack est disponible, sinon utiliser le défaut
        if (!availableTexturePacks.contains(packName)) {
            System.err.println("Le texture pack '" + packName + "' n'est pas disponible. Utilisation du pack par défaut.");
            packName = "default";
        }
        loadTextures(packName);
    }

    public String getCurrentTexturePack() {
        return currentTexturePack;
    }

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


    private void createDefaultTextures() {
        // Créer des images 32x32 par défaut si les fichiers ne sont pas trouvés
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(32, 32);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Texture joueur par défaut (bleu)
        gc.setFill(javafx.scene.paint.Color.BLUE);
        gc.fillOval(4, 4, 24, 24);
        textures.put("player", canvas.snapshot(null, null));

        // Texture joueur 2 par défaut (rouge)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.RED);
        gc.fillOval(4, 4, 24, 24);
        textures.put("player2", canvas.snapshot(null, null));

        // Texture bombe par défaut (noir)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillOval(4, 4, 24, 24);
        textures.put("bomb", canvas.snapshot(null, null));

        // Texture explosion par défaut (orange)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.ORANGE);
        gc.fillRect(2, 2, 28, 28);
        textures.put("explosion", canvas.snapshot(null, null));

        // Texture mur indestructible (gris)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.GRAY);
        gc.fillRect(0, 0, 32, 32);
        textures.put("wall_indestructible", canvas.snapshot(null, null));

        // Texture mur destructible (marron)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.BROWN);
        gc.fillRect(0, 0, 32, 32);
        textures.put("wall_destructible", canvas.snapshot(null, null));

        // Texture sol (vert)
        gc.clearRect(0, 0, 32, 32);
        gc.setFill(javafx.scene.paint.Color.LIGHTGREEN);
        gc.fillRect(0, 0, 32, 32);
        textures.put("ground", canvas.snapshot(null, null));

        System.out.println("Textures par défaut créées programmatiquement");
    }

    public Image getTexture(String name) {
        return textures.get(name);
    }

    public boolean hasTexture(String name) {
        return textures.containsKey(name);
    }

    // Méthode pour obtenir une texture avec rotation
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
