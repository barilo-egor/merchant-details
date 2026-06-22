package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cube-high-check")
public record CubeHighCheckProperties(String url, String key, String privateKey) implements CubeProperties {
}
