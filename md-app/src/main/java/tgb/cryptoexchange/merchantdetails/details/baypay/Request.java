package tgb.cryptoexchange.merchantdetails.details.baypay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
public class Request {

    @JsonProperty("order_id")
    private String orderId;

    private Integer amount;

    @JsonProperty("payment_method")
    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

    @JsonProperty("callback_url")
    private String callbackUrl;

}
