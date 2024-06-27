package fr.utc.piteux.doudech.sr03.payload.requests;

import jakarta.validation.constraints.NotNull;

public class LogoutRequest {
    @NotNull(message = "email is mandatory")
    private final String email;
    @NotNull(message = "token is mandatory")
    private final String token;

    public LogoutRequest(String mail, String token) {
        this.email = mail;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }
}
