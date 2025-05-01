package com.jat.jatbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class InfoConfig {
    
    @Value("${alpaca.userapikey}")
    private String keyId;

    @Value("${alpaca.usersecret}")
    private String secret;

    @Value("${users.selected.type}")
    private String type;

    @Value("${users.selected.source}")
    private String source;

    @Value("${users.selected.rem}")
    private String remMe;
}
