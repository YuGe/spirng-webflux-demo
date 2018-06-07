package me.yuge.springwebflux.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    public Mono<Void> sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        return Mono.fromCallable(() -> {
            emailSender.send(message);
            return Mono.empty();
        }).subscribeOn(Schedulers.elastic()).then();
    }

    public Mono<Void> sendHtmlMessage(String to, String subject, String text) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);
            message.setContent(text, "text/html; charset=utf-8");

            return Mono.fromCallable(() -> {
                emailSender.send(message);
                return Mono.empty();
            }).subscribeOn(Schedulers.elastic()).then();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
