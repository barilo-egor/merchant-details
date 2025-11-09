package tgb.cryptoexchange.merchantdetails.details.evopay;

import lombok.Data;

@Data
public class Request {

    private String customId;

    private Method paymentMethod;

    private Integer fiatSum;

    private final String fiatCurrencyCode = "RUB";

    private final String cryptoCurrencyCode = "USDT";
}
