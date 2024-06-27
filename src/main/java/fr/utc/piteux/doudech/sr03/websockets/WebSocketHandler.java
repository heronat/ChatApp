package fr.utc.piteux.doudech.sr03.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.utc.piteux.doudech.sr03.models.Users;
import fr.utc.piteux.doudech.sr03.services.MessageService;
import fr.utc.piteux.doudech.sr03.services.UserService;
import fr.utc.piteux.doudech.sr03.utils.JwtUtils;
import fr.utc.piteux.doudech.sr03.websockets.models.MessageBordcastSocket;
import fr.utc.piteux.doudech.sr03.websockets.models.MessageType;
import fr.utc.piteux.doudech.sr03.websockets.models.NotificationBordcastSocket;
import io.undertow.websockets.core.WebSocketMessages;
import org.jboss.logging.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

public class WebSocketHandler extends TextWebSocketHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
    private final UserService userService;
    private JwtUtils jwtUtils;
    private final MessageService messageService;

    public WebSocketHandler(UserService userService, JwtUtils jwtUtils, String nameChat, String socketUrl, MessageService messageService) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.nameChat = nameChat;
        this.socketUrl = socketUrl;
        this.messageService = messageService;

        logger.info(">> Création du chat " + nameChat);
    }

    private final String nameChat;
    private final String socketUrl;

    private Boolean isChatOpen = true;

    private final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());
    private HashMap<Users, WebSocketSession> connectedUsers = new HashMap<>();
    private HashMap<Integer, Users> connectedUsersWithIds = new HashMap<>();
    private List<Users> authorizedUser = new ArrayList<>();

    public void addAuthorizedUser(Users user){
        this.authorizedUser.add(user);
    }

    public void addConnectedUser(Users user, WebSocketSession session){
        this.connectedUsers.put(user, session);
    }

    //Gestion des messages reçus sur une websocket
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        if (!this.isChatOpen) return;
        ObjectMapper mapper = new ObjectMapper();
        String receivedMessage = (String) message.getPayload();
        MessageSocket messageSocket = mapper.readValue(receivedMessage, MessageSocket.class);

        Integer userId = getUserIdFromSession(session);
        if (userId == -1) {
            //session.sendMessage(new TextMessage("Vous n'êtes pas autorisé à accéder à ce chat"));
            MessageBordcastSocket messageBordcastSocket = new MessageBordcastSocket(
                    -1,
                    "SYSTEM",
                    "Vous n'êtes pas autorisé à accéder à ce chat",
                    MessageType.ERROR
            );
            session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(messageBordcastSocket)));
            session.close();
            return;
        }

        Users user = this.connectedUsersWithIds.get(userId);

        // Traite le message en fonction de son type
        if (messageSocket.getType() == MessageType.PICTURE) {
            logger.info("User : " + user.getUid() + "@" + user.getEmail() + " >> Envoie une image sur le chat " + this.nameChat);
            MessageBordcastSocket messageBordcastSocket = new MessageBordcastSocket(
                    user.getUid(),
                    user.getLastName() + " " + user.getFirstName(),
                    messageSocket.getMessage(),
                    MessageType.PICTURE
            );
            this.brodcastMessage(new ObjectMapper().writeValueAsString(messageBordcastSocket));
            return;
        }

        if (messageSocket.getType() == MessageType.TYPING_START || messageSocket.getType() == MessageType.TYPING_STOP) {
            // Si c'est un message de début ou fin de saisie, diffuse la notification à tous les utilisateurs du chat
            logger.info("User : " + user.getUid() + "@" + user.getEmail() + " >> ÉCRITURE STATUS :" + messageSocket.getType() + " CHAT : " + this.nameChat);
            NotificationBordcastSocket messageBordcastSocket = new NotificationBordcastSocket(
                    user.getUid(),
                    "SYSTEM",
                    user.getLastName() + " " + user.getFirstName(),
                    messageSocket.getType(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail()
            );
            this.brodcastMessage(new ObjectMapper().writeValueAsString(messageBordcastSocket));
            return;
        }

        Boolean isMessageToxic = this.messageService.isMessageToxic(messageSocket.getMessage());
        if (isMessageToxic) {
            // Si le message est toxique, envoie un message d'avertissement à l'utilisateur
            //session.sendMessage(new TextMessage("Votre derneir message contient des propos inappropriés, il n'a pas été envoyé. Merci de respecter les autres utilisateurs :)"));
            MessageBordcastSocket messageBordcastSocket = new MessageBordcastSocket(
                    -1,
                    "SYSTEM",
                    "Votre derneir message contient des propos inappropriés, il n'a pas été envoyé. Merci de respecter les autres utilisateurs :)",
                    MessageType.INAPPROPRIATE_MESSAGE
            );
            session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(messageBordcastSocket)));
            return;
        }

        // Si le message est un message texte normal, le diffuse à tous les utilisateurs du chat
        MessageBordcastSocket messageBordcastSocket = new MessageBordcastSocket(
                user.getUid(),
                user.getLastName() + " " + user.getFirstName(),
                messageSocket.getMessage(),
                MessageType.MESSAGE
        );

        this.brodcastMessage(new ObjectMapper().writeValueAsString(messageBordcastSocket));
    }

    //Gestion de la connexion à une websocket
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        if (!this.isChatOpen) return;
        Integer userId = getUserIdFromSession(session);
        if (userId == -1) {
            MessageBordcastSocket messageBordcastSocket = new MessageBordcastSocket(
                    -1,
                    "SYSTEM",
                    "Vous n'êtes pas autorisé à accéder à ce chat",
                    MessageType.ERROR
            );
            session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(messageBordcastSocket)));
            session.close();
            return;
        }

        Users connectedUser = this.userService.getUserById(userId);

        // Vérifie si l'utilisateur est déjà connecté à ce chat
        if (this.connectedUsers.containsKey(connectedUser)) {
            MessageBordcastSocket messageBordcastSocket = new MessageBordcastSocket(
                    -1,
                    "SYSTEM",
                    "Vous êtes déjà connecté à ce chat",
                    MessageType.ERROR
            );
            session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(messageBordcastSocket)));
            session.close(CloseStatus.SESSION_NOT_RELIABLE);
            return;
        }

        this.connectedUsers.put(connectedUser, session);
        this.connectedUsersWithIds.put(userId, connectedUser);

        // Vérifie si l'utilisateur est autorisé à accéder au chat
        if (connectedUser == null || !this.authorizedUser.contains(connectedUser)) {
            MessageBordcastSocket messageBordcastSocket = new MessageBordcastSocket(
                    -1,
                    "SYSTEM",
                    "Vous n'êtes pas autorisé à accéder à ce chat",
                    MessageType.ERROR
            );
            session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(messageBordcastSocket)));
            session.close();
            return;
        }

        logger.info("User : " + userId + "@" + connectedUser.getEmail() + " >> Vient de se connecter au chat " + this.nameChat);

        // Diffuse une notification à tous les utilisateurs pour informer de la connexion d'un nouvel utilisateur
        NotificationBordcastSocket notificationBordcastSocket = new NotificationBordcastSocket(
                connectedUser.getUid(),
                "SYSTEM",
                "L'utilisateur " + connectedUser.getEmail() + " vient de se connecter sur " + this.nameChat,
                MessageType.JOIN,
                connectedUser.getFirstName(),
                connectedUser.getLastName(),
                connectedUser.getEmail()
        );

        this.brodcastMessage(new ObjectMapper().writeValueAsString(notificationBordcastSocket));
    }

    //Gestion de la déconnexion d'un utilisateur d'une websocket
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        if (!this.isChatOpen) return;
        if (status.getCode() == 4500) {
            return;
        }
        Users user = getKeyByValue(this.connectedUsers, session);
        logger.info("User : " + user.getUid() + "@" + user.getEmail() + " >> Vient de se déconnecter au chat " + this.nameChat);
        NotificationBordcastSocket notificationBordcastSocket = new NotificationBordcastSocket(
                user.getUid(),
                "SYSTEM",
                "L'utilisateur " + user.getEmail() + " vient de se déconnecter!",
                MessageType.LEAVE,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
        this.brodcastMessage(new ObjectMapper().writeValueAsString(notificationBordcastSocket));

        this.connectedUsersWithIds.remove(user.getUid());
        this.authorizedUser.remove(user);
        this.connectedUsers.remove(user);
     }

    public void closedByClient(Users user) throws JsonProcessingException {
        logger.info("User : " + user.getUid() + "@" + user.getEmail() + " >> Vient de se déconnecter au chat " + this.nameChat);
        NotificationBordcastSocket notificationBordcastSocket = new NotificationBordcastSocket(
                user.getUid(),
                "SYSTEM",
                "L'utilisateur " + user.getEmail() + " vient de se déconnecter!",
                MessageType.LEAVE,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );

        this.brodcastMessage(new ObjectMapper().writeValueAsString(notificationBordcastSocket));


    }

    public List<Users> getConnectedUsers() {
        return new ArrayList<>(connectedUsers.keySet());
    }

    public String getSocketUrl() {
        return socketUrl;
    }

    //Envoie un message à tous les utilisateurs connectés à la websocket
    public void brodcastMessage(String message) {
        if (!this.isChatOpen) return;
        for (WebSocketSession session : connectedUsers.values()) {
            try {
                logger.info("Sending message to " + session.getUri().toString().split("token")[0] + " : " + message);
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                logger.error("Error while sending message to " + e.getMessage());
                // Si une erreur se produit lors de l'envoi du message, vérifie si la session est fermée
                if (e.getMessage().equals("UT002002: Channel is closed")) {
                    // Récupère l'utilisateur associé à la session qui a provoqué l'erreur
                    Users user = getKeyByValue(this.connectedUsers, session);

                    // Supprime l'utilisateur des listes de utilisateurs connectés
                    this.connectedUsersWithIds.remove(user.getUid());
                    this.authorizedUser.remove(user);
                    this.connectedUsers.remove(user);

                    logger.info("Session of " + user + " is closed / No longer available");
                    try {
                        this.closedByClient(user);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                    continue;
                }
                e.printStackTrace();
            }
        }

    }

    public <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Integer getUserIdFromSession(WebSocketSession session) {
        String token = session.getUri().getQuery();
        //List<String> jwtHeaders = session.getHandshakeHeaders().get("Authorization");
        if ( token == null  || !jwtUtils.validateJwtToken(token.split("=")[1]) ) {
            return -1;
        }
        return jwtUtils.getUserIdFromJwtToken(token.split("=")[1]);
    }

    //Si le chat est supprimé par son créateur alors qu'une websocket est ouverte, tous les utilisateurs connectés sont déconnectés
    public void killChat() throws JsonProcessingException {
        this.isChatOpen = false;
        MessageBordcastSocket messageBordcastSocket = new MessageBordcastSocket(
                -1,
                "SYSTEM",
                "Le chat a été fermé par l'administrateur!",
                MessageType.ERROR
        );
        try {
            String str = new ObjectMapper().writeValueAsString(messageBordcastSocket);
            for (WebSocketSession session : connectedUsers.values()) {
                session.sendMessage(new TextMessage(str));
                session.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}