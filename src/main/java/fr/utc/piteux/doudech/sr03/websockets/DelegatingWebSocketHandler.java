package fr.utc.piteux.doudech.sr03.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.utc.piteux.doudech.sr03.models.Canals;
import fr.utc.piteux.doudech.sr03.services.MessageService;
import fr.utc.piteux.doudech.sr03.services.UserService;
import fr.utc.piteux.doudech.sr03.services.WebSocketService;
import fr.utc.piteux.doudech.sr03.utils.JwtUtils;
import fr.utc.piteux.doudech.sr03.websockets.models.MessageType;
import org.jboss.logging.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DelegatingWebSocketHandler extends TextWebSocketHandler {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DelegatingWebSocketHandler.class);
    private Map<Canals, WebSocketHandler> webSocketHandlers = new HashMap<>();
    private final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());

    private final UserService usersService;
    private final JwtUtils jwtUtils;
    private final MessageService messageService;

    public DelegatingWebSocketHandler(UserService usersService, JwtUtils jwtUtils, MessageService messageService) {
        this.usersService = usersService;
        this.jwtUtils = jwtUtils;
        this.messageService = messageService;
    }

    // Délègue les messages reçus à la méthode handleMessage de la websocket associée
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        WebSocketHandler handler = getHandlerByPath(path);

        ObjectMapper mapper = new ObjectMapper();
        String receivedMessage = (String) message.getPayload();
        MessageSocket messageSocket = mapper.readValue(receivedMessage, MessageSocket.class);

        if (messageSocket.getType() == null) {
            throw new IllegalArgumentException("Message type must not be null");
        }

        //On vérifie si le message est une image ou un message texte
        if (messageSocket.getType().equals(MessageType.PICTURE)) {
            logger.log(Logger.Level.INFO, "Received picture on path: " + path);
        } else {
            logger.log(Logger.Level.INFO, "Received message on path: " + path + " with content: " + message.getPayload());
        }

        if (handler != null) {
            try {
                handler.handleMessage(session, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            session.sendMessage(new TextMessage("404 - No handler found for this path"));
            session.close();
        }
    }

    // Délègue la gestion de la connexion WebSocket à la websocket associée
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        WebSocketHandler handler = getHandlerByPath(path);

        if (handler != null) {
            handler.afterConnectionEstablished(session);
        } else {
            session.sendMessage(new TextMessage("404 - No handler found for this path"));
            session.close();
        }
    }

    // Délègue la fermerture de la connexion WebSocket à la websocket associée
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        WebSocketHandler handler = getHandlerByPath(path);
        if (handler != null) {
            try {
                handler.afterConnectionClosed(session, status);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Ajoute un gestionnaire WebSocket associé à un canal spécifique
    public WebSocketHandler addHandler(Canals canals) {
        if (canals.getCanalTitle() == null || canals.getCanalTitle().isEmpty()) {
            throw new IllegalArgumentException("Canal title must not be empty");
        }
        // Crée une URL WebSocket unique basée sur le titre du canal et l'ajoute aux gestionnaires actifs
        String socketUrl = "/ws/"+canals.getCanalTitle().toLowerCase(Locale.ROOT).replace(" ", "_");
        WebSocketHandler webSocketHandler = new WebSocketHandler(this.usersService, this.jwtUtils, canals.getCanalTitle(), socketUrl, this.messageService);
        this.webSocketHandlers.put(canals, webSocketHandler);
        return webSocketHandler;
    }

    // Supprime un gestionnaire WebSocket en fonction de l'id du canal
    public void removeHandler(int canalId) {
        for (Canals canal : this.webSocketHandlers.keySet()) {
            if (canal.getCid() == canalId) {
                logger.warn("Handlers count: " + this.webSocketHandlers.size());
                this.webSocketHandlers.remove(canal);
                logger.warn(">> Removed handler for canal: " + canalId);
                logger.warn(">> New handlers count: " + this.webSocketHandlers.size());
                return;
            }
        }
    }

    // Vérifie si un gestionnaire WebSocket est déjà initialisé pour un canal donné
    public boolean isCanalAlreadyInitialized(int id) {
        for (Canals canal : this.webSocketHandlers.keySet()) {
            if (canal.getCid() == id) {
                return true;
            }
        }
        return false;
    }

    // Récupère le gestionnaire WebSocket associé à un canal spécifique en fonction de l'ID
    public WebSocketHandler getHandler(int id) {
        for (Canals canal : this.webSocketHandlers.keySet()) {
            if (canal.getCid() == id) {
                return this.webSocketHandlers.get(canal);
            }
        }
        return null;
    }

    // Récupère le gestionnaire WebSocket associé à un chemin spécifique
    public WebSocketHandler getHandlerByPath(String path) {
        for (WebSocketHandler handler : this.webSocketHandlers.values()) {
            logger.warn("Checking handler for path: " + path + " -- " + handler.getSocketUrl());
            if (handler.getSocketUrl().equals(path)) {
                logger.warn("Found handler for path: " + path);
                return handler;
            }
        }
        return null;
    }
}
