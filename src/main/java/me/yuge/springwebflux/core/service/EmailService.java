package me.yuge.springwebflux.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender emailSender;

    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        emailSender.send(message);
    }

    public void sendHtmlMessage(String to, String subject, String text) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);
            message.setContent(text, "text/html; charset=utf-8");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        emailSender.send(message);
    }
}
