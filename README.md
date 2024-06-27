# ChatApp

ChatApp est une application développée dans le cadre de l'UV SR03 à l'UTC. Cette application permet à un utilisateur de créer ses propres canaux de discussion, inviter des personnes dessus, et ensuite dialoguer avec eux. Il y a également une partie administrateur qui permet de gérer les utilisateurs du réseau.

## Table des matières
- [Installation](#installation)
- [Encapsulation Docker](#docker)
- [Utilisation](#utilisation)
- [Technologies et bibliothèques utilisées](#technologies)
- [Architecture](#architecture)
- [Sécurité](#securite)
- [Analyseur de toxicité](#toxicite)
- [Contributeurs](#contributeurs)

## Installation

Pour utiliser notre application, il faut dans un premier temps cloner notre projet.

 ```bash
git clone https://gitlab.utc.fr/sr03-p24/devoir-2.git
```

L'application a entièrement été installée dans une image docker. Donc pour l'utiliser, vous allez avoir besoin de l'application Docker Desktop. Une fois cette dernière installée et lancée, il faut exécuter la commande suivante.

```bash
docker-compose up -d
```
Cela va permettre d'installer tout ce qui est nécessaire localement et lancer l'application.

## Encapsulation Docker

L'application est encapsulée dans 4 conteneurs Docker séparées. Chaque conteneur possède son fichier DockerFile qui permet de lancer l'image associée.

- Le conteneur "backend" contient l'application Spring Boot Java
- Le conteneur "chatapp_postgres" contient la base de données Postgresql
- Le conteneur "chatapp" contient l'application React
- Le conteneur "chatapp_toxicity_analyzer" contient l'analyseur de toxicité

Pour lancer l'application, il suffit de lancer la commande `docker-compose up -d` qui va lancer les 4 conteneurs en même temps.
## Utilisation

### Partie Utilisateur
La partie utilisateur est disponible sur l'URL `localhost/login`. Vous pouvez à partir de cette page vous créer un compte utilisateur et vous connecter à l'application. Sur la page d'accueil, vous avez accès à la liste des chats créés et rejoints.

Si vous êtes le propriétaire du chat, vous pouvez :
- rejoindre un chat
- modifier un chat
- envoyer des invitations
- supprimer un chat

Si vous n'êtes pas le propriétaire, vous pouvez seulement :
- rejoindre un chat
- quitter un chat

Dans la sidebar à gauche, vous pouvez accéder aux pages pour :
- afficher vos chats
- créer un nouveau chat en renseignant le titre, la date de validité et une description
- gérer vos invitations (accepter ou refuser)
- vous déconnecter de l'application

### Partie Administrateur
La partie administrateur est disponible sur l'URL `localhost:8080/admin/login`. A partir de cette page vous pouvez vous connecter avec un compte administrateur. (S'il n'y en a pas encore, vous pouvez créer un compte dans la partie utilisateur, puis aller dans votre base de données en local pour passer l'attribut is_admin de votre utilisateur à TRUE)

Sur la page d'accueil, vous avez accès à la liste de tous les utilisateurs actifs. \
Pour chaque utilisateur, vous pouvez :
- modifier les informations du compte
- rendre le compte actif / inactif
- changer le rôle du compte (utilisateur / administrateur)
- supprimer le compte

La barre de navigation vous permet d'accéder à une page pour créer un nouvel utilisateur, et également une page avec la liste des utilisateurs inactifs.

## Technologies et bibliothèques utilisées
- Spring Boot Java
- Hibernate
- React

### Dépendances Maven
- Thymeleaf
- Postgresql
- Jakarta
- Spring Boot
- JsonWebToken
- Bcrypt

### Bibliothèques graphiques
- PostCSS
- Tailwind
- Vite

## Architecture

### Organisation des fichiers Serveur
Le serveur est un projet Spring Boot Java utilisant Hibernate.

#### controller

Le dossier controller contient les classes de contrôleurs Spring MVC et d'API REST. Les contrôleurs sont responsables de gérer les requêtes HTTP entrantes, d'interagir avec les services applicatifs et de renvoyer les réponses appropriées.

Pour la partie Administrateur, on utilise les contrôleurs de Spring MVC pour faire les routes de l'application et renvoyer la bonne vue au client. La partie client a été développée avec Thymeleaf.

Pour la partie Utilisateur, on utilise des `@RestController` pour gérer les requêtes HTTP entrantes et renvoyer les réponses JSON appropriées.

#### models

Le dossier models contient les classes d'entités Hibernate. Ces classes représentent les objets du domaine d'application et sont mappées aux tables de la base de données. Chaque entité définit les attributs et les relations entre les différentes entités du modèle de données.

#### payloads
Le dossier payloads est utilisé pour encapsuler les données qui sont envoyées et reçues via l'API. Il est subdivisé en deux sous-dossiers :

- requests : Contient les classes qui représentent les données des requêtes entrantes. Par exemple, les formulaires de création ou de mise à jour de ressources.

- responses : Contient les classes qui représentent les données des réponses sortantes. Ces classes définissent la structure des réponses renvoyées au client après traitement des requêtes.

#### repository

Le dossier repository contient les interfaces de dépôt Spring Data JPA. Les dépôts sont responsables de l'abstraction des opérations CRUD (Create, Read, Update, Delete) sur les entités. En étendant les interfaces de dépôt de Spring Data, on bénéficie de nombreuses fonctionnalités prêtes à l'emploi pour interagir avec la base de données.

#### security

Ce dossier contient toute la logique permettant de gérer la sécurité des requêtes HTTP avec les tokens JWT au sein de l'application. On peut notamment choisir quelles routes sont filtrées par le serveur et donc non évaluées par la sécurité avec JWT. Sinon, chaque requête qui arrive va être évaluée pour vérifier que le token JWT reçu correspond bien à un token qui a été généré et signé précédemment par le serveur.

#### services

Le dossier services contient les classes de service. Les services encapsulent la logique métier de l'application. Ils sont responsables de traiter les données, d'appliquer les règles métier et d'orchestrer les interactions entre les contrôleurs et la base de données. Les services permettent de maintenir une séparation claire entre le traitement des requêtes HTTP et le traitement des données.

#### utils

Le dossier utils rassemble les classes utilitaires utilisés à plusieurs endroits dans l'application et qui ne concernent pas directement l'application de chat. On y retrouve notamment un fichier pour la gestion des tokens JWT, et un fichier pour gérer l'envoi de mails.

#### websockets

Ce dossier contient tous les modèles et fonctions permettant de gérer les websockets au sein de l'application. 3 classes de modèles ont été crées pour gérer plus efficacement les informations liées aux websockets au sein des méthodes. Les autres fichiers contiennent toutes les méthodes liées à la création, édition, suppression et la transmission de messages sur les différentes websockets.


### Organisation des fichiers client

Pour la partie utilisateur, le frontend a été réalisé en React.
Le dossier est divisé en sous-dossier pour organiser les pages et les composants :

- authentication : Contient tous les fichiers liés à la création d'un compte et à la connexion d'un utilisateur
- components : Contient tous les composants seuls qui peuvent être utilisés plusieurs fois dans l'application comme la sidebar. On retrouve aussi tout ce qui est lié aux chats.
- pages : contient les différentes pages auxquelles on peut accéder quand on utilise l'application. Elles incluent les composants définis dans le dossier précédent.
- store : contient toutes les fonctions liés au traitement de la connexion d'un utilisateur
- utils : contient des fonctions permettant de gérer les requêtes HTTP entrantes et sortantes
- App.jsx : application rassemblant toutes les routes vers les autres pages et spécifiant quelles routes sont protégées par l'authentification

## Sécurité

### HTTP Session
La sécurité de la partie administrateur est gérée avec HTTPSession. Quand un administrateur réussit à se connecter, une session HTTP est créée côté serveur et un identifiant de session unique est attribué à cet utilisateur. A chaque qu'il essaye d'effectuer une requête, on vérifie quel identifiant est stocké dans la session et si l'utilisateur correspondant existe toujours dans la base de données. En cas de déconnexion, la session est invalidée pour garantir que les informations sensibles ne sont plus accessibles.

### JWT Tokens
La sécurité de la partie utilisateur est gérée avec des JsonWebTokens. Lorsqu'un utilisateur se connecte, un token JWT est généré, signé par le serveur, puis renvoyé au client qui le stocke dans ses cookies. Pour chaque requête suivante, le client va inclure le token JWT dans sa requête HTTP et le serveur va vérifier la cohérence du token avant d'accepter la requête.
Pour ne pas interférer avec la partie administrateur et les pages de login, le fichier `WebSecurityConfig.java` contient une méthode filterChain() qui nous permet d'écarter certaines routes de la vérification.

## Analyseur de toxicité
Pour pousser le projet un peu plus loin, nous avons décidé d'ajouter un analyseur de toxicité à notre application. L'analyseur de toxicité des messages est un service intégré dans notre application de chat pour évaluer le contenu des messages envoyés par les utilisateurs. Ce service utilise une API Flask externe pour déterminer si un message spécifique est toxique. L'analyse de la toxicité des messages peut être activée ou désactivée dynamiquement via la configuration de l'application. Cela permet d'adapter le comportement de l'analyseur en fonction des besoins et des politiques de l'application.
Lorsque l'analyse est activée, le service prépare le message à analyser et l'envoi sous format JSON. La réponse, également sous format JSON, contient une étiquette qui indique si le message est toxique ou non. La réponse de l'API est traitée pour extraire l'étiquette de toxicité (T pour toxique, NT pour non toxique).
L'analyseur de toxicité des messages améliore la sécurité et le contrôle du contenu au sein de notre application de chat. En fournissant une évaluation automatisée de la nature des messages, nous nous assurons de maintenir un environnement en ligne respectueux et sûr pour tous les utilisateurs.
Dans le dossier toxic-analyzer, vous trouverez le code source en python permettant d'appeler l'API Flask et le Dockerfile pour construire l'image.

## Contributeurs
- Taieb Rayen Doudech
- Nathan Piteux

