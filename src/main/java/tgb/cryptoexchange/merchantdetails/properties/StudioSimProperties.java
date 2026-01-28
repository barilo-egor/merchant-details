package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("studio-sim")
public record StudioSimProperties(String url, String keySim) implements StudioConfig {

    @Override
    public String getKey(String method) {
        return keySim;
    }

}
