package fr.utc.piteux.doudech.sr03.models;

import fr.utc.piteux.doudech.sr03.models.roles.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.*;

@Entity
@Table(name="users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="uid")
    private int uid;
    @Column(name="first_name")
    @NotBlank(message = "first_name is mandatory")
    private String firstName;
    @Column(name="last_name")
    @NotBlank(message = "lastName is mandatory")
    private String lastName;
    @Column(name="email")
    @Email(message = "Email should be valid")
    private String email;

    @Column(name="password_hash")
    @NotBlank(message = "password_hash is mandatory")
    private String passwordHash;
    @Column(name="is_admin")
    private boolean isAdmin;
    @Column(name="is_users_enabled")
    private boolean isUserEnabled;

    // Users who joined the canal
    @ManyToMany
    @JoinTable( name = "users_canals",
            joinColumns = @JoinColumn( name = "users_id"),
            inverseJoinColumns = @JoinColumn( name = "canal_id")
    )
    private List<Canals> canalsJoined = new ArrayList<>();

    // Users who are invited to the canal
    @ManyToMany
    @JoinTable( name = "users_invitations",
            joinColumns = @JoinColumn( name = "users_id" ),
            inverseJoinColumns = @JoinColumn( name = "canal_id" )
    )
    private List<Canals> canalsInvitations = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(  name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();


    public Users(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.isUserEnabled = true;
        this.isAdmin = false;
    }

    public Users() {

    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isUserEnabled() {
        return isUserEnabled;
    }

    public void setUserEnabled(boolean userEnabled) {
        isUserEnabled = userEnabled;
    }

    public int getUid() {
        return uid;
    }

    public Long getLongUid() {
        return (long) uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Users user = (Users) o;
        return uid == user.uid &&
                isAdmin == user.isAdmin &&
                isUserEnabled == user.isUserEnabled &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName) &&
                Objects.equals(email, user.email) &&
                Objects.equals(passwordHash, user.passwordHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, firstName, lastName, email, passwordHash, isAdmin, isUserEnabled);
    }
}
