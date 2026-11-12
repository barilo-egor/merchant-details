package net.rcetech.md.integrations.platform.cashout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@JsonPropertyOrder(alphabetic = true)
public class Request {

    private Integer amount;

    @JsonSerialize(using = Method.Serializer.class)
    @JsonProperty("paymentMethod")
    private Method method;

    @JsonProperty("currency")
    public String getCurrency() {
        return "RUB";
    }
}
