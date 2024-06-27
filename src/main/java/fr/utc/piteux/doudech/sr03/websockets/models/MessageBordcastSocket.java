package fr.utc.piteux.doudech.sr03.websockets.models;

public class MessageBordcastSocket {

    private final Integer senderId;
    private final String sender;
    private final String message;
    private final MessageType type;

    public MessageBordcastSocket(Integer senderId, String sender, String message, MessageType type) {
        this.senderId = senderId;
        this.sender = sender;
        this.message = message;
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public MessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Integer getSenderId() {
        return senderId;
    }

}
