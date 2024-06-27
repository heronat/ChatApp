package fr.utc.piteux.doudech.sr03.payload.requests;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class ProfileUpdateRequest {
    private String email;
    private String firstName;
    private String lastName;

    private String password;
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
