package net.rcetech.md.integrations.platform.baypay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("order_id")
    private String orderId;

    private Integer amount;

    @JsonProperty("payment_method")
    private Method method;

    @JsonProperty("callback_url")
    private String callbackUrl;

}
