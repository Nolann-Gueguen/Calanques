Calanques App - Gestion de Réservations
Une application Android développée en Kotlin et Jetpack Compose permettant aux utilisateurs de découvrir les activités des Calanques de Marseille et de gérer leurs réservations.

Fonctionnalités Réalisées
Navigation et Interface
Navigation Intuitive : Barre de navigation basse avec quatre sections : Activités, Panier, Compte, Carte.

Design Moderne : Utilisation de Material Design 3, animations de transition et retours visuels (Toasts, indicateurs de chargement).

Thème Personnalisé : Identité visuelle basée sur la charte graphique définie pour le projet.

Gestion des Activités
Exploration par Catégories : Filtrage des activités par types (Plongée, Randonnée, Kayak, etc.).

Détails Complets : Écran de détail avec photo, description, durée et tarif dynamique.

Carte Interactive : Intégration d'OSMDroid pour localiser les activités sur une carte avec marqueurs interactifs.

Panier et Réservation
Panier Dynamique : Ajout d'activités avec sélection de date (DatePicker), d'heure (TimePicker) et du nombre de participants.

Modification des Réservations : Possibilité de modifier directement les paramètres (date, nombre de participants) d'une activité déjà présente dans le panier.

Paiement Simulé : Interface de saisie de carte bancaire avec validation des champs.

Compte Utilisateur
Authentification : Système de connexion avec gestion des jetons (tokens) via SessionManager.

Historique : Consultation des réservations effectuées.

Stack Technique
Langage : Kotlin

UI : Jetpack Compose

Réseau : Retrofit 2 et OkHttp pour les appels API

Images : Coil pour le chargement asynchrone

Cartographie : OSMDroid

Architecture : Pattern basé sur les composants de cycle de vie Android

Travaux à réaliser (To-Do)
Améliorations Techniques
[ ] Gestion du Cache : Implémenter une base de données locale (Room) pour permettre la consultation des activités hors-ligne.

[ ] Validation Côté Serveur : Gestion des conflits de disponibilité lors de la réservation.

[ ] Notifications : Système d'alerte avant le début des activités réservées.

Expérience Utilisateur
[ ] Favoris : Système de sauvegarde d'activités favorites.

[ ] Mode Sombre : Adaptation des couleurs pour le confort nocturne.

[ ] Profil Éditable : Modification des informations personnelles depuis l'onglet Compte.

Sécurité
[ ] Stockage Sécurisé : Utilisation de EncryptedSharedPreferences pour le jeton d'authentification.

[ ] Gestion des Erreurs : Amélioration des écrans d'erreur réseau.

Installation
Cloner le dépôt.

S'assurer que le serveur API est accessible.

Compiler le projet via Android Studio.
