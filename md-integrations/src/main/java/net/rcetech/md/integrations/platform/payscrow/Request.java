package net.rcetech.md.integrations.platform.payscrow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

    @JsonProperty("client_order_id")
    private String clientOrderId;

    @JsonProperty("payment_method_id")
    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentMethod;

    private Integer amount;

    @JsonProperty("order_side")
    private String orderSide = "Buy";

    @JsonProperty("unique_amount")
    private Boolean uniqueAmount;

    @JsonProperty("order_data")
    private OrderData orderData;

    @JsonProperty("redirect_url")
    private String redirectUrl;

    @JsonProperty("return_url")
    private String returnUrl;

    @Data
    public static class OrderData {

        @JsonProperty("client_order_id")
        private String clientOrderId;

        @JsonProperty("order_side")
        private String orderSide = "Buy";

        private Integer amount;
    }
}
