
package bomberman;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PlayerProfileManager {
    private static final String FILE_PATH = "profiles.dat";
    private Map<String, PlayerProfile> profiles;
    private PlayerProfile currentProfile;
    private static PlayerProfileManager instance;

    // Constructeur privé pour le Singleton
    private PlayerProfileManager() {
        profiles = new HashMap<>();
        loadProfiles();
    }

    // Méthode pour obtenir l'instance unique
    public static PlayerProfileManager getInstance() {
        if (instance == null) {
            instance = new PlayerProfileManager();
        }
        return instance;
    }

    // Méthode pour créer ou récupérer un profil
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

    // Getter pour le profil courant
    public PlayerProfile getCurrentProfile() {
        return currentProfile;
    }

    // Setter pour le profil courant
    public void setCurrentProfile(PlayerProfile profile) {
        this.currentProfile = profile;
    }

    // Récupérer tous les profils
    public Collection<PlayerProfile> getAllProfiles() {
        return new ArrayList<>(profiles.values());
    }

    // Sauvegarder les profils
    public void saveProfiles() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(profiles);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des profils : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Charger les profils
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

    // Supprimer un profil
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