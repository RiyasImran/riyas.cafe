package com.riyas.cafe.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Objects;

@Service
public class EmailSender {
    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text, List<String> list){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("test@example.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setCc(convertListToStringArray(list));
        emailSender.send(message);
    }

    public void forgetPasswordMail(String to, String subject, String password)throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("test@example.com");
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlMsg = "<p><b>Your Login details for Cafe Management System</b><br><b>Email: </b> " + to
                + " <br><b>Password: </b> " + password + "<br><a href=\"http://localhost:4200/\">Click here to login</a></p>";

        message.setContent(htmlMsg, "text/html");
        emailSender.send(message);

    }

    private String[] convertListToStringArray(List<String> list) {
        return (list == null || list.isEmpty())
                ? new String[0]
                : list.stream()
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }
}
