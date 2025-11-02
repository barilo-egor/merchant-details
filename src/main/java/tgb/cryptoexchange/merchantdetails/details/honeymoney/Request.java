package tgb.cryptoexchange.merchantdetails.details.honeymoney;

import lombok.Data;

@Data
public class Request {

    private String extId;

    private final String currency = "RUB";

    private Integer amount;

    private String callbackUrl;

    private String bank;

    private ClientDetails clientDetails;

    @Data
    public static class ClientDetails {

        private String clientId;
    }
}
