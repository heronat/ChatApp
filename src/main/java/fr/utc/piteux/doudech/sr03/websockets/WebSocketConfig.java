package fr.utc.piteux.doudech.sr03.websockets;

import fr.utc.piteux.doudech.sr03.services.MessageService;
import fr.utc.piteux.doudech.sr03.services.UserService;
import fr.utc.piteux.doudech.sr03.services.WebSocketService;
import fr.utc.piteux.doudech.sr03.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private DelegatingWebSocketHandler delegatingWebSocketHandler;

    @Resource
    private UserService userService;

    @Resource
    private MessageService messageService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        this.delegatingWebSocketHandler = new DelegatingWebSocketHandler(this.userService, this.jwtUtils, this.messageService);
        // Enregistre le gestionnaire principal des WebSockets avec le chemin "/ws/*" et autorise toutes les origines
        registry.addHandler(this.delegatingWebSocketHandler, "/ws/*").setAllowedOrigins("*");
    }

    public DelegatingWebSocketHandler getDelegatingWebSocketHandler() {
        return delegatingWebSocketHandler;
    }
}
