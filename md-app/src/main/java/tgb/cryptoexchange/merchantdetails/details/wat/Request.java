package tgb.cryptoexchange.merchantdetails.details.wat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;

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
