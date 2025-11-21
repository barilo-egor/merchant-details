package tgb.cryptoexchange.merchantdetails.config;

import lombok.Data;

@Data
public class CallbackConfig {

    private String callbackSecret;

    private String gatewayUrl;
}
