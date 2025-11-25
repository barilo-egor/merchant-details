package tgb.cryptoexchange.merchantdetails.details.payscrow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

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
}
