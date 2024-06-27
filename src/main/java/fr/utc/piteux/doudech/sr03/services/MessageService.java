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
public class MessageService {

    @Value("${utc.chat.app.analyzeMessagesToxicity}")
    private boolean ANALYZE_MESSAGE_TOXICITY;

    @Value("${utc.chat.app.toxicityAnalyzerLink}")
    private String toxicityAnalyzerLink;

    private final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());
    public Boolean isMessageToxic(String message) {
        if (ANALYZE_MESSAGE_TOXICITY) {
            // ANALYZE MESSAGE TOXICITY
            logger.warn("Analyzing message toxicity : " + message);
            try {
                // Création d'une connexion HTTP pour envoyer le message à l'analyseur de toxicité
                URL url = new URL(toxicityAnalyzerLink);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonInput = new JSONObject();
                jsonInput.put("message", message);

                String jsonInputString = jsonInput.toString();

                // Envoi du message à l'analyseur
                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    // Analyse de la réponse JSON
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String label = jsonObject.getString("label");
                    logger.warn("Toxicity API response : " + jsonObject.toString());
                    logger.warn("Message toxicity : " + label);
                    return label.equals("T");
                }
            } catch (IOException e) {
                logger.error("Error while analyzing message toxicity : " + e.getMessage());
                return false;
            }
        }
        return false;
    }
}
