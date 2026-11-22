package net.rcetech.md.integrations.platform.smackpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("return_url")
    public String returnUrl;

    private Integer amount;

    private String phone;

    @JsonProperty("client_id")
    private String clientId;

    private Metadata metadata;

    @JsonProperty("currency")
    public String getCurrency() {
        return "RUB";
    }

    @Data
    public static class Metadata {

        @JsonProperty("order_id")
        private String orderId;

    }
}
