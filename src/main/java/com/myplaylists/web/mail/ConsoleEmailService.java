package com.myplaylists.web.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("local")
@Component
@Slf4j
public class ConsoleEmailService implements EmailService {
    @Override
    public void sendEmail(EmailContent emailContent) {
        log.info("email sent : {}",emailContent.getBody());
    }
}
