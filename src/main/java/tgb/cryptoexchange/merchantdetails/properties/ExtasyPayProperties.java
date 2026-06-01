package tgb.cryptoexchange.merchantdetails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import tgb.cryptoexchange.merchantdetails.details.paybox.Method;

import java.util.List;

@ConfigurationProperties(prefix = "extasy-pay")
public record ExtasyPayProperties(String url, String token, String internalQrToken,
                                  String signKey) implements PayBoxProperties {

    public String getToken(Method method) {
        if (List.of(Method.SBER_QR, Method.VTB_QR).contains(method)) {
            return internalQrToken;
        }
        return token;
    }

}
