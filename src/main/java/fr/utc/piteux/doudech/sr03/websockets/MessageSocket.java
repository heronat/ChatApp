package fr.utc.piteux.doudech.sr03.websockets;


import fr.utc.piteux.doudech.sr03.websockets.models.MessageType;

public class MessageSocket {

    private String message;
    private MessageType type;

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

