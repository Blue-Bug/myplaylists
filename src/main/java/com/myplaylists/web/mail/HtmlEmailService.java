package com.myplaylists.web.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Profile("dev")
@Component
@RequiredArgsConstructor
@Slf4j
public class HtmlEmailService implements EmailService {
    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(EmailContent emailContent) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailContent.getTo());
            mimeMessageHelper.setSubject(emailContent.getSubject());
            mimeMessageHelper.setText(emailContent.getBody(),true);
            javaMailSender.send(mimeMessage);
            log.info("email sent : {}",emailContent.getBody());
        } catch (MessagingException e) {
            log.error("fail to send ",e);
        }
    }
}
