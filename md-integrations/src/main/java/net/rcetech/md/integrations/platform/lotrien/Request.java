package net.rcetech.md.integrations.platform.lotrien;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
public class Request {

    @JsonProperty("paymentMethods")
    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentMethod;

    private String fiatSum;

    @JsonProperty("cryptoCurrencyCode")
    private String cryptoCurrencyCode = "USDT";

    @JsonProperty("fiatCurrencyCode")
    private String fiatCurrencyCode;
}
