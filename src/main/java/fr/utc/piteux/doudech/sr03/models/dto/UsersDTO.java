package fr.utc.piteux.doudech.sr03.models.dto;

public class UsersDTO {

    private final int uid;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Boolean isAdmin;
    private final Boolean isUserEnabled;

    public UsersDTO(int uid, String firstName, String lastName, String email, Boolean isAdmin, Boolean isUserEnabled) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.isAdmin = isAdmin;
        this.isUserEnabled = isUserEnabled;
    }

    public int getUid() {
        return uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public Boolean getUserEnabled() {
        return isUserEnabled;
    }
}
