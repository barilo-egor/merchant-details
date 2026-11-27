package net.rcetech.md.integrations.platform.asgard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    private String orderId;

    private String merchantId;

    private Integer amount;

    private String callbackUri;

    @JsonProperty("currency")
    private String currency;

    private Method method;

}
