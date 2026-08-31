package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cube-low-check")
public record CubeLowCheckProperties(String url, String key, String privateKey) implements CubeProperties {
}
