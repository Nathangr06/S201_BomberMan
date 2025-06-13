package bomberman.model.profile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire de profils de joueurs pour le jeu Bomberman.
 * Cette classe implémente le pattern Singleton pour gérer les profils des joueurs,
 * incluant la persistance des données via la sérialisation.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class PlayerProfileManager {

    /** Chemin du fichier de sauvegarde des profils */
    private static final String FILE_PATH = "profiles.dat";

    /** Map contenant tous les profils de joueurs indexés par nom d'utilisateur */
    private Map<String, PlayerProfile> profiles;

    /** Profil actuellement sélectionné */
    private PlayerProfile currentProfile;

    /** Instance unique du gestionnaire (pattern Singleton) */
    private static PlayerProfileManager instance;

    /**
     * Constructeur privé pour le pattern Singleton.
     * Initialise la map des profils et charge les profils existants depuis le fichier.
     */
    private PlayerProfileManager() {
        profiles = new HashMap<>();
        loadProfiles();
    }

    /**
     * Obtient l'instance unique du gestionnaire de profils.
     * Crée l'instance si elle n'existe pas encore (lazy initialization).
     *
     * @return l'instance unique de PlayerProfileManager
     */
    public static PlayerProfileManager getInstance() {
        if (instance == null) {
            instance = new PlayerProfileManager();
        }
        return instance;
    }

    /**
     * Récupère un profil existant ou en crée un nouveau si inexistant.
     * Si le profil est créé, il devient automatiquement le profil courant.
     *
     * @param username le nom d'utilisateur du profil
     * @param firstName le prénom du joueur
     * @return le profil correspondant (existant ou nouvellement créé)
     */
    public PlayerProfile getOrCreateProfile(String username, String firstName) {
        PlayerProfile profile = profiles.get(username);
        if (profile == null) {
            profile = new PlayerProfile(username, firstName);
            profiles.put(username, profile);
            currentProfile = profile;
            saveProfiles();
        }
        return profile;
    }

    /**
     * Récupère le profil actuellement sélectionné.
     *
     * @return le profil courant, ou null si aucun profil n'est sélectionné
     */
    public PlayerProfile getCurrentProfile() {
        return currentProfile;
    }

    /**
     * Définit le profil courant.
     *
     * @param profile le profil à définir comme courant
     */
    public void setCurrentProfile(PlayerProfile profile) {
        this.currentProfile = profile;
    }

    /**
     * Récupère tous les profils de joueurs.
     *
     * @return une collection contenant tous les profils enregistrés
     */
    public Collection<PlayerProfile> getAllProfiles() {
        return new ArrayList<>(profiles.values());
    }

    /**
     * Sauvegarde tous les profils dans le fichier de persistance.
     * Utilise la sérialisation Java pour écrire la map des profils.
     * En cas d'erreur, affiche un message d'erreur et la stack trace.
     */
    public void saveProfiles() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(profiles);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des profils : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Charge les profils depuis le fichier de persistance.
     * Si le fichier n'existe pas, initialise une map vide.
     * En cas d'erreur de lecture, initialise une map vide et affiche un message d'erreur.
     */
    @SuppressWarnings("unchecked")
    private void loadProfiles() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            profiles = new HashMap<>();
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            profiles = (Map<String, PlayerProfile>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur lors du chargement des profils : " + e.getMessage());
            profiles = new HashMap<>();
        }
    }

    /**
     * Supprime un profil de joueur.
     * Si le profil supprimé était le profil courant, remet le profil courant à null.
     * Sauvegarde automatiquement les profils après suppression.
     *
     * @param username le nom d'utilisateur du profil à supprimer
     * @return true si le profil a été supprimé avec succès, false si le profil n'existait pas
     */
    public boolean deleteProfile(String username) {
        PlayerProfile removed = profiles.remove(username);
        if (removed != null) {
            if (currentProfile != null && currentProfile.getUsername().equals(username)) {
                currentProfile = null;
            }
            saveProfiles();
            return true;
        }
        return false;
    }
}