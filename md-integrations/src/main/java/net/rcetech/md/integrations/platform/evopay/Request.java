package net.rcetech.md.integrations.platform.evopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    private String customId;

    private Method paymentMethod;

    private Integer fiatSum;

    @JsonProperty("cryptoCurrencyCode")
    private String cryptoCurrencyCode = "USDT";

    @JsonProperty("fiatCurrencyCode")
    private String fiatCurrencyCode;
}
