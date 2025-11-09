package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("client_order_id")
    private String clientOrderId;

    @JsonProperty("payment_method_id")
    @JsonSerialize(using = Method.Serializer.class)
    private Method paymentMethod;

    private Integer amount;

    @JsonProperty("order_side")
    private String orderSide = "Buy";
}
