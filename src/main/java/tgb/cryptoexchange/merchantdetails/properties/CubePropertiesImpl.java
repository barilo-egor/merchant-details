package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cube")
public record CubePropertiesImpl(String url, String key, String privateKey) implements CubeProperties {
}
