package fr.utc.piteux.doudech.sr03.services;

import fr.utc.piteux.doudech.sr03.models.Canals;
import fr.utc.piteux.doudech.sr03.models.Users;
import fr.utc.piteux.doudech.sr03.models.dto.UsersDTO;
import fr.utc.piteux.doudech.sr03.websockets.WebSocketConfig;
import fr.utc.piteux.doudech.sr03.websockets.WebSocketHandler;
import jakarta.annotation.Resource;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



@Service
public class WebSocketService {

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Resource
    private UserService userService;

    private final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());


    public String createWebSocketHandler(Canals canals, Users user) {
        try {
            if (!webSocketConfig.getDelegatingWebSocketHandler().isCanalAlreadyInitialized(canals.getCid())) {
                webSocketConfig.getDelegatingWebSocketHandler().addHandler(canals);
            }
            webSocketConfig.getDelegatingWebSocketHandler().getHandler(canals.getCid()).addAuthorizedUser(user);
            return webSocketConfig.getDelegatingWebSocketHandler().getHandler(canals.getCid()).getSocketUrl();
        } catch (Exception e) {
            logger.error("Error while creating websocket handler -- CID={" + canals.getCid() + "} \n Canal : " + canals + "\n  // ERR MESSAGE : \n" + e.getMessage());
            logger.error(e);
            throw new RuntimeException("Error while creating websocket handler");
        }
    }


    public List<UsersDTO> getConnectedUsersDTOToCanal(Canals canals) {
        WebSocketHandler handler = webSocketConfig.getDelegatingWebSocketHandler()
                .getHandler(canals.getCid());
        if (handler == null) return new ArrayList<>();
        return handler
                .getConnectedUsers()
                .stream()
                .map(userService::getUserDTO)
                .collect(Collectors.toList());
    }

    public void killChannel(Canals canals) {
        WebSocketHandler handler = webSocketConfig.getDelegatingWebSocketHandler().getHandler(canals.getCid());
        if (handler != null) {
            try {
                handler.killChat();
                webSocketConfig.getDelegatingWebSocketHandler().removeHandler(canals.getCid());
            } catch (IOException e) {
                logger.error("Error while killing websocket handler -- CID={" + canals.getCid() + "} \n Canal : " + canals + "\n  // ERR MESSAGE : \n" + e.getMessage());
                logger.error(e);
                throw new RuntimeException("Error while killing websocket handler");
            }
        }
    }
}
