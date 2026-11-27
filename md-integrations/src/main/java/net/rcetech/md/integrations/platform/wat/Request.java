package net.rcetech.md.integrations.platform.wat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("id")
    private String orderId;

    @JsonProperty("rub_amount")
    private Integer amount;

    @JsonProperty("payment_method")
    private Method method;

}
