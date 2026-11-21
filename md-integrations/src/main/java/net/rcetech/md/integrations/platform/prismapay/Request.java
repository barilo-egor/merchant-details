package net.rcetech.md.integrations.platform.prismapay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

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
