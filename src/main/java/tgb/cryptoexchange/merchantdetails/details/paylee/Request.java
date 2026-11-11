package tgb.cryptoexchange.merchantdetails.details.paylee;

import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
public class Request {

    private Integer price;

    @JsonSerialize(using = Method.Serializer.class)
    private Method requisitesType;

    private String clientId;

    private String amountCurrency;

    public String getAmountCurrency() {
        return "USDT";
    }
}
