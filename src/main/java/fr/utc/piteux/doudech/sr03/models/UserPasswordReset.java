package fr.utc.piteux.doudech.sr03.models;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name="userPasswordReset")
public class UserPasswordReset {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private String token;
    private Date generationDate;
    private Date expirationDate;

    public UserPasswordReset(Users user, String token, Date generationDate, Date expirationDate) {
        this.user = user;
        this.token = token;
        this.generationDate = generationDate;
        this.expirationDate = expirationDate;
    }

    public UserPasswordReset() {
    }

    public Long getId() {
        return id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
