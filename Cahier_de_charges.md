# Cahier de Charges : Pro Authentication Suite v2.5

## 1. Objectifs du Projet

Développer une application Client-Serveur Java hautement sécurisée et facile à déployer, permettant une gestion centralisée des utilisateurs via une architecture réseau TCP et une persistance MySQL.

## 2. Spécifications Fonctionnelles

### 2.1. Gestion des Utilisateurs

- **Enregistrement** : Création de compte avec vérification d'unicité du pseudonyme.
- **Authentification** : Connexion sécurisée avec validation par hash.
- **Gestion de Session** : Attribution d'un ID de session unique (UUID) à chaque connexion.
- **Changement de Mot de Passe** : Mise à jour sécurisée nécessitant l'ancien mot de passe.
- **Déconnexion (Logout)** : Permet de quitter la session active sans fermer l'application client.

### 2.2. Interface Utilisateur (UX)

- Interface console interactive avec couleurs ANSI.
- Simulations de chargement pour une meilleure réactivité perçue.
- Bannières ASCII dynamiques pour le branding (CLIENT vs SERVER).

## 3. Spécifications Techniques

### 3.1. Architecture Logicielle

- **Multi-module Maven** :
  - `parent-project` : Gestion globale.
  - `server` : Logique métier et accès DB.
  - `client` : Interface utilisateur et communication réseau.
- **Réseau** : Communication bidirectionnelle via Sockets TCP sur le port 5000.
- **Multi-threading** : Un thread par client via `ClientHandler.java`.

### 3.2. Sécurité

- **Chiffrement** : Hachage unidirectionnel **SHA-256**.
- **Sécurité SQL** : Utilisation systématique de `PreparedStatement` pour bloquer les injections.
- **Session** : IDs de session aléatoires de 8 caractères.

### 3.3. Automatisation & Déploiement

- **Scripts de Lancement** :
  - `run_all.bat` : Auto-détection de Java/Maven, configuration `JAVA_HOME` dynamique, compilation et lancement simultané.
  - `setup_db.bat` : Initialisation automatique du schéma MySQL avec détection du chemin binaire.

## 4. Environnement Requis

- **JDK** : Microsoft OpenJDK 17.
- **Build tool** : Maven 3.9.6.
- **Database** : MySQL 8.4 Server.
- **OS** : Windows (Scripts batch optimisés).

## 5. Livrables

1. Code source modulaire complet.
2. Scripts PowerShell/Batch d'automatisation.
3. Schéma SQL d'initialisation (`schema.sql`).
4. Documentation utilisateur exhaustive (README.md).
