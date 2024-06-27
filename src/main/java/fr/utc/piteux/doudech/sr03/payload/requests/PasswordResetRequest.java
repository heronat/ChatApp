package fr.utc.piteux.doudech.sr03.payload.requests;


import jakarta.validation.constraints.NotBlank;
import org.wildfly.common.annotation.NotNull;

public class PasswordResetRequest {
    @NotBlank(message = "Password is mandatory")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
