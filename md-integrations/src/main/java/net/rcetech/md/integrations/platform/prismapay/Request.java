package net.rcetech.md.integrations.platform.prismapay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("amountIn")
    private Integer amount;

    private Method paymentMethod;

    private String type = "sell";

    private String baseCurrency;

    private String quoteCurrency = "USDT";
}
