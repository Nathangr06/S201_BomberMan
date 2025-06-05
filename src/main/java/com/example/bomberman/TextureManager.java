package com.example.bomberman;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static TextureManager instance;
    private Map<String, Image> textures;
    private String currentTexturePack = "default"; // par défaut

    private TextureManager() {
        textures = new HashMap<>();
        loadTextures("default");
    }

    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }

    public void setTexturePack(String packName) {
        loadTextures(packName);
    }


    private void loadTextures(String packName) {
        textures.clear();
        currentTexturePack = packName;

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