package bomberman.model.game;

/**
 * Horloge de partie pour le jeu Bomberman.
 * Cette classe gère le chronométrage d'une partie en cours, en calculant
 * et formatant le temps écoulé depuis le début de la partie. Elle fournit
 * des méthodes pour afficher le temps sous forme lisible et réinitialiser
 * le compteur pour de nouvelles parties.
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Chronométrage précis au niveau de la seconde</li>
 *   <li>Formatage automatique au format MM:SS</li>
 *   <li>Mise à jour continue pendant la partie</li>
 *   <li>Réinitialisation pour nouvelles parties</li>
 *   <li>Accès aux données brutes et formatées</li>
 * </ul>
 *
 * <p>Cycle de vie typique :</p>
 * <ol>
 *   <li>Création et initialisation automatique</li>
 *   <li>Appels réguliers à {@link #update()} dans la boucle de jeu</li>
 *   <li>Lecture du temps via {@link #getFormattedTime()} pour l'affichage</li>
 *   <li>Réinitialisation via {@link #reset()} pour nouvelle partie</li>
 * </ol>
 *
 * <p>Précision et performance :</p>
 * Le timer utilise {@code System.currentTimeMillis()} pour une précision
 * au niveau de la milliseconde, mais convertit automatiquement en secondes
 * pour l'affichage et l'usage dans le jeu. Cette approche offre un bon
 * compromis entre précision et lisibilité.
 *
 * <p>Format d'affichage :</p>
 * Le temps est affiché au format "MM:SS" avec zéros de remplissage :
 * <ul>
 *   <li>00:00 pour le début de partie</li>
 *   <li>01:30 pour 1 minute et 30 secondes</li>
 *   <li>99:59 maximum (limitation pratique du format)</li>
 * </ul>
 *
 * @author BUT1_TD3_G35
 * @version 1.0
 * @since 1.0
 */
public class GameTimer {

    /**
     * Timestamp de début de partie en millisecondes.
     * Capturé via System.currentTimeMillis() lors de l'initialisation ou du reset.
     */
    private long gameStartTime;

    /**
     * Durée de la partie en cours en secondes.
     * Calculée et mise à jour lors de chaque appel à update().
     */
    private long gameDuration;

    /**
     * Constructeur du timer de partie.
     * Initialise automatiquement le timer en appelant {@link #reset()}
     * pour démarrer le chronométrage immédiatement.
     */
    public GameTimer() {
        reset();
    }

    /**
     * Remet à zéro le timer et démarre un nouveau chronométrage.
     * Capture le timestamp actuel comme nouvelle heure de début
     * et remet la durée à zéro. Utilisé au début d'une nouvelle partie
     * ou lors d'un redémarrage.
     *
     * <p>Effets :</p>
     * <ul>
     *   <li>Nouveau timestamp de début (System.currentTimeMillis())</li>
     *   <li>Durée remise à 0</li>
     *   <li>Prêt pour nouveaux appels à update()</li>
     * </ul>
     */
    public void reset() {
        this.gameStartTime = System.currentTimeMillis();
        this.gameDuration = 0;
    }

    /**
     * Met à jour la durée de partie calculée.
     * Calcule le temps écoulé depuis le début de la partie en comparant
     * le timestamp actuel avec le timestamp de début. Convertit automatiquement
     * de millisecondes en secondes pour faciliter l'usage dans le jeu.
     *
     * <p>Calcul effectué :</p>
     * <pre>
     * durée = (timestamp_actuel - timestamp_début) / 1000
     * </pre>
     *
     * <p>Cette méthode doit être appelée régulièrement (typiquement dans
     * la boucle de jeu) pour maintenir le timer à jour.</p>
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        gameDuration = (currentTime - gameStartTime) / 1000;
    }

    /**
     * Retourne la durée de partie en secondes.
     * Fournit l'accès aux données brutes du timer pour les calculs
     * ou comparaisons nécessitant une précision au niveau de la seconde.
     *
     * @return La durée de la partie écoulée en secondes
     */
    public long getDuration() {
        return gameDuration;
    }

    /**
     * Retourne le temps formaté pour l'affichage utilisateur.
     * Convertit la durée en secondes vers un format MM:SS lisible
     * avec zéros de remplissage pour maintenir une largeur constante.
     *
     * <p>Format de sortie :</p>
     * <ul>
     *   <li><strong>Minutes</strong> : 00-99 (format sur 2 chiffres)</li>
     *   <li><strong>Séparateur</strong> : Deux-points (:)</li>
     *   <li><strong>Secondes</strong> : 00-59 (format sur 2 chiffres)</li>
     * </ul>
     *
     * <p>Exemples de formatage :</p>
     * <ul>
     *   <li>0 secondes → "00:00"</li>
     *   <li>65 secondes → "01:05"</li>
     *   <li>3661 secondes → "61:01"</li>
     * </ul>
     *
     * @return Le temps formaté sous forme de chaîne "MM:SS"
     */
    public String getFormattedTime() {
        long minutes = gameDuration / 60;
        long seconds = gameDuration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}