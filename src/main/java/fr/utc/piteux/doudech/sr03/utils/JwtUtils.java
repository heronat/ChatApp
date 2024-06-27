package fr.utc.piteux.doudech.sr03.utils;

import fr.utc.piteux.doudech.sr03.models.UserDetailsImpl;
import fr.utc.piteux.doudech.sr03.repository.TokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Autowired
    private TokenRepository tokenRepository;

    @Value("${utc.chat.app.jwtSecret}")
    private String jwtSecret;

    @Value("${utc.chat.app.jwtExpirationMs}")
    private int jwtExpirationMs;


    /**
     * Cette fonction génère un token JWT pour un utilisateur donné,
     * elle renvoie les informations dont on a besoin pour sauvegrader
     * le token, pour pouvoir le revoquer plus tard si besoin
     *
     * @param authentication : Authentication
     * @return List<Object> : [0] = token, [1] = now, [2] = expiryDate
     * */

    public List<Object> generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        List<Object> token = new ArrayList<>();

        token.add(Jwts.builder()
                .subject((userPrincipal.getUsername()))
                .claim("user", userPrincipal.getUser().getUid())
                .signWith(key())
                .issuedAt(now)
                .expiration(expiryDate)
                .compact());
        token.add(now);
        token.add(expiryDate);

        return token;
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public Integer getUserIdFromJwtToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        return claims.get("user", Integer.class);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            //Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            Jwts.parser().setSigningKey(key()).build().parse(authToken);

            tokenRepository.findTokenByToken(authToken).ifPresent(token -> {
                if(token.getRevoked()){
                    throw new RuntimeException("Token is revoked");
                }
            });

            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (RuntimeException e) {
            logger.error("JWT token is revoked: {}", e.getMessage());
        }

        return false;
    }
}
