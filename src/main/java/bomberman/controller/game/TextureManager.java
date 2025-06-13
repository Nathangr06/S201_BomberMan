package bomberman.controller.game;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire de textures pour le jeu Bomberman.
 * Cette classe singleton centralise la gestion des textures et des packs de textures,
 * permettant de personnaliser l'apparence visuelle du jeu. Elle gère le chargement
 * dynamique des textures depuis les ressources, la validation des packs de textures,
 * et fournit des fallbacks en cas d'échec de chargement.
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Gestion de multiples packs de textures</li>
 *   <li>Chargement et validation automatique des ressources</li>
 *   <li>Génération de textures de fallback programmées</li>
 *   <li>Transformation d'images (rotation)</li>
 *   <li>Interface utilisateur avec noms d'affichage personnalisés</li>
 * </ul>
 *
 * <p>Structure des packs de textures :</p>
 * <pre>
 * resources/texturepacks/
 * ├── default/ (généré programmatiquement)
 * ├── textures2/
 * │   ├── player.png
 * │   ├── player2.png
 * │   ├── bombe.png
 * │   ├── explosion.png
 * │   ├── mur_indestructible.png
 * │   ├── mur_destructible.png
 * │   └── sol.png
 * └── autres_packs/
 * </pre>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class TextureManager {

    /** Instance unique du singleton */
    private static TextureManager instance;

    /** Cache des textures chargées, indexées par nom */
    private Map<String, Image> textures;

    /** Nom du pack de textures actuellement utilisé */
    private String currentTexturePack = "default";

    /** Liste des packs de textures disponibles */
    private List<String> availableTexturePacks;

    /**
     * Constructeur privé du singleton.
     * Initialise le gestionnaire en scannant les packs disponibles
     * et en chargeant le pack par défaut.
     */
    public TextureManager() {
        textures = new HashMap<>();
        availableTexturePacks = new ArrayList<>();
        scanAvailableTexturePacks();
        loadTextures("default");
    }

    /**
     * Retourne l'instance unique du gestionnaire de textures.
     * Implémente le pattern Singleton pour garantir une seule instance.
     *
     * @return L'instance unique du TextureManager
     */
    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }

    /**
     * Scanne les packs de textures disponibles dans le dossier resources/texturepacks/.
     * Vérifie la disponibilité de chaque pack potentiel et maintient la liste
     * des packs utilisables. Le pack "default" est toujours disponible car
     * il est généré programmatiquement si nécessaire.
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
     * Vérifie si un pack de textures est disponible en testant la présence des fichiers requis.
     * Pour le pack "default", la méthode retourne toujours true car il peut être
     * généré programmatiquement. Pour les autres packs, vérifie la présence de
     * tous les fichiers de textures obligatoires.
     *
     * @param packName Le nom du pack à vérifier
     * @return true si le pack est disponible et complet, false sinon
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
     * Retourne la liste des packs de textures disponibles.
     * Crée une copie de la liste pour éviter les modifications externes.
     *
     * @return Une nouvelle liste contenant les noms des packs disponibles
     */
    public List<String> getAvailableTexturePacks() {
        return new ArrayList<>(availableTexturePacks);
    }

    /**
     * Retourne le nom formaté d'un pack de textures pour l'affichage utilisateur.
     * Ajoute des emojis et formate les noms pour une meilleure présentation
     * dans l'interface utilisateur.
     *
     * @param packName Le nom interne du pack
     * @return Le nom formaté avec emoji et mise en forme
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
     * Définit le pack de textures à utiliser.
     * Vérifie la disponibilité du pack demandé et bascule vers le pack par défaut
     * si le pack spécifié n'est pas disponible. Recharge toutes les textures
     * après le changement de pack.
     *
     * @param packName Le nom du pack de textures à utiliser
     */
    public void setTexturePack(String packName) {
        // Vérifier si le pack est disponible, sinon utiliser le défaut
        if (!availableTexturePacks.contains(packName)) {
            System.err.println("Le texture pack '" + packName + "' n'est pas disponible. Utilisation du pack par défaut.");
            packName = "default";
        }
        loadTextures(packName);
    }

    /**
     * Retourne le nom du pack de textures actuellement utilisé.
     *
     * @return Le nom du pack de textures actuel
     */
    public String getCurrentTexturePack() {
        return currentTexturePack;
    }

    /**
     * Charge toutes les textures d'un pack spécifique.
     * Vide le cache actuel et charge les nouvelles textures depuis les fichiers
     * ou génère les textures par défaut en cas d'échec. Met à jour le pack actuel.
     *
     * @param packName Le nom du pack à charger
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
     * Génère les textures par défaut programmatiquement.
     * Crée des textures simples de 32x32 pixels avec des formes géométriques
     * et des couleurs distinctives pour chaque élément du jeu. Cette méthode
     * sert de fallback lorsque les fichiers de textures ne peuvent pas être chargés.
     *
     * <p>Textures générées :</p>
     * <ul>
     *   <li>Joueur : Cercle bleu</li>
     *   <li>Joueur 2 : Cercle rouge</li>
     *   <li>Bombe : Cercle noir</li>
     *   <li>Explosion : Rectangle orange</li>
     *   <li>Mur indestructible : Rectangle gris</li>
     *   <li>Mur destructible : Rectangle marron</li>
     *   <li>Sol : Rectangle vert clair</li>
     * </ul>
     */
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

    /**
     * Retourne une texture par son nom.
     *
     * @param name Le nom de la texture à récupérer
     * @return L'image correspondante ou null si la texture n'existe pas
     */
    public Image getTexture(String name) {
        return textures.get(name);
    }

    /**
     * Vérifie si une texture existe dans le cache.
     *
     * @param name Le nom de la texture à vérifier
     * @return true si la texture existe, false sinon
     */
    public boolean hasTexture(String name) {
        return textures.containsKey(name);
    }

    /**
     * Génère une version pivotée d'une texture existante.
     * Crée dynamiquement une nouvelle image en appliquant une rotation
     * à la texture spécifiée. La rotation s'effectue autour du centre de l'image.
     *
     * @param name Le nom de la texture à faire pivoter
     * @param angle L'angle de rotation en degrés
     * @return Une nouvelle image avec la rotation appliquée, ou null si la texture n'existe pas
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