package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deora-low-check")
public record DeoraLowCheckProperties(String url, String token, String key,
                                      String secret) implements BridgePayProperties {
}
