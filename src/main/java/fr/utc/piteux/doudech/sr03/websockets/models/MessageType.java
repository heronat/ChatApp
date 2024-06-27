package fr.utc.piteux.doudech.sr03.websockets.models;

public enum MessageType {
    NOTIFICATION,
    MESSAGE,
    INAPPROPRIATE_MESSAGE,
    ERROR,
    PICTURE,
    TYPING_START,
    TYPING_STOP,
    JOIN,
    LEAVE
}
