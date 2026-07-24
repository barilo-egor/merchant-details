package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import tgb.cryptoexchange.merchantdetails.details.studio.Method;

@ConfigurationProperties("studio")
public record StudioProperties(String url, String keyCard, String keySbp) implements StudioConfig {

    @Override
    public String getKey(String method) {
        return Method.CARD.name().equalsIgnoreCase(method) ? keyCard : keySbp;
    }

}
