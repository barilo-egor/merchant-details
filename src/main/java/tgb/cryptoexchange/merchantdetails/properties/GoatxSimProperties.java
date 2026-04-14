package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goatx-sim")
public record GoatxSimProperties(String url, String apiKey, String merchantId, String merchantContractId, String login) implements GoatxProperties {}
