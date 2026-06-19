package tgb.cryptoexchange.merchantdetails.details.prismapay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("amountIn")
    private Integer amount;

    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentMethod;

    public String getType() {
        return "sell";
    }

    public String getBaseCurrency() {
        return "RUB";
    }

    public String getQuoteCurrency() {
        return "USDT";
    }
}
