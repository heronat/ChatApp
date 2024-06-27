package fr.utc.piteux.doudech.sr03.models;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name="tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    private String username;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private Date generationDate;
    private Date ExpirationDate;

    private Boolean isRevoked;

    public Token(String token, String username, Users user, Date generationDate, Date expirationDate) {
        this.token = token;
        this.username = username;
        this.user = user;
        this.generationDate = generationDate;
        ExpirationDate = expirationDate;
        this.isRevoked = false;
    }

    public Token() {
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public Users getUser() {
        return user;
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    public Date getExpirationDate() {
        return ExpirationDate;
    }

    public Boolean getRevoked() {
        return isRevoked;
    }

    // getters and setters
}