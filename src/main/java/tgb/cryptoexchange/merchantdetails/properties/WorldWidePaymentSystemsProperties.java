package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wwps")
public record WorldWidePaymentSystemsProperties(String url, String token, String merchantId) {
}
