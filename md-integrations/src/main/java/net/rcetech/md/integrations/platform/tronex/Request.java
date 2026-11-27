package net.rcetech.md.integrations.platform.tronex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("id")
    private String orderId;

    private Integer amount;

    @JsonProperty("paymethod")
    private Method method;

    @JsonProperty("source_bank")
    private String sourceBank;

    @JsonProperty("callback_url")
    private String callbackUrl;

}
