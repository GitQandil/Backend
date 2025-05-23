package murshedi.backend.email;

public interface EmailSender {
    void send(String to, String subject, String emailContent);
}
