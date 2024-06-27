package fr.utc.piteux.doudech.sr03.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailSender {


    public EmailSender() {
    }

    // Cette méthode permet d'envoyer un mail à un destinataire donné en paramètre, avec un sujet et un contenu donné
    // Attention, il faut être connecté au réseau de l'UTC pour pouvoir envoyer un mail avec l'adresse 3333
    public static void sendMail(String Receiver, String Subject, String Content) throws RuntimeException {
        String from = "3333@utc.fr";
        String host = "smtp1.utc.fr";

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "25");
        props.put("mail.debug", "true");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.connectiontimeout", "5000");

        Session session = Session.getInstance(props, null);
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Receiver));
            message.setSubject(Subject);
            message.setContent(Content, "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            System.out.println(e.toString());
            throw new RuntimeException(e);
        }
    }
}
