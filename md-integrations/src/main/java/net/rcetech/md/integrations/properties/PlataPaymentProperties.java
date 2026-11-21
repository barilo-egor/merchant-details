package net.rcetech.md.integrations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plata-payment")
public record PlataPaymentProperties(String url, String token, String merchantId) implements LevelPayProperties{
}
