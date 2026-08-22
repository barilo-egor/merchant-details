package tgb.cryptoexchange.merchantdetails.details.wat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class Request {

    @JsonProperty("id")
    private String orderId;

    @JsonProperty("rub_amount")
    private Integer amount;

    @JsonProperty("payment_method")
    @JsonSerialize(using = Method.Serializer.class)
    private Method method;

}
