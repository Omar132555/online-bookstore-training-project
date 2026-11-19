package com.book.store.online_book_store.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class AppConfig {
    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
