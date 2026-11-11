package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prisma-pay")
public record PrismaPayProperties(String url, String token) {
}
