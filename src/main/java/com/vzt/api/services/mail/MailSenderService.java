package com.vzt.api.services.mail;


import com.vzt.api.models.authentication.MailStatus;
import com.vzt.api.models.mail.SentMail;
import com.vzt.api.repositories.authentication.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MailSenderService {

    @Autowired
    private JavaMailSender sender;

    @Value("${mail-account.name}")
    private String mailAccount;

    @Autowired
    private TemplateEngine templateEngine;

    private final UserRepository userRepository;


    public MailStatus send(String to, String subject, String template, Context context){
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String htmlContent = templateEngine.process(template, context);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            sender.send(message);
            userRepository.findByEmail(to).ifPresent(user -> {
                SentMail sentMail = new SentMail(null, subject, MailStatus.SUCCESS, LocalDateTime.now());
                user.getSentMails().add(sentMail);
                userRepository.save(user);
            });
            return  MailStatus.SUCCESS;
        } catch (Exception e) {
            userRepository.findByEmail(to).ifPresent(user -> {
                SentMail sentMail = new SentMail(null, subject, MailStatus.FAILED, LocalDateTime.now());
                user.getSentMails().add(sentMail);
                userRepository.save(user);
            });
            return MailStatus.FAILED;
        }

    }


}
