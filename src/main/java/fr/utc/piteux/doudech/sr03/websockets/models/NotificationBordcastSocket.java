package fr.utc.piteux.doudech.sr03.websockets.models;

public class NotificationBordcastSocket extends MessageBordcastSocket {

    private final String userFirstName;
    private final String userLastName;
    private final String userEmail;


    public NotificationBordcastSocket(Integer senderId, String sender, String message, MessageType type, String userFirstName, String userLastName, String userEmail) {
        super(senderId, sender, message, type);
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.userEmail = userEmail;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
