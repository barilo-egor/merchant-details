package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cube-sim")
public record CubeSimProperties(String url, String key, String privateKey) implements CubeProperties {
}
