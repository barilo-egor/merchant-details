package tgb.cryptoexchange.merchantdetails.details.appexbit;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
class Request {

    private String amountFiat;

    private FiatInfo fiatInfo;

    private String goodReturnLink;

    private String badReturnLink;

    private final Integer type = 0;

    private final String tokenCode = "USDT";

    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentMethod;

    @Data
    public static class FiatInfo {

        private final String fiatCode = "RUB";
    }
}
