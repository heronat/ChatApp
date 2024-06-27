package fr.utc.piteux.doudech.sr03;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.crypto.SecretKey;

@SpringBootApplication
@EntityScan(basePackages = "fr.utc.piteux.doudech.sr03")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void generateRandomSecret() {
        SecretKey key = Jwts.SIG.HS256.key().build();
        String secretString = Encoders.BASE64.encode(key.getEncoded());
        System.out.println(secretString);
    }

}
