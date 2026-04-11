package com.xs.chat.Configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
}
