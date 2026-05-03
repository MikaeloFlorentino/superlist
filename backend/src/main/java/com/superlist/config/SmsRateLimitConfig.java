package com.superlist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.sms")
@Data
public class SmsRateLimitConfig {
    private int codeLength = 6;
    private int codeExpirationMinutes = 10;
    private int rateLimitMax = 3;
    private int rateLimitWindowSeconds = 60;
}
