package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import tgb.cryptoexchange.merchantdetails.details.eclipsegate.Method;

@ConfigurationProperties("eclipse-gate")
public record EclipseGateProperties(String url, String keyCard, String keySbp) implements EclipseGateConfig {

    @Override
    public String apiKey(String method) {
        return Method.CARD.name().equalsIgnoreCase(method) ? keyCard : keySbp;
    }

}
