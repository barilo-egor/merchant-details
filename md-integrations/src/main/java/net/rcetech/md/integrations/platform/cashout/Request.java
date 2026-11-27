package net.rcetech.md.integrations.platform.cashout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder(alphabetic = true)
public class Request {

    private Integer amount;

    @JsonProperty("paymentMethod")
    private Method method;

    @JsonProperty("currency")
    private String currency;
}
