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

    private String type = "sell";

    private String baseCurrency;

    private String quoteCurrency = "USDT";
}
