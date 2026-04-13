package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goatx")
public record GoatxProperties (String url, String apiKey, String merchantId, String merchantContractId, String login) {}
