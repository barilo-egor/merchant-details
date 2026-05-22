package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import tgb.cryptoexchange.merchantdetails.details.eclipsegate.Method;

@ConfigurationProperties("eclipse-gate-low-check")
public record EclipseGateLowCheckProperties(String url, String keyCard, String keySbp,
                                            String keySim) implements EclipseGateConfig {

    @Override
    public String apiKey(String method) {
        if (Method.CARD.name().equalsIgnoreCase(keyCard)) {
            return keyCard;
        } else if (Method.SBP.name().equalsIgnoreCase(keySbp)) {
            return keySbp;
        } else return keySim;
    }

}
