package murshedi.backend.email;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService implements EmailSender{

    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;


    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void send(String to, String subject, String emailContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setText(emailContent, true); // HTML content
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("no-reply@yourdomain.com", "No Reply");
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.error("Error while sending email", e);
            throw new IllegalStateException("Error while sending email");
        }
    }

}
