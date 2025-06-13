package bomberman.model.profile;

import java.io.Serializable;

/**
 * Profil de joueur pour le jeu Bomberman.
 * Cette classe représente les données persistantes d'un joueur, incluant
 * ses informations personnelles et ses statistiques de jeu. Elle implémente
 * Serializable pour permettre la sauvegarde et le chargement des profils
 * sur le système de fichiers.
 *
 * <p>Données stockées :</p>
 * <ul>
 *   <li><strong>Identification</strong> : Nom d'utilisateur et prénom</li>
 *   <li><strong>Statistiques</strong> : Parties jouées et parties gagnées</li>
 *   <li><strong>Calculs dérivés</strong> : Taux de victoire (calculé dynamiquement)</li>
 * </ul>
 *
 * <p>Utilisation typique :</p>
 * <ol>
 *   <li>Création du profil avec nom d'utilisateur et prénom</li>
 *   <li>Incrémentation automatique des statistiques après chaque partie</li>
 *   <li>Sauvegarde périodique via le PlayerProfileManager</li>
 *   <li>Chargement au démarrage pour continuité des données</li>
 * </ol>
 *
 * <p>Gestion des statistiques :</p>
 * Les statistiques sont mises à jour automatiquement par le système de jeu :
 * <ul>
 *   <li>Parties jouées : Incrémentées à chaque fin de partie</li>
 *   <li>Parties gagnées : Incrémentées uniquement en cas de victoire</li>
 *   <li>Intégrité : Les parties gagnées ne peuvent jamais dépasser les parties jouées</li>
 * </ul>
 *
 * <p>Sérialisation :</p>
 * La classe implémente Serializable avec un serialVersionUID fixe pour
 * garantir la compatibilité lors des mises à jour de la classe, permettant
 * ainsi la migration des profils existants.
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class PlayerProfile implements Serializable {

    /**
     * Identifiant de version pour la sérialisation.
     * Garantit la compatibilité lors du chargement de profils sauvegardés
     * avec des versions antérieures de la classe.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Nom d'utilisateur unique du joueur.
     * Utilisé comme identifiant principal pour distinguer les profils.
     * Défini lors de la création et non modifiable par la suite.
     */
    private String username;

    /**
     * Prénom du joueur pour personnalisation.
     * Utilisé pour l'affichage dans l'interface utilisateur et
     * pour une expérience plus personnalisée.
     */
    private String firstName;

    /**
     * Nombre total de parties jouées.
     * Compteur incrémenté automatiquement à chaque fin de partie,
     * indépendamment du résultat (victoire, défaite, match nul).
     */
    private int gamesPlayed;

    /**
     * Nombre total de parties gagnées.
     * Compteur incrémenté uniquement lorsque le joueur remporte
     * une victoire claire (pas en cas de match nul).
     */
    private int gamesWon;

    /**
     * Constructeur d'un profil de joueur.
     * Initialise un nouveau profil avec les informations personnelles
     * et remet à zéro les statistiques de jeu.
     *
     * @param username Le nom d'utilisateur unique du joueur
     * @param firstName Le prénom du joueur pour personnalisation
     * @throws IllegalArgumentException si username ou firstName sont null ou vides
     */
    public PlayerProfile(String username, String firstName) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur ne peut pas être vide");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom ne peut pas être vide");
        }

        this.username = username;
        this.firstName = firstName;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
    }

    // ==================== ACCESSEURS ====================

    /**
     * Retourne le nom d'utilisateur du joueur.
     * Le nom d'utilisateur sert d'identifiant unique et est immuable
     * après la création du profil.
     *
     * @return Le nom d'utilisateur du joueur
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retourne le prénom du joueur.
     * Utilisé pour l'affichage dans l'interface utilisateur
     * et pour personnaliser l'expérience de jeu.
     *
     * @return Le prénom du joueur
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Retourne le nombre total de parties jouées.
     * Inclut toutes les parties terminées, quel que soit leur résultat
     * (victoires, défaites, matches nuls).
     *
     * @return Le nombre de parties jouées
     */
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    /**
     * Retourne le nombre total de parties gagnées.
     * Inclut uniquement les parties remportées clairement,
     * excluant les matches nuls.
     *
     * @return Le nombre de parties gagnées
     */
    public int getGamesWon() {
        return gamesWon;
    }

    // ==================== MÉTHODES DE MISE À JOUR ====================

    /**
     * Incrémente le compteur de parties jouées.
     * Cette méthode doit être appelée à la fin de chaque partie,
     * indépendamment du résultat. Elle maintient l'historique
     * complet de l'activité du joueur.
     *
     * <p>Utilisation :</p>
     * Appelée automatiquement par le GameManager lors de
     * la détection d'une fin de partie (victoire, défaite, ou match nul).
     */
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    /**
     * Incrémente le compteur de parties gagnées.
     * Cette méthode doit être appelée uniquement lorsque le joueur
     * remporte une victoire claire. Elle ne doit pas être appelée
     * en cas de match nul ou de défaite.
     *
     * <p>Invariant maintenu :</p>
     * Le nombre de parties gagnées ne peut jamais dépasser
     * le nombre de parties jouées. Cette cohérence est garantie
     * par l'usage approprié des deux méthodes d'incrémentation.
     *
     * <p>Utilisation :</p>
     * Appelée automatiquement par le GameManager lors de
     * la détection d'une victoire du joueur.
     */
    public void incrementGamesWon() {
        this.gamesWon++;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Calcule le taux de victoire du joueur.
     * Retourne le pourcentage de parties gagnées par rapport
     * aux parties jouées, avec gestion du cas de division par zéro.
     *
     * @return Le taux de victoire en pourcentage (0.0 à 100.0)
     */
    public double getWinRate() {
        if (gamesPlayed == 0) {
            return 0.0;
        }
        return (double) gamesWon / gamesPlayed * 100.0;
    }

    /**
     * Retourne une représentation textuelle du profil.
     * Inclut les informations personnelles et les statistiques
     * dans un format lisible pour le débogage et l'affichage.
     *
     * @return Une chaîne décrivant le profil
     */
    @Override
    public String toString() {
        return String.format("PlayerProfile{username='%s', firstName='%s', " +
                        "gamesPlayed=%d, gamesWon=%d, winRate=%.1f%%}",
                username, firstName, gamesPlayed, gamesWon, getWinRate());
    }

    /**
     * Vérifie l'égalité avec un autre objet.
     * Deux profils sont considérés égaux s'ils ont le même nom d'utilisateur,
     * celui-ci servant d'identifiant unique.
     *
     * @param obj L'objet à comparer
     * @return true si les profils sont égaux, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        PlayerProfile that = (PlayerProfile) obj;
        return username.equals(that.username);
    }

    /**
     * Calcule le code de hachage du profil.
     * Basé sur le nom d'utilisateur pour cohérence avec equals().
     *
     * @return Le code de hachage
     */
    @Override
    public int hashCode() {
        return username.hashCode();
    }
}