package ru.aten.telegram_bot.frameworksAndDrivers.email;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendEmailWithAttachmentAsync(String to, String subject, String body, File attachment) throws MessagingException {

        sendEmailWithAttachment(to, subject, body, attachment);

    }

    private void sendEmailWithAttachment(String to, String subject, String body, File attachment) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("dondonnybot@mail.ru");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        if (attachment != null) {
            helper.addAttachment(attachment.getName(), attachment);
        }

        mailSender.send(message);
    }

}
