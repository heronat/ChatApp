package fr.utc.piteux.doudech.sr03.payload.requests;


public class LoginRequest {
    private String email;
    private String password;


    public String getEmail() {
        return email;
    }

    public void setEmail(String mail) {
        this.email = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
